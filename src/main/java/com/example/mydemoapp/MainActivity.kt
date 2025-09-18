package com.example.mydemoapp

import android.car.Car
import android.car.VehiclePropertyIds
import android.car.hardware.CarPropertyValue
import android.car.hardware.property.CarPropertyManager
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.amazonaws.auth.CognitoCachingCredentialsProvider
import com.amazonaws.regions.Regions
import com.amazonaws.services.sns.AmazonSNSClient
import com.amazonaws.services.sns.model.PublishRequest
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {

    private lateinit var car: Car
    private lateinit var carPropertyManager: CarPropertyManager
    private val mTAG = "MainActivity"
    private var speedLimit: Float = 0.0f
    private val customerId = "CUST123"
    private lateinit var snsClient: AmazonSNSClient
    private lateinit var dbHelper: SpeedDatabaseHelper

    val firebaseSender = FirebaseNotification()
    val awsSender = AWSNotification()
    val notificationManager = NotificationManager(firebaseSender, awsSender)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        car = Car.createCar(this)
        carPropertyManager = car.getCarManager(Car.PROPERTY_SERVICE) as CarPropertyManager

        // Initialize AWS SNS client
        initializeAWS()
        
        // Initialize DB
        dbHelper = FCMDatabaseHelper(this)

        fetchSpeedLimit()
        startSpeedMonitoring()
        registerFirebaseNotifications()
    }

    private fun initializeAWS() {
        val credentialsProvider = CognitoCachingCredentialsProvider(
            applicationContext,
            "MY_COGNITO_ID", // Replace with your actual Cognito Identity Pool ID
            Regions.DEFAULT_REGION
        )
        snsClient = AmazonSNSClient(credentialsProvider)
    }
    
    // Method to get the speed limit from server
    private fun fetchSpeedLimit() {
        // TODO : Actual value is to be taken from Firebase
        // for now assuming the hardcoded value
        speedLimit = 100f
        Log.d(mTAG, "Speed limit : $speedLimit")
    }
    
    // Method to monitor car speed
    private fun startSpeedMonitoring() {
        val carSpeedRunnable = Runnable {
            carPropertyManager.registerCallback(
                carPropertyEventCallback,
                VehiclePropertyIds.PERF_VEHICLE_SPEED,
                CarPropertyManager.SENSOR_RATE_ONCHANGE
            )
        }

        val handler = Handler(mainLooper)
        handler.post(carSpeedRunnable)
    }
    
    // callback method
    private val carPropertyEventCallback = object : CarPropertyManager.CarPropertyEventCallback {
        override fun onChangeEvent(value: CarPropertyValue<*>) {
            val currentSpeed = value.value as? Float ?: 0.0f
            val message = "Overspeed detected! Current: $currentSpeed km/h (Limit: $speedLimit)"

            // Check if speed exceeds the speed limit
            if (currentSpeed > speedLimit) {
                Log.w(mTAG, "Speed limit exceeded! Sending alert...")

                // Save locally in SQLite
                dbHelper.insertEvent(customerId, speedLimit, currentSpeed)
    
                // Push to Firebase Realtime Database
                val dbRef = FirebaseDatabase.getInstance().reference
                val eventId = dbRef.push().key ?: "event_${System.currentTimeMillis()}"
                dbRef.child("alerts").child(eventId).setValue(
                    mapOf(
                        "customerId" to customerId,
                        "limit" to speedLimit,
                        "current" to currentSpeed,
                        "message" to message
                    )
                )
                notificationManager.sendNotification(
                    "Speed limit exceeded! Sending alert...",
                    currentSpeed
                )
            }
        }

        override fun onErrorEvent(propertyId: Int, zone: Int) {
            Log.e(mTAG, "Error in getting property: $propertyId")
        }
    }

    // Register Firebase for notifications
    private fun registerFirebaseNotifications() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(mTAG, "Exception in Fetching task", task.exception)
                return@addOnCompleteListener
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        carPropertyManager.unregisterCallback(carPropertyEventCallback)
        car.disconnect()
    }
}
