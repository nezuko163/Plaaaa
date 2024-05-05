package com.example.plaaaa.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.AudioManager.ACTION_AUDIO_BECOMING_NOISY
import android.media.AudioManager.AUDIOFOCUS_LOSS
import android.media.AudioManager.AUDIOFOCUS_LOSS_TRANSIENT
import android.media.AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK
import android.media.AudioManager.AUDIOFOCUS_REQUEST_GRANTED
import android.media.MediaPlayer
import android.media.MediaPlayer.MEDIA_ERROR_IO
import android.media.MediaPlayer.MEDIA_ERROR_MALFORMED
import android.media.MediaPlayer.MEDIA_ERROR_UNKNOWN
import android.media.MediaPlayer.OnCompletionListener
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Parcelable
import android.service.media.MediaBrowserService
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.MediaSessionCompat.QueueItem
import android.support.v4.media.session.PlaybackStateCompat
import android.support.v4.media.session.PlaybackStateCompat.REPEAT_MODE_ALL
import android.support.v4.media.session.PlaybackStateCompat.REPEAT_MODE_ONE
import android.support.v4.media.session.PlaybackStateCompat.STATE_ERROR
import android.support.v4.media.session.PlaybackStateCompat.STATE_PAUSED
import android.support.v4.media.session.PlaybackStateCompat.STATE_PLAYING
import android.support.v4.media.session.PlaybackStateCompat.STATE_SKIPPING_TO_NEXT
import android.support.v4.media.session.PlaybackStateCompat.STATE_STOPPED
import android.text.TextUtils
import android.util.Log
import android.view.KeyEvent
import android.widget.MediaController.MediaPlayerControl
import android.widget.Toast
import androidx.core.content.IntentCompat
import androidx.media.AudioManagerCompat.AUDIOFOCUS_GAIN
import androidx.media.MediaBrowserServiceCompat
import androidx.media.session.MediaButtonReceiver
import com.example.plaaaa.R
import com.example.plaaaa.player.Player
import com.example.plaaaa.ui.adapter.Audio
import java.io.IOException

class MediaPlaybackService : MediaBrowserServiceCompat(), MediaPlayer.OnErrorListener {

    private val TAG = "MEDIA_SERVICE_ZALUPA"

    private val channelId = "music"
    private var currentIndex = -1L
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var player: Player
    private val list: ArrayList<Audio> = ArrayList()
    private lateinit var audioFocusRequest: AudioFocusRequest
    private lateinit var mediaSessionCompat: MediaSessionCompat

    private val mediaSessionCallback: MediaSessionCompat.Callback =
        object : MediaSessionCompat.Callback() {

            override fun onMediaButtonEvent(mediaButtonEvent: Intent?): Boolean {
                val keyEvent: KeyEvent? = mediaButtonEvent?.parcelable(
                    Intent.EXTRA_KEY_EVENT,
                    KeyEvent::class.java
                )

                keyEvent?.let { event ->
                    when (event.keyCode) {
                        KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE -> {
                            if (player.isPlaying()) onPause()
                            else onPlay()
                        }

                        KeyEvent.KEYCODE_MEDIA_STOP -> onStop()
                        KeyEvent.KEYCODE_MEDIA_PLAY -> onPlay()
                        KeyEvent.KEYCODE_MEDIA_PAUSE -> onPause()
                        KeyEvent.KEYCODE_MEDIA_SKIP_BACKWARD -> onSkipToPrevious()
                        KeyEvent.KEYCODE_MEDIA_SKIP_FORWARD -> onSkipToNext()
                    }
                }
                return super.onMediaButtonEvent(mediaButtonEvent)
            }

            override fun onPause() {
                super.onPause()
                player.pause()
                setMediaPlaybackState(STATE_PAUSED, getBundleWithSongDuration())
                refreshNotification()
            }

