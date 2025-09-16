package com.example.mydemoapp

import android.util.Log
import android.widget.Toast
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.RemoteMessage

class FirebaseNotification : NotificationInterface {
    private val mTAG = "FirebaseNotification"

    override fun sendNotification(
        message: String,
        speed: Float,
        onSuccess: () -> Unit,
        onFailure: (Throwable) -> Unit,
    ) {
        try {
            Log.d(mTAG,"Sending via Firebase: $message")
            onSuccess()
            notifyFirebaseServer(speed)
            sendSpeedExceededAlert(speed)
        } catch (e: Exception) {
            onFailure(e)
        }
    }

    // Method to notify server about the speed limit exceed
    private fun notifyFirebaseServer(currentSpeed: Float) {
        FirebaseMessaging.getInstance().subscribeToTopic("speed_alerts")
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(
                        mTAG,
                        "Car speed limit exceeded : $currentSpeed"
                    )
                } else {
                    Log.e(mTAG, "Failed to notify")
                }
            }
    }

    // Method to notify HU about exceeding speed limit
    private fun sendSpeedExceededAlert(currentSpeed: Float) {
        val messageTitle = "Speed Alert!"
        val messageBody = "Car exceeded speed limit! Current speed: $currentSpeed km/h"
        FirebaseMessaging.getInstance().send(
            RemoteMessage.Builder("car_speed_alert")
                .setMessageId("1")
                .addData("title", messageTitle)
                .addData("body", messageBody)
                .build()
        )
    }
}