package com.peudenhau.prealarm.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.peudenhau.prealarm.R
import com.peudenhau.prealarm.schedule.TaskPlan
import com.peudenhau.prealarm.ui.formatClock
import com.peudenhau.prealarm.ui.formatDuration
import com.peudenhau.prealarm.ui.formatMinutesShort

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionScreen(
    session: SessionState,
    now: Long,
    onMarkDone: () -> Unit,
    onStop: () -> Unit,
) {
    var showStopDialog by remember { mutableStateOf(false) }
    BackHandler { showStopDialog = true }

    val plan = session.plan

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(session.routine.name) },
                navigationIcon = {
                    IconButton(onClick = { showStopDialog = true }) {
                        Icon(Icons.Default.Close, contentDescription = null)
                    }
                },
            )
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            CuteBackground()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            DepartureHeader(session, now)
            if (session.allDone) {
                AllDoneCard(session, now)
            } else {
                CurrentTaskCard(session, now, onMarkDone)
            }
            TaskTimeline(session, now)
            Spacer(Modifier.height(24.dp))
        }
        }
    }

    if (showStopDialog) {
        AlertDialog(
            onDismissRequest = { showStopDialog = false },
            title = { Text(stringResource(R.string.session_stop_confirm_title)) },
            text = { Text(stringResource(R.string.session_stop_confirm_text)) },
            confirmButton = {
                TextButton(onClick = { showStopDialog = false; onStop() }) {
                    Text(stringResource(R.string.session_stop))
                }
            },
            dismissButton = {
                TextButton(onClick = { showStopDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            },
        )
    }
}

private fun currentDriftMs(session: SessionState, now: Long): Long {
    val plan = session.plan
    val index = session.currentIndex
    return if (index == -1) {
        now - plan.departureEpochMs
    } else {
        val actualStart = if (index == 0) session.startedAtEpochMs else session.completedAt[index - 1] ?: now
        actualStart - plan.tasks[index].plannedStartEpochMs
    }
}

@Composable
private fun DepartureHeader(session: SessionState, now: Long) {
    val plan = session.plan
    val driftMs = currentDriftMs(session, now)
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                stringResource(R.string.session_leaving_at, formatClock(plan.departureEpochMs)),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            StatusChip(driftMs)
            Text(
                stringResource(R.string.session_departure_in, formatDuration(plan.departureEpochMs - now)),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
    }
}

@Composable
private fun StatusChip(driftMs: Long) {
    val onTimeThresholdMs = 60_000L
    val (text, color) = when {
        driftMs < -onTimeThresholdMs ->
            stringResource(R.string.session_ahead, formatMinutesShort(driftMs)) to
                MaterialTheme.colorScheme.tertiary
        driftMs <= onTimeThresholdMs ->
            stringResource(R.string.session_on_time) to MaterialTheme.colorScheme.primary
        else ->
            stringResource(R.string.session_behind, formatMinutesShort(driftMs)) to
                MaterialTheme.colorScheme.error
    }
    Surface(
        shape = MaterialTheme.shapes.small,
        color = color.copy(alpha = 0.14f),
        contentColor = color,
    ) {
        Text(
            text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelLarge,
        )
    }
}

@Composable
private fun CurrentTaskCard(session: SessionState, now: Long, onMarkDone: () -> Unit) {
    val index = session.currentIndex
    val taskPlan = session.plan.tasks[index]
    val remainingMs = taskPlan.plannedEndEpochMs - now
    val durationMs = (taskPlan.plannedEndEpochMs - taskPlan.plannedStartEpochMs).coerceAtLeast(1L)
    val progress = ((now - taskPlan.plannedStartEpochMs).toFloat() / durationMs).coerceIn(0f, 1f)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                stringResource(R.string.session_current_task),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            Text(
                taskPlan.task.name,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            if (remainingMs >= 0) {
                Text(
                    formatDuration(remainingMs),
                    fontSize = 56.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = PlayfulFamily,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            } else {
                Text(
                    stringResource(R.string.session_overshoot, formatDuration(remainingMs)),
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error,
                )
            }
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth(),
            )
            Button(
                onClick = onMarkDone,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = MaterialTheme.shapes.large,
            ) {
                Text(
                    stringResource(R.string.session_done_button),
                    style = MaterialTheme.typography.titleMedium,
                )
            }
            session.plan.tasks.getOrNull(index + 1)?.let { next ->
                Text(
                    stringResource(R.string.session_next_up, next.task.name),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
        }
    }
}

@Composable
private fun AllDoneCard(session: SessionState, now: Long) {
    val spareMs = session.plan.departureEpochMs - now
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.size(48.dp),
            )
            Text(
                stringResource(R.string.session_all_done_title),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                textAlign = TextAlign.Center,
            )
            Text(
                if (spareMs >= 0) {
                    stringResource(R.string.session_all_done_text, formatMinutesShort(spareMs))
                } else {
                    stringResource(R.string.session_all_done_late_text, formatMinutesShort(spareMs))
                },
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun TaskTimeline(session: SessionState, now: Long) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        session.plan.tasks.forEachIndexed { index, taskPlan ->
            TimelineRow(
                taskPlan = taskPlan,
                done = index in session.completedAt,
                doneAt = session.completedAt[index],
                isCurrent = index == session.currentIndex,
                now = now,
            )
        }
    }
}

@Composable
private fun TimelineRow(
    taskPlan: TaskPlan,
    done: Boolean,
    doneAt: Long?,
    isCurrent: Boolean,
    now: Long,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        when {
            done -> Icon(
                Icons.Default.CheckCircle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
            isCurrent -> Icon(
                Icons.Default.RadioButtonChecked,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
            else -> Icon(
                Icons.Outlined.RadioButtonUnchecked,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Column(modifier = Modifier.weight(1f).padding(start = 12.dp)) {
            Text(taskPlan.task.name, style = MaterialTheme.typography.bodyLarge)
            Text(
                "${formatClock(taskPlan.plannedStartEpochMs)} – ${formatClock(taskPlan.plannedEndEpochMs)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        when {
            done && doneAt != null -> Text(
                stringResource(R.string.session_task_done_at, formatClock(doneAt)),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
            )
            isCurrent && taskPlan.plannedEndEpochMs - now > 0 -> Text(
                stringResource(R.string.session_remaining_left, formatDuration(taskPlan.plannedEndEpochMs - now)),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}
