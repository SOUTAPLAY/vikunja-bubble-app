package com.sou56.vikunjabubble

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Icon
import androidx.core.app.NotificationCompat
import androidx.core.app.Person

object BubbleNotificationHelper {

    private const val CHANNEL_ID  = "vikunja_bubble_channel"
    private const val NOTIF_ID    = 1001
    private const val SHORTCUT_ID = "bubble_shortcut"

    fun createChannel(context: Context) {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "バブルチャンネル",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Vikunjaタスク入力バブル"
        }
        val nm = context.getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(channel)
    }

    fun showBubble(context: Context, message: String) {
        val bubbleIntent = PendingIntent.getActivity(
            context, 0,
            Intent(context, BubbleActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )

        val icon = Icon.createWithResource(context, R.mipmap.ic_launcher)

        val bubbleMetadata = Notification.BubbleMetadata.Builder(bubbleIntent, icon)
            .setDesiredHeight(600)
            .setAutoExpandBubble(false)
            .setSuppressNotification(false)
            .build()

        val person = Person.Builder()
            .setName("Vikunja")
            .setIcon(androidx.core.graphics.drawable.IconCompat.createWithResource(context, R.mipmap.ic_launcher))
            .build()

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("予定入力")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setShortcutId(SHORTCUT_ID)
            .addPerson(person)
            .setBubbleMetadata(
                NotificationCompat.BubbleMetadata.Builder(
                    bubbleIntent,
                    androidx.core.graphics.drawable.IconCompat.createWithResource(context, R.mipmap.ic_launcher)
                )
                .setDesiredHeight(600)
                .setAutoExpandBubble(false)
                .setSuppressNotification(false)
                .build()
            )
            .build()

        val nm = context.getSystemService(NotificationManager::class.java)
        nm.notify(NOTIF_ID, notification)
    }
}
