package com.example.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "questions")
data class Question(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val text: String,
    val category: String, // "deep", "playful", "future", "memories"
    val isFavorite: Boolean = false,
    val isDiscussed: Boolean = false,
    val isCustom: Boolean = false
)
