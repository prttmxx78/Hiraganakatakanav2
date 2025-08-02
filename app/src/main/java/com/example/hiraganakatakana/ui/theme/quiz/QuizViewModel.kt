// ui/quiz/QuizViewModel.kt
package com.example.hiraganakatakana.ui.quiz

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.hiraganakatakana.data.database.CharacterEntity
import com.example.hiraganakatakana.data.database.QuizResult
import com.example.hiraganakatakana.data.repository.CharacterRepository
import com.example.hiraganakatakana.utils.SharedPreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class QuizViewModel @Inject constructor(
    private val repository: CharacterRepository,
    private val sharedPrefsManager: SharedPreferencesManager
) : ViewModel() {

    private val _quizState = MutableLiveData<QuizState>()
    val quizState: LiveData<QuizState> = _quizState

    private val _currentQuestion = MutableLiveData<QuizQuestion>()
    val currentQuestion: LiveData<QuizQuestion> = _currentQuestion

    private val _quizResults = MutableLiveData<List<QuizResult>>()
    val quizResults: LiveData<List<QuizResult>> = _quizResults

    private val _timeRemaining = MutableLiveData<Long>()
    val timeRemaining: LiveData<Long> = _timeRemaining

    private var quizQuestions = mutableListOf<QuizQuestion>()
    private var currentQuestionIndex = 0
    private var results = mutableListOf<QuizResult>()
    private var questionStartTime = 0L

    fun startQuiz(type: String, questionCount: Int = 10) {
        viewModelScope.launch {
            _quizState.value = QuizState.Loading

            try {
                val characters = repository.getRandomCharactersForQuiz(type, questionCount)
                quizQuestions = generateQuizQuestions(characters)
                currentQuestionIndex = 0
                results.clear()

                if (quizQuestions.isNotEmpty()) {
                    _quizState.value = QuizState.InProgress(
                        currentQuestion = currentQuestionIndex + 1,
                        totalQuestions = quizQuestions.size
                    )
                    showNextQuestion()
                } else {
                    _quizState.value = QuizState.Error("No questions available")
                }
            } catch (e: Exception) {
                _quizState.value = QuizState.Error(e.message ?: "Unknown error")
            }
        }
    }

    private suspend fun generateQuizQuestions(characters: List<CharacterEntity>): List<QuizQuestion> {
        return characters.map { character ->
            val allCharacters = repository.getRandomCharactersForQuiz(character.type, 20)
            val incorrectOptions = allCharacters
                .filter { it.id != character.id }
                .shuffled()
                .take(3)
                .map { it.romaji }

            val options = (incorrectOptions + character.romaji).shuffled()

            QuizQuestion(
                character = character,
                options = options,
                correctAnswer = character.romaji
            )
        }
    }

    private fun showNextQuestion() {
        if (currentQuestionIndex < quizQuestions.size) {
            questionStartTime = System.currentTimeMillis()
            _currentQuestion.value = quizQuestions[currentQuestionIndex]
        } else {
            finishQuiz()
        }
    }

    fun submitAnswer(selectedAnswer: String) {
        val question = _currentQuestion.value ?: return
        val timeSpent = System.currentTimeMillis() - questionStartTime
        val isCorrect = selectedAnswer == question.correctAnswer

        val result = QuizResult(
            characterId = question.character.id,
            userAnswer = selectedAnswer,
            correctAnswer = question.correctAnswer,
            isCorrect = isCorrect,
            timeSpent = timeSpent
        )

        results.add(result)

        viewModelScope.launch {
            // Update character progress
            repository.updateCharacterProgress(question.character.id, isCorrect)

            currentQuestionIndex++

            if (currentQuestionIndex < quizQuestions.size) {
                _quizState.value = QuizState.InProgress(
                    currentQuestion = currentQuestionIndex + 1,
                    totalQuestions = quizQuestions.size
                )
                showNextQuestion()
            } else {
                finishQuiz()
            }
        }
    }

    private fun finishQuiz() {
        _quizResults.value = results

        val correctCount = results.count { it.isCorrect }
        val totalQuestions = results.size
        val accuracy = if (totalQuestions > 0) (correctCount.toFloat() / totalQuestions) * 100 else 0f
        val totalTime = results.sumOf { it.timeSpent }

        _quizState.value = QuizState.Completed(
            score = correctCount,
            totalQuestions = totalQuestions,
            accuracy = accuracy,
            totalTime = totalTime
        )

        // Update study time and streak
        sharedPrefsManager.addStudyTime(totalTime)
        if (correctCount > 0) {
            sharedPrefsManager.incrementStudyStreak()
        }
        sharedPrefsManager.setLastStudyTime(System.currentTimeMillis())
    }

    fun restartQuiz(type: String) {
        startQuiz(type, sharedPrefsManager.getQuizLength())
    }

    data class QuizQuestion(
        val character: CharacterEntity,
        val options: List<String>,
        val correctAnswer: String
    )

    sealed class QuizState {
        object Loading : QuizState()
        data class InProgress(val currentQuestion: Int, val totalQuestions: Int) : QuizState()
        data class Completed(
            val score: Int,
            val totalQuestions: Int,
            val accuracy: Float,
            val totalTime: Long
        ) : QuizState()
        data class Error(val message: String) : QuizState()
    }
}