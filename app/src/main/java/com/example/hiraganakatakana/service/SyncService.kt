// service/SyncService.kt
package com.example.hiraganakatakana.service

import android.app.IntentService
import android.content.Intent
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.hiraganakatakana.data.repository.CharacterRepository
import com.example.hiraganakatakana.utils.SharedPreferencesManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@AndroidEntryPoint
class SyncService : IntentService("SyncService") {

    @Inject
    lateinit var repository: CharacterRepository

    @Inject
    lateinit var sharedPrefsManager: SharedPreferencesManager

    override fun onHandleIntent(intent: Intent?) {
        val syncType = intent?.getStringExtra(EXTRA_SYNC_TYPE) ?: SYNC_TYPE_FULL

        try {
            when (syncType) {
                SYNC_TYPE_FULL -> performFullSync()
                SYNC_TYPE_PROGRESS -> syncProgress()
                SYNC_TYPE_CHARACTERS -> syncCharacters()
            }

            sendSyncResult(true, "Sync completed successfully")

        } catch (e: Exception) {
            sendSyncResult(false, "Sync failed: ${e.message}")
        }
    }

    private fun performFullSync() {
        runBlocking {
            // Sync characters from server
            repository.syncCharactersFromServer()

            // Update last sync time
            sharedPrefsManager.setLastStudyTime(System.currentTimeMillis())
        }
    }

    private fun syncProgress() {
        // Sync user progress to server
        // Implementation depends on your backend API
    }

    private fun syncCharacters() {
        runBlocking {
            repository.syncCharactersFromServer()
        }
    }

    private fun sendSyncResult(success: Boolean, message: String) {
        val resultIntent = Intent(ACTION_SYNC_COMPLETE).apply {
            putExtra(EXTRA_SYNC_SUCCESS, success)
            putExtra(EXTRA_SYNC_MESSAGE, message)
        }
        LocalBroadcastManager.getInstance(this).sendBroadcast(resultIntent)
    }

    companion object {
        const val ACTION_SYNC_COMPLETE = "com.example.hiraganakatakana.SYNC_COMPLETE"
        const val EXTRA_SYNC_TYPE = "sync_type"
        const val EXTRA_SYNC_SUCCESS = "sync_success"
        const val EXTRA_SYNC_MESSAGE = "sync_message"

        const val SYNC_TYPE_FULL = "full"
        const val SYNC_TYPE_PROGRESS = "progress"
        const val SYNC_TYPE_CHARACTERS = "characters"
    }
}