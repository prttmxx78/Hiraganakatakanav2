// service/StudyReminderService.kt
package com.example.hiraganakatakana.service

import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import android.os.Looper
import com.example.hiraganakatakana.broadcast.StudyReminderReceiver
import com.example.hiraganakatakana.utils.SharedPreferencesManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class StudyReminderService : Service() {

    @Inject
    lateinit var sharedPrefsManager: SharedPreferencesManager

    private lateinit var serviceHandler: Handler
    private lateinit var serviceLooper: Looper

    override fun onCreate() {
        super.onCreate()

        val handlerThread = HandlerThread("StudyReminderService").apply {
            start()
        }

        serviceLooper = handlerThread.looper
        serviceHandler = Handler(serviceLooper)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        serviceHandler.post {
            performBackgroundWork(intent)
            stopSelf(startId)
        }

        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun performBackgroundWork(intent: Intent?) {
        when (intent?.action) {
            ACTION_CHECK_STUDY_PROGRESS -> checkStudyProgress()
            ACTION_CLEANUP_DATA -> cleanupOldData()
            ACTION_CALCULATE_STATS -> calculateStudyStats()
        }
    }

    private fun checkStudyProgress() {
        val lastStudyTime = sharedPrefsManager.getLastStudyTime()
        val currentTime = System.currentTimeMillis()
        val oneDayInMillis = 24 * 60 * 60 * 1000

        // Check if user needs a reminder
        if (currentTime - lastStudyTime > oneDayInMillis) {
            sendStudyReminder()
        }

        // Check streak status
        if (currentTime - lastStudyTime > 2 * oneDayInMillis) {
            // Reset streak if missed for 2 days
            sharedPrefsManager.resetStudyStreak()
        }
    }

    private fun cleanupOldData() {
        // Clean up old cached data or temporary files
        // This is a placeholder for actual cleanup logic
    }

    private fun calculateStudyStats() {
        // Calculate and cache study statistics
        // This can be used for analytics or user insights
    }

    private fun sendStudyReminder() {
        val intent = Intent(StudyReminderReceiver.ACTION_STUDY_REMINDER)
        sendBroadcast(intent)
    }

    companion object {
        const val ACTION_CHECK_STUDY_PROGRESS = "check_study_progress"
        const val ACTION_CLEANUP_DATA = "cleanup_data"
        const val ACTION_CALCULATE_STATS = "calculate_stats"
    }
}