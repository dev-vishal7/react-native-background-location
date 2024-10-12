package com.backgroundlocation

import android.content.Intent
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.Promise
import com.backgroundlocation.BackgroundLocationService


class BackgroundLocationModule(reactContext: ReactApplicationContext) :
  ReactContextBaseJavaModule(reactContext) {

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


  companion object {
    const val NAME = "BackgroundLocation"
  }
}
