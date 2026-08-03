package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.bluetooth.X6BTDeviceManager
import com.example.data.AppDatabase
import com.example.data.CardSwipeEntity
import com.example.data.CardSwipeRepository
import com.example.data.FileStorageManager
import com.example.decoder.TrackDecoder
import com.example.model.DecodedCardData
import com.example.model.HardwareMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

enum class BrandFilter { ALL, VISA, MASTERCARD, AMEX, DISCOVER, OTHER }
enum class TrackPresenceFilter { ALL, TRACK1_ONLY, TRACK2_ONLY, BOTH_PRESENT }
enum class ValidityFilter { ALL, VALID_ONLY, FLAGGED_ONLY }

class EasyMSRViewModel(application: Application) : AndroidViewModel(application) {

    val deviceManager = X6BTDeviceManager()
    private val repository: CardSwipeRepository

    private val _decodedCardData = MutableStateFlow(
        TrackDecoder.decodeAllTracks(
            raw1 = "",
            raw2 = "",
            raw3 = null
        )
    )
    val decodedCardData: StateFlow<DecodedCardData> = _decodedCardData.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _brandFilter = MutableStateFlow(BrandFilter.ALL)
    val brandFilter: StateFlow<BrandFilter> = _brandFilter.asStateFlow()

    private val _trackFilter = MutableStateFlow(TrackPresenceFilter.ALL)
    val trackFilter: StateFlow<TrackPresenceFilter> = _trackFilter.asStateFlow()

    private val _validityFilter = MutableStateFlow(ValidityFilter.ALL)
    val validityFilter: StateFlow<ValidityFilter> = _validityFilter.asStateFlow()

    private val _favoritesOnly = MutableStateFlow(false)
    val favoritesOnly: StateFlow<Boolean> = _favoritesOnly.asStateFlow()

    val savedSwipes: StateFlow<List<CardSwipeEntity>>

