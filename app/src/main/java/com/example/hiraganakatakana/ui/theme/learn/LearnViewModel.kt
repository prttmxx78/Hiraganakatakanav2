// ui/learn/LearnViewModel.kt
package com.example.hiraganakatakana.ui.learn

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hiraganakatakana.data.database.CharacterEntity
import com.example.hiraganakatakana.data.repository.CharacterRepository
import com.example.hiraganakatakana.utils.SharedPreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LearnViewModel @Inject constructor(
    private val repository: CharacterRepository,
    private val sharedPrefsManager: SharedPreferencesManager
) : ViewModel() {

    private val _currentCharacter = MutableLiveData<CharacterEntity>()
    val currentCharacter: LiveData<CharacterEntity> = _currentCharacter

    private val _score = MutableLiveData<Int>()
    val score: LiveData<Int> = _score

    private val _streak = MutableLiveData<Int>()
    val streak: LiveData<Int> = _streak

    private val _sessionProgress = MutableLiveData<SessionProgress>()
    val sessionProgress: LiveData<SessionProgress> = _sessionProgress

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private var currentScore = 0
    private var currentStreak = 0
    private var sessionStartTime = 0L
    private var charactersStudied = 0
    private var correctAnswers = 0

    fun startLearnSession(type: String) {
        sessionStartTime = System.currentTimeMillis()
        charactersStudied = 0
        correctAnswers = 0
        currentScore = 0
        currentStreak = 0

        _score.value = currentScore
        _streak.value = currentStreak

        loadNextCharacter(type)
    }

    fun loadNextCharacter(type: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val characters = repository.getCharactersNeedingPractice(type, 1)
                if (characters.isNotEmpty()) {
                    _currentCharacter.value = characters.first()
                } else {
                    // No characters need practice, get random one
                    val randomCharacters = repository.getRandomCharactersForQuiz(type, 1)
                    if (randomCharacters.isNotEmpty()) {
                        _currentCharacter.value = randomCharacters.first()
                    }
                }
            } catch (e: Exception) {
                // Handle error
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun submitAnswer(userAnswer: String, type: String) {
        val character = _currentCharacter.value ?: return
        val isCorrect = userAnswer.lowercase() == character.romaji.lowercase()

        viewModelScope.launch {
            // Update character progress
            repository.updateCharacterProgress(character.id, isCorrect)

            // Update session stats
            charactersStudied++
            if (isCorrect) {
                correctAnswers++
                currentScore += calculateScore()
                currentStreak++
            } else {
                currentStreak = 0
            }

            _score.value = currentScore
            _streak.value = currentStreak
            _sessionProgress.value = SessionProgress(
                charactersStudied = charactersStudied,
                correctAnswers = correctAnswers,
                accuracy = if (charactersStudied > 0) (correctAnswers.toFloat() / charactersStudied) * 100 else 0f
            )

            // Update last study time
            sharedPrefsManager.setLastStudyTime(System.currentTimeMillis())

            // Load next character after a brief delay
            kotlinx.coroutines.delay(1000)
            loadNextCharacter(type)
        }
    }

    private fun calculateScore(): Int {
        val baseScore = 10
        val streakBonus = currentStreak * 2
        return baseScore + streakBonus
    }

    fun endSession() {
        val sessionDuration = System.currentTimeMillis() - sessionStartTime
        sharedPrefsManager.addStudyTime(sessionDuration)

        if (correctAnswers > 0) {
            sharedPrefsManager.incrementStudyStreak()
        }
    }

    data class SessionProgress(
        val charactersStudied: Int,
        val correctAnswers: Int,
        val accuracy: Float
    )
}