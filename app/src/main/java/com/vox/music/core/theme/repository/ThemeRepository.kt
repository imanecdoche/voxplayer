package com.vox.music.core.theme.repository

import android.content.Context
import android.net.Uri
import com.vox.music.core.theme.model.AssetPathsConfig
import com.vox.music.core.theme.model.ColorSchemeConfig
import com.vox.music.core.theme.model.DimensionsConfig
import com.vox.music.core.theme.model.OffsetsConfig
import com.vox.music.core.theme.model.PaddingsConfig
import com.vox.music.core.theme.model.VoxThemeConfig
import com.vox.music.core.theme.storage.ThemeFileManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ThemeRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    val fileManager = ThemeFileManager(context)

    private val _themeStateFlow = MutableStateFlow(fileManager.loadActiveTheme())
    val themeStateFlow: StateFlow<VoxThemeConfig> = _themeStateFlow.asStateFlow()

    fun updateTheme(transform: (VoxThemeConfig) -> VoxThemeConfig) {
        val current = _themeStateFlow.value
        val updated = transform(current)
        _themeStateFlow.value = updated
        fileManager.saveActiveTheme(updated)
    }

    fun setThemeConfig(newConfig: VoxThemeConfig) {
        _themeStateFlow.value = newConfig
        fileManager.saveActiveTheme(newConfig)
    }

    fun resetToDefault() {
        val default = fileManager.resetToDefault()
        _themeStateFlow.value = default
    }

    fun ingestBackgroundImage(uri: Uri, targetSlot: BackgroundSlot): Result<String> {
        val res = fileManager.ingestImage(uri, isHeaderIcon = false)
        if (res.isSuccess) {
            val relPath = res.getOrThrow()
            updateTheme { current ->
                val assets = when (targetSlot) {
                    BackgroundSlot.HOME -> current.assets.copy(homeBackgroundRelativePath = relPath)
                    BackgroundSlot.TRACKLIST -> current.assets.copy(tracklistBackgroundRelativePath = relPath)
                    BackgroundSlot.PLAYER -> current.assets.copy(playerBackgroundRelativePath = relPath)
                    BackgroundSlot.SETTINGS -> current.assets.copy(settingsBackgroundRelativePath = relPath)
                }
                current.copy(assets = assets)
            }
        }
        return res
    }

    fun clearBackgroundImage(targetSlot: BackgroundSlot) {
        updateTheme { current ->
            val assets = when (targetSlot) {
                BackgroundSlot.HOME -> current.assets.copy(homeBackgroundRelativePath = null)
                BackgroundSlot.TRACKLIST -> current.assets.copy(tracklistBackgroundRelativePath = null)
                BackgroundSlot.PLAYER -> current.assets.copy(playerBackgroundRelativePath = null)
                BackgroundSlot.SETTINGS -> current.assets.copy(settingsBackgroundRelativePath = null)
            }
            current.copy(assets = assets)
        }
    }

    fun ingestHeaderIcon(uri: Uri): Result<String> {
        val res = fileManager.ingestImage(uri, isHeaderIcon = true)
        if (res.isSuccess) {
            val relPath = res.getOrThrow()
            updateTheme { current ->
                current.copy(assets = current.assets.copy(customHeaderIconRelativePath = relPath))
            }
        }
        return res
    }

    fun clearHeaderIcon() {
        updateTheme { current ->
            current.copy(assets = current.assets.copy(customHeaderIconRelativePath = null))
        }
    }

    fun ingestSingleIcon(slotName: String, uri: Uri): Result<String> {
        return fileManager.ingestSingleIcon(slotName, uri).also {
            // Trigger state change so UI refreshes icons
            _themeStateFlow.value = _themeStateFlow.value.copy()
        }
    }

    fun importIconPack(uri: Uri): Result<String> {
        val res = fileManager.importIconPack(uri)
        if (res.isSuccess) {
            val relPath = res.getOrThrow()
            updateTheme { current ->
                current.copy(assets = current.assets.copy(customIconPackDirRelativePath = relPath))
            }
        }
        return res
    }

    fun clearIconPack() {
        updateTheme { current ->
            current.copy(assets = current.assets.copy(customIconPackDirRelativePath = null))
        }
    }

    fun exportTheme(uri: Uri): Result<Unit> {
        return fileManager.exportThemeBundle(uri)
    }

    fun importTheme(uri: Uri): Result<Unit> {
        val res = fileManager.importThemeBundle(uri)
        if (res.isSuccess) {
            _themeStateFlow.value = res.getOrThrow()
            return Result.success(Unit)
        }
        return Result.failure(res.exceptionOrNull() ?: Exception("Import failed"))
    }

    fun resolveFile(relativePath: String?): File? {
        return fileManager.resolveFile(relativePath)
    }

    fun getIconFile(slotName: String): File? {
        return fileManager.getIconSlotFile(slotName, _themeStateFlow.value.assets.customIconPackDirRelativePath)
    }
}

enum class BackgroundSlot {
    HOME, TRACKLIST, PLAYER, SETTINGS
}
