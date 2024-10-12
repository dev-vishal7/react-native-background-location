package com.backgroundlocation

import android.content.Intent
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.Promise
import com.backgroundlocation.BackgroundLocationService
import com.backgroundlocation.ConfigurationService
import com.facebook.react.bridge.ReadableMap
import android.util.Log


class BackgroundLocationModule(reactContext: ReactApplicationContext) :
  ReactContextBaseJavaModule(reactContext) {
    private val configService: ConfigurationService = ConfigurationService(reactContext)

  override fun getName(): String {
    return NAME
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
          promise.resolve("Configuration saved successfully")
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

  companion object {
    const val NAME = "BackgroundLocation"
  }
}
