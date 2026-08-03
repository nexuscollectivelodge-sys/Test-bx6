package com.example.data

import kotlinx.coroutines.flow.Flow

class CardSwipeRepository(private val dao: CardSwipeDao) {

    val allSwipes: Flow<List<CardSwipeEntity>> = dao.getAllSwipes()
    val favoriteSwipes: Flow<List<CardSwipeEntity>> = dao.getFavoriteSwipes()

    fun searchSwipes(query: String): Flow<List<CardSwipeEntity>> {
        return dao.searchSwipes(query)
    }

    suspend fun insertSwipe(swipe: CardSwipeEntity): Long {
        return dao.insertSwipe(swipe)
    }

    suspend fun updateSwipe(swipe: CardSwipeEntity) {
        dao.updateSwipe(swipe)
    }

    suspend fun deleteSwipe(swipe: CardSwipeEntity) {
        dao.deleteSwipe(swipe)
    }

    suspend fun deleteSwipeById(id: Long) {
        dao.deleteSwipeById(id)
    }

    suspend fun deleteAllSwipes() {
        dao.deleteAllSwipes()
    }
}
