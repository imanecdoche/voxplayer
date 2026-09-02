package com.vox.music.core.theme.storage

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.vox.music.core.theme.model.AssetPathsConfig
import com.vox.music.core.theme.model.VoxThemeConfig
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.util.UUID
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import kotlin.math.max
import kotlin.math.min

class ThemeFileManager(private val context: Context) {

    val themeDir: File = File(context.filesDir, "theme_engine").apply { if (!exists()) mkdirs() }
    val backgroundsDir: File = File(themeDir, "backgrounds").apply { if (!exists()) mkdirs() }
    val iconsDir: File = File(themeDir, "icons").apply { if (!exists()) mkdirs() }
    val activePackDir: File = File(iconsDir, "active_pack").apply { if (!exists()) mkdirs() }
    val activeConfigFile: File = File(themeDir, "active_theme.json")

    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()

    fun loadActiveTheme(): VoxThemeConfig {
        return try {
            if (activeConfigFile.exists()) {
                val json = activeConfigFile.readText()
                gson.fromJson(json, VoxThemeConfig::class.java) ?: VoxThemeConfig()
            } else {
                val defaultConfig = VoxThemeConfig()
                saveActiveTheme(defaultConfig)
                defaultConfig
            }
        } catch (e: Exception) {
            e.printStackTrace()
            VoxThemeConfig()
        }
    }

