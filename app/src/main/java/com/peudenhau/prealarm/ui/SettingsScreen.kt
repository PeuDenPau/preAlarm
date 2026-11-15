package com.peudenhau.prealarm.ui

import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.IntentCompat
import com.peudenhau.prealarm.R
import com.peudenhau.prealarm.data.AppSettings
import com.peudenhau.prealarm.data.SoundChannel
import com.peudenhau.prealarm.data.ThemeMode

private data class LanguageOption(val tag: String, val label: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settings: AppSettings,
    onClose: () -> Unit,
    onThemeMode: (ThemeMode) -> Unit,
    onDynamicColor: (Boolean) -> Unit,
    onSoundPicked: (String, String) -> Unit,
    onSoundChannel: (SoundChannel) -> Unit,
    onVibrate: (Boolean) -> Unit,
) {
    val context = LocalContext.current
    var showLanguageDialog by remember { mutableStateOf(false) }
    var previewRingtone by remember { mutableStateOf<Ringtone?>(null) }

    val soundPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val uri = result.data?.let {
            IntentCompat.getParcelableExtra(it, RingtoneManager.EXTRA_RINGTONE_PICKED_URI, Uri::class.java)
        }
        if (uri != null) {
            val title = RingtoneManager.getRingtone(context, uri)?.getTitle(context).orEmpty()
            onSoundPicked(uri.toString(), title)
        }
    }

    DisposableEffect(Unit) {
        onDispose { previewRingtone?.stop() }
    }

    fun playPreview() {
        previewRingtone?.stop()
        val uri = if (settings.soundUri.isBlank()) {
            RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        } else {
            Uri.parse(settings.soundUri)
        }
        val ringtone = RingtoneManager.getRingtone(context, uri) ?: return
        ringtone.audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        ringtone.play()
        previewRingtone = ringtone
        Handler(Looper.getMainLooper()).postDelayed({
            runCatching { if (ringtone.isPlaying) ringtone.stop() }
        }, 3_000L)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title)) },
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
                .fillMaxSize()
                .padding(padding)
        ) {
            CuteBackground()
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                SettingsCard(
                    title = stringResource(R.string.settings_language),
                    summary = currentLanguageLabel(),
                    onClick = { showLanguageDialog = true },
                )

                SettingsCard(
                    title = stringResource(R.string.settings_theme),
                    summary = "",
                ) {
                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        val modes = listOf(
                            ThemeMode.SYSTEM to stringResource(R.string.theme_system),
                            ThemeMode.LIGHT to stringResource(R.string.theme_light),
                            ThemeMode.DARK to stringResource(R.string.theme_dark),
                        )
                        modes.forEachIndexed { index, (mode, label) ->
                            SegmentedButton(
                                selected = settings.themeMode == mode,
                                onClick = { onThemeMode(mode) },
                                shape = SegmentedButtonDefaults.itemShape(
                                    index = index,
                                    count = modes.size,
                                ),
                            ) { Text(label) }
                        }
                    }
                }

                SettingsCard(title = stringResource(R.string.settings_dynamic_colors), summary = "") {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            stringResource(R.string.settings_dynamic_colors_desc),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f),
                        )
                        Switch(
                            checked = settings.dynamicColor,
                            onCheckedChange = onDynamicColor,
                            enabled = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S,
                        )
                    }
                }

                SettingsCard(
                    title = stringResource(R.string.settings_sound),
                    summary = settings.soundTitle.ifBlank { stringResource(R.string.settings_sound_default) },
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(onClick = {
                            val pickerIntent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER).apply {
                                putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALARM)
                                putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "")
                                putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false)
                            }
                            soundPicker.launch(pickerIntent)
                        }) { Text(stringResource(R.string.change_sound)) }
                        TextButton(onClick = { playPreview() }) {
                            Text(stringResource(R.string.preview_sound))
                        }
                    }
                }

                SettingsCard(
                    title = stringResource(R.string.settings_channel),
                    summary = stringResource(R.string.settings_channel_desc),
                ) {
                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        val channels = listOf(
                            SoundChannel.ALARM to stringResource(R.string.channel_alarm),
                            SoundChannel.MEDIA to stringResource(R.string.channel_media),
                        )
                        channels.forEachIndexed { index, (channel, label) ->
                            SegmentedButton(
                                selected = settings.soundChannel == channel,
                                onClick = { onSoundChannel(channel) },
                                shape = SegmentedButtonDefaults.itemShape(
                                    index = index,
                                    count = channels.size,
                                ),
                            ) { Text(label) }
                        }
                    }
                }

                SettingsCard(
                    title = stringResource(R.string.settings_vibrate),
                    summary = stringResource(R.string.settings_vibrate_desc),
                ) {
                    Switch(checked = settings.vibrate, onCheckedChange = onVibrate)
                }
            }
        }
    }

    if (showLanguageDialog) {
        val options = listOf(
            LanguageOption("", stringResource(R.string.language_system)),
            LanguageOption("es", "Español"),
            LanguageOption("en", "English"),
            LanguageOption("ca", "Català"),
            LanguageOption("de", "Deutsch"),
            LanguageOption("fr", "Français"),
            LanguageOption("it", "Italiano"),
            LanguageOption("ja", "日本語"),
            LanguageOption("zh", "中文"),
        )
        val currentTag = androidx.appcompat.app.AppCompatDelegate
            .getApplicationLocales().toLanguageTags()
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            title = { Text(stringResource(R.string.settings_language)) },
            text = {
                Column {
                    options.forEach { option ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    val localeList = if (option.tag.isBlank()) {
                                        androidx.core.os.LocaleListCompat.getEmptyLocaleList()
                                    } else {
                                        androidx.core.os.LocaleListCompat.forLanguageTags(option.tag)
                                    }
                                    androidx.appcompat.app.AppCompatDelegate.setApplicationLocales(localeList)
                                    showLanguageDialog = false
                                }
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            RadioButton(
                                selected = currentTag == option.tag ||
                                    (option.tag.isBlank() && currentTag.isBlank()),
                                onClick = null,
                            )
                            Text(
                                option.label,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(start = 12.dp),
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLanguageDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            },
        )
    }
}

@Composable
private fun currentLanguageLabel(): String {
    val tags = androidx.appcompat.app.AppCompatDelegate.getApplicationLocales().toLanguageTags()
    return when {
        tags.isBlank() -> stringResource(R.string.language_system)
        tags.startsWith("es") -> "Español"
        tags.startsWith("ca") -> "Català"
        tags.startsWith("de") -> "Deutsch"
        tags.startsWith("fr") -> "Français"
        tags.startsWith("it") -> "Italiano"
        tags.startsWith("ja") -> "日本語"
        tags.startsWith("zh") -> "中文"
        else -> "English"
    }
}

@Composable
private fun SettingsCard(
    title: String,
    summary: String,
    onClick: (() -> Unit)? = null,
    content: (@Composable () -> Unit)? = null,
) {
    Card(
        onClick = { onClick?.invoke() },
        enabled = onClick != null,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            if (summary.isNotBlank()) {
                Text(
                    summary,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            content?.invoke()
        }
    }
}
