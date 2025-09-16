package com.example.mydemoapp

import android.util.Log

class NotificationManager(
    private val firebaseInterface: NotificationInterface,
    private val awsInterface: NotificationInterface,
) {
    fun sendNotification(message: String, speed: Float) {
        firebaseInterface.sendNotification(
            message,
            speed,
            onSuccess = { Log.d("NotificationManager","Notification sent via primary channel!") },
            onFailure = { error ->
                Log.d("NotificationManager","Primary channel failed: ${error.message}. Falling back...")
                awsInterface.sendNotification(
                    message,
                    speed,
                    onSuccess = { Log.d("NotificationManager","Notification sent via fallback channel!") },
                    onFailure = { fallbackError ->
                        Log.d("NotificationManager","Fallback channel also failed: ${fallbackError.message}")
                    }
                )
            }
        )
    }
}
