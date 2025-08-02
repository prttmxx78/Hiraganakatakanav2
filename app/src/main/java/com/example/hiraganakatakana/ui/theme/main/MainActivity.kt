// ui/main/MainActivity.kt
package com.example.hiraganakatakana.ui.main

import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.Network
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.hiraganakatakana.R
import com.example.hiraganakatakana.broadcast.SyncResultReceiver
import com.example.hiraganakatakana.data.remote.NetworkUtils
import com.example.hiraganakatakana.databinding.ActivityMainBinding
import com.example.hiraganakatakana.service.SyncService
import com.example.hiraganakatakana.utils.AlarmManagerUtils
import com.example.hiraganakatakana.utils.JobSchedulerUtils
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()
    private lateinit var syncResultReceiver: SyncResultReceiver

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            runOnUiThread {
                viewModel.syncData()
            }
        }

        override fun onLost(network: Network) {
            runOnUiThread {
                showNetworkLostMessage()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupNavigation()
        setupObservers()
        setupSyncReceiver()
        registerNetworkCallback()
        scheduleBackgroundTasks()
        handleIntent(intent)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun setupNavigation() {
        val navController = findNavController(R.id.nav_host_fragment)
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home,
                R.id.navigation_learn,
                R.id.navigation_quiz,
                R.id.navigation_progress,
                R.id.navigation_settings
            )
        )

        setupActionBarWithNavController(navController, appBarConfiguration)
        binding.bottomNavigation.setupWithNavController(navController)
    }

    private fun setupObservers() {
        viewModel.syncStatus.observe(this) { status ->
            when (status) {
                is MainViewModel.SyncStatus.Loading -> {
                    // Show loading indicator if needed
                }
                is MainViewModel.SyncStatus.Success -> {
                    // Show success message or update UI
                }
                is MainViewModel.SyncStatus.Error -> {
                    Toast.makeText(this, "Sync error: ${status.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }

        viewModel.isLoading.observe(this) { isLoading ->
            // Update loading state in UI
            binding.progressBar?.visibility = if (isLoading) {
                android.view.View.VISIBLE
            } else {
                android.view.View.GONE
            }
        }
    }

    private fun setupSyncReceiver() {
        syncResultReceiver = SyncResultReceiver { success, message ->
            runOnUiThread {
                if (success) {
                    viewModel.refreshProgress()
                } else {
                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                }
            }
        }

        val filter = IntentFilter(SyncService.ACTION_SYNC_COMPLETE)
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(syncResultReceiver, filter)
    }

    private fun registerNetworkCallback() {
        NetworkUtils.registerNetworkCallback(this, networkCallback)
    }

    private fun scheduleBackgroundTasks() {
        // Schedule periodic sync job
        JobSchedulerUtils.scheduleDataSyncJob(this)

        // Set up study reminders
        AlarmManagerUtils.scheduleStudyReminder(this)
        AlarmManagerUtils.scheduleDailyStreakCheck(this)
    }

    private fun handleIntent(intent: Intent?) {
        intent?.getStringExtra("action")?.let { action ->
            when (action) {
                "learn" -> navigateToLearn()
                "quiz" -> navigateToQuiz()
            }
        }
    }

    private fun navigateToLearn() {
        findNavController(R.id.nav_host_fragment)
            .navigate(R.id.navigation_learn)
    }

    private fun navigateToQuiz() {
        findNavController(R.id.nav_host_fragment)
            .navigate(R.id.navigation_quiz)
    }

    private fun showNetworkLostMessage() {
        Toast.makeText(this, "Network connection lost", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            LocalBroadcastManager.getInstance(this)
                .unregisterReceiver(syncResultReceiver)
        } catch (e: IllegalArgumentException) {
            // Receiver was not registered
        }
    }
}