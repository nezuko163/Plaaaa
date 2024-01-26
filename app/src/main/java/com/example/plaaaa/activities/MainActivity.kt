package com.example.plaaaa.activities


import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.plaaaa.R
import com.example.plaaaa.adapter.Audio
import com.example.plaaaa.adapter.AudioAdapter
import com.example.plaaaa.databinding.ActivityMainBinding
import com.example.plaaaa.tools.AllAudios
import com.example.plaaaa.views.BtmSheeet
import com.example.plaaaa.player.Player
import com.squareup.picasso.Picasso
import com.example.plaaaa.tools.PermissionUtil
import com.google.android.material.slider.Slider

class MainActivity : AppCompatActivity() {
    private lateinit var sheetBehavior: BtmSheeet
    private lateinit var binding: ActivityMainBinding
    private val picasso = Picasso.get()
    private lateinit var player: Player

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
        if (PermissionUtil.hasPermissions(this)) {
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
        Log.i(TAG, "init: AEAAAEA")
        initPlayer()
        initList()
        initRcView()
        initBtmSheet()
    }

    private fun initPlayer() {
        player = Player(applicationContext)
        player.onCompletion = { addOnCompletionListener() }
    }

    private fun initList() {
        lst = AllAudios.getAudios(applicationContext)
        lst.forEach {
            Log.i(TAG, "initList: ${it.audio_uri}")
        }
        player.lst = lst
    }

    private fun initRcView() {
        adapter.setList(lst)
        adapter.onTrackClick = { audio: Audio ->
            if (audio.audio_uri != null) {
                sheetBehavior.bindBtmSheet(audio)
                player.curIndex = adapter.pos
                player.other(audio.audio_uri)
                player.play()
                changePlayIcon(true)
            }
        }
        binding.rcView.layoutManager = LinearLayoutManager(this)
        binding.rcView.adapter = adapter
    }


    companion object {
        const val TAG = "MAIN_ACTIVITY"
    }

    private fun initBtmSheet() {
        binding.btmsheet.apply {
            play.setOnClickListener {
                if (player.isPlaying()) {
                    player.pause()
                    changePlayIcon(false)
                } else {
                    player.play()
                    changePlayIcon(true)
                }
            }

            icon.setOnClickListener {
                player.stop()
                sheetBehavior.hide()
            }

            previous.setOnClickListener {
                if (player.currentTime() / 1000 >= 5) {
                    val previous = player.previousTrack() ?: return@setOnClickListener

                    val uri = previous.audio_uri ?: return@setOnClickListener

                    sheetBehavior.bindBtmSheet(previous)
                    player.other(uri)
                    player.play()

                } else player.seekTo(0)
            }

            next.setOnClickListener {
                val next = player.nextTrack() ?: return@setOnClickListener

                val uri = next.audio_uri ?: return@setOnClickListener

                sheetBehavior.bindBtmSheet(next)
                player.other(uri)
                player.play()
            }
        }

        initSlider()

        sheetBehavior = BtmSheeet(binding.btmsheet)
        sheetBehavior.initBtmSheet()
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
                timeNow.text = "${value.toInt() / 60}:${(value.toInt() % 60)}"
            }
        }

    }

    private fun addOnCompletionListener() {
        player.mediaPlayer.setOnCompletionListener {
            val audio = player.nextTrack()
            if (audio == null) return@setOnCompletionListener
            if (audio.audio_uri == null) return@setOnCompletionListener
            sheetBehavior.bindBtmSheet(audio)
            player.other(audio.audio_uri)
            player.play()
        }
    }

    private fun changePlayIcon(isPlaying: Boolean) {
        if (isPlaying) {
            binding.btmsheet.play.setImageResource(R.drawable.pause)
//            picasso
//                .load(R.drawable.pause)
//                .error(R.drawable.pause)
//                .into(binding.btmsheet.play)
        } else {
            binding.btmsheet.play.setImageResource(R.drawable.play)
//            picasso
//                .load(R.drawable.play)
//                .error(R.drawable.play)
//                .into(binding.btmsheet.play)
        }
    }
}