    private val _savedFilesList = MutableStateFlow<List<File>>(emptyList())
    val savedFilesList: StateFlow<List<File>> = _savedFilesList.asStateFlow()

    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    init {
        val dao = AppDatabase.getInstance(application).cardSwipeDao()
        repository = CardSwipeRepository(dao)

        val rawSwipesFlow = _searchQuery.flatMapLatest { query ->
            if (query.isBlank()) {
                repository.allSwipes
            } else {
                repository.searchSwipes(query)
            }
        }

        savedSwipes = combine(
            rawSwipesFlow,
            _brandFilter,
            _trackFilter,
            _validityFilter,
            _favoritesOnly
        ) { swipes, brand, track, validity, favOnly ->
            swipes.filter { entity ->
                // Brand filter
                val matchesBrand = when (brand) {
                    BrandFilter.ALL -> true
                    BrandFilter.VISA -> entity.cardBrand.equals("Visa", ignoreCase = true)
                    BrandFilter.MASTERCARD -> entity.cardBrand.equals("Mastercard", ignoreCase = true)
                    BrandFilter.AMEX -> entity.cardBrand.contains("Amex", ignoreCase = true) || entity.cardBrand.contains("American Express", ignoreCase = true)
                    BrandFilter.DISCOVER -> entity.cardBrand.equals("Discover", ignoreCase = true)
                    BrandFilter.OTHER -> !listOf("Visa", "Mastercard", "American Express", "Discover").any { entity.cardBrand.contains(it, ignoreCase = true) }
                }

                // Track filter
                val matchesTrack = when (track) {
                    TrackPresenceFilter.ALL -> true
                    TrackPresenceFilter.TRACK1_ONLY -> entity.rawTrack1.isNotEmpty() && entity.rawTrack2.isEmpty()
                    TrackPresenceFilter.TRACK2_ONLY -> entity.rawTrack2.isNotEmpty() && entity.rawTrack1.isEmpty()
                    TrackPresenceFilter.BOTH_PRESENT -> entity.rawTrack1.isNotEmpty() && entity.rawTrack2.isNotEmpty()
                }

                // Validity filter
                val matchesValidity = when (validity) {
                    ValidityFilter.ALL -> true
                    ValidityFilter.VALID_ONLY -> entity.primaryAccountNumber.length >= 12
                    ValidityFilter.FLAGGED_ONLY -> entity.primaryAccountNumber.length < 12
                }

                // Favorites filter
                val matchesFav = if (favOnly) entity.isFavorite else true

                matchesBrand && matchesTrack && matchesValidity && matchesFav
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        // Real-time hardware stream collection
        viewModelScope.launch {
            deviceManager.incomingSwipeStream.collect { (t1, t2, t3) ->
                onProcessRawInput(t1, t2, t3)
            }
        }

        refreshSavedFilesList()
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun setBrandFilter(filter: BrandFilter) {
        _brandFilter.value = filter
    }

    fun setTrackFilter(filter: TrackPresenceFilter) {
        _trackFilter.value = filter
    }

    fun setValidityFilter(filter: ValidityFilter) {
        _validityFilter.value = filter
    }

    fun toggleFavoritesOnly() {
        _favoritesOnly.value = !_favoritesOnly.value
    }

    fun onProcessRawInput(raw1: String?, raw2: String?, raw3: String?) {
        val decoded = TrackDecoder.decodeAllTracks(raw1, raw2, raw3)
        _decodedCardData.value = decoded
        deviceManager.triggerBeeperAndLed()
        deviceManager.logConsole(
            isOutgoing = false,
            tag = "REALTIME_SWIPE",
            hex = "DEC",
            ascii = "Instant Decoded: ${decoded.cardBrand.displayName} - PAN: ${decoded.maskedPan}"
        )
        showNotification("Real-time decoded: ${decoded.cardBrand.displayName} (${decoded.maskedPan.takeLast(8)})")
    }

    fun simulatePresetSwipe(presetIndex: Int) {
        when (presetIndex) {
            0 -> onProcessRawInput(
                "%B4532015029318491^SMITH/JANE A^2811101000000000?",
                ";4532015029318491=2811101000000000?",
                null
            )
            1 -> onProcessRawInput(
                "%B5412751234567890^JOHNSON/ROBERT^2709201000000000?",
                ";5412751234567890=2709201000000000?",
                null
            )
            2 -> onProcessRawInput(
                "%B378282246810005^ALVAREZ/MARIA L^2905101000000000?",
                ";378282246810005=2905101000000000?",
                null
            )
            3 -> onProcessRawInput(
                "%B6011000987654321^WILSON/DAVID^2612101000000000?",
                ";6011000987654321=2612101000000000?",
                null
            )
            else -> onProcessRawInput(
                "%B4000123456789010^TEST/USER^2912101000000000?",
                ";4000123456789010=2912101000000000?",
                ";9912345678901234567890?"
            )
        }
    }

    fun saveCurrentSwipe(title: String = "") {
        val current = _decodedCardData.value
        if (current.primaryAccountNumber.isEmpty() && current.rawTrack1.isEmpty() && current.rawTrack2.isEmpty()) {
            showNotification("No swipe data to save")
            return
        }

        val cardTitleFinal = title.ifEmpty { "${current.cardBrand.displayName} (${current.maskedPan.takeLast(9)})" }

        viewModelScope.launch {
            val entity = CardSwipeEntity(
                cardTitle = cardTitleFinal,
                cardBrand = current.cardBrand.displayName,
                rawTrack1 = current.rawTrack1,
                rawTrack2 = current.rawTrack2,
                rawTrack3 = current.rawTrack3,
                primaryAccountNumber = current.primaryAccountNumber,
                maskedPan = current.maskedPan,
                cardholderName = current.cardholderName,
                expiryFormatted = current.expiryFormatted,
                serviceCode = current.serviceCodeInfo?.code ?: "101",
                isFavorite = false
            )
            repository.insertSwipe(entity)
            showNotification("Saved swipe record to database")
        }
    }

    fun toggleFavorite(entity: CardSwipeEntity) {
        viewModelScope.launch {
            repository.updateSwipe(entity.copy(isFavorite = !entity.isFavorite))
        }
    }

    fun deleteSwipe(entity: CardSwipeEntity) {
        viewModelScope.launch {
            repository.deleteSwipe(entity)
            showNotification("Deleted swipe record")
        }
    }

    fun writeTracksToDevice(pan: String, name: String, expiryYYMM: String, serviceCode: String) {
        val t1 = TrackDecoder.buildTrack1(pan, name, expiryYYMM, serviceCode)
        val t2 = TrackDecoder.buildTrack2(pan, expiryYYMM, serviceCode)

        deviceManager.setHardwareMode(HardwareMode.WRITE)
        deviceManager.sendRawCommand("1B 77", "WRITE T1: $t1 | T2: $t2")
        deviceManager.triggerBeeperAndLed()

        onProcessRawInput(t1, t2, null)
        showNotification("Track write command executed on X6BT")
    }

    fun eraseCard() {
        deviceManager.setHardwareMode(HardwareMode.ERASE)
        deviceManager.sendRawCommand("1B 65", "ERASE ALL TRACKS COMMAND")
        deviceManager.triggerBeeperAndLed()
        showNotification("X6BT Card Erase Command Sent")
    }

    // --- FILE STORAGE DATABASE OPERATIONS (Write to File, Read from File, Copy File, Erase File) ---

    fun refreshSavedFilesList() {
        _savedFilesList.value = FileStorageManager.listSavedFiles(getApplication())
    }

    fun exportToCsvFile() {
        val currentSwipes = savedSwipes.value
        val file = FileStorageManager.exportToCsv(getApplication(), currentSwipes)
        refreshSavedFilesList()
        showNotification("Exported ${currentSwipes.size} records to CSV: ${file.name}")
    }

    fun writeDatabaseToFile(customFileName: String = "") {
        val currentSwipes = savedSwipes.value
        val file = FileStorageManager.exportToJson(getApplication(), currentSwipes, customFileName)
        refreshSavedFilesList()
        showNotification("Wrote database (${currentSwipes.size} records) to file: ${file.name}")
    }

    fun readDatabaseFromFile(file: File) {
        viewModelScope.launch {
            val content = FileStorageManager.readFromFile(file)
            val imported = FileStorageManager.importFromJsonOrCsv(content)
            if (imported.isNotEmpty()) {
                for (item in imported) {
                    repository.insertSwipe(item)
                }
                showNotification("Read & restored ${imported.size} records from ${file.name}")
            } else {
                showNotification("No valid card records found in ${file.name}")
            }
        }
    }

    fun copySavedFile(file: File, newName: String) {
        val copied = FileStorageManager.copyFile(getApplication(), file, newName)
        refreshSavedFilesList()
        showNotification("Copied file to ${copied.name}")
    }

    fun eraseSavedFile(file: File) {
        val success = FileStorageManager.eraseFile(file)
        refreshSavedFilesList()
        if (success) {
            showNotification("Erased file: ${file.name}")
        } else {
            showNotification("Failed to erase file")
        }
    }

    fun clearUserMessage() {
        _userMessage.value = null
    }

    private fun showNotification(msg: String) {
        _userMessage.value = msg
    }
}
