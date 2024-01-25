package com.example.plaaaa.tools

import android.content.Context
import android.net.Uri
import com.CodeBoy.MediaFacer.AudioGet
import com.CodeBoy.MediaFacer.MediaFacer
import com.example.plaaaa.adapter.Audio

class AllAudios {
    companion object {
        fun getAudios(context: Context): ArrayList<Audio> {
            val lst_audio = ArrayList<Audio>()

            MediaFacer.withAudioContex(context)
                .getAllAudioContent(AudioGet.externalContentUri)
                .forEach {
                    if (it.duration == 0L) return@forEach
                    val audio = Audio(
                        it.filePath,
                        it.name,
                        it.album,
                        it.artist,
                        it.duration,
                        it.date_taken,
                        it.art_uri,
                        Uri.parse(it.assetFileStringUri),
                    )

                    lst_audio.add(audio)
                }
            return lst_audio
        }

    }
}