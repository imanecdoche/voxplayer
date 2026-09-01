package com.vox.music.core.model

data class Playlist(
    val id: Long = 0L,
    val name: String,
    val trackCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
