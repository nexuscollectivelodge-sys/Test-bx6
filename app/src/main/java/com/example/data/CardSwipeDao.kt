package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CardSwipeDao {
    @Query("SELECT * FROM card_swipes ORDER BY timestamp DESC")
    fun getAllSwipes(): Flow<List<CardSwipeEntity>>

    @Query("SELECT * FROM card_swipes WHERE isFavorite = 1 ORDER BY timestamp DESC")
    fun getFavoriteSwipes(): Flow<List<CardSwipeEntity>>

    @Query("SELECT * FROM card_swipes WHERE cardholderName LIKE '%' || :query || '%' OR primaryAccountNumber LIKE '%' || :query || '%' OR cardBrand LIKE '%' || :query || '%' ORDER BY timestamp DESC")
    fun searchSwipes(query: String): Flow<List<CardSwipeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSwipe(swipe: CardSwipeEntity): Long

    @Update
    suspend fun updateSwipe(swipe: CardSwipeEntity)

    @Delete
    suspend fun deleteSwipe(swipe: CardSwipeEntity)

    @Query("DELETE FROM card_swipes WHERE id = :id")
    suspend fun deleteSwipeById(id: Long)

    @Query("DELETE FROM card_swipes")
    suspend fun deleteAllSwipes()
}
