package com.example.reminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.data.TaskEntity

object ReminderScheduler {
    private const val TAG = "ReminderScheduler"

    fun scheduleRemindersForTask(context: Context, task: TaskEntity) {
        cancelRemindersForTask(context, task.id)

        if (task.isCompleted) return

        val currentTime = System.currentTimeMillis()
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // 1. Calculate automated smart reminders
        val automatedTimes = calculateAutomatedReminders(task)
        automatedTimes.forEachIndexed { index, triggerTime ->
            if (triggerTime > currentTime) {
                val requestCode = ((task.id * 10) + (index + 1)).toInt()
                scheduleExactAlarm(context, alarmManager, triggerTime, requestCode, task.title, task.category, "Smart Reminder")
            }
        }

        // 2. Schedule custom manual reminder if set
        task.customReminderTime?.let { customTime ->
            if (customTime > currentTime) {
                val requestCode = ((task.id * 10) + 4).toInt()
                scheduleExactAlarm(context, alarmManager, customTime, requestCode, task.title, task.category, "Custom Reminder")
            }
        }
    }

    fun cancelRemindersForTask(context: Context, taskId: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java)

        for (i in 1..4) {
            val requestCode = ((taskId * 10) + i).toInt()
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
        Log.d(TAG, "Cancelled all potential scheduled reminders for task $taskId")
    }

    private fun scheduleExactAlarm(
        context: Context,
        alarmManager: AlarmManager,
        triggerTime: Long,
        requestCode: Int,
        taskTitle: String,
        taskCategory: String,
        reminderType: String
    ) {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("TASK_ID", (requestCode / 10).toLong())
            putExtra("TASK_TITLE", taskTitle)
            putExtra("TASK_CATEGORY", taskCategory)
            putExtra("REMINDER_TYPE", reminderType)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                    )
                } else {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                    )
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            }
            Log.d(TAG, "Scheduled alarm for request code $requestCode at $triggerTime ($reminderType)")
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException scheduling exact alarm, falling back", e)
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error scheduling alarm", e)
        }
    }

    fun calculateAutomatedReminders(task: TaskEntity): List<Long> {
        val triggers = mutableListOf<Long>()
        val target = task.targetDateTime
        val diffMillis = target - System.currentTimeMillis()
        val diffDays = diffMillis / (1000 * 60 * 60 * 24)

        if (diffMillis <= 0) return triggers

        if (diffDays < 1) {
            // Same-Day Task (< 24 hours): T_target - 8 hours
            val trigger = target - (8L * 60 * 60 * 1000)
            triggers.add(trigger)
        } else if (diffDays in 1..7) {
            // Daily / Weekly (2 - 7 Days): Target - 3 days and Target - 2 days
            val trigger1 = target - (3L * 24 * 60 * 60 * 1000)
            val trigger2 = target - (2L * 24 * 60 * 60 * 1000)
            triggers.add(trigger1)
            triggers.add(trigger2)
        } else {
            // Monthly / Long-term (> 7 days): Tiered at 10, 5, and 2 days
            val trigger1 = target - (10L * 24 * 60 * 60 * 1000)
            val trigger2 = target - (5L * 24 * 60 * 60 * 1000)
            val trigger3 = target - (2L * 24 * 60 * 60 * 1000)
            triggers.add(trigger1)
            triggers.add(trigger2)
            triggers.add(trigger3)
        }
        return triggers
    }
}
