package com.example.plaaaa.player


import android.content.Context
import android.media.MediaPlayer
import android.media.MediaPlayer.OnCompletionListener
import android.net.Uri
import android.util.Log
import com.example.plaaaa.adapter.Audio
import com.example.plaaaa.tools.Tool
import java.lang.IllegalStateException

open class Player(open val context: Context) {
    lateinit var mediaPlayer: MediaPlayer
    lateinit var lst: ArrayList<Audio>
    lateinit var onCompletion: () -> Unit

    var isLooping = false
    var curIndex : Int? = null
    var isSrcSetted = false
    var lastTrack: Uri? = null


    private fun createMp(resId: Int) {
        createMp(Tool.resIdToUri(context, resId))
    }


    private fun createMp(uri: Uri) {
        mediaPlayer = MediaPlayer.create(context, uri)
        mediaPlayer.setOnCompletionListener {
            onCompletion.invoke()
        }
    }

    private fun changeAudio(uri: Uri) {
        mediaPlayer.reset()
        mediaPlayer.setDataSource(context, uri)
        mediaPlayer.setOnCompletionListener {
            onCompletion.invoke()
        }
        mediaPlayer.prepare()
    }

    fun currentTime() = mediaPlayer.currentPosition

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
            curIndex = null
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

    fun previousTrack(): Audio? {
        if (curIndex == null) return null
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