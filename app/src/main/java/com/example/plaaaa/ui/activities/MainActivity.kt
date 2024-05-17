package com.example.plaaaa.ui.activities


import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.media.AudioManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaControllerCompat.*
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import android.view.Menu
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.plaaaa.R
import com.example.plaaaa.ui.adapter.Audio
import com.example.plaaaa.ui.adapter.AudioAdapter
import com.example.plaaaa.databinding.ActivityMainBinding
import com.example.plaaaa.tools.AllAudios
import com.example.plaaaa.ui.views.BtmSheeet
import com.example.plaaaa.service.MediaPlaybackService1
import com.squareup.picasso.Picasso
import com.example.plaaaa.tools.PermissionUtil
import com.example.plaaaa.tools.Tool
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.slider.Slider
import kotlinx.coroutines.Runnable
import kotlin.math.log


class MainActivity : AppCompatActivity() {
    private lateinit var sheetBehavior: BtmSheeet
    private lateinit var binding: ActivityMainBinding

    //    private lateinit var player: Player
    private lateinit var mediaBrowser: MediaBrowserCompat
    private lateinit var mediaControllerCompat: MediaControllerCompat

    private lateinit var controllerCallback: Callback
    private lateinit var connectionCallback: MediaBrowserCompat.ConnectionCallback

    private val picasso = Picasso.get()

