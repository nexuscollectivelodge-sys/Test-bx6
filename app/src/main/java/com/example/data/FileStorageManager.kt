package com.example.data

import android.content.Context
import com.example.decoder.TrackDecoder
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object FileStorageManager {

    private const val STORAGE_DIR_NAME = "easymsr_database_files"

    fun getStorageDir(context: Context): File {
        val dir = File(context.filesDir, STORAGE_DIR_NAME)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    /**
     * Write raw string content to a file in app local storage
     */
    fun writeToFile(context: Context, fileName: String, content: String): File {
        val dir = getStorageDir(context)
        val file = File(dir, fileName)
        file.writeText(content, Charsets.UTF_8)
        return file
    }

    /**
     * Read string content from a file
     */
    fun readFromFile(file: File): String {
        return if (file.exists()) file.readText(Charsets.UTF_8) else ""
    }

    /**
     * Copy an existing file with a new destination file name
     */
    fun copyFile(context: Context, sourceFile: File, newFileName: String): File {
        val dir = getStorageDir(context)
        val destFile = File(dir, newFileName)
        sourceFile.copyTo(destFile, overwrite = true)
        return destFile
    }

    /**
     * Erase/Delete a file from local storage
     */
    fun eraseFile(file: File): Boolean {
        return if (file.exists()) file.delete() else false
    }

    /**
     * List all database/backup files stored in the app directory
     */
    fun listSavedFiles(context: Context): List<File> {
        val dir = getStorageDir(context)
        return dir.listFiles()?.sortedByDescending { it.lastModified() } ?: emptyList()
    }

    /**
     * Export card swipes list to CSV format
     */
    fun exportToCsv(context: Context, swipes: List<CardSwipeEntity>, customFileName: String = ""): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val name = customFileName.ifEmpty { "easymsr_export_$timeStamp.csv" }
        val sb = StringBuilder()

        // CSV Header
        sb.append("ID,Timestamp,Date,Title,Brand,Masked_PAN,PAN,Cardholder_Name,Expiry,Service_Code,Favorite,Track1,Track2,Track3,Notes\n")

        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
        for (swipe in swipes) {
            val formattedDate = dateFormat.format(Date(swipe.timestamp))
            sb.append("${swipe.id},")
            sb.append("${swipe.timestamp},")
            sb.append("\"$formattedDate\",")
            sb.append("\"${escapeCsv(swipe.cardTitle)}\",")
            sb.append("\"${escapeCsv(swipe.cardBrand)}\",")
            sb.append("\"${escapeCsv(swipe.maskedPan)}\",")
            sb.append("\"${escapeCsv(swipe.primaryAccountNumber)}\",")
            sb.append("\"${escapeCsv(swipe.cardholderName)}\",")
            sb.append("\"${escapeCsv(swipe.expiryFormatted)}\",")
            sb.append("\"${escapeCsv(swipe.serviceCode)}\",")
            sb.append("${swipe.isFavorite},")
            sb.append("\"${escapeCsv(swipe.rawTrack1)}\",")
            sb.append("\"${escapeCsv(swipe.rawTrack2)}\",")
            sb.append("\"${escapeCsv(swipe.rawTrack3)}\",")
            sb.append("\"${escapeCsv(swipe.notes)}\"\n")
        }

        return writeToFile(context, name, sb.toString())
    }

    /**
     * Export card swipes database to JSON format
     */
    fun exportToJson(context: Context, swipes: List<CardSwipeEntity>, customFileName: String = ""): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val name = customFileName.ifEmpty { "easymsr_backup_$timeStamp.json" }

        val jsonArray = JSONArray()
        for (swipe in swipes) {
            val obj = JSONObject().apply {
                put("id", swipe.id)
                put("timestamp", swipe.timestamp)
                put("cardTitle", swipe.cardTitle)
                put("cardBrand", swipe.cardBrand)
                put("rawTrack1", swipe.rawTrack1)
                put("rawTrack2", swipe.rawTrack2)
                put("rawTrack3", swipe.rawTrack3)
                put("primaryAccountNumber", swipe.primaryAccountNumber)
                put("maskedPan", swipe.maskedPan)
                put("cardholderName", swipe.cardholderName)
                put("expiryFormatted", swipe.expiryFormatted)
                put("serviceCode", swipe.serviceCode)
                put("isFavorite", swipe.isFavorite)
                put("notes", swipe.notes)
            }
            jsonArray.put(obj)
        }

        return writeToFile(context, name, jsonArray.toString(2))
    }

    /**
     * Import database records from JSON or CSV string content
     */
    fun importFromJsonOrCsv(fileContent: String): List<CardSwipeEntity> {
        val imported = mutableListOf<CardSwipeEntity>()
        val trimmed = fileContent.trim()

        if (trimmed.startsWith("[") || trimmed.startsWith("{")) {
            // Parse JSON
            try {
                val array = if (trimmed.startsWith("[")) JSONArray(trimmed) else JSONArray().put(JSONObject(trimmed))
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    val t1 = obj.optString("rawTrack1", "")
                    val t2 = obj.optString("rawTrack2", "")
                    val t3 = obj.optString("rawTrack3", "")
                    val decoded = TrackDecoder.decodeAllTracks(t1, t2, t3)

                    val entity = CardSwipeEntity(
                        timestamp = obj.optLong("timestamp", System.currentTimeMillis()),
                        cardTitle = obj.optString("cardTitle", decoded.cardBrand.displayName),
                        cardBrand = obj.optString("cardBrand", decoded.cardBrand.displayName),
                        rawTrack1 = t1,
                        rawTrack2 = t2,
                        rawTrack3 = t3,
                        primaryAccountNumber = obj.optString("primaryAccountNumber", decoded.primaryAccountNumber),
                        maskedPan = obj.optString("maskedPan", decoded.maskedPan),
                        cardholderName = obj.optString("cardholderName", decoded.cardholderName),
                        expiryFormatted = obj.optString("expiryFormatted", decoded.expiryFormatted),
                        serviceCode = obj.optString("serviceCode", decoded.serviceCodeInfo?.code ?: "101"),
                        isFavorite = obj.optBoolean("isFavorite", false),
                        notes = obj.optString("notes", "")
                    )
                    imported.add(entity)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            // Parse CSV
            val lines = trimmed.lines()
            if (lines.size > 1) {
                for (line in lines.drop(1)) {
                    if (line.isBlank()) continue
                    val parts = parseCsvLine(line)
                    if (parts.size >= 10) {
                        val t1 = if (parts.size > 11) unescapeCsv(parts[11]) else ""
                        val t2 = if (parts.size > 12) unescapeCsv(parts[12]) else ""
                        val t3 = if (parts.size > 13) unescapeCsv(parts[13]) else ""
                        val decoded = TrackDecoder.decodeAllTracks(t1, t2, t3)

                        val entity = CardSwipeEntity(
                            cardTitle = unescapeCsv(parts.getOrElse(3) { "Imported Card" }),
                            cardBrand = unescapeCsv(parts.getOrElse(4) { decoded.cardBrand.displayName }),
                            rawTrack1 = t1,
                            rawTrack2 = t2,
                            rawTrack3 = t3,
                            primaryAccountNumber = unescapeCsv(parts.getOrElse(6) { decoded.primaryAccountNumber }),
                            maskedPan = unescapeCsv(parts.getOrElse(5) { decoded.maskedPan }),
                            cardholderName = unescapeCsv(parts.getOrElse(7) { decoded.cardholderName }),
                            expiryFormatted = unescapeCsv(parts.getOrElse(8) { decoded.expiryFormatted }),
                            serviceCode = unescapeCsv(parts.getOrElse(9) { "101" }),
                            isFavorite = parts.getOrElse(10) { "false" }.toBoolean(),
                            notes = if (parts.size > 14) unescapeCsv(parts[14]) else ""
                        )
                        imported.add(entity)
                    }
                }
            }
        }

        return imported
    }

    private fun escapeCsv(value: String): String {
        return value.replace("\"", "\"\"")
    }

    private fun unescapeCsv(value: String): String {
        var res = value.trim()
        if (res.startsWith("\"") && res.endsWith("\"") && res.length >= 2) {
            res = res.substring(1, res.length - 1)
        }
        return res.replace("\"\"", "\"")
    }

    private fun parseCsvLine(line: String): List<String> {
        val result = mutableListOf<String>()
        val sb = StringBuilder()
        var inQuotes = false

        for (ch in line) {
            if (ch == '\"') {
                inQuotes = !inQuotes
            } else if (ch == ',' && !inQuotes) {
                result.add(sb.toString())
                sb.clear()
            } else {
                sb.append(ch)
            }
        }
        result.add(sb.toString())
        return result
    }
}
