package com.example.mydemoapp

import android.util.Log
import com.amazonaws.services.sns.AmazonSNSClient
import com.amazonaws.services.sns.model.PublishRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AWSNotification : NotificationInterface {
    private val mTAG = "AWSNotification"
    private val speedLimit = 100f
    private val topicArn = "SNS_TOPIC_ARN"
    private lateinit var snsClient: AmazonSNSClient


    override fun sendNotification(
        message: String,
        speed: Float,
        onSuccess: () -> Unit,
        onFailure: (Throwable) -> Unit,
    ) {
        try {
            Log.d(mTAG,"Sending via AWS: $message")
            onSuccess()
            checkSpeedAndSendAlert(speed)
        } catch (e: Exception) {
            onFailure(e)
        }
    }

    private fun checkSpeedAndSendAlert(currentSpeed: Float) {
        if (currentSpeed > speedLimit) {
            val message =
                "Alert: Car speed exceeded the limit! Current Speed: $currentSpeed km/h, Speed Limit: $speedLimit km/h"

            // Launch in a Coroutine to send alert asynchronously
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val publishRequest = PublishRequest()
                        .withMessage(message)
                        .withTopicArn(topicArn)

                    snsClient.publish(publishRequest)
                    Log.d(mTAG, "Speed alert sent successfully.")
                } catch (e: Exception) {
                    Log.e(mTAG, "Failed to send speed alert: ${e.message}")
                }
            }
        }
    }
}