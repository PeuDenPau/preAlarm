package com.peudenhau.prealarm

import android.app.Application
import com.peudenhau.prealarm.alarm.AlarmScheduler
import com.peudenhau.prealarm.data.RoutineRepository
import com.peudenhau.prealarm.data.SettingsRepository

class PreAlarmApplication : Application() {
    val repository: RoutineRepository by lazy { RoutineRepository(this) }
    val settingsRepository: SettingsRepository by lazy { SettingsRepository(this) }
    val scheduler: AlarmScheduler by lazy { AlarmScheduler(this) }
}
