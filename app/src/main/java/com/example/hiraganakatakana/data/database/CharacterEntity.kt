// data/database/CharacterEntity.kt
package com.example.hiraganakatakana.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "characters")
data class CharacterEntity(
    @PrimaryKey val id: Int,
    val character: String,
    val romaji: String,
    val type: String, // "hiragana" or "katakana"
    val category: String, // "basic", "dakuten", "combo"
    val audioUrl: String? = null,
    val isLearned: Boolean = false,
    val correctCount: Int = 0,
    val incorrectCount: Int = 0,
    val lastStudied: Long = 0
)

// Progress tracking model
data class StudyProgress(
    val totalCharacters: Int,
    val learnedCharacters: Int,
    val accuracy: Float,
    val studyStreak: Int
)

// Quiz result model
data class QuizResult(
    val characterId: Int,
    val userAnswer: String,
    val correctAnswer: String,
    val isCorrect: Boolean,
    val timeSpent: Long
)