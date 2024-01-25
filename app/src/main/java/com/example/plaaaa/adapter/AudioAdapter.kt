package com.example.plaaaa.adapter

import android.annotation.SuppressLint
import android.graphics.PorterDuff
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.plaaaa.R
import com.example.plaaaa.databinding.AudioItemBinding
import com.squareup.picasso.Picasso
import java.lang.RuntimeException


class AudioAdapter : RecyclerView.Adapter<AudioAdapter.AudioViewHolder>() {
    private var lst = ArrayList<Audio>()
    private lateinit var lastTrack: AudioViewHolder
    private val picasso = Picasso.get()

    lateinit var onTrackClick: (Audio) -> Unit

    companion object {
        const val TAG = "AUDIO_ADAPTER"
    }

    inner class AudioViewHolder(item: View) : RecyclerView.ViewHolder(item) {
        val binding = AudioItemBinding.bind(item)
        var isPlayable = false

        init {
            item.setOnClickListener {
                Log.i(TAG, "123123: 123")
                onTrackClick.invoke(lst[bindingAdapterPosition])

                if (!::lastTrack.isInitialized) {
                    lastTrack = this
                    lastTrack.isPlayable = true
                    lastTrack.setGrayFilter(binding.icon)
                } else {
                    if (lastTrack == this) {
                        lastTrack.isPlayable = false
                    } else {
                        lastTrack.clearGrayFilter(lastTrack.binding.icon)
                        lastTrack = this
                        setGrayFilter(lastTrack.binding.icon)
                    }
                }
            }
        }

        @SuppressLint("SetTextI18n")
        fun bind(audio: Audio) {
            binding.audioName.text = audio.name
            binding.authorName.text = audio.artist
            binding.duration.text = "${audio.duration / 60000}:${(audio.duration % 60000) / 1000}"

            if (audio.art_uri == null) setDefaultIcon()
            else {
                setIcon(audio.art_uri)
            }
        }

        private fun setIcon(uri: Uri) {
            Log.i(TAG, "setIcon: $uri")
            picasso.load(uri)
                .error(R.drawable.flowers)
                .into(binding.icon)

        }

        private fun setDefaultIcon() {
            picasso
                .load(R.drawable.flowers)
                .into(binding.icon)
        }

        private fun setGrayFilter(view: ImageView) {
            view.setColorFilter(R.color.black, PorterDuff.Mode.SRC_OVER)
        }

        private fun clearGrayFilter(view: ImageView) {
            view.clearColorFilter()
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AudioViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.audio_item, parent, false)
        return AudioViewHolder(itemView)
    }

    override fun getItemCount() = lst.size

    override fun onBindViewHolder(holder: AudioViewHolder, position: Int) {
        holder.bind(lst[position])
    }

    @SuppressLint("NotifyDataSetChanged")
    fun addAudio(audio: Audio) {
        lst.add(audio)
        sortList()
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun deleteAudio(audio: Audio) {
        lst.remove(audio)
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun redactAudio(audio_old: Audio, audio_new: Audio) {
        lst[lst.indexOf(audio_old)] = audio_new
        sortList()
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setList(_list: ArrayList<Audio>) {
        lst = _list
        notifyDataSetChanged()

        if (lst.size == 0) {
            throw RuntimeException("asd")
        }
    }

    private fun sortList() {
        lst.sortBy {
            it.date
        }
    }
}