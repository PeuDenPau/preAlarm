package com.peudenhau.prealarm.data

import kotlinx.serialization.Serializable

@Serializable
data class PreTask(
    val id: Long,
    val name: String,
    val minutes: Int,
)

@Serializable
data class Routine(
    val id: Long,
    val name: String,
    val departureMinutes: Int,
    val tasks: List<PreTask> = emptyList(),
) {
    val totalMinutes: Int get() = tasks.sumOf { it.minutes }
}
