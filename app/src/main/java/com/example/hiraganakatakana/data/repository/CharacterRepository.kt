// data/repository/CharacterRepository.kt
package com.example.hiraganakatakana.data.repository

import androidx.lifecycle.LiveData
import com.example.hiraganakatakana.data.database.CharacterDao
import com.example.hiraganakatakana.data.database.CharacterEntity
import com.example.hiraganakatakana.data.database.StudyProgress
import com.example.hiraganakatakana.data.remote.ApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CharacterRepository @Inject constructor(
    private val characterDao: CharacterDao,
    private val apiService: ApiService
) {

    // LiveData for UI observation
    fun getHiraganaCharacters(): LiveData<List<CharacterEntity>> {
        return characterDao.getCharactersByType("hiragana")
    }

    fun getKatakanaCharacters(): LiveData<List<CharacterEntity>> {
        return characterDao.getCharactersByType("katakana")
    }

    fun getHiraganaLearnedCount(): LiveData<Int> {
        return characterDao.getLearnedCount("hiragana")
    }

    fun getKatakanaLearnedCount(): LiveData<Int> {
        return characterDao.getLearnedCount("katakana")
    }

    fun getHiraganaTotalCount(): LiveData<Int> {
        return characterDao.getTotalCount("hiragana")
    }

    fun getKatakanaTotalCount(): LiveData<Int> {
        return characterDao.getTotalCount("katakana")
    }

    // Suspend functions for one-time operations
    suspend fun getCharacterById(id: Int): CharacterEntity? {
        return withContext(Dispatchers.IO) {
            characterDao.getCharacterById(id)
        }
    }

    suspend fun getRandomCharactersForQuiz(type: String, count: Int): List<CharacterEntity> {
        return withContext(Dispatchers.IO) {
            characterDao.getRandomCharacters(type, count)
        }
    }

    suspend fun getCharactersNeedingPractice(type: String, count: Int): List<CharacterEntity> {
        return withContext(Dispatchers.IO) {
            characterDao.getCharactersNeedingPractice(type, count)
        }
    }

    suspend fun getStudyProgress(type: String): StudyProgress {
        return withContext(Dispatchers.IO) {
            val totalCharacters = characterDao.getTotalCount(type)
            val learnedCharacters = characterDao.getLearnedCount(type)
            val accuracy = characterDao.getAccuracyForType(type) ?: 0f

            StudyProgress(
                totalCharacters = totalCharacters.value ?: 0,
                learnedCharacters = learnedCharacters.value ?: 0,
                accuracy = accuracy,
                studyStreak = calculateStudyStreak(type)
            )
        }
    }

    private suspend fun calculateStudyStreak(type: String): Int {
        val recentStudied = characterDao.getRecentlyStudiedCharacters(type)
        // Simple streak calculation - can be made more sophisticated
        return if (recentStudied.isNotEmpty()) {
            val lastStudiedToday = recentStudied.any { character ->
                val today = System.currentTimeMillis()
                val oneDayAgo = today - (24 * 60 * 60 * 1000)
                character.lastStudied > oneDayAgo
            }
            if (lastStudiedToday) 1 else 0 // Simplified
        } else 0
    }

    // Update character progress
    suspend fun updateCharacterProgress(characterId: Int, isCorrect: Boolean) {
        withContext(Dispatchers.IO) {
            val timestamp = System.currentTimeMillis()
            if (isCorrect) {
                characterDao.incrementCorrectCount(characterId, timestamp)
            } else {
                characterDao.incrementIncorrectCount(characterId, timestamp)
            }
        }
    }

    suspend fun updateCharacter(character: CharacterEntity) {
        withContext(Dispatchers.IO) {
            characterDao.update(character)
        }
    }

    // Network operations
    suspend fun syncCharactersFromServer(): Result<List<CharacterEntity>> {
        return withContext(Dispatchers.IO) {
            try {
                val characters = apiService.getCharacters()
                characterDao.insertAll(characters)
                Result.success(characters)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun refreshData(): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val result = syncCharactersFromServer()
                if (result.isSuccess) {
                    Result.success("Data refreshed successfully")
                } else {
                    Result.failure(result.exceptionOrNull() ?: Exception("Unknown error"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}