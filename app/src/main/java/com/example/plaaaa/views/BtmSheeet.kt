package com.example.plaaaa.views

import android.os.CountDownTimer
import android.view.View
import com.example.plaaaa.R
import com.example.plaaaa.adapter.Audio
import com.example.plaaaa.databinding.BtmSheetBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.squareup.picasso.Picasso
import java.util.Timer

class BtmSheeet(val binding: BtmSheetBinding) {
    private val sheetBehavior = BottomSheetBehavior.from(binding.root)
    private val picasso = Picasso.get()



    fun initBtmSheet() {
        binding.apply {
            arrowImg.setOnClickListener {
                if (sheetBehavior.state != BottomSheetBehavior.STATE_EXPANDED)
                    sheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
                else
                    sheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
            }
        }


        val btmSheetCallBack = object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {}
        }

        sheetBehavior.onDetachedFromLayoutParams()

        sheetBehavior.addBottomSheetCallback(btmSheetCallBack)
        sheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN

    }

    fun expand() {
        sheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
    }

    fun collapse() {
        sheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
    }

    fun hide() {
        sheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
    }

    fun state() = sheetBehavior.state

    fun bindBtmSheet(audio: Audio) {
        picasso
            .load(audio.art_uri)
            .error(R.drawable.flowers)
            .into(binding.icon)

        binding.name.text = audio.name
        binding.author.text = audio.artist

        binding.slide.value = 0f
        binding.slide.valueTo = (audio.duration / 1000).toFloat()

        if (sheetBehavior.state == BottomSheetBehavior.STATE_HIDDEN) sheetBehavior.state =
            BottomSheetBehavior.STATE_COLLAPSED
    }

    companion object {
        const val TAG = "BTM_SHEET"
    }
}