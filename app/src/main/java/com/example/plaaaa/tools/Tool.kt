package com.example.plaaaa.tools

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.media.MediaMetadata
import android.net.Uri
import android.os.Build
import android.support.v4.media.MediaMetadataCompat
import com.example.plaaaa.ui.adapter.Audio
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class Tool {
    companion object {
        fun saveBitmapToTemporaryFile(bitmap: Bitmap): File? {
            return try {
                val file = File.createTempFile("temp", ".jpg")
                val outputStream = FileOutputStream(file)
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                outputStream.close()
                file
            } catch (e: IOException) {
                e.printStackTrace()
                null
            }
        }

        fun resIdToUri(context: Context, resId: Int): Uri {
            val uri = Uri.Builder()
                .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
                .authority(context.resources.getResourcePackageName(resId))
                .appendPath(context.resources.getResourceTypeName(resId))
                .appendPath(context.resources.getResourceEntryName(resId))
                .build()

            return uri
        }

        fun metadataBuilder(audio: Audio?, context: Context): MediaMetadataCompat.Builder? {
            if (audio == null) return null
            val source = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ImageDecoder.createSource(context.contentResolver, audio.art_uri!!)
            } else {
                TODO("VERSION.SDK_INT < P")
            }
            val bitmap = ImageDecoder.decodeBitmap(source)

            val builder = MediaMetadataCompat.Builder()
                    .putBitmap(MediaMetadataCompat.METADATA_KEY_ART, bitmap)
            .putString(MediaMetadataCompat.METADATA_KEY_TITLE, audio.name)
            .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, audio.album)
            .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, audio.artist)
            .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, audio.duration)

            return builder
        }
    }
}