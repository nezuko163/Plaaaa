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
import com.example.plaaaa.player.Player
import com.example.plaaaa.tools.AllAudios
import com.example.plaaaa.btmSheet.BtmSheeet
import com.squareup.picasso.Picasso
import com.example.plaaaa.tools.PermissionUtil

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
        player = Player(applicationContext)
        Log.i(TAG, "init: AEAAAEA")
        initList()
        initRcView()
        initBtmSheet()
    }


    private fun initList() {
        lst = AllAudios.getAudios(applicationContext)
        lst.forEach {
            Log.i(TAG, "initList: ${it.audio_uri}")
        }
    }

    private fun initRcView() {
        adapter.setList(lst)
        adapter.onTrackClick = { audio: Audio ->
            if (audio.audio_uri != null) {
                sheetBehavior.bindBtmSheet(audio)
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
        }


        sheetBehavior = BtmSheeet(binding.btmsheet)
        sheetBehavior.initBtmSheet()
    }

    private fun changePlayIcon(isPlaying: Boolean) {
        if (isPlaying) {
//            picasso
//                .load(R.drawable.pause)
//                .error(R.drawable.pause)
//                .into(binding.btmsheet.play)
            binding.btmsheet.play.setImageResource(R.drawable.pause)
        } else {
//            picasso
//                .load(R.drawable.play)
//                .error(R.drawable.play)
//                .into(binding.btmsheet.play)
            binding.btmsheet.play.setImageResource(R.drawable.play)
        }
    }
}
