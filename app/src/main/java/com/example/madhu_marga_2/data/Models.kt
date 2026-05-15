package com.example.madhu_marga_2.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "hives")
data class Hive(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val hiveId: String,
    val location: String,
    val lastHealthScore: Int = 100
)

@Entity(tableName = "inspections")
data class Inspection(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val hiveId: Long,
    val date: Long,
    val isQueenPresent: Boolean,
    val pestsSeen: Boolean,
    val activityLevel: String, // Low, Medium, High
    val notes: String,
    val temperature: Double = 0.0,
    val honeyFlow: String = "Normal", // Low, Normal, High
    val broodCondition: String = "Healthy" // Poor, Healthy, Excellent
)

@Entity(tableName = "harvests")
data class Harvest(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val hiveId: Long,
    val date: Long,
    val quantity: Double // in kg
)

enum class AlertSeverity {
    CRITICAL, WARNING, NORMAL
}

data class SmartAlert(
    val id: String,
    val title: String,
    val description: String,
    val timestamp: Long,
    val severity: AlertSeverity,
    val suggestedAction: String,
    val hiveId: Long? = null
)
