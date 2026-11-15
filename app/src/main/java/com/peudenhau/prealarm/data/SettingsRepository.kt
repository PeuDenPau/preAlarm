package com.peudenhau.prealarm.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

enum class ThemeMode { SYSTEM, LIGHT, DARK }

enum class SoundChannel { ALARM, MEDIA }

data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val dynamicColor: Boolean = true,
    val soundUri: String = "",
    val soundTitle: String = "",
    val soundChannel: SoundChannel = SoundChannel.ALARM,
    val vibrate: Boolean = true,
)

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsRepository(private val context: Context) {

    private val themeKey = stringPreferencesKey("theme_mode")
    private val dynamicKey = booleanPreferencesKey("dynamic_color")
    private val soundUriKey = stringPreferencesKey("sound_uri")
    private val soundTitleKey = stringPreferencesKey("sound_title")
    private val channelKey = stringPreferencesKey("sound_channel")
    private val vibrateKey = booleanPreferencesKey("vibrate")

    val settings: Flow<AppSettings> = context.settingsDataStore.data.map { prefs ->
        AppSettings(
            themeMode = runCatching { ThemeMode.valueOf(prefs[themeKey] ?: "SYSTEM") }
                .getOrDefault(ThemeMode.SYSTEM),
            dynamicColor = prefs[dynamicKey] ?: true,
            soundUri = prefs[soundUriKey] ?: "",
            soundTitle = prefs[soundTitleKey] ?: "",
            soundChannel = runCatching { SoundChannel.valueOf(prefs[channelKey] ?: "ALARM") }
                .getOrDefault(SoundChannel.ALARM),
            vibrate = prefs[vibrateKey] ?: true,
        )
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.settingsDataStore.edit { it[themeKey] = mode.name }
    }

    suspend fun setDynamicColor(enabled: Boolean) {
        context.settingsDataStore.edit { it[dynamicKey] = enabled }
    }

    suspend fun setSound(uri: String, title: String) {
        context.settingsDataStore.edit {
            it[soundUriKey] = uri
            it[soundTitleKey] = title
        }
    }

    suspend fun setSoundChannel(channel: SoundChannel) {
        context.settingsDataStore.edit { it[channelKey] = channel.name }
    }

    suspend fun setVibrate(enabled: Boolean) {
        context.settingsDataStore.edit { it[vibrateKey] = enabled }
    }
}
