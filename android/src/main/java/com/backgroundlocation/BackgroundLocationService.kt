package com.backgroundlocation

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.location.Criteria
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.*
import android.provider.Settings
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.gson.Gson
import okhttp3.*
import java.io.IOException
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.ZonedDateTime
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import android.Manifest
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
import androidx.core.app.ServiceCompat
import android.os.Build
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent


class BackgroundLocationService : Service() {

    private val TAG = "BackgroundLocationService"
    private val LOCATION_UPDATE_INTERVAL = 2 * 1000 
    private val NOTIFICATION_ID = 12345
    private val CHANNEL_ID = "BackgroundLocationServiceChannel"
    private lateinit var mLocationManager: LocationManager
    private lateinit var wakeLock: PowerManager.WakeLock
    private var lastLocation: Location? = null
    private val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS")

    override fun onCreate() {
        super.onCreate()

        mLocationManager = getSystemService(LOCATION_SERVICE) as LocationManager

        val powerManager = getSystemService(POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "BackgroundLocationService::WakeLock")
        wakeLock.acquire(60 * 1000L)
        createNotification()
    }

    private fun createNotification(): Notification {

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,"Foreground",
                NotificationManager.IMPORTANCE_HIGH
            )
            val manager = getSystemService(
                NotificationManager::class.java
            )
            manager.createNotificationChannel(serviceChannel)
        }
        val notificationIntent = packageManager.getLaunchIntentForPackage(packageName)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent,PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
        .setContentTitle("App is running")
        .setContentText("Tap for more information or to stop the app.")
        .setContentIntent(pendingIntent)
        .setSmallIcon(android.R.drawable.ic_menu_mylocation)
        .setPriority(NotificationCompat.PRIORITY_LOW)
        .build()

        startForeground(1,notification)
        return notification
    }
    

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        if (wakeLock.isHeld) {
            wakeLock.release()
        }    
    }
}
