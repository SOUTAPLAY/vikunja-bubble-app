package com.sou56.vikunjabubble

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.Person
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat

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
            // バブルを有効化するために必須
            setAllowBubbles(true)
        }
        val nm = context.getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(channel)
    }

    fun showBubble(context: Context, message: String) {
        val icon = IconCompat.createWithResource(context, R.mipmap.ic_launcher)

        // ショートカットが必須 (Android 11+)
        val person = Person.Builder()
            .setName("Vikunja")
            .setIcon(icon)
            .setImportant(true)
            .build()

        val shortcut = ShortcutInfoCompat.Builder(context, SHORTCUT_ID)
            .setIntent(Intent(context, BubbleActivity::class.java).setAction(Intent.ACTION_VIEW))
            .setShortLabel("Vikunjaバブル")
            .setIcon(icon)
            .setPerson(person)
            .setLongLived(true)
            .build()
        ShortcutManagerCompat.pushDynamicShortcut(context, shortcut)

        val bubbleIntent = PendingIntent.getActivity(
            context, 0,
            Intent(context, BubbleActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )

        val bubbleMetadata = NotificationCompat.BubbleMetadata.Builder(
            bubbleIntent, icon
        )
            .setDesiredHeight(600)
            .setAutoExpandBubble(true)
            .setSuppressNotification(true)
            .build()

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("予定入力")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setShortcutId(SHORTCUT_ID)
            .addPerson(person)
            .setBubbleMetadata(bubbleMetadata)
            .build()

        val nm = context.getSystemService(NotificationManager::class.java)
        nm.notify(NOTIF_ID, notification)
    }
}
