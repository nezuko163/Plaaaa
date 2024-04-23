package com.example.plaaaa.player

import androidx.media3.common.MediaItem

class Playlist() {
    var items = ArrayList<MediaItem>()
    var index: Int? = null
    var isLooping = false

    fun next(): MediaItem? {
        if (index == null) return null

        if (index == items.size - 1) {
            if (isLooping) {
                index = 0
            } else return null
        } else {
            index = index!! + 1
        }

        return items[index!!]
    }

    fun current(): MediaItem? {
        if (index == null) return null
        else return items[index!!]
    }

    fun previos(): MediaItem? {
        if (index == null) return null

        if (index == 0) {
            if (isLooping) {
                index = items.size - 1
            } else return null
        } else {
            index = index!! - 1
        }

        return items[index!!]
    }
}