package com.peudenhau.prealarm

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.peudenhau.prealarm.ui.AppViewModel
import com.peudenhau.prealarm.ui.EditorScreen
import com.peudenhau.prealarm.ui.SessionScreen
import com.peudenhau.prealarm.ui.screens.HomeScreen
import com.peudenhau.prealarm.ui.theme.PreAlarmTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PreAlarmTheme {
                PreAlarmApp()
            }
        }
    }
}

@Composable
fun PreAlarmApp(vm: AppViewModel = viewModel(factory = AppViewModel.Factory)) {
    val routines by vm.routines.collectAsStateWithLifecycle()
    val editorState by vm.editor.collectAsStateWithLifecycle()
    val sessionState by vm.session.collectAsStateWithLifecycle()
    val now by vm.now.collectAsStateWithLifecycle()

    val activeSession = sessionState
    if (activeSession != null) {
        SessionScreen(
            session = activeSession,
            now = now,
            onMarkDone = vm::markDone,
            onStop = vm::stopSession,
        )
        return
    }

    val activeEditor = editorState
    if (activeEditor != null) {
        BackHandler { vm.closeEditor() }
        EditorScreen(
            state = activeEditor,
            onClose = vm::closeEditor,
            onNameChange = vm::updateEditorName,
            onDepartureChange = vm::updateEditorDeparture,
            onAddTask = vm::addTask,
            onRemoveTask = vm::removeTask,
            onMoveTask = vm::moveTask,
            onSave = vm::saveEditor,
        )
        return
    }

    HomeScreen(
        routines = routines,
        now = now,
        canScheduleExact = vm.canScheduleExact(),
        onStart = vm::startSession,
        onEdit = vm::openEditor,
        onDelete = vm::deleteRoutine,
    )
}
