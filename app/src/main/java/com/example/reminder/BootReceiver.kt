package com.example.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.data.TaskDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    companion object {
        private const val TAG = "BootReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d(TAG, "Phone rebooted. Rescheduling all future reminders.")
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val db = TaskDatabase.getDatabase(context)
                    val dao = db.taskDao()
                    val tasks = dao.getAllTasks().first()
                    var count = 0
                    tasks.forEach { task ->
                        if (!task.isCompleted) {
                            ReminderScheduler.scheduleRemindersForTask(context, task)
                            count++
                        }
                    }
                    Log.d(TAG, "Successfully rescheduled $count reminders on boot.")
                } catch (e: Exception) {
                    Log.e(TAG, "Error rescheduling reminders on boot", e)
                }
            }
        }
    }
}
