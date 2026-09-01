package com.vox.music.core.storage

import android.content.Context
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MediaStoreObserver @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var observer: ContentObserver? = null
    private var debounceJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default)

    fun registerObserver(onChangeAction: () -> Unit) {
        if (observer != null) return

        observer = object : ContentObserver(Handler(Looper.getMainLooper())) {
            override fun onChange(selfChange: Boolean, uri: Uri?) {
                super.onChange(selfChange, uri)
                // Debounce rapid content provider changes
                debounceJob?.cancel()
                debounceJob = scope.launch {
                    delay(1000)
                    onChangeAction()
                }
            }
        }

        context.contentResolver.registerContentObserver(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            true,
            observer!!
        )
    }

    fun unregisterObserver() {
        observer?.let {
            context.contentResolver.unregisterContentObserver(it)
            observer = null
        }
        debounceJob?.cancel()
    }
}
