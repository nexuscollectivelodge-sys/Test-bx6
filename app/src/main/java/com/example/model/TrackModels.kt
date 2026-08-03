package com.example.model

enum class CardBrand(val displayName: String) {
    VISA("Visa"),
    MASTERCARD("Mastercard"),
    AMEX("American Express"),
    DISCOVER("Discover"),
    JCB("JCB"),
    DINERS_CLUB("Diners Club"),
    UNION_PAY("UnionPay"),
    UNKNOWN("Unknown Card")
}

data class ServiceCodeInfo(
    val code: String,
    val interchange: String,
    val authorization: String,
    val pinService: String
)

data class ParsedTrack1(
    val raw: String,
    val formatCode: Char,
    val pan: String,
    val cardholderName: String?,
    val expirationYYMM: String?,
    val serviceCode: String?,
    val discretionaryData: String?,
    val isValid: Boolean,
    val validationError: String? = null
)

data class ParsedTrack2(
    val raw: String,
    val pan: String,
    val expirationYYMM: String?,
    val serviceCode: String?,
    val discretionaryData: String?,
    val isValid: Boolean,
    val validationError: String? = null
)

data class ParsedTrack3(
    val raw: String,
    val accountData: String?,
    val isValid: Boolean,
    val validationError: String? = null
)

data class DecodedCardData(
    val rawTrack1: String = "",
    val rawTrack2: String = "",
    val rawTrack3: String = "",
    val track1: ParsedTrack1? = null,
    val track2: ParsedTrack2? = null,
    val track3: ParsedTrack3? = null,
    val cardBrand: CardBrand = CardBrand.UNKNOWN,
    val primaryAccountNumber: String = "",
    val maskedPan: String = "",
    val cardholderName: String = "",
    val expiryFormatted: String = "",
    val isExpired: Boolean = false,
    val serviceCodeInfo: ServiceCodeInfo? = null,
    val binNumber: String = "",
    val issuerName: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

data class BluetoothDeviceInfo(
    val name: String,
    val address: String,
    val isConnected: Boolean = false,
    val signalDbm: Int = -60,
    val deviceType: String = "X6BT Reader"
)

enum class HardwareMode {
    READ,
    WRITE,
    ERASE
}

data class ConsoleLogEntry(
    val id: Long = System.currentTimeMillis(),
    val isOutgoing: Boolean,
    val tag: String,
    val hexData: String,
    val asciiData: String,
    val timestampFormatted: String
)
