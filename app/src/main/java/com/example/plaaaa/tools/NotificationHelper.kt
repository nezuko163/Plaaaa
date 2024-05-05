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
import androidx.core.content.ContextCompat
import androidx.media.session.MediaButtonReceiver
import com.example.plaaaa.R

class NotificationHelper {
    companion object {
        fun notificationBuilder(
            context: Context,
            mediaSession: MediaSessionCompat,
            isPlaying: Boolean,
            channelId: String = "angelwitta"
        ): NotificationCompat.Builder {
            val controller = mediaSession.controller
            val mediaMetadata = controller.metadata
            val description = mediaMetadata.description

            val builder = NotificationCompat.Builder(context, channelId).apply {

                // Add the metadata for the currently playing track
                setContentTitle(description.title)
                setContentText(description.subtitle)
                setSubText(description.description)
                setLargeIcon(description.iconBitmap)


                // Enable launching the player by clicking the notification
                setContentIntent(controller.sessionActivity)

                // Stop the service when the notification is swiped away
                setDeleteIntent(
                    MediaButtonReceiver.buildMediaButtonPendingIntent(
                        context,
                        PlaybackStateCompat.ACTION_STOP
                    )
                )

                // Make the transport controls visible on the lockscreen
                setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

                // Add an app icon and set its accent color
                // Be careful about the color
                setSmallIcon(R.drawable.flowers)
                val color = ContextCompat.getColor(context, R.color.cyan)
                setColor(color)

                // Add a pause button
                addAction(
                    NotificationCompat.Action(
                        if (isPlaying) R.drawable.pause else R.drawable.play,
                        context.getString(if (isPlaying) R.string.pause else R.string.play),
                        MediaButtonReceiver.buildMediaButtonPendingIntent(
                            context,
                            PlaybackStateCompat.ACTION_PLAY_PAUSE
                        )
                    )
                )

                // Add a skip to previous button
                addAction(
                    NotificationCompat.Action(
                        R.drawable.baseline_fast_rewind_24,
                        context.getString(R.string.previous),
                        MediaButtonReceiver.buildMediaButtonPendingIntent(
                            context,
                            PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
                        )
                    )
                )

                // Add skip to next button
                addAction(
                    NotificationCompat.Action(
                        R.drawable.baseline_fast_forward_24,
                        context.getString(R.string.next),
                        MediaButtonReceiver.buildMediaButtonPendingIntent(
                            context,
                            PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
                        )
                    )
                )

                // Take advantage of MediaStyle features
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

                setPriority(NotificationCompat.PRIORITY_DEFAULT)
            }

            return builder
        }
    }
}