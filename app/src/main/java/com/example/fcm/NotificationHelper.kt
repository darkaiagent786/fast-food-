package com.example.fcm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R

object NotificationHelper {
    const val CHANNEL_ORDERS = "channel_order_status"
    const val CHANNEL_PROMOS = "channel_promotional"

    // Initialize notification channels for Android O+
    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // 1. Order Status Updates Channel (High Importance, popup/banner)
            val ordersChannel = NotificationChannel(
                CHANNEL_ORDERS,
                "Order Status Updates",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Real-time updates regarding your hot, delicious fast food orders"
                enableLights(true)
                enableVibration(true)
            }

            // 2. Promotional offers Channel (Default Importance)
            val promoChannel = NotificationChannel(
                CHANNEL_PROMOS,
                "Promotions & Discounts",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Exclusive discounts, golden tokens, and free snack giveaways"
                enableLights(true)
                enableVibration(true)
            }

            manager.createNotificationChannel(ordersChannel)
            manager.createNotificationChannel(promoChannel)
        }
    }

    // Displays an actual Android System Notification on the device
    fun showNotification(
        context: Context,
        title: String,
        message: String,
        channelId: String = CHANNEL_ORDERS
    ) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create channels gracefully just in case
        createNotificationChannels(context)

        // Intent when the user taps on the push notification
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            System.currentTimeMillis().toInt(),
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Select golden tint and small icon
        val notificationBuilder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.stat_sys_phone_call) // Safe fallback icon
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)
            .setPriority(
                if (channelId == CHANNEL_ORDERS) NotificationCompat.PRIORITY_HIGH 
                else NotificationCompat.PRIORITY_DEFAULT
            )
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setContentIntent(pendingIntent)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))

        // Trigger notification build
        val notificationId = System.currentTimeMillis().toInt()
        try {
            notificationManager.notify(notificationId, notificationBuilder.build())
        } catch (e: SecurityException) {
            // This catches POST_NOTIFICATIONS permission denials gracefully
            e.printStackTrace()
        }
    }
}