    fun saveActiveTheme(config: VoxThemeConfig) {
        try {
            val json = gson.toJson(config)
            activeConfigFile.writeText(json)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun resetToDefault(): VoxThemeConfig {
        val defaultConfig = VoxThemeConfig()
        saveActiveTheme(defaultConfig)
        return defaultConfig
    }

    /**
     * Ingests an image URI (background or header icon) into internal storage.
     * Compresses raster images to WEBP 85% with max resolution 1080x2400.
     * Rejects EPS format.
     */
    fun ingestImage(uri: Uri, isHeaderIcon: Boolean = false): Result<String> {
        return try {
            val mimeType = context.contentResolver.getType(uri) ?: ""
            val fileName = getFileName(uri).lowercase()

            if (fileName.endsWith(".eps") || mimeType.contains("eps") || mimeType.contains("postscript")) {
                return Result.failure(IllegalArgumentException("Format EPS tidak didukung! Gunakan PNG, WEBP, atau SVG."))
            }

            val targetDir = if (isHeaderIcon) iconsDir else backgroundsDir
            val uuid = UUID.randomUUID().toString()

            if (fileName.endsWith(".svg") || mimeType.contains("svg")) {
                val targetFile = File(targetDir, "$uuid.svg")
                context.contentResolver.openInputStream(uri)?.use { input ->
                    targetFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                val relativePath = targetFile.relativeTo(themeDir).path
                Result.success(relativePath)
            } else {
                // Raster image: Decode, scale to max 1080x2400, compress to WEBP 85%
                val inputStream = context.contentResolver.openInputStream(uri)
                    ?: return Result.failure(IllegalStateException("Gagal membuka gambar."))

                val bitmap = BitmapFactory.decodeStream(inputStream)
                inputStream.close()

                if (bitmap == null) {
                    return Result.failure(IllegalStateException("Format gambar tidak valid atau rusak."))
                }

                val scaledBitmap = scaleDownBitmap(bitmap, maxW = 1080, maxH = 2400)
                val targetFile = File(targetDir, "$uuid.webp")

                targetFile.outputStream().use { outStream ->
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        scaledBitmap.compress(Bitmap.CompressFormat.WEBP_LOSSY, 85, outStream)
                    } else {
                        @Suppress("DEPRECATION")
                        scaledBitmap.compress(Bitmap.CompressFormat.WEBP, 85, outStream)
                    }
                }

                if (scaledBitmap != bitmap) {
                    bitmap.recycle()
                }

                val relativePath = targetFile.relativeTo(themeDir).path
                Result.success(relativePath)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    /**
     * Ingests a single icon file for a specific slot (e.g. ic_play, ic_pause).
     */
    fun ingestSingleIcon(slotName: String, uri: Uri): Result<String> {
        return try {
            val mimeType = context.contentResolver.getType(uri) ?: ""
            val fileName = getFileName(uri).lowercase()

            if (fileName.endsWith(".eps") || mimeType.contains("eps")) {
                return Result.failure(IllegalArgumentException("Format EPS tidak didukung! Gunakan PNG, WEBP, atau SVG."))
            }

            val ext = if (fileName.endsWith(".svg") || mimeType.contains("svg")) "svg" else "webp"
            val targetFile = File(iconsDir, "$slotName.$ext")

            if (ext == "svg") {
                context.contentResolver.openInputStream(uri)?.use { input ->
                    targetFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
            } else {
                context.contentResolver.openInputStream(uri)?.use { input ->
                    val bitmap = BitmapFactory.decodeStream(input)
                    if (bitmap != null) {
                        val scaled = scaleDownBitmap(bitmap, maxW = 256, maxH = 256)
                        targetFile.outputStream().use { outStream ->
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                                scaled.compress(Bitmap.CompressFormat.WEBP_LOSSY, 90, outStream)
                            } else {
                                @Suppress("DEPRECATION")
                                scaled.compress(Bitmap.CompressFormat.WEBP, 90, outStream)
                            }
                        }
                        if (scaled != bitmap) scaled.recycle()
                    }
                }
            }

            val relativePath = targetFile.relativeTo(themeDir).path
            Result.success(relativePath)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    /**
     * Extracts a .zip or .voxpack icon archive into theme_engine/icons/active_pack/.
     * Validates contents and rejects if any EPS or invalid archives.
     */
    fun importIconPack(uri: Uri): Result<String> {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
                ?: return Result.failure(IllegalStateException("Gagal membaca arsip icon pack."))

            // Clear previous active pack
            activePackDir.deleteRecursively()
            activePackDir.mkdirs()

            ZipInputStream(BufferedInputStream(inputStream)).use { zipIn ->
                var entry: ZipEntry? = zipIn.nextEntry
                while (entry != null) {
                    val entryName = entry.name.lowercase()
                    if (entryName.endsWith(".eps")) {
                        return Result.failure(IllegalArgumentException("Arsip mengandung format .eps yang tidak didukung."))
                    }

                    val sanitizedName = File(entry.name).name
                    if (!entry.isDirectory && sanitizedName.isNotEmpty()) {
                        val targetFile = File(activePackDir, sanitizedName)
                        targetFile.outputStream().use { out ->
                            zipIn.copyTo(out)
                        }
                    }
                    zipIn.closeEntry()
                    entry = zipIn.nextEntry
                }
            }

            val relativePath = activePackDir.relativeTo(themeDir).path
            Result.success(relativePath)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    /**
     * Exports active_theme.json, backgrounds/, and icons/ into a .voxtheme bundle (ZIP format).
     */
    fun exportThemeBundle(targetUri: Uri): Result<Unit> {
        return try {
            val outputStream = context.contentResolver.openOutputStream(targetUri)
                ?: return Result.failure(IllegalStateException("Gagal membuka lokasi penyimpanan output."))

            ZipOutputStream(BufferedOutputStream(outputStream)).use { zipOut ->
                // 1. Add active_theme.json
                if (activeConfigFile.exists()) {
                    addFileToZip(activeConfigFile, "active_theme.json", zipOut)
                }

                // 2. Add backgrounds
                backgroundsDir.listFiles()?.forEach { file ->
                    if (file.isFile) {
                        addFileToZip(file, "backgrounds/${file.name}", zipOut)
                    }
                }

                // 3. Add icons
                iconsDir.walkTopDown().filter { it.isFile }.forEach { file ->
                    val relPath = file.relativeTo(iconsDir).path
                    addFileToZip(file, "icons/$relPath", zipOut)
                }
            }

            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    /**
     * Imports a .voxtheme bundle: Unzips to staging, validates active_theme.json,
     * overwrites theme_engine/, and returns the new VoxThemeConfig.
     */
    fun importThemeBundle(uri: Uri): Result<VoxThemeConfig> {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
                ?: return Result.failure(IllegalStateException("Gagal membuka file .voxtheme."))

            val stagingDir = File(context.cacheDir, "theme_staging_${UUID.randomUUID()}").apply { mkdirs() }

            ZipInputStream(BufferedInputStream(inputStream)).use { zipIn ->
                var entry: ZipEntry? = zipIn.nextEntry
                while (entry != null) {
                    val targetFile = File(stagingDir, entry.name)
                    if (entry.isDirectory) {
                        targetFile.mkdirs()
                    } else {
                        targetFile.parentFile?.mkdirs()
                        targetFile.outputStream().use { out ->
                            zipIn.copyTo(out)
                        }
                    }
                    zipIn.closeEntry()
                    entry = zipIn.nextEntry
                }
            }

            // Validate json
            val stagingConfig = File(stagingDir, "active_theme.json")
            if (!stagingConfig.exists()) {
                stagingDir.deleteRecursively()
                return Result.failure(IllegalArgumentException("File .voxtheme tidak valid (active_theme.json tidak ditemukan)."))
            }

            val jsonText = stagingConfig.readText()
            val parsedConfig = gson.fromJson(jsonText, VoxThemeConfig::class.java)
                ?: return Result.failure(IllegalArgumentException("Gagal membaca struktur tema."))

            // Overwrite into theme_engine
            stagingDir.copyRecursively(themeDir, overwrite = true)
            stagingDir.deleteRecursively()

            saveActiveTheme(parsedConfig)
            Result.success(parsedConfig)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    fun resolveFile(relativePath: String?): File? {
        if (relativePath.isNullOrBlank()) return null
        val file = File(themeDir, relativePath)
        return if (file.exists()) file else null
    }

    fun getIconSlotFile(slotName: String, customPackDir: String? = null): File? {
        // 1. Check active pack first
        val packDir = if (!customPackDir.isNullOrBlank()) File(themeDir, customPackDir) else activePackDir
        val extensions = listOf("webp", "png", "svg", "jpg", "jpeg")
        if (packDir.exists() && packDir.isDirectory) {
            for (ext in extensions) {
                val f = File(packDir, "$slotName.$ext")
                if (f.exists()) return f
            }
        }
        // 2. Check individual icon in iconsDir
        for (ext in extensions) {
            val f = File(iconsDir, "$slotName.$ext")
            if (f.exists()) return f
        }
        return null
    }

    private fun addFileToZip(file: File, zipPath: String, zipOut: ZipOutputStream) {
        FileInputStream(file).use { fi ->
            BufferedInputStream(fi).use { origin ->
                val entry = ZipEntry(zipPath)
                zipOut.putNextEntry(entry)
                origin.copyTo(zipOut)
                zipOut.closeEntry()
            }
        }
    }

    private fun scaleDownBitmap(realImage: Bitmap, maxW: Int, maxH: Int): Bitmap {
        val width = realImage.width
        val height = realImage.height
        if (width <= maxW && height <= maxH) {
            return realImage
        }
        val ratio = min(maxW.toFloat() / width, maxH.toFloat() / height)
        val targetW = (ratio * width).toInt().coerceAtLeast(1)
        val targetH = (ratio * height).toInt().coerceAtLeast(1)
        return Bitmap.createScaledBitmap(realImage, targetW, targetH, true)
    }

    private fun getFileName(uri: Uri): String {
        return uri.lastPathSegment ?: "unknown_file"
    }
}
