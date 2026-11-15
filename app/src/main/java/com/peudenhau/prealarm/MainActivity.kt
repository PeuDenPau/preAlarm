package com.peudenhau.prealarm

import android.os.Bundle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.peudenhau.prealarm.data.ThemeMode
import com.peudenhau.prealarm.ui.AppViewModel
import com.peudenhau.prealarm.ui.EditorScreen
import com.peudenhau.prealarm.ui.SessionScreen
import com.peudenhau.prealarm.ui.SettingsScreen
import com.peudenhau.prealarm.ui.screens.HomeScreen
import com.peudenhau.prealarm.ui.theme.PreAlarmTheme

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PreAlarmRoot()
        }
    }
}

@Composable
fun PreAlarmRoot(vm: AppViewModel = viewModel(factory = AppViewModel.Factory)) {
    val settings by vm.settings.collectAsStateWithLifecycle()
    PreAlarmTheme(
        darkTheme = when (settings.themeMode) {
            ThemeMode.SYSTEM -> isSystemInDarkTheme()
            ThemeMode.LIGHT -> false
            ThemeMode.DARK -> true
        },
        dynamicColor = settings.dynamicColor,
    ) {
        PreAlarmApp(vm)
    }
}

@Composable
fun PreAlarmApp(vm: AppViewModel = viewModel(factory = AppViewModel.Factory)) {
    val routines by vm.routines.collectAsStateWithLifecycle()
    val editorState by vm.editor.collectAsStateWithLifecycle()
    val sessionState by vm.session.collectAsStateWithLifecycle()
    val settingsState by vm.settings.collectAsStateWithLifecycle()
    val settingsOpen by vm.settingsOpen.collectAsStateWithLifecycle()
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

    if (settingsOpen) {
        BackHandler { vm.closeSettings() }
        SettingsScreen(
            settings = settingsState,
            onClose = vm::closeSettings,
            onThemeMode = vm::setThemeMode,
            onDynamicColor = vm::setDynamicColor,
            onSoundPicked = vm::setSound,
            onSoundChannel = vm::setSoundChannel,
            onVibrate = vm::setVibrate,
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
            onUpdateTask = vm::updateTask,
            onRemoveTask = vm::removeTask,
            onMoveTask = vm::moveTask,
            onSave = vm::saveEditor,
            onStartTemporary = vm::startTemporary,
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
        onOpenSettings = vm::openSettings,
    )
}