    private val PERMISSION_STORAGE = 8000
    private val adapter = AudioAdapter()
    private var lst = ArrayList<Audio>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        requirePermission()
    }

    override fun onStart() {
        super.onStart()
        mediaBrowser.connect()
        Log.i(TAG, "onStart: 321")
        Log.i(TAG, "onStart: controller =  ${::mediaControllerCompat.isInitialized}")
    }


    private fun requirePermission() {
        if (PermissionUtil.hasExternalStoragePermission(this)) {
            Toast.makeText(this, "aeee", Toast.LENGTH_LONG).show()
            init()
        } else {
            Toast.makeText(this, "neeeeeeeeeeeeeet", Toast.LENGTH_LONG).show()
            PermissionUtil.requestPermissions(this, PERMISSION_STORAGE)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == PERMISSION_STORAGE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "yeeeeeeeees", Toast.LENGTH_LONG).show()
                init()
            }
        }
    }


    private fun init() {
        Log.i(TAG, "init: initialized")
        initMediaBrowserService()
        Thread.sleep(1000)
        initList()
        initRcView()
        initBtmSheet()
    }

    private fun initMediaBrowserService() {
        initCallbacks()
        mediaBrowser = MediaBrowserCompat(
            this,
            ComponentName(this, MediaPlaybackService1::class.java),
            connectionCallback,
            null
        )
        Log.i(TAG, "initMediaBrowserService: connect")
        Log.i(TAG, "initMediaBrowserService: ${::mediaBrowser.isInitialized}")
//        mediaBrowser.connect()

        bindService(Intent(this, MediaPlaybackService1::class.java),
            object : ServiceConnection {
                override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                    mediaControllerCompat =
                }

                override fun onServiceDisconnected(name: ComponentName?) {
                    TODO("Not yet implemented")
                }

            },
            Context.BIND_AUTO_CREATE
            )

        Log.i(TAG, "initMediaBrowserService: ${mediaBrowser.sessionToken}")
        Log.i(TAG, "initMediaBrowserService: conneccccc")
    }

    private fun initCallbacks() {
        connectionCallback = object : MediaBrowserCompat.ConnectionCallback() {
            override fun onConnected() {
                mediaControllerCompat =
                    MediaControllerCompat(this@MainActivity, mediaBrowser.sessionToken)
                setMediaController(this@MainActivity, mediaControllerCompat)
                buildTransportControls()
            }

            override fun onConnectionSuspended() {
                Log.i(TAG, "onConnectionSuspended: 2")
                mediaControllerCompat.unregisterCallback(controllerCallback)
            }

            override fun onConnectionFailed() {
                Log.i(TAG, "onConnectionFailed: 3")
                mediaControllerCompat.unregisterCallback(controllerCallback)
            }
        }

        controllerCallback = object : Callback() {
            override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
                super.onMetadataChanged(metadata)

                if (metadata == null) return
                sheetBehavior.bindBtmSheet(metadata)
            }

            override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
                super.onPlaybackStateChanged(state)
                if (state == null) return

                changePlayIcon(state.state == PlaybackStateCompat.STATE_PLAYING)

            }

            override fun onSessionDestroyed() {
                super.onSessionDestroyed()
                mediaBrowser.disconnect()
            }
        }
        Log.i(TAG, "initCallbacks: initialized")
    }


    fun buildTransportControls() {
        mediaControllerCompat.registerCallback(controllerCallback)
    }


    private fun initList() {
        lst = AllAudios.getAudios(applicationContext)

        val bundle = Bundle().apply {
            putParcelableArrayList("list", lst)
        }
        mediaControllerCompat.sendCommand("set_list", bundle, null)
    }

    private fun initRcView() {
        adapter.setList(lst)
        adapter.onTrackClick = { audio: Audio ->
            if (audio.audio_uri != null) {
                Tool.metadataBuilder(audio, this@MainActivity)
                    ?.let { sheetBehavior.bindBtmSheet(it.build()) }
//                player.curIndex = adapter.pos
//                player.other(audio.audio_uri)
//                player.play()
                mediaControllerCompat.transportControls.skipToQueueItem(adapter.pos.toLong())

                changePlayIcon(true)
                Thread(AeRunnable()).start()
            }
        }
        mediaControllerCompat.transportControls.setRepeatMode(PlaybackStateCompat.REPEAT_MODE_ALL)
        binding.rcView.layoutManager = LinearLayoutManager(this)
        binding.rcView.adapter = adapter
    }


    companion object {
        const val TAG = "MAIN_ACTIVITY"
    }

    private fun initBtmSheet() {
        binding.btmsheet.apply {
            play.setOnClickListener {
                if (mediaControllerCompat.playbackState?.state == PlaybackStateCompat.STATE_PLAYING) {
                    mediaControllerCompat.transportControls.pause()
                    changePlayIcon(false)
                } else {
                    mediaControllerCompat.transportControls.play()
                    changePlayIcon(true)
                }
            }

            icon.setOnClickListener {
                sheetBehavior.hide()
            }

            previous.setOnClickListener {
                if (mediaControllerCompat.playbackState.position / 1000 >= 5) {
                    val metadata = mediaControllerCompat.metadata
                    sheetBehavior.bindBtmSheet(metadata)
                    Thread(AeRunnable()).start()

                } else mediaControllerCompat.transportControls.seekTo(0)
            }

            next.setOnClickListener {
                playNext()
            }
        }

        initSlider()

        sheetBehavior = BtmSheeet(binding.btmsheet)
        sheetBehavior.btmSheetCallBack = object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                    mediaControllerCompat.transportControls.stop()
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {}

        }
        sheetBehavior.initBtmSheet()
    }

    private fun playNext() {
        val next = mediaControllerCompat.metadata
        sheetBehavior.bindBtmSheet(next)
        Thread(AeRunnable()).start()
    }

    @SuppressLint("SetTextI18n")
    private fun initSlider() {
        binding.btmsheet.apply {
            slide.addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
                override fun onStartTrackingTouch(slider: Slider) {
                }

                override fun onStopTrackingTouch(slider: Slider) {
                    val ms: Long = slider.value.toLong() * 1000
                    mediaControllerCompat.transportControls.seekTo(ms)
                }
            })

            slide.addOnChangeListener { slider, value, fromUser ->
                val mins = value.toInt() / 60
                val secs = value.toInt() % 60
                timeNow.text = "${mins}:${secs.toString().padStart(2, '0')}"
            }
        }


    }


    inner class AeRunnable : Runnable {
        var mins = 0
        var secs = 0

        @SuppressLint("SetTextI18n")
        override fun run() {
            Thread.sleep(500)
            while (sheetBehavior.state() != BottomSheetBehavior.STATE_HIDDEN) {
                Log.i(TAG, "run: 123")
                mins = ((mediaControllerCompat.playbackState.position / 1000) / 60).toInt()
                secs = ((mediaControllerCompat.playbackState.position / 1000) % 60).toInt()
                try {
                    runOnUiThread {
                        binding.btmsheet.timeNow.text =
                            "${mins}:${secs.toString().padStart(2, '0')}"
                    }
                    Thread.sleep(500)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            return
        }
    }


    private fun changePlayIcon(isPlaying: Boolean) {
        if (isPlaying) {
            binding.btmsheet.play.setImageResource(R.drawable.pause)
        } else {
            binding.btmsheet.play.setImageResource(R.drawable.play)
        }
    }

    private fun onCompletion() {
        playNext()
    }

    override fun onResume() {
        super.onResume()
        volumeControlStream = AudioManager.STREAM_MUSIC
    }

    override fun onStop() {
        super.onStop()

        mediaControllerCompat.transportControls.stop()
        mediaControllerCompat.unregisterCallback(controllerCallback)
        mediaBrowser.disconnect()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::sheetBehavior.isInitialized) {
            sheetBehavior.hide()
        }
    }
}
