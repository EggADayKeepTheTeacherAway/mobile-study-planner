package org.classapp.studyplanner.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import org.classapp.studyplanner.MainActivity
import org.classapp.studyplanner.R

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra("title") ?: "Study Reminder"
        val message = intent.getStringExtra("message") ?: "You have an assignment due soon!"
        val assignmentId = intent.getIntExtra("assignmentId", -1)

        showNotification(context, title, message, assignmentId)
    }

    private fun showNotification(context: Context, title: String, message: String, id: Int) {
        val channelId = "assignment_reminders"
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Assignment Reminders"
            val descriptionText = "Notifications for upcoming assignment deadlines"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("assignmentId", id)
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            context, id, intent, 
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.outline_task_alt_24)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)

        with(NotificationManagerCompat.from(context)) {
            // notificationId is a unique int for each notification that you must define
            try {
                notify(id, builder.build())
            } catch (e: SecurityException) {
                // Handle the case where notification permission is not granted
                e.printStackTrace()
            }
        }
    }
}