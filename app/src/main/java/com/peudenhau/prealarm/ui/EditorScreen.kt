package com.peudenhau.prealarm.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.peudenhau.prealarm.R
import com.peudenhau.prealarm.data.PreTask
import com.peudenhau.prealarm.ui.formatMinutesOfDay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
    state: EditorState,
    onClose: () -> Unit,
    onNameChange: (String) -> Unit,
    onDepartureChange: (Int) -> Unit,
    onAddTask: (String, Int) -> Unit,
    onUpdateTask: (Int, String, Int) -> Unit,
    onRemoveTask: (Int) -> Unit,
    onMoveTask: (Int, Int) -> Unit,
    onSave: () -> Unit,
    onStartTemporary: () -> Unit,
) {
    var showTimePicker by remember { mutableStateOf(false) }
    var editIndex by remember { mutableStateOf<Int?>(null) }
    var newTaskName by remember { mutableStateOf("") }
    var newTaskMinutes by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(
                            if (state.routineId == null) R.string.editor_new_title else R.string.editor_edit_title
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
            )
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(padding)
        ) {
            CuteBackground()
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                OutlinedTextField(
                    value = state.name,
                    onValueChange = onNameChange,
                    label = { Text(stringResource(R.string.routine_name_label)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )

                ElevatedCard(
                    onClick = { showTimePicker = true },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            Icons.Outlined.Schedule,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                        )
                        Column(modifier = Modifier.weight(1f).padding(start = 12.dp)) {
                            Text(
                                stringResource(R.string.departure_time_label),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(
                                stringResource(
                                    R.string.departure_time_value,
                                    formatMinutesOfDay(state.departureMinutes),
                                ),
                                style = MaterialTheme.typography.titleLarge,
                            )
                            if (state.tasks.isNotEmpty()) {
                                Text(
                                    stringResource(
                                        R.string.start_preview,
                                        formatMinutesOfDay(state.previewStartMinutes),
                                    ),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                            }
                        }
                    }
                }

                Text(
                    stringResource(R.string.tasks_section),
                    style = MaterialTheme.typography.titleMedium,
                )

                state.tasks.forEachIndexed { index, task ->
                    TaskRow(
                        index = index,
                        task = task,
                        canMoveUp = index > 0,
                        canMoveDown = index < state.tasks.lastIndex,
                        onMove = onMoveTask,
                        onEdit = { editIndex = index },
                        onRemove = onRemoveTask,
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    OutlinedTextField(
                        value = newTaskName,
                        onValueChange = { newTaskName = it },
                        label = { Text(stringResource(R.string.task_name_hint)) },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                    )
                    OutlinedTextField(
                        value = newTaskMinutes,
                        onValueChange = { newTaskMinutes = it.filter { ch -> ch.isDigit() }.take(3) },
                        label = { Text(stringResource(R.string.minutes_hint)) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.width(88.dp),
                    )
                    FilledTonalIconButton(onClick = {
                        val minutes = newTaskMinutes.toIntOrNull() ?: 0
                        if (newTaskName.isNotBlank() && minutes > 0) {
                            onAddTask(newTaskName, minutes)
                            newTaskName = ""
                            newTaskMinutes = ""
                        }
                    }) {
                        Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_task))
                    }
                }

                if (!state.isValid) {
                    Text(
                        text = stringResource(
                            if (state.name.isBlank()) R.string.editor_needs_name else R.string.editor_needs_tasks
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = onClose,
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(stringResource(R.string.cancel))
                    }
                    Button(
                        onClick = onSave,
                        enabled = state.isValid,
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(stringResource(R.string.save))
                    }
                }
                FilledTonalButton(
                    onClick = onStartTemporary,
                    enabled = state.isValid,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = MaterialTheme.shapes.large,
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text(stringResource(R.string.start_without_saving))
                }
            }
        }
    }

    if (showTimePicker) {
        val timeState = rememberTimePickerState(
            initialHour = state.departureMinutes / 60,
            initialMinute = state.departureMinutes % 60,
            is24Hour = true,
        )
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = { Text(stringResource(R.string.pick_time_title)) },
            text = { TimePicker(state = timeState) },
            confirmButton = {
                TextButton(onClick = {
                    onDepartureChange(timeState.hour * 60 + timeState.minute)
                    showTimePicker = false
                }) { Text(stringResource(R.string.ok)) }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text(stringResource(R.string.cancel))
                }
            },
        )
    }

    editIndex?.let { index ->
        if (index in state.tasks.indices) {
            val task = state.tasks[index]
            var editName by remember(index) { mutableStateOf(task.name) }
            var editMinutesText by remember(index) { mutableStateOf(task.minutes.toString()) }
            AlertDialog(
                onDismissRequest = { editIndex = null },
                title = { Text(stringResource(R.string.edit_task)) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = editName,
                            onValueChange = { editName = it },
                            label = { Text(stringResource(R.string.task_name_hint)) },
                            singleLine = true,
                        )
                        OutlinedTextField(
                            value = editMinutesText,
                            onValueChange = { editMinutesText = it.filter { ch -> ch.isDigit() }.take(3) },
                            label = { Text(stringResource(R.string.minutes_hint)) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        val minutes = editMinutesText.toIntOrNull() ?: 0
                        if (editName.isNotBlank() && minutes > 0) {
                            onUpdateTask(index, editName, minutes)
                            editIndex = null
                        }
                    }) { Text(stringResource(R.string.save)) }
                },
                dismissButton = {
                    TextButton(onClick = { editIndex = null }) {
                        Text(stringResource(R.string.cancel))
                    }
                },
            )
        }
    }
}

@Composable
private fun TaskRow(
    index: Int,
    task: PreTask,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    onMove: (Int, Int) -> Unit,
    onEdit: () -> Unit,
    onRemove: (Int) -> Unit,
) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f).padding(start = 8.dp)) {
                Text(task.name, style = MaterialTheme.typography.bodyLarge)
                Text(
                    stringResource(R.string.task_minutes_value, task.minutes),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            IconButton(onClick = { onMove(index, -1) }, enabled = canMoveUp) {
                Icon(
                    Icons.Default.KeyboardArrowUp,
                    contentDescription = stringResource(R.string.move_up),
                )
            }
            IconButton(onClick = { onMove(index, 1) }, enabled = canMoveDown) {
                Icon(
                    Icons.Default.KeyboardArrowDown,
                    contentDescription = stringResource(R.string.move_down),
                )
            }
            IconButton(onClick = onEdit) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = stringResource(R.string.edit_task),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            IconButton(onClick = { onRemove(index) }) {
                Icon(
                    Icons.Outlined.Delete,
                    contentDescription = stringResource(R.string.remove_task),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
