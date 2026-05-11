package org.classapp.studyplanner.receiver

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import org.classapp.studyplanner.data.local.entity.Assignment
import java.time.LocalDateTime
import java.time.ZoneId

object NotificationScheduler {

    fun scheduleNotification(context: Context, assignment: Assignment) {
        // If notification is not enabled (null), don't schedule anything
        if (assignment.notification == null) return

        // Schedule for 3 days before and 1 day before
        scheduleAlarm(context, assignment, 3)
        scheduleAlarm(context, assignment, 1)
    }

    private fun scheduleAlarm(context: Context, assignment: Assignment, daysBefore: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val deadline = assignment.deadline ?: return
        
        // Calculate alarm time
        val triggerTime = deadline.minusDays(daysBefore.toLong())
        
        // Don't schedule if time is in the past
        if (triggerTime.isBefore(LocalDateTime.now())) return

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("title", "Deadline Reminder")
            putExtra("message", "Assignment '${assignment.title}' is due in $daysBefore day(s)!")
            putExtra("assignmentId", assignment.id)
        }

        // Use a unique request code for each (assignmentId + daysBefore) to avoid overwriting
        val requestCode = assignment.id * 10 + daysBefore

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val triggerAtMillis = triggerTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            }
        } else
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
    }

    fun cancelNotification(context: Context, assignmentId: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java)
        
        // Cancel both 3-day and 1-day alarms
        listOf(3, 1).forEach { daysBefore ->
            val requestCode = assignmentId * 10 + daysBefore
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
            alarmManager.cancel(pendingIntent)
        }
    }
}