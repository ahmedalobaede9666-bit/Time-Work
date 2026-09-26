package com.ahmedalobaedy.timework.data

import android.content.Context
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

data class WorkRecord(
    val date: LocalDate,
    val startMillis: Long,
    val endMillis: Long,
    val durationMillis: Long
)

class WorkSessionStore(context: Context) {
    private val prefs = context.getSharedPreferences("time_work_store", Context.MODE_PRIVATE)

    val activeStartMillis: Long?
        get() = prefs.getLong(KEY_ACTIVE_START, -1L).takeIf { it > 0L }

    fun startWork(now: Long = System.currentTimeMillis()) {
        if (activeStartMillis == null) {
            prefs.edit().putLong(KEY_ACTIVE_START, now).apply()
        }
    }

    fun finishWork(now: Long = System.currentTimeMillis()): WorkRecord? {
        val start = activeStartMillis ?: return null
        val end = now.coerceAtLeast(start)
        val date = Instant.ofEpochMilli(start).atZone(ZoneId.systemDefault()).toLocalDate()
        val record = WorkRecord(date, start, end, end - start)
        val updated = (records() + record).takeLast(365)
        prefs.edit()
            .remove(KEY_ACTIVE_START)
            .putString(KEY_RECORDS, encode(updated))
            .apply()
        return record
    }

    fun records(): List<WorkRecord> {
        val raw = prefs.getString(KEY_RECORDS, "").orEmpty()
        if (raw.isBlank()) return emptyList()

        return raw.lineSequence().mapNotNull { line ->
            val parts = line.split('|')
            if (parts.size != 4) return@mapNotNull null

            runCatching {
                WorkRecord(
                    date = LocalDate.parse(parts[0]),
                    startMillis = parts[1].toLong(),
                    endMillis = parts[2].toLong(),
                    durationMillis = parts[3].toLong()
                )
            }.getOrNull()
        }.toList()
    }

    fun hourlyRate(): Double =
        prefs.getString(KEY_HOURLY_RATE, "0")?.toDoubleOrNull() ?: 0.0

    fun standardHours(): Double =
        prefs.getString(KEY_STANDARD_HOURS, "8")?.toDoubleOrNull() ?: 8.0

    fun overtimeMultiplier(): Double =
        prefs.getString(KEY_OVERTIME_MULTIPLIER, "1.5")?.toDoubleOrNull() ?: 1.5

    fun currency(): String =
        prefs.getString(KEY_CURRENCY, "IQD").orEmpty().ifBlank { "IQD" }

    fun savePaySettings(
        rate: Double,
        standardHours: Double,
        multiplier: Double,
        currency: String
    ) {
        prefs.edit()
            .putString(KEY_HOURLY_RATE, rate.coerceAtLeast(0.0).toString())
            .putString(KEY_STANDARD_HOURS, standardHours.coerceAtLeast(0.1).toString())
            .putString(KEY_OVERTIME_MULTIPLIER, multiplier.coerceAtLeast(1.0).toString())
            .putString(KEY_CURRENCY, currency.trim().ifBlank { "IQD" })
            .apply()
    }

    fun estimatedPay(durationMillis: Long): Double {
        val totalHours = durationMillis / 3_600_000.0
        val normalHours = minOf(totalHours, standardHours())
        val overtimeHours = (totalHours - standardHours()).coerceAtLeast(0.0)

        return (normalHours * hourlyRate()) +
            (overtimeHours * hourlyRate() * overtimeMultiplier())
    }

    private fun encode(records: List<WorkRecord>): String =
        records.joinToString("\n") {
            "${it.date}|${it.startMillis}|${it.endMillis}|${it.durationMillis}"
        }

    companion object {
        private const val KEY_ACTIVE_START = "active_start"
        private const val KEY_RECORDS = "records"
        private const val KEY_HOURLY_RATE = "hourly_rate"
        private const val KEY_STANDARD_HOURS = "standard_hours"
        private const val KEY_OVERTIME_MULTIPLIER = "overtime_multiplier"
        private const val KEY_CURRENCY = "currency"
    }
}