            override fun onSkipToQueueItem(id: Long) {
                super.onSkipToQueueItem(id)

                if (id >= list.size) return

                val playbackState = mediaSessionCompat.controller.playbackState.state
                currentIndex = id
                onPrepare()
                if (playbackState == PlaybackStateCompat.STATE_PLAYING ||
                    playbackState == STATE_SKIPPING_TO_NEXT
                ) onPlay()
            }

            override fun onSkipToNext() {
                super.onSkipToNext()

                val audio = player.nextTrack()
                if (audio == null) {
                    Log.i(TAG, "onSkipToNext: audio = null")
                    onStop()
                }

                onSkipToQueueItem(player.curIndex!!.toLong())
            }

            override fun onSkipToPrevious() {
                super.onSkipToPrevious()

                val audio = player.previousTrack()
                if (audio == null) {
                    Log.i(TAG, "onSkipToPrevious: audio = null")
                    onStop()
                }

                onSkipToQueueItem(player.curIndex!!.toLong())
            }

            override fun onAddQueueItem(description: MediaDescriptionCompat?) {
                onAddQueueItem(description, list.size)
            }

            override fun onAddQueueItem(description: MediaDescriptionCompat?, index: Int) {
                super.onAddQueueItem(description, index)

                val presetId = description?.extras?.getLong("queue_id")
                var id: Number = when {
                    presetId == null -> 0
                    list.size < presetId -> list.size
                    else -> presetId
                }

                val item = QueueItem(description, id,)
                try {
                    list.add(id.toInt(), item)
                } catch (e: IndexOutOfBoundsException) {
                    list.add(item)
                }

                mediaSessionCompat.setQueue(list)
            }

            override fun onPlay() {
                super.onPlay()

                try {
                    if (::player.isInitialized && !player.isPlaying()) {
                        val audioManager =
                            applicationContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager

                        audioFocusRequest = AudioFocusRequest.Builder(AUDIOFOCUS_GAIN).run {
                            setAudioAttributes(AudioAttributes.Builder().run {
                                setOnAudioFocusChangeListener(afChangeListener)
                                setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                                build()
                            })
                            build()
                        }

                        val audioFocusRequestOutcome =
                            audioManager.requestAudioFocus(audioFocusRequest)
                        if (audioFocusRequestOutcome == AUDIOFOCUS_REQUEST_GRANTED) {
                            startService(
                                Intent(
                                    applicationContext,
                                    MediaBrowserService::class.java
                                )
                            )
                            mediaSessionCompat.isActive = true
                            try {
                                refreshNotification()
                                player.play()
                                setMediaPlaybackState(STATE_PLAYING, getBundleWithSongDuration())
                            } catch (_: NullPointerException) {
                                onError(player.mediaPlayer, MEDIA_ERROR_UNKNOWN, 0)
                            }
                        }
                    }
                } catch (e: Exception) {
                    onError(player.mediaPlayer, MEDIA_ERROR_UNKNOWN, MEDIA_ERROR_IO)
                }
            }

            override fun onPrepare() {
//                if (list.isEmpty()) onError(mediaPlayer, MEDIA_ERROR_UNKNOWN, 0)

//                if (currentIndex == -1L) currentIndex = 0

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

                    setCurrentMetadata()
                    refreshNotification()
                    setMediaPlaybackState(0)

                } catch (_: IOException) {
                    onError(player.mediaPlayer, MEDIA_ERROR_UNKNOWN, MEDIA_ERROR_IO)
                } catch (_: IllegalStateException) {
                    onError(player.mediaPlayer, MEDIA_ERROR_UNKNOWN, MEDIA_ERROR_IO)
                } catch (_: IllegalArgumentException) {
                    onError(player.mediaPlayer, MEDIA_ERROR_UNKNOWN, MEDIA_ERROR_MALFORMED)
                }
            }

