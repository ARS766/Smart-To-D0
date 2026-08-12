package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String?,
    val category: String, // Misal: Game, Olahraga, Belajar, Tugas, dll
    val targetDateTime: Long, // Epoch Timestamp in millis
    val repeatType: String, // NONE, DAILY, WEEKLY, MONTHLY
    val isCompleted: Boolean = false,
    val colorAccentHex: String, // Hex color for the theme selector (e.g., "#FFBB86FC")
    val customReminderTime: Long? = null, // Epoch Timestamp in millis chosen by user manually
    val urgency: String = "MEDIUM" // LOW, MEDIUM, HIGH
)
