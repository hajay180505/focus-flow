package com.streakfreak.focusflow


import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d("BootReceiver", "Boot complete detected, starting notification service.")
            val serviceIntent = Intent(context, AppNotificationService::class.java)
            context.startService(serviceIntent)
        }
    }
}