package com.example.mydemoapp

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessaging

class FCMDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {

    companion object {
        private const val DB_NAME = "fcm_helper.db"
        private const val DB_VERSION = 1
        private const val TABLE_NAME = "speed_events"

        private const val COL_ID = "id"
        private const val COL_CUSTOMER = "customer_id"
        private const val COL_LIMIT = "speed_limit"
        private const val COL_CURRENT = "current_speed"
        private const val COL_TIMESTAMP = "timestamp"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTable = """
            CREATE TABLE $TABLE_NAME (
                $COL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_CUSTOMER TEXT,
                $COL_LIMIT REAL,
                $COL_CURRENT REAL,
                $COL_TIMESTAMP DATETIME DEFAULT CURRENT_TIMESTAMP
            )
        """
        db.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    fun insertEvent(customerId: String, limit: Float, current: Float) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_CUSTOMER, customerId)
            put(COL_LIMIT, limit)
            put(COL_CURRENT, current)
        }
        db.insert(TABLE_NAME, null, values)
        db.close()

        // Trigger FCM after saving event
        triggerFCM(customerId, limit, current)
    }

    private fun triggerFCM(customerId: String, limit: Float, current: Float) {
        val message = "Overspeed detected! Current: $current km/h (Limit: $limit)"

        // Save message in Firebase Realtime DB
        val dbRef = FirebaseDatabase.getInstance().reference
        val eventId = dbRef.push().key ?: "event_${System.currentTimeMillis()}"
        dbRef.child("alerts").child(eventId).setValue(
            mapOf(
                "customerId" to customerId,
                "limit" to limit,
                "current" to current,
                "message" to message
            )
        )

        // Notify via FCM Topic
        FirebaseMessaging.getInstance().subscribeToTopic("speed_alerts")
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("SpeedDB", "Subscribed to speed_alerts topic")
                }
            }
    }
}
