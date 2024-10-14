package com.backgroundlocation

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.backgroundlocation.ConfigurationService

class BootReceiver : BroadcastReceiver() {

    private val TAG = "BootReceiver"

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d(TAG, "Device rebooted - Checking startOnBoot configuration")

            // Retrieve the configuration to check if startOnBoot is enabled
            val configService = ConfigurationService(context)
            val config = configService.getConfig()
            val startOnBoot = config.toMap()["startOnBoot"] as? Boolean ?: false

            if (startOnBoot) {
                Log.d(TAG, "startOnBoot is enabled - Starting BackgroundLocationService")

                // Start the BackgroundLocationService
                val serviceIntent = Intent(context, BackgroundLocationService::class.java)
                context.startForegroundService(serviceIntent)
            } else {
                Log.d(TAG, "startOnBoot is disabled - Not starting service")
            }
        }
    }
}
