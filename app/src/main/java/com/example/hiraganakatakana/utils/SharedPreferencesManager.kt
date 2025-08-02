// utils/SharedPreferencesManager.kt
package com.example.hiraganakatakana.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SharedPreferencesManager @Inject constructor(context: Context) {

    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(
        PREF_NAME,
        Context.MODE_PRIVATE
    )

    // Study Reminder Settings
    fun setStudyReminderEnabled(enabled: Boolean) {
        sharedPreferences.edit {
            putBoolean(KEY_REMINDER_ENABLED, enabled)
        }
    }

    fun isStudyReminderEnabled(): Boolean {
        return sharedPreferences.getBoolean(KEY_REMINDER_ENABLED, true)
    }

    fun setStudyReminderTime(hour: Int, minute: Int) {
        sharedPreferences.edit {
            putInt(KEY_REMINDER_HOUR, hour)
            putInt(KEY_REMINDER_MINUTE, minute)
        }
    }

    fun getStudyReminderTime(): ReminderTime {
        return ReminderTime(
            hour = sharedPreferences.getInt(KEY_REMINDER_HOUR, 20),
            minute = sharedPreferences.getInt(KEY_REMINDER_MINUTE, 0)
        )
    }

    // Study Progress
    fun setLastStudyTime(time: Long) {
        sharedPreferences.edit {
            putLong(KEY_LAST_STUDY_TIME, time)
        }
    }

    fun getLastStudyTime(): Long {
        return sharedPreferences.getLong(KEY_LAST_STUDY_TIME, 0)
    }

    fun setStudyStreak(streak: Int) {
        sharedPreferences.edit {
            putInt(KEY_STUDY_STREAK, streak)
        }
    }

    fun getStudyStreak(): Int {
        return sharedPreferences.getInt(KEY_STUDY_STREAK, 0)
    }

    fun incrementStudyStreak() {
        val currentStreak = getStudyStreak()
        setStudyStreak(currentStreak + 1)
    }

    fun resetStudyStreak() {
        setStudyStreak(0)
    }

    // App Settings
    fun setDifficulty(difficulty: Difficulty) {
        sharedPreferences.edit {
            putString(KEY_DIFFICULTY, difficulty.name)
        }
    }

    fun getDifficulty(): Difficulty {
        val difficultyString = sharedPreferences.getString(KEY_DIFFICULTY, Difficulty.MEDIUM.name)
        return try {
            Difficulty.valueOf(difficultyString ?: Difficulty.MEDIUM.name)
        } catch (e: IllegalArgumentException) {
            Difficulty.MEDIUM
        }
    }

    fun setSoundEnabled(enabled: Boolean) {
        sharedPreferences.edit {
            putBoolean(KEY_SOUND_ENABLED, enabled)
        }
    }

    fun isSoundEnabled(): Boolean {
        return sharedPreferences.getBoolean(KEY_SOUND_ENABLED, true)
    }

    fun setVibrationEnabled(enabled: Boolean) {
        sharedPreferences.edit {
            putBoolean(KEY_VIBRATION_ENABLED, enabled)
        }
    }

    fun isVibrationEnabled(): Boolean {
        return sharedPreferences.getBoolean(KEY_VIBRATION_ENABLED, true)
    }

    // Quiz Settings
    fun setQuizLength(length: Int) {
        sharedPreferences.edit {
            putInt(KEY_QUIZ_LENGTH, length)
        }
    }

    fun getQuizLength(): Int {
        return sharedPreferences.getInt(KEY_QUIZ_LENGTH, 10)
    }

    fun setShowHints(show: Boolean) {
        sharedPreferences.edit {
            putBoolean(KEY_SHOW_HINTS, show)
        }
    }

    fun shouldShowHints(): Boolean {
        return sharedPreferences.getBoolean(KEY_SHOW_HINTS, true)
    }

    // User Progress
    fun setTotalStudyTime(time: Long) {
        sharedPreferences.edit {
            putLong(KEY_TOTAL_STUDY_TIME, time)
        }
    }

    fun getTotalStudyTime(): Long {
        return sharedPreferences.getLong(KEY_TOTAL_STUDY_TIME, 0)
    }

    fun addStudyTime(additionalTime: Long) {
        val currentTime = getTotalStudyTime()
        setTotalStudyTime(currentTime + additionalTime)
    }

    // First launch
    fun setFirstLaunch(isFirst: Boolean) {
        sharedPreferences.edit {
            putBoolean(KEY_FIRST_LAUNCH, isFirst)
        }
    }

    fun isFirstLaunch(): Boolean {
        return sharedPreferences.getBoolean(KEY_FIRST_LAUNCH, true)
    }

    // Clear all data
    fun clearAllData() {
        sharedPreferences.edit {
            clear()
        }
    }

    companion object {
        private const val PREF_NAME = "hiragana_katakana_prefs"

        // Keys
        private const val KEY_REMINDER_ENABLED = "reminder_enabled"
        private const val KEY_REMINDER_HOUR = "reminder_hour"
        private const val KEY_REMINDER_MINUTE = "reminder_minute"
        private const val KEY_LAST_STUDY_TIME = "last_study_time"
        private const val KEY_STUDY_STREAK = "study_streak"
        private const val KEY_DIFFICULTY = "difficulty"
        private const val KEY_SOUND_ENABLED = "sound_enabled"
        private const val KEY_VIBRATION_ENABLED = "vibration_enabled"
        private const val KEY_QUIZ_LENGTH = "quiz_length"
        private const val KEY_SHOW_HINTS = "show_hints"
        private const val KEY_TOTAL_STUDY_TIME = "total_study_time"
        private const val KEY_FIRST_LAUNCH = "first_launch"
    }
}

// Data classes
data class ReminderTime(val hour: Int, val minute: Int)

enum class Difficulty {
    EASY, MEDIUM, HARD
}