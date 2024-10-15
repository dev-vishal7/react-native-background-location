package com.backgroundlocation

import android.content.Intent
import android.content.IntentFilter
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.Promise
import com.backgroundlocation.BackgroundLocationService
import com.backgroundlocation.ConfigurationService
import com.facebook.react.bridge.ReadableMap
import com.facebook.react.modules.core.DeviceEventManagerModule
import android.util.Log
import android.content.BroadcastReceiver
import android.content.Context
import android.os.Bundle
import com.facebook.react.bridge.Arguments
import android.location.Location
import com.facebook.react.bridge.WritableMap
import com.facebook.react.modules.core.DeviceEventManagerModule.RCTDeviceEventEmitter


class BackgroundLocationModule(reactContext: ReactApplicationContext) :
  ReactContextBaseJavaModule(reactContext) {
    private val configService: ConfigurationService = ConfigurationService(reactContext)

    private val broadcastReceiver = object : BroadcastReceiver() {
      override fun onReceive(context: Context, intent: Intent) {
          Log.d(TAG, "Broadcast received in BackgroundLocationModule: ${intent.action}")
          val eventName = when (intent.action) {
              "com.backgroundlocation.ACTIVITY_CHANGE" -> "onActivityChange"
              "com.backgroundlocation.LOCATION_CHANGE" -> "onLocationChange"
              "com.backgroundlocation.PROVIDER_CHANGE" -> "onProviderChange"
              "com.backgroundlocation.MOTION_CHANGE" -> "onMotionChange"
              else -> return
          }
  
          if (eventName == "onLocationChange") {
              // Retrieve the Location object from the intent
              val location: Location? = intent.getParcelableExtra("location")
              location?.let {
                  Log.d(TAG, "Received location: dd ${it}, Long ${it.longitude}")
                  val eventData = Arguments.createMap().apply {
                      putDouble("latitude", it.latitude)
                      putDouble("longitude", it.longitude)
                      putDouble("altitude", it.altitude)
                      putDouble("accuracy", it.accuracy.toDouble())
                      putDouble("speed", it.speed.toDouble())
                      putString("provider", it.provider)
                      putDouble("bearing", it.bearing.toDouble())
                  }
                  sendEvent(eventName, eventData)
              }
          } else {
              val eventData = Arguments.createMap()
              intent.extras?.let { extras ->
                  for (key in extras.keySet()) {
                      eventData.putString(key, extras.getString(key))
                  }
              }
              sendEvent(eventName, eventData)
          }
      }
  }
  
  


    override fun getName(): String {
        return NAME
    }

    @ReactMethod
    fun startListeningLocationChangeEvent() {
        val intentFilter = IntentFilter().apply {
            addAction("com.backgroundlocation.LOCATION_CHANGE")
        }
        reactApplicationContext.registerReceiver(broadcastReceiver, intentFilter)
    }

    @ReactMethod
    fun startListeningProvideChangeEvent() {
        val intentFilter = IntentFilter().apply {
            addAction("com.backgroundlocation.PROVIDER_CHANGE")
        }
        reactApplicationContext.registerReceiver(broadcastReceiver, intentFilter)
    }
    
    @ReactMethod
    fun startListeningActivityChangeEvent() {
        val intentFilter = IntentFilter().apply {
            addAction("com.backgroundlocation.ACTIVITY_CHANGE")
        }
        reactApplicationContext.registerReceiver(broadcastReceiver, intentFilter)
    }
    
    @ReactMethod
    fun startListeningMotionChangeEvent() {
        val intentFilter = IntentFilter().apply {
            addAction("com.backgroundlocation.MOTION_CHANGE")
        }
        reactApplicationContext.registerReceiver(broadcastReceiver, intentFilter)
    }
    
    // Unregister the broadcast receiver
    @ReactMethod
    fun stopListeningForEvents() {
        reactApplicationContext.unregisterReceiver(broadcastReceiver)
    }

    @ReactMethod
    fun startBackgroundService(promise: Promise) {
        val intent = Intent(reactApplicationContext, BackgroundLocationService::class.java)
        reactApplicationContext.startService(intent)
        promise.resolve("Background location service started")
    }

    @ReactMethod
    fun stopBackgroundService(promise: Promise) {
        val intent = Intent(reactApplicationContext, BackgroundLocationService::class.java)
        reactApplicationContext.stopService(intent)
        promise.resolve("Background location service stopped")
    }

    @ReactMethod
    fun saveConfiguration(config: ReadableMap, promise: Promise) {
        try {
            val configMap = config.toHashMap() as Map<String, Any>
            configService.saveConfig(configMap)
        } catch (e: Exception) {
            promise.reject("CONFIG_ERROR", "Failed to save configuration", e)
        }
    }

    @ReactMethod
    fun getConfiguration(promise: Promise) {
        try {
            val config = configService.getConfig()
            promise.resolve(config)
        } catch (e: Exception) {
            promise.reject("CONFIG_ERROR", "Failed to retrieve configuration", e)
        }
    }

    // Method to send events to JavaScript
    fun sendEvent(eventName: String, params: WritableMap?) {
      val eventEmitter = reactApplicationContext.getJSModule(RCTDeviceEventEmitter::class.java)
      eventEmitter.emit(eventName, params)
  }
  
    companion object {
        const val NAME = "BackgroundLocation"
        private const val TAG = "BackgroundLocationModule"
    }
}
