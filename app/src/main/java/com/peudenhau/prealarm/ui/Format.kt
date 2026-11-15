package com.peudenhau.prealarm.ui

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val clockFormatter = DateTimeFormatter.ofPattern("HH:mm").withZone(ZoneId.systemDefault())

fun formatClock(epochMs: Long): String = clockFormatter.format(Instant.ofEpochMilli(epochMs))

fun formatMinutesOfDay(minutesOfDay: Int): String =
    String.format(Locale.US, "%02d:%02d", minutesOfDay / 60, minutesOfDay % 60)

fun formatDuration(ms: Long): String {
    val totalSeconds = Math.abs(ms) / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) {
        String.format(Locale.US, "%d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format(Locale.US, "%d:%02d", minutes, seconds)
    }
}

fun formatMinutesShort(ms: Long): String {
    val minutes = Math.round(Math.abs(ms) / 60_000.0).toInt()
    return if (minutes >= 60) {
        val h = minutes / 60
        val m = minutes % 60
        if (m == 0) "${h}h" else "${h}h ${m}min"
    } else {
        "${minutes}min"
    }
}
