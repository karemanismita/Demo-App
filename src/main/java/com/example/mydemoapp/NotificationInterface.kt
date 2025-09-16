package com.example.mydemoapp

interface NotificationInterface {
    fun sendNotification(
        message: String,
        speed: Float,
        onSuccess: () -> Unit,
        onFailure: (Throwable) -> Unit,
    )
}
