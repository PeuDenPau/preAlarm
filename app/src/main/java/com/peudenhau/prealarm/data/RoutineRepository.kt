package com.peudenhau.prealarm.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val Context.routinesDataStore: DataStore<Preferences> by preferencesDataStore(name = "routines")

class RoutineRepository(private val context: Context) {

    private val json = Json { ignoreUnknownKeys = true }
    private val key = stringPreferencesKey("routines_json")

    val routines: Flow<List<Routine>> = context.routinesDataStore.data.map { prefs ->
        decode(prefs[key])
    }

    suspend fun save(routine: Routine) {
        upsert { list ->
            val updated = list.filterNot { it.id == routine.id } + routine
            updated.sortedBy { it.departureMinutes }
        }
    }

    suspend fun delete(routineId: Long) {
        upsert { list -> list.filterNot { it.id == routineId } }
    }

    private suspend fun upsert(transform: (List<Routine>) -> List<Routine>) {
        context.routinesDataStore.edit { prefs ->
            val current = decode(prefs[key])
            prefs[key] = json.encodeToString<List<Routine>>(transform(current))
        }
    }

    private fun decode(text: String?): List<Routine> =
        text?.let { runCatching { json.decodeFromString<List<Routine>>(it) }.getOrDefault(emptyList()) }
            ?: emptyList()
}
