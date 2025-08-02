// data/remote/ApiService.kt
package com.example.hiraganakatakana.data.remote

import com.example.hiraganakatakana.data.database.CharacterEntity
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    @GET("characters")
    suspend fun getCharacters(): List<CharacterEntity>

    @GET("characters/{type}")
    suspend fun getCharactersByType(@Path("type") type: String): List<CharacterEntity>

    @GET("characters/audio/{id}")
    suspend fun getCharacterAudio(@Path("id") characterId: Int): String

    @GET("progress/sync")
    suspend fun syncProgress(
        @Query("user_id") userId: String,
        @Query("progress") progress: String
    ): ApiResponse<String>
}

data class ApiResponse<T>(
    val success: Boolean,
    val data: T?,
    val message: String?
)