package com.streakfreak.focusflow

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
//import androidx.preference.isNotEmpty
//import androidx.privacysandbox.tools.core.generator.build
import com.streakfreak.focusflow.MainActivity // Replace with your main activity
import kotlinx.coroutines.*
import java.util.concurrent.TimeUnit

class AppNotificationService : Service() {

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.Default + job)
    private lateinit var dbHelper: Database // Replace with your DB helper
    private val CHANNEL_ID = "app_notifications"
    private val NOTIFICATION_ID = 1

    override fun onCreate() {
        super.onCreate()
        dbHelper = Database(this) // Initialize your database helper
        createNotificationChannel()
        scheduleNotifications()
        Log.d("NotificationService", "Service created")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("NotificationService", "Service started")
        return START_STICKY // Keep the service running even if the app is closed
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null // Not a bound service
    }

    private fun scheduleNotifications() {
        scope.launch {
            while (isActive) { // Keep running as long as the coroutine is active.
                showNotification()
                delay(TimeUnit.SECONDS.toMillis(30))  // Wait for 3 hours
            }
        }
    }

    private fun showNotification() {
        scope.launch(Dispatchers.IO) { // Database operations on IO thread
            val notificationContent = getNotificationContentFromDb()
            Log.d("Nottu",notificationContent)
            if(notificationContent==""){
                println("No notif")
                return@launch
            }
            withContext(Dispatchers.Main) { // Update UI (notification) on Main thread
                val intent = Intent(this@AppNotificationService, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                val pendingIntent: PendingIntent =
                    PendingIntent.getActivity(this@AppNotificationService, 0, intent, PendingIntent.FLAG_IMMUTABLE)

                val builder = NotificationCompat.Builder(this@AppNotificationService, CHANNEL_ID)
                    .setSmallIcon(R.drawable.logo) // Replace with your icon
                    .setContentTitle("App Reminder")
                    .setContentText(notificationContent)
                    .setStyle(NotificationCompat.BigTextStyle().bigText(notificationContent))  // Expandable notification
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true) // Dismiss when tapped
                    .setColor(ContextCompat.getColor(this@AppNotificationService, R.color.alertColor)) // Use a color resource
                // Add actions if needed (e.g., snooze, dismiss)

                with(NotificationManagerCompat.from(this@AppNotificationService)) {
                    notify(NOTIFICATION_ID, builder.build())
                }
                Log.d("NotificationService", "Notification shown")
            }
        }
    }
    private fun getNotificationContentFromDb(): String {
        return try {
            val data = dbHelper.getStreaksForNotification() // Call your database method
            if (data.isNotEmpty()) {
                Log.d("Shkibid", data.joinToString(separator = "\n") { "${it.userName} in ${it.app} has not continued streak ${it.streak}"  })

                val res = data.joinToString(separator = "\n") { if (it.streak.toInt() == 0) "${it.userName} in ${it.app} has not continued streak ${it.streak}" else "" }
                Log.d("Myir",res)
                res
            } else {
                ""
            }
        } catch (e: Exception) {
            Log.e("NotificationService", "Error fetching notification data: ${e.message}", e)
            "Error fetching notification data."
        }
    }

    private fun createNotificationChannel() {
        val name = "App Notifications"
        val descriptionText = "Notifications for app usage streaks"
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
            description = descriptionText
        }
        val notificationManager: NotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()  // Cancel the coroutine job when the service is destroyed
        dbHelper.close() // Close database.
        Log.d("NotificationService", "Service destroyed")
    }
}