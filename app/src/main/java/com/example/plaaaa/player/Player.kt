package com.example.plaaaa.player


import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.util.Log
import com.example.plaaaa.tools.Tool
import java.lang.IllegalStateException

class Player(val context: Context) {
    private lateinit var mediaPlayer: MediaPlayer
    var isSrcSetted = false
    var lastTrack: Uri? = null

//    fun lastTrack() = lastTrack

    private fun createMp(resId: Int) {
        createMp(Tool.resIdToUri(context, resId))
    }

    private fun createMp(uri: Uri) {
        mediaPlayer = MediaPlayer.create(context, uri)
    }

    private fun changeAudio(uri: Uri) {
        mediaPlayer.reset()
        mediaPlayer.setDataSource(context, uri)
        mediaPlayer.prepare()
    }

    fun play() {
        if (!isSrcSetted) {
            return
        }
        try {
            if (!mediaPlayer.isPlaying) {
                mediaPlayer.start()
            }

        } catch (e: IllegalStateException) {
            e.printStackTrace()
        }
    }

    fun stop() {
        if (isSrcSetted) {
            Log.i(TAG, "stop: 123")
            mediaPlayer.stop()
            mediaPlayer.release()
            lastTrack = null
            isSrcSetted = false
        }
    }

    fun pause() {
        if (!isSrcSetted) return
        if (mediaPlayer.isPlaying) mediaPlayer.pause()
    }

    fun isPlaying() = mediaPlayer.isPlaying

    fun other(resId: Int) {
        other(Tool.resIdToUri(context, resId))
    }

    fun other(uri: Uri) {
        if (isSrcSetted) {
            changeAudio(uri)
        } else {
            createMp(uri)
        }
        isSrcSetted = true

        lastTrack = uri
    }

    companion object {
        const val TAG = "PLAYER"
    }
}