package com.example.plaaaa.ui.adapter

import android.net.Uri

data class Audio(
    val path: String,
    val name: String,
    val album: String,
    val artist: String?,
    val duration: Long,
    val date: Long,
    val art_uri: Uri? = null,
    val audio_uri: Uri? = null,
)
