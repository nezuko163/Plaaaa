package com.example.plaaaa.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer.OnCompletionListener
import android.media.MediaPlayer.OnErrorListener
import android.os.Binder
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.os.ResultReceiver
import android.service.media.MediaBrowserService
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.text.TextUtils
import android.util.Log
import androidx.media.AudioManagerCompat
import androidx.media.MediaBrowserServiceCompat
import androidx.media.session.MediaButtonReceiver
import com.example.plaaaa.R
import com.example.plaaaa.player.Player
import com.example.plaaaa.tools.NotificationHelper
import com.example.plaaaa.tools.Pashalko
import com.example.plaaaa.tools.Tool
import com.example.plaaaa.ui.adapter.Audio


class MediaPlaybackService1 : MediaBrowserServiceCompat() {

    lateinit var player: Player
    lateinit var mediaSession: MediaSessionCompat
    private lateinit var audioFocusRequest: AudioFocusRequest
    private lateinit var noisyReceiver: NoisyReceiver

    lateinit var list: ArrayList<Audio>
    var index = -1

    lateinit var onErrorListener: OnErrorListener
    val onCompletionListener = OnCompletionListener {
        val repeatMode = mediaSession.controller.repeatMode
        when (repeatMode) {
            PlaybackStateCompat.REPEAT_MODE_ONE -> {}
            PlaybackStateCompat.REPEAT_MODE_ALL -> callback.onSkipToNext()
            else -> callback.onStop()
        }

        callback.onPrepare()
        callback.onPlay()
    }


