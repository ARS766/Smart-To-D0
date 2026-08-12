package com.example.reminder

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.data.TaskDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AlarmReceiver : BroadcastReceiver() {
    companion object {
        private const val TAG = "AlarmReceiver"
        const val ACTION_MARK_DONE = "com.example.reminder.ACTION_MARK_DONE"
        const val CHANNEL_ID = "smart_todo_reminders"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        val taskId = intent.getLongExtra("TASK_ID", -1L)

        if (action == ACTION_MARK_DONE) {
            if (taskId != -1L) {
                Log.d(TAG, "Marking task $taskId as completed from notification action")
                CoroutineScope(Dispatchers.IO).launch {
                    val db = TaskDatabase.getDatabase(context)
                    val dao = db.taskDao()
                    val task = dao.getTaskById(taskId)
                    if (task != null) {
                        val updatedTask = task.copy(isCompleted = true)
                        dao.updateTask(updatedTask)
                        ReminderScheduler.cancelRemindersForTask(context, taskId)
                        Log.d(TAG, "Task $taskId successfully marked as completed")
                    }
                    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    notificationManager.cancel(taskId.toInt())
                }
            }
            return
        }

        val taskTitle = intent.getStringExtra("TASK_TITLE") ?: "Task Reminder"
        val taskCategory = intent.getStringExtra("TASK_CATEGORY") ?: "Task"
        val reminderType = intent.getStringExtra("REMINDER_TYPE") ?: "Smart Reminder"

        if (taskId == -1L) return

        showNotification(context, taskId, taskTitle, taskCategory, reminderType)
    }

    private fun showNotification(
        context: Context,
        taskId: Long,
        title: String,
        category: String,
        reminderType: String
    ) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Task Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Smart automated reminder alerts"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val contentIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val contentPendingIntent = PendingIntent.getActivity(
            context,
            taskId.toInt(),
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val doneIntent = Intent(context, AlarmReceiver::class.java).apply {
            action = ACTION_MARK_DONE
            putExtra("TASK_ID", taskId)
        }
        val donePendingIntent = PendingIntent.getBroadcast(
            context,
            taskId.toInt() + 100000,
            doneIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Notification Icon: Use standard system drawable or fallback to lock icon
        val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle(title)
            .setContentText("[$category] $reminderType")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(contentPendingIntent)
            .addAction(
                android.R.drawable.checkbox_on_background,
                "Selesai",
                donePendingIntent
            )

        notificationManager.notify(taskId.toInt(), notificationBuilder.build())
    }
}
