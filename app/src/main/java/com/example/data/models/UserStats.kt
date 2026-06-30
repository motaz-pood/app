package com.example.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_stats")
data class UserStats(
    @PrimaryKey val id: Int = 1,
    val partnerAName: String = "الطرف الأول",
    val partnerBName: String = "الطرف الثاني",
    val connectionPoints: Int = 10,
    val currentStreak: Int = 1,
    val lastCompletedDate: Long = 0
)
