package com.peudenhau.prealarm.schedule

import com.peudenhau.prealarm.data.PreTask
import java.time.Duration
import java.time.Instant
import java.time.ZoneId

data class TaskPlan(
    val task: PreTask,
    val plannedStartEpochMs: Long,
    val plannedEndEpochMs: Long,
)

data class SessionPlan(
    val startEpochMs: Long,
    val departureEpochMs: Long,
    val tasks: List<TaskPlan>,
)

object ScheduleCalculator {

    fun buildPlan(
        departureMinutesOfDay: Int,
        tasks: List<PreTask>,
        nowMillis: Long,
        zone: ZoneId = ZoneId.systemDefault(),
    ): SessionPlan {
        val todayStart = Instant.ofEpochMilli(nowMillis).atZone(zone).toLocalDate()
            .atStartOfDay(zone).toInstant().toEpochMilli()
        var departure = todayStart + departureMinutesOfDay * 60_000L
        if (departure <= nowMillis) departure += Duration.ofDays(1).toMillis()
        val start = departure - tasks.sumOf { it.minutes } * 60_000L
        var cursor = start
        val plans = tasks.map { task ->
            val taskStart = cursor
            cursor += task.minutes * 60_000L
            TaskPlan(task, taskStart, cursor)
        }
        return SessionPlan(startEpochMs = start, departureEpochMs = departure, tasks = plans)
    }
}
