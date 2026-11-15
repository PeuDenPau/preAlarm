package com.peudenhau.prealarm.schedule

import com.peudenhau.prealarm.data.PreTask
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.ZoneId

class ScheduleCalculatorTest {

    private val zone = ZoneId.of("UTC")
    private val now = java.time.LocalDateTime.of(2025, 1, 15, 10, 0)
        .atZone(zone).toInstant().toEpochMilli()

    private val tasks = listOf(
        PreTask(1, "Ducha", 15),
        PreTask(2, "Vestirse", 10),
        PreTask(3, "Desayunar", 20),
    )

    @Test
    fun `plan anchors to departure time`() {
        val plan = ScheduleCalculator.buildPlan(14 * 60, tasks, now, zone)
        val departure = java.time.LocalDateTime.of(2025, 1, 15, 14, 0)
            .atZone(zone).toInstant().toEpochMilli()
        assertEquals(departure, plan.departureEpochMs)
        assertEquals(45 * 60_000L, plan.departureEpochMs - plan.startEpochMs)
    }

    @Test
    fun `tasks are chained in order`() {
        val plan = ScheduleCalculator.buildPlan(14 * 60, tasks, now, zone)
        assertEquals(plan.tasks[0].plannedEndEpochMs, plan.tasks[1].plannedStartEpochMs)
        assertEquals(plan.tasks[1].plannedEndEpochMs, plan.tasks[2].plannedStartEpochMs)
        assertEquals(plan.departureEpochMs, plan.tasks.last().plannedEndEpochMs)
    }

    @Test
    fun `departure in the past moves to tomorrow`() {
        val plan = ScheduleCalculator.buildPlan(8 * 60, tasks, now, zone)
        val tomorrow8 = java.time.LocalDateTime.of(2025, 1, 16, 8, 0)
            .atZone(zone).toInstant().toEpochMilli()
        assertEquals(tomorrow8, plan.departureEpochMs)
    }

    @Test
    fun `start can fall before midnight`() {
        val now2330 = java.time.LocalDateTime.of(2025, 1, 15, 23, 30)
            .atZone(zone).toInstant().toEpochMilli()
        val plan = ScheduleCalculator.buildPlan(0 * 60 + 15, tasks, now2330, zone)
        val departure = java.time.LocalDateTime.of(2025, 1, 16, 0, 15)
            .atZone(zone).toInstant().toEpochMilli()
        assertEquals(departure, plan.departureEpochMs)
        assertEquals(departure - 45 * 60_000L, plan.startEpochMs)
    }
}
