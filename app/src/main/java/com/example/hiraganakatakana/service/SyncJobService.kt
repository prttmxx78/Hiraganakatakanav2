// service/SyncJobService.kt
package com.example.hiraganakatakana.service

import android.app.job.JobInfo
import android.app.job.JobParameters
import android.app.job.JobScheduler
import android.app.job.JobService
import android.content.ComponentName
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import androidx.annotation.RequiresApi
import com.example.hiraganakatakana.data.repository.CharacterRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
@AndroidEntryPoint
class SyncJobService : JobService() {

    @Inject
    lateinit var repository: CharacterRepository

    private var jobCoroutine: Job? = null

    override fun onStartJob(params: JobParameters?): Boolean {
        jobCoroutine = CoroutineScope(Dispatchers.IO).launch {
            try {
                performSync()
                jobFinished(params, false) // Job completed successfully
            } catch (e: Exception) {
                jobFinished(params, true) // Reschedule job on failure
            }
        }

        return true // Job is running asynchronously
    }

    override fun onStopJob(params: JobParameters?): Boolean {
        jobCoroutine?.cancel()
        return true // Reschedule job if it was stopped before completion
    }

    private suspend fun performSync() {
        // Perform data synchronization
        repository.syncCharactersFromServer()

        // Additional sync operations can be added here
        // For example: sync user progress, download audio files, etc.
    }

    companion object {
        const val JOB_ID = 1000
    }
}

object JobSchedulerUtils {

    fun scheduleDataSyncJob(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val jobScheduler = context.getSystemService(Context.JOB_SCHEDULER_SERVICE)
                    as JobScheduler

            val jobInfo = JobInfo.Builder(
                SyncJobService.JOB_ID,
                ComponentName(context, SyncJobService::class.java)
            )
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setPersisted(true)
                .setPeriodic(24 * 60 * 60 * 1000) // 24 hours
                .setRequiresCharging(false)
                .setRequiresDeviceIdle(false)
                .build()

            val result = jobScheduler.schedule(jobInfo)
            if (result == JobScheduler.RESULT_SUCCESS) {
                // Job scheduled successfully
            }
        }
    }

    fun cancelDataSyncJob(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val jobScheduler = context.getSystemService(Context.JOB_SCHEDULER_SERVICE)
                    as JobScheduler
            jobScheduler.cancel(SyncJobService.JOB_ID)
        }
    }

    fun isJobScheduled(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val jobScheduler = context.getSystemService(Context.JOB_SCHEDULER_SERVICE)
                    as JobScheduler

            return jobScheduler.allPendingJobs.any {
                it.id == SyncJobService.JOB_ID
            }
        }
        return false
    }
}