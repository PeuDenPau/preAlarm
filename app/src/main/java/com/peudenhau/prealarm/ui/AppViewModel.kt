package com.peudenhau.prealarm.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.peudenhau.prealarm.PreAlarmApplication
import com.peudenhau.prealarm.R
import com.peudenhau.prealarm.alarm.AlarmScheduler
import com.peudenhau.prealarm.data.PreTask
import com.peudenhau.prealarm.data.Routine
import com.peudenhau.prealarm.data.RoutineRepository
import com.peudenhau.prealarm.schedule.ScheduleCalculator
import com.peudenhau.prealarm.schedule.SessionPlan
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class EditorState(
    val routineId: Long?,
    val name: String,
    val departureMinutes: Int,
    val tasks: List<PreTask>,
    val nextTaskId: Long,
) {
    val isValid: Boolean get() = name.isNotBlank() && tasks.isNotEmpty()
}

data class SessionState(
    val routine: Routine,
    val plan: SessionPlan,
    val startedAtEpochMs: Long,
    val completedAt: Map<Int, Long> = emptyMap(),
) {
    val currentIndex: Int
        get() = plan.tasks.indices.firstOrNull { it !in completedAt } ?: -1
    val allDone: Boolean get() = currentIndex == -1
}

class AppViewModel(app: Application) : AndroidViewModel(app) {

    companion object {
        val Factory = viewModelFactory {
            initializer {
                AppViewModel(this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as Application)
            }
        }
    }

    private val repository = (app as PreAlarmApplication).repository
    private val scheduler = (app as PreAlarmApplication).scheduler

    val routines: StateFlow<List<Routine>> = repository.routines
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _editor = MutableStateFlow<EditorState?>(null)
    val editor: StateFlow<EditorState?> = _editor.asStateFlow()

    private val _session = MutableStateFlow<SessionState?>(null)
    val session: StateFlow<SessionState?> = _session.asStateFlow()

    private val _now = MutableStateFlow(System.currentTimeMillis())
    val now: StateFlow<Long> = _now.asStateFlow()

    init {
        viewModelScope.launch {
            while (isActive) {
                _now.value = System.currentTimeMillis()
                delay(500)
            }
        }
    }

    fun canScheduleExact(): Boolean = scheduler.canScheduleExact()

    fun openEditor(routine: Routine?) {
        _editor.value = if (routine == null) {
            EditorState(
                routineId = null,
                name = "",
                departureMinutes = 8 * 60,
                tasks = emptyList(),
                nextTaskId = 1L,
            )
        } else {
            EditorState(
                routineId = routine.id,
                name = routine.name,
                departureMinutes = routine.departureMinutes,
                tasks = routine.tasks,
                nextTaskId = (routine.tasks.maxOfOrNull { it.id } ?: 0L) + 1L,
            )
        }
    }

    fun closeEditor() {
        _editor.value = null
    }

    fun updateEditorName(name: String) {
        _editor.value?.let { _editor.value = it.copy(name = name) }
    }

    fun updateEditorDeparture(minutes: Int) {
        _editor.value?.let { _editor.value = it.copy(departureMinutes = minutes) }
    }

    fun addTask(name: String, minutes: Int) {
        _editor.value?.let { editor ->
            if (name.isBlank() || minutes <= 0) return
            _editor.value = editor.copy(
                tasks = editor.tasks + PreTask(id = editor.nextTaskId, name = name.trim(), minutes = minutes),
                nextTaskId = editor.nextTaskId + 1,
            )
        }
    }

    fun removeTask(index: Int) {
        _editor.value?.let { editor ->
            _editor.value = editor.copy(tasks = editor.tasks.filterIndexed { i, _ -> i != index })
        }
    }

    fun moveTask(index: Int, delta: Int) {
        _editor.value?.let { editor ->
            val target = index + delta
            if (target !in editor.tasks.indices) return
            val tasks = editor.tasks.toMutableList()
            val item = tasks.removeAt(index)
            tasks.add(target, item)
            _editor.value = editor.copy(tasks = tasks)
        }
    }

    fun saveEditor() {
        val editor = _editor.value ?: return
        if (!editor.isValid) return
        viewModelScope.launch {
            val id = editor.routineId ?: (routines.value.maxOfOrNull { it.id } ?: 0L) + 1L
            repository.save(
                Routine(
                    id = id,
                    name = editor.name.trim(),
                    departureMinutes = editor.departureMinutes,
                    tasks = editor.tasks,
                )
            )
            _editor.value = null
        }
    }

    fun deleteRoutine(routine: Routine) {
        if (_session.value?.routine?.id == routine.id) stopSession()
        viewModelScope.launch { repository.delete(routine.id) }
    }

    fun startSession(routine: Routine) {
        val nowMs = System.currentTimeMillis()
        val plan = ScheduleCalculator.buildPlan(routine.departureMinutes, routine.tasks, nowMs)
        if (plan.tasks.isEmpty()) return
        scheduler.cancelSession()
        val context = getApplication<Application>()
        val first = plan.tasks.first()
        scheduler.scheduleSession(
            plan = plan,
            startText = context.getString(R.string.notify_start_text, first.task.name, first.task.minutes),
            taskEndText = { index -> context.getString(R.string.notify_task_end_text, plan.tasks[index].task.name) },
            departureText = context.getString(R.string.notify_departure_text),
        )
        _session.value = SessionState(
            routine = routine,
            plan = plan,
            startedAtEpochMs = nowMs,
        )
    }

    fun markDone() {
        val current = _session.value ?: return
        val index = current.currentIndex
        if (index == -1) return
        scheduler.cancelTaskEnd(index)
        _session.value = current.copy(completedAt = current.completedAt + (index to System.currentTimeMillis()))
    }

    fun stopSession() {
        scheduler.cancelSession()
        _session.value = null
    }
}
