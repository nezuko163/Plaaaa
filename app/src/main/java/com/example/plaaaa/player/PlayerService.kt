package com.example.plaaaa.player

import android.app.Service
import android.content.Intent
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.IBinder
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.SimpleExoPlayer
import androidx.media3.extractor.ExtractorsFactory


class PlayerService : Service() {

    private val NOTIFICATION_ID = 404
    private val NOTIFICATION_DEFAULT_CHANNEL_ID = "default_channel"

    private val metadataBuilder = MediaMetadataCompat.Builder()

    private val stateBuilder = PlaybackStateCompat.Builder().setActions(
        PlaybackStateCompat.ACTION_PLAY
                or PlaybackStateCompat.ACTION_STOP
                or PlaybackStateCompat.ACTION_PAUSE
                or PlaybackStateCompat.ACTION_PLAY_PAUSE
                or PlaybackStateCompat.ACTION_SKIP_TO_NEXT
                or PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
    )

    private val mediaSession: MediaSessionCompat? = null

    private val audioManager: AudioManager? = null
    private val audioFocusRequest: AudioFocusRequest? = null
    private val audioFocusRequested = false

//    private lateinit var  exoPlayer: ExoPlayer? = null
//    private val extractorsFactory: ExtractorsFactory? = null
//    private val dataSourceFactory: DataSource.Factory? = null


    override fun onBind(intent: Intent?): IBinder? {
        TODO("Not yet implemented")
    }
}