    private val afChangeListener = AudioManager.OnAudioFocusChangeListener { focusChange ->
        when (focusChange) {
            AudioManager.AUDIOFOCUS_LOSS, AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                mediaSession.controller.transportControls.pause()
            }

            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> player.mediaPlayer.setVolume(
                0.3f,
                0.3f
            )

            AudioManagerCompat.AUDIOFOCUS_GAIN -> player.mediaPlayer.setVolume(1.0f, 1.0f)
        }
    }

    inner class NoisyReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == AudioManager.ACTION_AUDIO_BECOMING_NOISY) {
                mediaSession.controller.transportControls.pause()
            }
        }
    }

    val callback = object : MediaSessionCompat.Callback() {
        override fun onPlay() {
            super.onPlay()

            Log.i(TAG, "onPlay: ")
            val am = getSystemService(Context.AUDIO_SERVICE) as AudioManager

            audioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN).run {
                setOnAudioFocusChangeListener(afChangeListener)
                setAudioAttributes(AudioAttributes.Builder().run {
                    setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    build()
                })
                build()
            }
            val result = am.requestAudioFocus(audioFocusRequest)
            if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                startService(Intent(applicationContext, MediaBrowserService::class.java))
                mediaSession.isActive = true
                setMediaPlaybackState(PlaybackStateCompat.STATE_PLAYING)
                player.play()
                registerReceiver(
                    noisyReceiver,
                    IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY)
                )
                setMediaMetadata()
                refreshNotification()
                Thread(PlayerPosition()).start()

            }
        }

        override fun onPause() {
            super.onPause()

            Log.i(TAG, "onPause: ")

            player.pause()
            unregisterReceiver(noisyReceiver)
            setMediaPlaybackState(PlaybackStateCompat.STATE_PAUSED)
            stopForeground(CHANNEL_ID)
        }

        override fun onStop() {
            super.onStop()

            val am = getSystemService(Context.AUDIO_SERVICE) as AudioManager
            am.abandonAudioFocusRequest(audioFocusRequest)
            unregisterReceiver(noisyReceiver)
            stopSelf()
            mediaSession.isActive = false
            player.stop()
            stopForeground(CHANNEL_ID)
        }

        override fun onSeekTo(pos: Long) {
            super.onSeekTo(pos)

            player.seekTo(pos.toInt())
        }

        override fun onRewind() {
            super.onRewind()
        }

        override fun onSkipToNext() {
            super.onSkipToNext()

            if (!::player.isInitialized) return
            val next = player.previousTrack()

            if (next == null) {
                onStop()
                Log.i(TAG, "onSkipToNext: zalupa")
            }

            onSkipToQueueItem(player.curIndex!!.toLong())
        }


        override fun onSkipToPrevious() {
            super.onSkipToPrevious()


            if (!::player.isInitialized) return
            val prev = player.previousTrack()

            if (prev == null) {
                onStop()
                Log.i(TAG, "onSkipToPrevious: zalupa")
            }

            onSkipToQueueItem(player.curIndex!!.toLong())
        }

        override fun onSkipToQueueItem(id: Long) {
            super.onSkipToQueueItem(id)

            setMediaPlaybackState(
                PlaybackStateCompat.STATE_SKIPPING_TO_QUEUE_ITEM,
                getBundleWithSongDuration()
            )

            if (id >= list.size) return

            val playbackState = mediaSession.controller.playbackState.state
            index = id.toInt()

            onPrepare()
        }

        override fun onCommand(command: String?, extras: Bundle?, cb: ResultReceiver?) {
            super.onCommand(command, extras, cb)

            if (command == null) return

            if (TextUtils.equals("set_list", command)) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    getList(extras)
                }
            }
        }


        override fun onPrepare() {
            super.onPrepare()
            if (player.currentTrack() == null) {
                Log.i(TAG, "onPrepare: cur track is null")
                return
            }

            if (player.currentTrack()!!.audio_uri == null) {
                Log.i(TAG, "onPrepare: cur track audio uri is null")
                return
            }

            try {
                player.other(player.currentTrack()!!.audio_uri!!)
//                player.resetAtrributes()

                setMediaMetadata()
                refreshNotification()
                setMediaPlaybackState(PlaybackStateCompat.STATE_NONE)
            } catch (e: Exception) {
                Log.i(TAG, "onPrepare: 123321123321")
            }

        }
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        stopSelf()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.action?.let {
            when (it) {
                "play" -> callback.onPlay()
                "pause" -> callback.onPause()
                "next" -> callback.onSkipToNext()
                "previous" -> callback.onSkipToPrevious()
                "set_list" -> getList(intent.extras)
            }
        }
        MediaButtonReceiver.handleIntent(mediaSession, intent)
        return super.onStartCommand(intent, flags, startId)

    }

    override fun onCreate() {
        super.onCreate()

        init()
    }

    private fun init() {
        Log.i(TAG, "init: 321")
        initMediaSession()
        initPlayer()
        initReceiver()
    }

    private fun initReceiver() {
        noisyReceiver = NoisyReceiver()
    }

    private fun initPlayer() {
        player = Player(applicationContext)
        player.onCompletionListener = onCompletionListener

    }

    private fun initMediaSession() {
        mediaSession = MediaSessionCompat(applicationContext, TAG).apply {
            setFlags(MediaSessionCompat.FLAG_HANDLES_QUEUE_COMMANDS)
            setCallback(callback)
            setSessionToken(sessionToken)
            val builder = PlaybackStateCompat
                .Builder()
                .setActions(
                    PlaybackStateCompat.ACTION_PLAY or
                            PlaybackStateCompat.ACTION_PAUSE or
                            PlaybackStateCompat.ACTION_STOP or
                            PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS or
                            PlaybackStateCompat.ACTION_SKIP_TO_NEXT or
                            PlaybackStateCompat.ACTION_PLAY_PAUSE
                )

            setPlaybackState(builder.build())
        }
    }

    override fun onDestroy() {
        super.onDestroy()


        if (::player.isInitialized) player.stop()
        if (::mediaSession.isInitialized) mediaSession.release()
    }

    private fun getList(bundle: Bundle?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            list = bundle?.getParcelableArrayList("list", Audio::class.java)!!
            player.list = list
        }
    }

    private fun setMediaPlaybackState(state: Int, bundle: Bundle? = null) {
        val playbackPosition = player.currentTime().toLong() ?: 0L
        val playbackSpeed = player.mediaPlayer.playbackParams.speed ?: 0f
        val playbackStateBuilder = PlaybackStateCompat.Builder()
            .setState(state, playbackPosition, playbackSpeed)
            .setActiveQueueItemId(if (player.curIndex == null) 0L else player.curIndex!!.toLong())
        playbackStateBuilder.setExtras(bundle)
        playbackStateBuilder.setState(state, playbackPosition, playbackSpeed)
        mediaSession.setPlaybackState(playbackStateBuilder.build())
    }

    private fun setMediaMetadata() {
        mediaSession.setMetadata(
            Tool.metadataBuilder(
                player.currentTrack(),
                applicationContext
            )?.build()
        )
    }

    private fun refreshNotification() {
        val builder = NotificationHelper.notificationBuilder(
            applicationContext,
            mediaSession,
            player.isPlaying()
        )

        startForeground(CHANNEL_ID, builder.build())
    }

    inner class PlayerPosition : Runnable {
        override fun run() {
            while (mediaSession.controller.playbackState.state != PlaybackStateCompat.STATE_NONE ||
                mediaSession.controller.playbackState.state != PlaybackStateCompat.STATE_STOPPED ||
                mediaSession.controller.playbackState.state != PlaybackStateCompat.STATE_ERROR
            ) {
                val state = mediaSession.controller.playbackState.state
                setMediaPlaybackState(state)
                Thread.sleep(900)
            }
        }
    }

    override fun onGetRoot(
        clientPackageName: String,
        clientUid: Int,
        rootHints: Bundle?
    ): BrowserRoot? {
        Log.i(TAG, "onGetRoot: con")
        if (TextUtils.equals(clientPackageName, packageName)) {
            Log.i(TAG, "onGetRoot: OH YEAAAH")
            return BrowserRoot(getString(R.string.app_name), null)
        } else {
            Log.i(TAG, "onGetRoot: 123")
            return null
        }
    }

    override fun onLoadChildren(
        parentId: String,
        result: Result<MutableList<MediaBrowserCompat.MediaItem>>
    ) {
        Log.i(TAG, "onLoadChildren: 123")
        result.sendResult(null)
    }

    override fun onBind(intent: Intent?): IBinder {
        return PlayerServiceBinder()
    }

    inner class PlayerServiceBinder : Binder() {
        fun getMediaSessionToken(): MediaSessionCompat.Token {
            return mediaSession.sessionToken
        }
    }

    override fun bindService(service: Intent, conn: ServiceConnection, flags: Int): Boolean {
        Log.i(TAG, "bindService: 123")
        return super.bindService(service, conn, flags)
    }


    companion object {
        const val TAG = "PLAYBACK_SERVICE"

        @Pashalko
        val CHANNEL_ID = 1488
    }

    private fun getBundleWithSongDuration(): Bundle {
        val playbackDuration = player.mediaPlayer.duration ?: 0
        return Bundle().apply {
            putInt("duration", playbackDuration)
        }
    }
}