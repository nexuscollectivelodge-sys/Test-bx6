package com.example.decoder

import com.example.model.CardBrand
import com.example.model.DecodedCardData
import com.example.model.ParsedTrack1
import com.example.model.ParsedTrack2
import com.example.model.ParsedTrack3
import com.example.model.ServiceCodeInfo
import java.util.Calendar

object TrackDecoder {

    fun decodeAllTracks(raw1: String?, raw2: String?, raw3: String?): DecodedCardData {
        val clean1 = raw1?.trim() ?: ""
        val clean2 = raw2?.trim() ?: ""
        val clean3 = raw3?.trim() ?: ""

        val parsed1 = parseTrack1(clean1)
        val parsed2 = parseTrack2(clean2)
        val parsed3 = parseTrack3(clean3)

        // Determine Primary Account Number (PAN) prioritize Track 2 or Track 1
        val pan = parsed2?.pan?.ifEmpty { null }
            ?: parsed1?.pan?.ifEmpty { null }
            ?: ""

        val brand = detectCardBrand(pan)
        val masked = maskPan(pan)

        // Cardholder Name
        val name = parsed1?.cardholderName
            ?.replace('/', ' ')
            ?.trim()
            ?.ifEmpty { "" }
            ?: ""

        // Expiration Date (YYMM)
        val expiryYYMM = parsed2?.expirationYYMM
            ?: parsed1?.expirationYYMM
            ?: ""

        val expiryFormatted = formatExpiry(expiryYYMM)
        val expired = isDateExpired(expiryYYMM)

        // Service Code
        val rawServiceCode = parsed2?.serviceCode
            ?: parsed1?.serviceCode
            ?: ""
        val serviceInfo = parseServiceCode(rawServiceCode)

        val bin = if (pan.length >= 6) pan.substring(0, 6) else ""
        val issuer = getIssuerName(bin, brand)

        return DecodedCardData(
            rawTrack1 = clean1,
            rawTrack2 = clean2,
            rawTrack3 = clean3,
            track1 = parsed1,
            track2 = parsed2,
            track3 = parsed3,
            cardBrand = brand,
            primaryAccountNumber = pan,
            maskedPan = masked,
            cardholderName = name,
            expiryFormatted = expiryFormatted,
            isExpired = expired,
            serviceCodeInfo = serviceInfo,
            binNumber = bin,
            issuerName = issuer,
            timestamp = System.currentTimeMillis()
        )
    }

    fun parseTrack1(raw: String): ParsedTrack1? {
        if (raw.isBlank()) return null
        var content = raw
        // Remove start/end sentinels if present
        if (content.startsWith("%")) content = content.substring(1)
        if (content.endsWith("?")) content = content.substring(0, content.length - 1)

        if (content.isEmpty()) {
            return ParsedTrack1(raw, ' ', "", null, null, null, null, false, "Empty Track 1")
        }

        val formatCode = content[0]
        val body = content.substring(1)
        val parts = body.split("^")

        val pan = parts.getOrNull(0)?.filter { it.isDigit() } ?: ""
        val rawName = parts.getOrNull(1)?.trim()
        val nameFormatted = rawName?.let {
            if (it.contains("/")) {
                val nameParts = it.split("/")
                val last = nameParts.getOrNull(0)?.trim() ?: ""
                val first = nameParts.getOrNull(1)?.trim() ?: ""
                if (first.isNotEmpty()) "$first $last" else last
            } else {
                it
            }
        }

        val remaining = parts.getOrNull(2) ?: ""
        val expiry = if (remaining.length >= 4) remaining.substring(0, 4) else null
        val serviceCode = if (remaining.length >= 7) remaining.substring(4, 7) else null
        val discretionary = if (remaining.length > 7) remaining.substring(7) else null

        val isValid = pan.isNotEmpty() && pan.length >= 12 && expiry != null
        val error = if (!isValid) "Invalid Track 1 format or missing PAN/expiry" else null

        return ParsedTrack1(
            raw = raw,
            formatCode = formatCode,
            pan = pan,
            cardholderName = nameFormatted ?: rawName,
            expirationYYMM = expiry,
            serviceCode = serviceCode,
            discretionaryData = discretionary,
            isValid = isValid,
            validationError = error
        )
    }

    fun parseTrack2(raw: String): ParsedTrack2? {
        if (raw.isBlank()) return null
        var content = raw
        if (content.startsWith(";")) content = content.substring(1)
        if (content.endsWith("?")) content = content.substring(0, content.length - 1)

        if (content.isEmpty()) {
            return ParsedTrack2(raw, "", null, null, null, false, "Empty Track 2")
        }

        val delimiterIndex = content.indexOf('=').takeIf { it != -1 }
            ?: content.indexOf('D').takeIf { it != -1 }
            ?: -1

        if (delimiterIndex == -1) {
            val digitsOnly = content.filter { it.isDigit() }
            return ParsedTrack2(raw, digitsOnly, null, null, null, false, "Missing separator (=)")
        }

        val pan = content.substring(0, delimiterIndex).filter { it.isDigit() }
        val rest = content.substring(delimiterIndex + 1)

        val expiry = if (rest.length >= 4) rest.substring(0, 4) else null
        val serviceCode = if (rest.length >= 7) rest.substring(4, 7) else null
        val discretionary = if (rest.length > 7) rest.substring(7) else null

        val isValid = pan.isNotEmpty() && pan.length >= 12 && expiry != null
        val error = if (!isValid) "Invalid Track 2 structure" else null

        return ParsedTrack2(
            raw = raw,
            pan = pan,
            expirationYYMM = expiry,
            serviceCode = serviceCode,
            discretionaryData = discretionary,
            isValid = isValid,
            validationError = error
        )
    }

