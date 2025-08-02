// broadcast/SyncResultReceiver.kt
package com.example.hiraganakatakana.broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.hiraganakatakana.service.SyncService

class SyncResultReceiver(
    private val onResult: (success: Boolean, message: String) -> Unit
) : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == SyncService.ACTION_SYNC_COMPLETE) {
            val success = intent.getBooleanExtra(SyncService.EXTRA_SYNC_SUCCESS, false)
            val message = intent.getStringExtra(SyncService.EXTRA_SYNC_MESSAGE) ?: "Unknown result"
            onResult(success, message)
        }
    }
}