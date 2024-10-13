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
import com.facebook.react.bridge.ReadableMap 
import com.backgroundlocation.ConfigurationService

class BackgroundLocationService : Service() {

    private val TAG = "BackgroundLocationService"
    private val LOCATION_UPDATE_INTERVAL = 2 * 1000 // 2 seconds
    private val NOTIFICATION_ID = 12345
    private val CHANNEL_ID = "BackgroundLocationServiceChannel"
    private lateinit var mLocationManager: LocationManager
    private lateinit var wakeLock: PowerManager.WakeLock
    private lateinit var configService: ConfigurationService
    private var distanceFilter: Float = 50f 
    private var desiredAccuracy: String = "LOW"
    private var notificationTitle : String = "App is running" 
    private var notificationDescription : String = "Tracking your location" 

    override fun onCreate() {
        super.onCreate()

        mLocationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        configService = ConfigurationService(this) 
        val config = configService.getConfig()
        
        val configMap = config.toMap()
        Log.d(TAG, "Config Map: $configMap")

        distanceFilter = when (val value = configMap["distanceFilter"]) {
            is Int -> value.toFloat() // Convert Int to Float
            is Double -> value.toFloat() // Convert Double to Float
            else -> 50f // Default value
        }

        desiredAccuracy = configMap["desiredAccuracy"] as? String ?: "LOW"
        notificationTitle = configMap["notificationTitle"] as? String ?: ""
        notificationDescription = configMap["notificationDescription"] as? String ?: ""
        
        val powerManager = getSystemService(POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "BackgroundLocationService::WakeLock")
        wakeLock.acquire(60 * 1000L)
        
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
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
            .setContentTitle(notificationTitle)
            .setContentText(notificationDescription)
            .setContentIntent(pendingIntent)
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        startForeground(NOTIFICATION_ID, notification)
        return notification
    }

    private fun startLocationUpdates() {
        try {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                // Determine the provider based on desired accuracy
                val provider = when (desiredAccuracy) {
                    "HIGH" -> LocationManager.GPS_PROVIDER // High accuracy, uses GPS
                    "MEDIUM" -> LocationManager.PASSIVE_PROVIDER // Medium accuracy, passive provider to balance
                    else -> LocationManager.NETWORK_PROVIDER // Low accuracy, uses network (Wi-Fi, cell towers)
                }
                
                mLocationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    LOCATION_UPDATE_INTERVAL.toLong(),
                    distanceFilter, // The minimum distance (in meters) for location updates
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
        if (this::wakeLock.isInitialized && wakeLock.isHeld) {
            wakeLock.release()
        }
    }
}
