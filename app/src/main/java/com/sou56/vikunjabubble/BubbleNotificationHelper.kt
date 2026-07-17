package com.sou56.vikunjabubble

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.Person
import androidx.core.content.ContextCompat
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat

object BubbleNotificationHelper {

    private const val CHANNEL_ID   = "vikunja_bubble_channel"
    private const val NOTIF_ID     = 1001
    private const val SHORTCUT_ID  = "vikunja_bubble_conversation"
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

        val appContext = context.applicationContext
        val icon = IconCompat.createWithResource(appContext, R.mipmap.ic_launcher)

        // BubbleActivity を起動する Intent
        // FLAG_ACTIVITY_NEW_TASK は applicationContext から起動する場合に必須
        val targetIntent = Intent(appContext, BubbleActivity::class.java).apply {
            action = Intent.ACTION_VIEW
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        val bubbleIntent = PendingIntent.getActivity(
            appContext,
            100,
            targetIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val contentIntent = PendingIntent.getActivity(
            appContext,
            101,
            Intent(appContext, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val person = Person.Builder()
            .setName("Vikunja")
            .setImportant(true)
            .setIcon(icon)
            .build()

        // ---- ショートカットを登録 ----
        // Android のバブル API は「登録済みの動的ショートカット」に紐付いた通知のみ
        // バブルとして表示する。ショートカット登録直後に通知を発行すると
        // システムがショートカットを認識する前になってしまいバブルが無視される。
        // そのため、初回は 300ms 待ってから通知を発行する。
        val doNotify = Runnable {
            postBubbleNotification(appContext, person, icon, bubbleIntent, contentIntent, message)
        }

        if (!shortcutPushed) {
            val shortcut = ShortcutInfoCompat.Builder(appContext, SHORTCUT_ID)
                .setShortLabel("Vikunja")
                .setLongLived(true)
                .setIcon(icon)
                .setIntent(targetIntent)
                .setPerson(person)
                .setCategories(setOf(CATEGORY_TEXT_SHARE_TARGET))
                .build()
            ShortcutManagerCompat.pushDynamicShortcut(appContext, shortcut)
            shortcutPushed = true
            // 300ms 待ってショートカットをシステムに反映させてから通知を発行
            Handler(Looper.getMainLooper()).postDelayed(doNotify, 300L)
        } else {
            doNotify.run()
        }
    }

    private fun postBubbleNotification(
        context: Context,
        person: Person,
        icon: IconCompat,
        bubbleIntent: PendingIntent,
        contentIntent: PendingIntent,
        message: String
    ) {
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
