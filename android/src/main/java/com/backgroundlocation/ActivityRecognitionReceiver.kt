package com.backgroundlocation

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.location.ActivityRecognitionResult
import com.google.android.gms.location.DetectedActivity

class ActivityRecognitionReceiver : BroadcastReceiver() {

    private val TAG = "ActivityRecognitionReceiver"

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "onReceive called") // Log to check if the receiver is triggered
        val result = ActivityRecognitionResult.extractResult(intent)
        
        // Check if the result is not null
        if (result != null) {
            val activities = result.probableActivities
            for (activity in activities) {
                val activityType = getActivityString(activity.type)
                val confidence = activity.confidence
                
                // Log the confidence of the detected activity
                Log.d(TAG, "Detected activity: $activityType with confidence: $confidence")

                // You can set a threshold for confidence before broadcasting
                if (confidence > 50) {  // Example threshold
                    val activityIntent = Intent("com.backgroundlocation.ACTIVITY_CHANGE")
                    activityIntent.putExtra("activity", activityType)
                    activityIntent.putExtra("confidence", confidence)
                    context.sendBroadcast(activityIntent)
                }
            }
        } else {
            Log.w(TAG, "No activity recognition result found.")
        }
    }

    private fun getActivityString(activityType: Int): String {
        return when(activityType) {
            DetectedActivity.IN_VEHICLE -> "IN_VEHICLE"
            DetectedActivity.ON_BICYCLE -> "ON_BICYCLE"
            DetectedActivity.ON_FOOT -> "ON_FOOT"
            DetectedActivity.RUNNING -> "RUNNING"
            DetectedActivity.WALKING -> "WALKING"
            DetectedActivity.STILL -> "STILL"
            DetectedActivity.TILTING -> "TILTING"
            DetectedActivity.UNKNOWN -> "UNKNOWN"
            else -> "UNKNOWN"
        }
    }
}
