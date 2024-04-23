package com.example.plaaaa.tools

import android.app.Notification
import android.app.Notification.MediaStyle
import android.content.Context
import android.media.session.MediaSession
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.MediaSessionCompat.Token
import android.support.v4.media.session.PlaybackStateCompat
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.media.session.MediaButtonReceiver
import com.example.plaaaa.R

class NotificationHelper {
    companion object {
        fun from(context: Context, mediaSession: MediaSessionCompat): NotificationCompat.Builder? {
            val controller = mediaSession.controller
            val mediaMetaData = controller.metadata

            if (mediaMetaData == null) {
                Toast.makeText(context, "zalupa", Toast.LENGTH_LONG).show()
                return null
            }
            val description = mediaMetaData.description

            val channelId = "angelwitta"
            val builder = NotificationCompat.Builder(context, channelId)
            builder.apply {
                setContentTitle(description.title)
                setContentText(description.subtitle)
                setSubText(description.description)
                setLargeIcon(description.iconBitmap)
                setContentIntent(controller.sessionActivity)
                setDeleteIntent(
                    MediaButtonReceiver.buildMediaButtonPendingIntent(
                        context,
                        PlaybackStateCompat.ACTION_STOP
                    )
                )
                setSmallIcon(R.drawable.flowers)
                addAction(
                    NotificationCompat.Action(
                        R.drawable.pause,
                        context.getString(R.string.pause),
                        MediaButtonReceiver.buildMediaButtonPendingIntent(
                            context,
                            PlaybackStateCompat.ACTION_PLAY_PAUSE
                        )
                    )
                )

                setStyle(
                    androidx.media.app.NotificationCompat.MediaStyle()
                        .setMediaSession(mediaSession.sessionToken)
                        .setShowActionsInCompactView(0)

                        // Add a cancel button
                        .setShowCancelButton(true)
                        .setCancelButtonIntent(
                            MediaButtonReceiver.buildMediaButtonPendingIntent(
                                context,
                                PlaybackStateCompat.ACTION_STOP
                            )
                        )
                )

            }


            return builder
        }
    }
}