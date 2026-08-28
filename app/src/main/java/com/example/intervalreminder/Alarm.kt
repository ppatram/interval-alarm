package com.example.intervalreminder

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "alarms")
data class Alarm(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val intervalMinutes: Long,
    val startHour: Int,
    val startMinute: Int,
    val endHour: Int,
    val endMinute: Int,
    val message: String,
    val vibrate: Boolean,
    val days: String, // Comma-separated booleans: "true,false,..."
    val enabled: Boolean,
    val soundUri: String? = null
)
