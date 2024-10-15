package com.backgroundlocation

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.*
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.ActivityRecognitionClient
import com.google.android.gms.location.ActivityRecognitionResult
import com.google.android.gms.location.DetectedActivity
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.OnFailureListener
import com.facebook.react.bridge.ReadableMap
import android.Manifest
import com.google.android.gms.location.ActivityRecognition


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
    private var notificationTitle: String = "App is running"
    private var notificationDescription: String = "Tracking your location"
    private var stopTimeout: Int = 5
    private var stopOnTerminate: Boolean = false
    private var startOnBoot: Boolean = false
    private var stopHandler: Handler? = null
    private var stopRunnable: Runnable? = null
    private lateinit var activityRecognitionClient: ActivityRecognitionClient

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
        activityRecognitionClient = ActivityRecognition.getClient(this)

        desiredAccuracy = configMap["desiredAccuracy"] as? String ?: "LOW"
        notificationTitle = configMap["notificationTitle"] as? String ?: ""
        notificationDescription = configMap["notificationDescription"] as? String ?: ""
        stopTimeout = configMap["stopTimeout"] as? Int ?: 5
        stopOnTerminate = configMap["stopOnTerminate"] as? Boolean ?: false
        startOnBoot = configMap["startOnBoot"] as? Boolean ?: false

        val powerManager = getSystemService(POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "BackgroundLocationService::WakeLock")
        wakeLock.acquire(60 * 1000L)

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, android.Manifest.permission.FOREGROUND_SERVICE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            createNotification()
            startLocationUpdates()
            requestActivityUpdates()

            initializeStopTimeout(stopTimeout)
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
                val provider = when (desiredAccuracy) {
                    "HIGH" -> LocationManager.GPS_PROVIDER
                    "MEDIUM" -> LocationManager.PASSIVE_PROVIDER
                    else -> LocationManager.NETWORK_PROVIDER
                }

                mLocationManager.requestLocationUpdates(
                    provider,
                    LOCATION_UPDATE_INTERVAL.toLong(),
                    distanceFilter,
                    mLocationListener
                )
            } else {
                Log.e(TAG, "Location permission not granted")
            }
        } catch (e: SecurityException) {
            Log.e(TAG, "Error: ${e.message}")
        }
    }

    private fun requestActivityUpdates() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_GRANTED) {
            val intent = Intent(this, ActivityRecognitionReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    
            activityRecognitionClient.requestActivityUpdates(
                500, 
                pendingIntent
            ).addOnSuccessListener {
                Log.d(TAG, "Successfully requested activity updates")
            }.addOnFailureListener { e ->
                Log.e(TAG, "Failed to request activity updates: ${e.message}")
            }
        } else {
            Log.e(TAG, "Activity recognition permission not granted")
        }
    }
    

    private val mLocationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            Log.d(TAG, "GPS Location updated: ${location.latitude}, ${location.longitude}")
            resetStopTimeout()
            val intent = Intent("com.backgroundlocation.LOCATION_CHANGE")
            intent.putExtra("location", location)  // Use putParcelable for Location object
            applicationContext.sendBroadcast(intent)            
        }

        override fun onProviderEnabled(provider: String) {
            sendProviderChangeBroadcast("enabled", provider)
        }
        
        override fun onProviderDisabled(provider: String) {
            sendProviderChangeBroadcast("disabled", provider)
        }
        
        private fun sendProviderChangeBroadcast(status: String, provider: String) {
            val intent = Intent("com.backgroundlocation.PROVIDER_CHANGE")
            intent.putExtra("status", status)
            applicationContext.sendBroadcast(intent)
        }
        

        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
    }

    private fun initializeStopTimeout(timeout: Int) {
        stopHandler = Handler(Looper.getMainLooper())
        stopRunnable = Runnable {
            stopSelf() // Stop the service after the timeout
            Log.d(TAG, "Service stopped due to stopTimeout")
        }
        stopHandler?.postDelayed(stopRunnable!!, timeout * 60 * 1000L) // Timeout in minutes
    }

    private fun resetStopTimeout() {
        stopHandler?.removeCallbacks(stopRunnable!!)
        stopHandler?.postDelayed(stopRunnable!!, stopTimeout * 60 * 1000L)
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        if (stopOnTerminate) {
            stopSelf()
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
        stopHandler?.removeCallbacks(stopRunnable!!)
        if (this::wakeLock.isInitialized && wakeLock.isHeld) {
            wakeLock.release()
        }
    }
}
