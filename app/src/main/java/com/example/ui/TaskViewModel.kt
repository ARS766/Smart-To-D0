package com.example.ui

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.TaskDatabase
import com.example.data.TaskEntity
import com.example.data.TaskRepository
import com.example.reminder.ReminderScheduler
import com.example.ui.theme.AccentPalette
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

class TaskViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: TaskRepository
    private val sharedPrefs = application.getSharedPreferences("smart_todo_prefs", android.content.Context.MODE_PRIVATE)

    // Dynamic Personalization States
    val username = MutableStateFlow("")
    val currentPalette = MutableStateFlow(AccentPalette.BOLD_NEON)
    val isDarkMode = MutableStateFlow(true)
    val isOledMode = MutableStateFlow(true)

    init {
        val database = TaskDatabase.getDatabase(application)
        repository = TaskRepository(database.taskDao())
        username.value = sharedPrefs.getString("username", "") ?: ""
    }

    fun saveUsername(name: String) {
        sharedPrefs.edit().putString("username", name.trim()).apply()
        username.value = name.trim()
    }

    // Filtering State
    val selectedCategory = MutableStateFlow("Semua")
    val selectedTimeFilter = MutableStateFlow("Semua") // Semua, Hari Ini, Mendatang, Terlambat

    // Raw Tasks
    val allTasks: StateFlow<List<TaskEntity>> = repository.allTasks
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Filtered Tasks for highly reactive UI
    val filteredTasks: StateFlow<List<TaskEntity>> = combine(
        allTasks,
        selectedCategory,
        selectedTimeFilter
    ) { tasks, category, timeFilter ->
        val calendar = Calendar.getInstance()
        
        // Start and end of today bounds
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfToday = calendar.timeInMillis
        
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        val endOfToday = calendar.timeInMillis

        tasks.filter { task ->
            val matchesCategory = if (category == "Semua") {
                true
            } else {
                task.category.lowercase() == category.lowercase()
            }

            val matchesTime = when (timeFilter) {
                "Hari Ini" -> task.targetDateTime in startOfToday..endOfToday
                "Mendatang" -> task.targetDateTime > endOfToday
                "Terlambat" -> task.targetDateTime < startOfToday && !task.isCompleted
                else -> true
            }

            matchesCategory && matchesTime
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun addTask(
        title: String,
        description: String?,
        category: String,
        targetDateTime: Long,
        repeatType: String,
        colorAccentHex: String,
        customReminderTime: Long?,
        urgency: String
    ) {
        viewModelScope.launch {
            val task = TaskEntity(
                title = title,
                description = description,
                category = category,
                targetDateTime = targetDateTime,
                repeatType = repeatType,
                isCompleted = false,
                colorAccentHex = colorAccentHex,
                customReminderTime = customReminderTime,
                urgency = urgency
            )
            val newId = repository.insertTask(task)
            val savedTask = task.copy(id = newId)
            ReminderScheduler.scheduleRemindersForTask(getApplication(), savedTask)
        }
    }

    fun updateTask(task: TaskEntity) {
        viewModelScope.launch {
            repository.updateTask(task)
            ReminderScheduler.scheduleRemindersForTask(getApplication(), task)
        }
    }

    fun deleteTask(task: TaskEntity) {
        viewModelScope.launch {
            repository.deleteTask(task)
            ReminderScheduler.cancelRemindersForTask(getApplication(), task.id)
        }
    }

    fun toggleTaskCompletion(task: TaskEntity) {
        viewModelScope.launch {
            val updatedStatus = !task.isCompleted
            val updatedTask = task.copy(isCompleted = updatedStatus)
            repository.updateTask(updatedTask)

            if (updatedStatus) {
                // Completed: Cancel active notifications for this task instance
                ReminderScheduler.cancelRemindersForTask(getApplication(), task.id)

                // Recurring task: Auto generate next occurrence (T_next = T_current + Offset)
                if (task.repeatType != "NONE") {
                    val nextTargetTime = calculateNextOccurrence(task.targetDateTime, task.repeatType)
                    val nextCustomReminder = task.customReminderTime?.let {
                        calculateNextOccurrence(it, task.repeatType)
                    }

                    val recurringTask = TaskEntity(
                        title = task.title,
                        description = task.description,
                        category = task.category,
                        targetDateTime = nextTargetTime,
                        repeatType = task.repeatType,
                        isCompleted = false,
                        colorAccentHex = task.colorAccentHex,
                        customReminderTime = nextCustomReminder,
                        urgency = task.urgency
                    )
                    
                    val newId = repository.insertTask(recurringTask)
                    val savedRecurringTask = recurringTask.copy(id = newId)
                    ReminderScheduler.scheduleRemindersForTask(getApplication(), savedRecurringTask)
                    Log.d("TaskViewModel", "Auto-spawned recurring task ${task.title} for next occurrence.")
                }
            } else {
                // Uncompleted: Reschedule alarms
                ReminderScheduler.scheduleRemindersForTask(getApplication(), updatedTask)
            }
        }
    }

    private fun calculateNextOccurrence(currentEpoch: Long, repeatType: String): Long {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = currentEpoch
        }
        when (repeatType) {
            "DAILY" -> calendar.add(Calendar.DAY_OF_YEAR, 1)
            "WEEKLY" -> calendar.add(Calendar.WEEK_OF_YEAR, 7)
            "MONTHLY" -> calendar.add(Calendar.MONTH, 1)
        }
        return calendar.timeInMillis
    }

    class Factory(private val application: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(TaskViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return TaskViewModel(application) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
