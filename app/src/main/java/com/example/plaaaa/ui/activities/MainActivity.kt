package com.example.plaaaa.ui.activities


import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.pm.PackageManager
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.session.MediaController
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat.Callback
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
import com.example.plaaaa.player.Player
import com.example.plaaaa.service.MediaPlaybackService1
import com.squareup.picasso.Picasso
import com.example.plaaaa.tools.PermissionUtil
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.slider.Slider
import kotlinx.coroutines.Runnable


class MainActivity : AppCompatActivity() {
    private lateinit var sheetBehavior: BtmSheeet
    private lateinit var binding: ActivityMainBinding

    //    private lateinit var player: Player
    private lateinit var mediaBrowser: MediaBrowserCompat
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
            if (!grantResults.isEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "yeeeeeeeees", Toast.LENGTH_LONG).show()
                init()
            }
        }
    }


    private fun init() {
        Log.i(TAG, "init: 123")
        initList()
        initMediaBrowserService()
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
    }

    private fun initCallbacks() {
        connectionCallback = object : MediaBrowserCompat.ConnectionCallback() {
            override fun onConnected() {

                // Get the token for the MediaSession
                mediaBrowser.sessionToken.also { token ->

                    // Create a MediaControllerCompat
                    val mediaController = MediaControllerCompat(
                        this@MainActivity, // Context
                        token
                    )

                    // Save the controller
                    MediaControllerCompat.setMediaController(this@MainActivity, mediaController)
                }

                // Finish building the UI
                buildTransportControls()
            }

            override fun onConnectionSuspended() {
                // The Service has crashed. Disable transport controls until it automatically reconnects
            }

            override fun onConnectionFailed() {
                // The Service has refused our connection
            }
        }
    }


    fun buildTransportControls() {
    }


    private fun initList() {
        lst = AllAudios.getAudios(applicationContext)
        lst.forEach {
            Log.i(TAG, "initList: ${it.audio_uri}")
        }

        val bundle = Bundle().apply {
            putParcelableArrayList("list", lst)
        }
        MediaControllerCompat.getMediaController(this)
            .sendCommand("set_list", bundle, null)
    }

    private fun initRcView() {
        val mediaController = MediaControllerCompat.getMediaController(this)
        adapter.setList(lst)
        adapter.onTrackClick = { audio: Audio ->
            if (audio.audio_uri != null) {
                sheetBehavior.bindBtmSheet(audio)
//                player.curIndex = adapter.pos
//                player.other(audio.audio_uri)
//                player.play()
                mediaController.transportControls.skipToQueueItem(adapter.pos.toLong())

                changePlayIcon(true)
                Thread(AeRunnable()).start()
            }
        }
        binding.rcView.layoutManager = LinearLayoutManager(this)
        binding.rcView.adapter = adapter
    }


    companion object {
        const val TAG = "MAIN_ACTIVITY"
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        return super.onCreateOptionsMenu(menu)
    }

    private fun initBtmSheet() {
        binding.btmsheet.apply {
            play.setOnClickListener {
                if (mediaController.playbackState?.state == PlaybackStateCompat.STATE_PLAYING) {
                    mediaController.transportControls.pause()
                    changePlayIcon(false)
                } else {
                    mediaController.transportControls.play()
                    changePlayIcon(true)
                }
            }

            icon.setOnClickListener {
                sheetBehavior.hide()
            }

            previous.setOnClickListener {
                if (mediaController.playbackState?.position .currentTime() / 1000 >= 5) {
                    val previous = player.previousTrack() ?: return@setOnClickListener

                    val uri = previous.audio_uri ?: return@setOnClickListener

                    sheetBehavior.bindBtmSheet(previous)
                    player.other(uri)
                    player.play()
                    Thread(AeRunnable()).start()

                } else player.seekTo(0)
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
                    player.stop()
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {}

        }
        sheetBehavior.initBtmSheet()
    }

    private fun playNext() {
        val next = player.nextTrack() ?: return

        val uri = next.audio_uri ?: return

        sheetBehavior.bindBtmSheet(next)
        player.other(uri)
        player.play()
        Thread(AeRunnable()).start()
    }

    private fun initSlider() {
        binding.btmsheet.apply {
            slide.addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
                override fun onStartTrackingTouch(slider: Slider) {
                }

                override fun onStopTrackingTouch(slider: Slider) {
                    val ms: Int = slider.value.toInt() * 1000
                    player.seekTo(ms)
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
            Thread.sleep(100)
            while (sheetBehavior.state() != BottomSheetBehavior.STATE_HIDDEN) {
                Log.i(TAG, "run: 123")
                if (!player.isPlaying()) {
                    Thread.sleep(2000)
                    continue
                }
                mins = (player.currentTime() / 1000) / 60
                secs = (player.currentTime() / 1000) % 60
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

        MediaControllerCompat.getMediaController(this)?.unregisterCallback(controllerCallback)
        mediaBrowser.disconnect()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::sheetBehavior.isInitialized) {
            sheetBehavior.hide()
        }
    }
}
