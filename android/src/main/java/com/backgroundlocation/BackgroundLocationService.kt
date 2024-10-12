package com.backgroundlocation

import android.app.*
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.*
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager

class BackgroundLocationService : Service() {

    private val TAG = "BackgroundLocationService"
    private val LOCATION_UPDATE_INTERVAL = 2 * 1000 // 2 seconds
    private val NOTIFICATION_ID = 12345
    private val CHANNEL_ID = "BackgroundLocationServiceChannel"
    private lateinit var mLocationManager: LocationManager
    private lateinit var wakeLock: PowerManager.WakeLock
    private var lastLocation: Location? = null

    override fun onCreate() {
        super.onCreate()

        mLocationManager = getSystemService(LOCATION_SERVICE) as LocationManager

        val powerManager = getSystemService(POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "BackgroundLocationService::WakeLock")
        wakeLock.acquire(60 * 1000L)
    if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
        ContextCompat.checkSelfPermission(this, android.Manifest.permission.FOREGROUND_SERVICE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            
        createNotification()
        startLocationUpdates()
    } else {
        Log.e(TAG, "Required permissions not granted")
    }
    }

    private fun createNotification(): Notification {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID, "Foreground", NotificationManager.IMPORTANCE_HIGH
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }

        val notificationIntent = packageManager.getLaunchIntentForPackage(packageName)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("App is running")
            .setContentText("Tap for more information or to stop the app.")
            .setContentIntent(pendingIntent)
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        startForeground(1, notification)
        return notification
    }

    private fun startLocationUpdates() {
        Log.d(TAG, "Location updates started")
        try {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mLocationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    LOCATION_UPDATE_INTERVAL.toLong(),
                    0f,
                    mLocationListener
                )
            } else {
                Log.e(TAG, "Location permission not granted")
            }
        } catch (e: SecurityException) {
            Log.e(TAG, "Error: ${e.message}")
        }
    }

    private val mLocationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            Log.d(TAG, "GPS Location updated: ${location.latitude}, ${location.longitude}")
            // Process location update
        }

        override fun onProviderDisabled(provider: String) {
            Log.d(TAG, "Location provider disabled: $provider")
        }

        override fun onProviderEnabled(provider: String) {
            Log.d(TAG, "Location provider enabled: $provider")
        }

        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
            // Handle status changes if needed
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        mLocationManager.removeUpdates(mLocationListener)
        if (wakeLock.isHeld) {
            wakeLock.release()
        }
    }
}
