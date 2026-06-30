package com.example.data.local

import androidx.room.*
import com.example.data.models.Challenge
import com.example.data.models.Question
import com.example.data.models.UserStats
import kotlinx.coroutines.flow.Flow

@Dao
interface WisaalDao {

    // --- Challenges ---
    @Query("SELECT * FROM challenges ORDER BY id ASC")
    fun getAllChallenges(): Flow<List<Challenge>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChallenge(challenge: Challenge)

    @Update
    suspend fun updateChallenge(challenge: Challenge)

    @Delete
    suspend fun deleteChallenge(challenge: Challenge)

    @Query("SELECT COUNT(*) FROM challenges WHERE isCompleted = 1")
    fun getCompletedChallengesCountFlow(): Flow<Int>

    // --- Questions ---
    @Query("SELECT * FROM questions ORDER BY id DESC")
    fun getAllQuestions(): Flow<List<Question>>

    @Query("SELECT * FROM questions WHERE category = :category ORDER BY id DESC")
    fun getQuestionsByCategory(category: String): Flow<List<Question>>

    @Query("SELECT * FROM questions WHERE isFavorite = 1 ORDER BY id DESC")
    fun getFavoriteQuestions(): Flow<List<Question>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestion(question: Question)

    @Update
    suspend fun updateQuestion(question: Question)

    @Delete
    suspend fun deleteQuestion(question: Question)

    @Query("SELECT COUNT(*) FROM questions WHERE isDiscussed = 1")
    fun getDiscussedQuestionsCountFlow(): Flow<Int>

    // --- User Stats ---
    @Query("SELECT * FROM user_stats WHERE id = 1 LIMIT 1")
    fun getUserStatsFlow(): Flow<UserStats?>

    @Query("SELECT * FROM user_stats WHERE id = 1 LIMIT 1")
    suspend fun getUserStats(): UserStats?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserStats(userStats: UserStats)

    @Update
    suspend fun updateUserStats(userStats: UserStats)
}
