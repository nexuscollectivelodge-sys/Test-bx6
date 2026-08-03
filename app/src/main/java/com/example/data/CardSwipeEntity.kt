package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "card_swipes")
data class CardSwipeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val cardTitle: String,
    val cardBrand: String,
    val rawTrack1: String,
    val rawTrack2: String,
    val rawTrack3: String,
    val primaryAccountNumber: String,
    val maskedPan: String,
    val cardholderName: String,
    val expiryFormatted: String,
    val serviceCode: String,
    val isFavorite: Boolean = false,
    val notes: String = ""
)
