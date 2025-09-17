package com.example.mydemoapp

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.app.NotificationChannel
import android.app.NotificationManager
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import android.util.Log
import androidx.core.app.NotificationCompat

class CustomFBMService : FirebaseMessagingService() {
    private val mTAG = "MyFBMService"

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d(mTAG, "From: ${remoteMessage.from}")

        remoteMessage.notification?.let {
            Log.d(mTAG, "Message : ${it.body}")
            showNotification(it.title, it.body)
        }
    }

    private fun showNotification(title: String?, message: String?) {
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "fbm_notification_channel"

        val channel = NotificationChannel(
            channelId,
            "FBM Notifications",
            NotificationManager.IMPORTANCE_HIGH
        )
        notificationManager.createNotificationChannel(channel)

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        notificationManager.notify(0, notificationBuilder.build())
    }

    override fun onNewToken(token: String) {
        Log.d(mTAG, "new token: $token")
    }
}
