package com.sou56.vikunjabubble

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.Person
import androidx.core.content.ContextCompat
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat

object BubbleNotificationHelper {

    private const val CHANNEL_ID = "vikunja_bubble_channel"
    private const val NOTIF_ID = 1001
    private const val SHORTCUT_ID = "vikunja_bubble_conversation"
    private const val CATEGORY_TEXT_SHARE_TARGET =
        "com.sou56.vikunjabubble.category.TEXT_SHARE_TARGET"

    // ショートカットが既に登録済みかどうかのフラグ
    private var shortcutPushed = false

    fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Vikunja Bubble",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Vikunja bubble conversation"
                setAllowBubbles(true)
            }
            val nm = context.getSystemService(NotificationManager::class.java)
            nm.createNotificationChannel(channel)
        }
    }

    fun showBubble(context: Context, message: String) {
        // Android 13+ は通知権限を再確認。なければ何もしない。
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    context, Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) return
        }

        val icon = IconCompat.createWithResource(context, R.mipmap.ic_launcher)

        val targetIntent = Intent(context, BubbleActivity::class.java).apply {
            action = Intent.ACTION_VIEW
        }

        val bubbleIntent = PendingIntent.getActivity(
            context,
            100,
            targetIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val contentIntent = PendingIntent.getActivity(
            context,
            101,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val person = Person.Builder()
            .setName("Vikunja")
            .setImportant(true)
            .setIcon(icon)
            .build()

        // ショートカットは最初の1回だけ push する。
        // 何度も呼ぶと端末の上限を超えてクラッシュするため。
        if (!shortcutPushed) {
            val shortcut = ShortcutInfoCompat.Builder(context, SHORTCUT_ID)
                .setShortLabel("Vikunja")
                .setLongLived(true)
                .setIcon(icon)
                .setIntent(targetIntent)
                .setPerson(person)
                .setCategories(setOf(CATEGORY_TEXT_SHARE_TARGET))
                .build()
            ShortcutManagerCompat.pushDynamicShortcut(context, shortcut)
            shortcutPushed = true
        }

        val style = NotificationCompat.MessagingStyle(person)
            .setConversationTitle("Vikunja")
            .addMessage(message, System.currentTimeMillis(), person)

        val bubbleMetadata = NotificationCompat.BubbleMetadata.Builder(
            bubbleIntent,
            icon
        )
            .setDesiredHeight(600)
            .setAutoExpandBubble(true)
            .setSuppressNotification(true)
            .build()

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Vikunja")
            .setContentText(message)
            .setContentIntent(contentIntent)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setStyle(style)
            .setShortcutId(SHORTCUT_ID)
            .addPerson(person)
            .setBubbleMetadata(bubbleMetadata)
            .setOnlyAlertOnce(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        NotificationManagerCompat.from(context).notify(NOTIF_ID, notification)
    }
}
