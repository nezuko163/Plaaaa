package com.example.plaaaa.ui.adapter

import android.net.Uri
import android.os.Build
import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.RequiresApi


data class Audio(
    val path: String,
    val name: String,
    val album: String,
    val artist: String?,
    val duration: Long,
    val date: Long? = null,
    val art_uri: Uri? = null,
    val audio_uri: Uri? = null,
) : Parcelable {
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString(),
        parcel.readLong(),
        parcel.readValue(Long::class.java.classLoader) as? Long,
        parcel.readParcelable(Uri::class.java.classLoader, Uri::class.java),
        parcel.readParcelable(Uri::class.java.classLoader, Uri::class.java)
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(path)
        parcel.writeString(name)
        parcel.writeString(album)
        parcel.writeString(artist)
        parcel.writeLong(duration)
        parcel.writeValue(date)
        parcel.writeParcelable(art_uri, flags)
        parcel.writeParcelable(audio_uri, flags)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Audio> {
        override fun createFromParcel(parcel: Parcel): Audio {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                Audio(parcel)
            } else {
                TODO()
            }
        }

        override fun newArray(size: Int): Array<Audio?> {
            return arrayOfNulls(size)
        }
    }
}