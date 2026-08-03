package com.example.fcm

import android.util.Log
import com.example.data.DatabaseService
import com.example.data.FcmPayload
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    private val TAG = "FCM_Service"

    // Triggered automatically when Firebase issues/refreshes a cloud registration token
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "Generated registration token: $token")
        
        // Update the in-memory database singleton so we can display it in developer panels
        try {
            DatabaseService.getInstance(applicationContext).updateFcmToken(token)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Called when a real push notification payload arrives from downstream FCM
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d(TAG, "Inbound FCM payload source: ${remoteMessage.from}")

        var title = "Rahman Fast Food Update"
        var body = "Your culinary reward is ready!"
        var rawType = "Promo"

        // 1. Try to extract from the notification visual wrapper
        remoteMessage.notification?.let {
            title = it.title ?: title
            body = it.body ?: body
        }

        // 2. Try to extract custom payload keys if provided in standard data payload
        if (remoteMessage.data.isNotEmpty()) {
            title = remoteMessage.data["title"] ?: remoteMessage.data["notification_title"] ?: title
            body = remoteMessage.data["body"] ?: remoteMessage.data["notification_body"] ?: body
            rawType = remoteMessage.data["type"] ?: remoteMessage.data["category"] ?: "Promo"
        }

        // Standardize channel selection
        val channelId = if (rawType.lowercase().contains("order") || title.lowercase().contains("order")) {
            NotificationHelper.CHANNEL_ORDERS
        } else {
            NotificationHelper.CHANNEL_PROMOS
        }

        val standardizedType = if (channelId == NotificationHelper.CHANNEL_ORDERS) "Order Update" else "Exclusive Promo"

        // Update database log
        val payload = FcmPayload(
            title = title,
            body = body,
            type = standardizedType,
            timestamp = System.currentTimeMillis()
        )

        try {
            DatabaseService.getInstance(applicationContext).receiveFcmPayload(payload)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Trigger real visual head-up notification alert on the phone
        NotificationHelper.showNotification(
            context = applicationContext,
            title = title,
            message = body,
            channelId = channelId
        )
    }
}
