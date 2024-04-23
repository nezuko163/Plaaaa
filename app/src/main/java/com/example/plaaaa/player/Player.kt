package com.example.plaaaa.player


import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.os.PowerManager
import android.util.Log
import com.example.plaaaa.ui.adapter.Audio
import com.example.plaaaa.tools.Tool
import java.lang.IllegalStateException

class Player(val context: Context) {

    lateinit var mediaPlayer: MediaPlayer
    lateinit var lst: ArrayList<Audio>
    var isLooping = false
    var curIndex: Int? = null
    var isSrcSetted = false


    lateinit var onCompletionListener: MediaPlayer.OnCompletionListener
    lateinit var onErrorListener: MediaPlayer.OnErrorListener

    public fun currentTrack(): Audio? =
        if (curIndex != null) lst[curIndex!!] else null

    private fun createMp(resId: Int) {
        createMp(Tool.resIdToUri(context, resId))
    }

    public fun resetAtrributes() {
        mediaPlayer.apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            setOnCompletionListener(onCompletionListener)
            setOnErrorListener(onErrorListener)
            setWakeMode(context, PowerManager.PARTIAL_WAKE_LOCK)
            setVolume(1.0f, 1.0f)
        }
    }

    private fun createMp(uri: Uri) {
        mediaPlayer = MediaPlayer.create(context, uri)
    }

    private fun changeAudio(uri: Uri) {
        mediaPlayer.setDataSource(context, uri)
        mediaPlayer.prepare()
    }

    fun currentTime() = mediaPlayer.currentPosition

    fun play() {
        if (!isSrcSetted) {
            Log.i(TAG, "play: соурс не сдан")
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
            isSrcSetted = false
            curIndex = null
        } else Log.i(TAG, "stop: соурс нот сеттед")
    }

    fun pause() {
        if (!isSrcSetted) {
            Log.i(TAG, "pause: соурс нот сеттед")
            return
        }
        if (mediaPlayer.isPlaying) mediaPlayer.pause()
    }

    fun isPlaying() = mediaPlayer.isPlaying

    fun other(resId: Int) {
        other(Tool.resIdToUri(context, resId))
    }

    fun other(uri: Uri) {
        if (isSrcSetted) {
            Log.i(TAG, "other: srcSetted")
            changeAudio(uri)
        } else {
            Log.i(TAG, "other: srcNotSetted")
            createMp(uri)
        }
        resetAtrributes()
        isSrcSetted = true
    }

    fun previousTrack(): Audio? {
        if (curIndex == null) {
            Log.i(TAG, "previousTrack: индекс не задан")
            return null
        }
        if (curIndex == 0) {
            if (isLooping) {
                curIndex = lst.size - 1
                return lst[curIndex!!]
            }
            return null
        }

        curIndex = curIndex!! - 1
        return lst[curIndex!!]
    }

    fun nextTrack(): Audio? {
        if (curIndex == null) return null
        if (curIndex == lst.size - 1) {
            if (isLooping) {
                curIndex = 0
                return lst[curIndex!!]
            }
            return null
        }

        curIndex = curIndex!! + 1
        return lst[curIndex!!]
    }


    fun seekTo(ms: Int) {
        mediaPlayer.seekTo(ms)
    }

    companion object {
        const val TAG = "PLAYER"
    }
}