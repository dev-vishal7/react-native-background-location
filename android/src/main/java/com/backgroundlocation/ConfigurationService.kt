package com.backgroundlocation

import android.content.Context
import android.content.SharedPreferences
import android.util.Log

class ConfigurationService(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("BackgroundLocationPrefs", Context.MODE_PRIVATE)

    // Save a map of configurations into SharedPreferences
    fun saveConfig(config: Map<String, Any>) {
        val editor = prefs.edit()
        config.forEach { (key, value) ->
            when (value) {
                is String -> editor.putString(key, value)
                is Boolean -> editor.putBoolean(key, value)
                is Float -> editor.putFloat(key, value)
                is Int -> editor.putInt(key, value) 
                is Long -> editor.putLong(key, value)
                is Double -> editor.putInt(key, value.toInt()) 
                else -> throw IllegalArgumentException("Unsupported type for preference: $key")
            }
        }
        editor.apply()
    }
    
    // Retrieve the configuration from SharedPreferences
    fun getConfig(): Map<String, Any> {
        val config = mutableMapOf<String, Any>()
    
        config["desiredAccuracy"] = prefs.getString("desiredAccuracy", "LOW") ?: "LOW" // Provide a default if null
        config["distanceFilter"] = prefs.getInt("distanceFilter", 50) 
        config["stopTimeout"] = prefs.getInt("stopTimeout", 5)
        config["stopOnTerminate"] = prefs.getBoolean("stopOnTerminate", true)
        config["startOnBoot"] = prefs.getBoolean("startOnBoot", false)
        config["notificationTitle"] = prefs.getString("notificationTitle", "App is running") ?: ""
        config["notificationDescription"] = prefs.getString("notificationDescription", "Tracking your location") ?: ""
    
        return config 
    }
}
