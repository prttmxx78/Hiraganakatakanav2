// data/database/CharacterDao.kt
package com.example.hiraganakatakana.data.database

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface CharacterDao {

    // Basic queries
    @Query("SELECT * FROM characters WHERE type = :type ORDER BY id")
    fun getCharactersByType(type: String): LiveData<List<CharacterEntity>>

    @Query("SELECT * FROM characters WHERE type = :type ORDER BY id")
    suspend fun getCharactersByTypeSync(type: String): List<CharacterEntity>

    @Query("SELECT * FROM characters WHERE id = :id")
    suspend fun getCharacterById(id: Int): CharacterEntity?

    // Learning progress queries
    @Query("SELECT * FROM characters WHERE isLearned = 0 AND type = :type LIMIT :limit")
    suspend fun getUnlearnedCharacters(type: String, limit: Int = 10): List<CharacterEntity>

    @Query("SELECT COUNT(*) FROM characters WHERE isLearned = 1 AND type = :type")
    fun getLearnedCount(type: String): LiveData<Int>

    @Query("SELECT COUNT(*) FROM characters WHERE type = :type")
    fun getTotalCount(type: String): LiveData<Int>

    // Quiz queries
    @Query("SELECT * FROM characters WHERE type = :type ORDER BY RANDOM() LIMIT :count")
    suspend fun getRandomCharacters(type: String, count: Int): List<CharacterEntity>

    @Query("""
        SELECT * FROM characters 
        WHERE type = :type AND correctCount < 3 
        ORDER BY RANDOM() 
        LIMIT :count
    """)
    suspend fun getCharactersNeedingPractice(type: String, count: Int): List<CharacterEntity>

    // Statistics queries
    @Query("""
        SELECT AVG(CAST(correctCount AS FLOAT) / (correctCount + incorrectCount)) 
        FROM characters 
        WHERE type = :type AND (correctCount + incorrectCount) > 0
    """)
    suspend fun getAccuracyForType(type: String): Float?

    @Query("""
        SELECT * FROM characters 
        WHERE type = :type AND lastStudied > 0 
        ORDER BY lastStudied DESC 
        LIMIT :limit
    """)
    suspend fun getRecentlyStudiedCharacters(type: String, limit: Int = 5): List<CharacterEntity>

    // CRUD operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(characters: List<CharacterEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(character: CharacterEntity)

    @Update
    suspend fun update(character: CharacterEntity)

    @Delete
    suspend fun delete(character: CharacterEntity)

    @Query("DELETE FROM characters")
    suspend fun deleteAll()

    // Batch update for performance
    @Query("""
        UPDATE characters 
        SET correctCount = correctCount + 1, 
            lastStudied = :timestamp,
            isLearned = (correctCount + 1) >= 3
        WHERE id = :characterId
    """)
    suspend fun incrementCorrectCount(characterId: Int, timestamp: Long)

    @Query("""
        UPDATE characters 
        SET incorrectCount = incorrectCount + 1, 
            lastStudied = :timestamp 
        WHERE id = :characterId
    """)
    suspend fun incrementIncorrectCount(characterId: Int, timestamp: Long)
}