    fun parseTrack3(raw: String): ParsedTrack3? {
        if (raw.isBlank()) return null
        var content = raw
        if (content.startsWith(";") || content.startsWith("%") || content.startsWith("+")) {
            content = content.substring(1)
        }
        if (content.endsWith("?")) content = content.substring(0, content.length - 1)

        return ParsedTrack3(
            raw = raw,
            accountData = content.ifEmpty { null },
            isValid = content.isNotEmpty()
        )
    }

    fun detectCardBrand(pan: String): CardBrand {
        if (pan.isEmpty()) return CardBrand.UNKNOWN
        return when {
            pan.startsWith("4") -> CardBrand.VISA
            pan.matches(Regex("^(5[1-5]|222[1-9]|22[3-9][0-9]|2[3-6][0-9]{2}|27[0-1][0-9]|2720).*")) -> CardBrand.MASTERCARD
            pan.matches(Regex("^(34|37).*")) -> CardBrand.AMEX
            pan.matches(Regex("^(6011|65|64[4-9]).*")) -> CardBrand.DISCOVER
            pan.matches(Regex("^(352[8-9]|35[3-8][0-9]).*")) -> CardBrand.JCB
            pan.matches(Regex("^(30[0-5]|36|38).*")) -> CardBrand.DINERS_CLUB
            pan.startsWith("62") -> CardBrand.UNION_PAY
            else -> CardBrand.UNKNOWN
        }
    }

    fun maskPan(pan: String): String {
        if (pan.length < 8) return pan
        val firstFour = pan.take(4)
        val lastFour = pan.takeLast(4)
        val maskedMiddle = "*".repeat(pan.length - 8)
        val full = "$firstFour$maskedMiddle$lastFour"
        return full.chunked(4).joinToString(" ")
    }

    fun formatExpiry(yymm: String): String {
        if (yymm.length != 4) return "N/A"
        val yy = yymm.substring(0, 2)
        val mm = yymm.substring(2, 4)
        return "$mm/20$yy"
    }

    fun isDateExpired(yymm: String): Boolean {
        if (yymm.length != 4) return false
        val yy = yymm.substring(0, 2).toIntOrNull() ?: return false
        val mm = yymm.substring(2, 4).toIntOrNull() ?: return false

        val cal = Calendar.getInstance()
        val currentYear = cal.get(Calendar.YEAR) % 100
        val currentMonth = cal.get(Calendar.MONTH) + 1 // 1-based

        return if (yy < currentYear) {
            true
        } else if (yy == currentYear) {
            mm < currentMonth
        } else {
            false
        }
    }

    fun parseServiceCode(code: String): ServiceCodeInfo? {
        if (code.length != 3) return null
        val c1 = code[0]
        val c2 = code[1]
        val c3 = code[2]

        val interchange = when (c1) {
            '1' -> "1 - International Interchange Allowed"
            '2' -> "2 - International Interchange (IC Card)"
            '5' -> "5 - National Interchange Only"
            '6' -> "6 - National Interchange (IC Card)"
            '7' -> "7 - Private / Closed Loop"
            '9' -> "9 - Test Card"
            else -> "$c1 - Standard Interchange"
        }

        val auth = when (c2) {
            '0' -> "0 - Normal Processing"
            '2' -> "2 - Contact Issuer via Online System"
            '4' -> "4 - Online Authorization Required"
            else -> "$c2 - Special Processing"
        }

        val pin = when (c3) {
            '0' -> "0 - No PIN Restrictions / Normal"
            '1' -> "1 - ATM Only, PIN Required"
            '2' -> "2 - Prompt for PIN If Allowed"
            '3' -> "3 - ATM Only"
            '5' -> "5 - PIN Required If Terminal Capable"
            '6' -> "6 - Prompt PIN Always"
            else -> "$c3 - Custom PIN Logic"
        }

        return ServiceCodeInfo(
            code = code,
            interchange = interchange,
            authorization = auth,
            pinService = pin
        )
    }

    fun getIssuerName(bin: String, brand: CardBrand): String {
        return brand.displayName
    }

    /**
     * Build ISO 7811 Track 1 String
     */
    fun buildTrack1(pan: String, name: String, expiryYYMM: String, serviceCode: String = "101"): String {
        val cleanPan = pan.filter { it.isDigit() }
        val cleanName = name.trim().replace(' ', '/').uppercase()
        val cleanExpiry = expiryYYMM.filter { it.isDigit() }.padStart(4, '0')
        val cleanSc = serviceCode.filter { it.isDigit() }.padStart(3, '0')
        return "%B$cleanPan^$cleanName^$cleanExpiry$cleanSc?"
    }

    /**
     * Build ISO 7811 Track 2 String
     */
    fun buildTrack2(pan: String, expiryYYMM: String, serviceCode: String = "101"): String {
        val cleanPan = pan.filter { it.isDigit() }
        val cleanExpiry = expiryYYMM.filter { it.isDigit() }.padStart(4, '0')
        val cleanSc = serviceCode.filter { it.isDigit() }.padStart(3, '0')
        return ";$cleanPan=$cleanExpiry$cleanSc?"
    }
}
