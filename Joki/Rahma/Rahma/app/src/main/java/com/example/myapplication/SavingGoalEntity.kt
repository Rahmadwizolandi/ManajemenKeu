package com.example.myapplication

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saving_goal")
data class SavingGoalEntity(
    @PrimaryKey(autoGenerate = true) 
    val id: Int = 0, // Sekarang bisa banyak target (auto-increment)
    val name: String,
    val targetAmount: Long,
    val currentAmount: Long
)
