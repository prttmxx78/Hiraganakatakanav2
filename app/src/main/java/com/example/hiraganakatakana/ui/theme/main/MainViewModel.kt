// ui/main/MainViewModel.kt
package com.example.hiraganakatakana.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hiraganakatakana.data.database.CharacterEntity
import com.example.hiraganakatakana.data.database.StudyProgress
import com.example.hiraganakatakana.data.repository.CharacterRepository
import com.example.hiraganakatakana.utils.SharedPreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: CharacterRepository,
    private val sharedPrefsManager: SharedPreferencesManager
) : ViewModel() {

    private val _syncStatus = MutableLiveData<SyncStatus>()
    val syncStatus: LiveData<SyncStatus> = _syncStatus

    private val _hiraganaProgress = MutableLiveData<StudyProgress>()
    val hiraganaProgress: LiveData<StudyProgress> = _hiraganaProgress

    private val _katakanaProgress = MutableLiveData<StudyProgress>()
    val katakanaProgress: LiveData<StudyProgress> = _katakanaProgress

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    // LiveData from repository
    val hiraganaCharacters = repository.getHiraganaCharacters()
    val katakanaCharacters = repository.getKatakanaCharacters()
    val hiraganaLearnedCount = repository.getHiraganaLearnedCount()
    val katakanaLearnedCount = repository.getKatakanaLearnedCount()
    val hiraganaTotalCount = repository.getHiraganaTotalCount()
    val katakanaTotalCount = repository.getKatakanaTotalCount()

    init {
        loadProgress()
    }

    fun syncData() {
        viewModelScope.launch {
            _syncStatus.value = SyncStatus.Loading
            _isLoading.value = true

            try {
                val result = repository.refreshData()
                if (result.isSuccess) {
                    _syncStatus.value = SyncStatus.Success
                    loadProgress()
                } else {
                    _syncStatus.value = SyncStatus.Error(
                        result.exceptionOrNull()?.message ?: "Unknown error"
                    )
                }
            } catch (e: Exception) {
                _syncStatus.value = SyncStatus.Error(e.message ?: "Unknown error")
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun loadProgress() {
        viewModelScope.launch {
            try {
                val hiraganaProgressData = repository.getStudyProgress("hiragana")
                val katakanaProgressData = repository.getStudyProgress("katakana")

                _hiraganaProgress.value = hiraganaProgressData
                _katakanaProgress.value = katakanaProgressData
            } catch (e: Exception) {
                // Handle error silently or show message
            }
        }
    }

    fun refreshProgress() {
        loadProgress()
    }

    fun getStudyStreak(): Int {
        return sharedPrefsManager.getStudyStreak()
    }

    fun getTotalStudyTime(): Long {
        return sharedPrefsManager.getTotalStudyTime()
    }

    sealed class SyncStatus {
        object Loading : SyncStatus()
        object Success : SyncStatus()
        data class Error(val message: String) : SyncStatus()
    }
}