            override fun onStop() {
                super.onStop()

                list.clear()
                mediaSessionCompat.setQueue(null)
                currentIndex = -1
                player.stop()
                stopForeground(STOP_FOREGROUND_REMOVE)

                try {
                    val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
                    audioManager.abandonAudioFocusRequest(audioFocusRequest)
                } catch (_: UninitializedPropertyAccessException) {
                }

                setMediaPlaybackState(STATE_STOPPED)
                stopSelf()
            }

            override fun onSeekTo(pos: Long) {
                super.onSeekTo(pos)

                player.seekTo(pos.toInt())
            }


        }


    private fun getBundleWithSongDuration(): Bundle {
        val playbackDuration = player.mediaPlayer.duration ?: 0
        return Bundle().apply {
            putInt("duration", playbackDuration)
        }
    }

    private val afChangeListener = AudioManager.OnAudioFocusChangeListener { focusChange ->
        when (focusChange) {
            AUDIOFOCUS_LOSS, AUDIOFOCUS_LOSS_TRANSIENT -> {
                mediaSessionCompat.controller.transportControls.pause()
            }

            AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> player.mediaPlayer.setVolume(0.3f, 0.3f)
            AUDIOFOCUS_GAIN -> player.mediaPlayer.setVolume(1.0f, 1.0f)
        }
    }

    private val noisyReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (::player.isInitialized) {
                if (player.isPlaying()) mediaSessionCallback.onPause()
            }
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
        mediaSessionCompat.setPlaybackState(playbackStateBuilder.build())
    }

    private fun refreshNotification() {
        TODO("Not yet implemented")
    }

    private fun setCurrentMetadata() {
        TODO("Not yet implemented")
    }

    override fun onCreate() {
        super.onCreate()

        init()
    }

    private fun init() {
        initMediaSession()
        initPlayer()
        initReciever()
    }

    private fun initReciever() {
        val filter = IntentFilter(ACTION_AUDIO_BECOMING_NOISY)
        registerReceiver(noisyReceiver, filter)
    }


    private fun initPlayer() {
        player = Player(applicationContext)

        player.onCompletionListener = OnCompletionListener {
            val repeatMode = mediaSessionCompat.controller.repeatMode
            when (repeatMode) {
                REPEAT_MODE_ONE -> {}
                REPEAT_MODE_ALL -> mediaSessionCallback.onSkipToNext()
                else -> mediaSessionCallback.onStop()
            }

            mediaSessionCallback.onPrepare()
            mediaSessionCallback.onPlay()
        }
    }

    private fun initMediaSession() {
        mediaSessionCompat = MediaSessionCompat(baseContext, channelId).apply {
            setFlags(MediaSessionCompat.FLAG_HANDLES_QUEUE_COMMANDS)
            setCallback(mediaSessionCallback)
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

    override fun onError(mp: MediaPlayer?, what: Int, extra: Int): Boolean {
        setMediaPlaybackState(STATE_ERROR)
        mediaSessionCompat.controller.transportControls.stop()
        stopForeground(STOP_FOREGROUND_REMOVE)
        Toast.makeText(application, "zhopa", Toast.LENGTH_LONG).show()
        return true
    }

    override fun onGetRoot(
        clientPackageName: String,
        clientUid: Int,
        rootHints: Bundle?,
    ): BrowserRoot? {
        return if (TextUtils.equals(clientPackageName, packageName)) {
            BrowserRoot(getString(R.string.app_name), null)
        } else null
    }

    override fun onLoadChildren(
        parentId: String,
        result: Result<MutableList<MediaBrowserCompat.MediaItem>>,
    ) {
        result.sendResult(null)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        MediaButtonReceiver.handleIntent(mediaSessionCompat, intent)
        return super.onStartCommand(intent, flags, startId)
    }

    inline fun <reified T : Parcelable> Intent.parcelable(key: String, java: Class<KeyEvent>): T? =
        IntentCompat.getParcelableExtra(this, key, T::class.java)
}