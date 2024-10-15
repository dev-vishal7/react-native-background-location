package com.backgroundlocation

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.location.ActivityRecognitionResult
import com.google.android.gms.location.DetectedActivity

class ActivityRecognitionReceiver : BroadcastReceiver() {

    private val TAG = "ActivityRecognitionReceiver"
    private var isMoving = false

    override fun onReceive(context: Context, intent: Intent) {
        val result = ActivityRecognitionResult.extractResult(intent)
        
        if (result != null) {
            val activities = result.probableActivities
            for (activity in activities) {
                val activityType = getActivityString(activity.type)
                val confidence = activity.confidence
                    
                // Ensure a threshold confidence
                if (confidence > 50) {
                    val isStationary = activity.type == DetectedActivity.STILL
                    val isCurrentlyMoving = activity.type == DetectedActivity.WALKING || 
                                            activity.type == DetectedActivity.RUNNING || 
                                            activity.type == DetectedActivity.IN_VEHICLE || 
                                            activity.type == DetectedActivity.ON_BICYCLE
                    sendActivityChangeBroadcast(context, activityType)
                    if (isMoving && isStationary) {
                        isMoving = false
                        Log.d(TAG, "Motion changed: Now stationary")
                        sendMotionChangeBroadcast(context, "stationary")
                    } else if (!isMoving && isCurrentlyMoving) {
                        isMoving = true
                        Log.d(TAG, "Motion changed: Now moving")
                        sendMotionChangeBroadcast(context, "moving")
                    }
                }
            }
        } else {
            Log.w(TAG, "No activity recognition result found.")
        }
    }

    private fun sendMotionChangeBroadcast(context: Context, motionState: String) {
        val motionIntent = Intent("com.backgroundlocation.MOTION_CHANGE")
        motionIntent.putExtra("motionState", motionState)
        context.sendBroadcast(motionIntent)
    }

    private fun sendActivityChangeBroadcast(context: Context, activity: String) {
        val activityIntent = Intent("com.backgroundlocation.ACTIVITY_CHANGE")
        activityIntent.putExtra("activity", activity)
        context.sendBroadcast(activityIntent)
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
