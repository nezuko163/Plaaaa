package com.example.plaaaa.tools

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
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
    }
}