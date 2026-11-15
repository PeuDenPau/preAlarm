package com.peudenhau.prealarm.alarm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.NotificationCompat
import com.peudenhau.prealarm.MainActivity
import com.peudenhau.prealarm.R

class AlarmReceiver : BroadcastReceiver() {

    companion object {
        private const val CHANNEL_ID = "session_alerts"
        private const val ALERT_DURATION_MS = 4_000L
    }

    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra(AlarmScheduler.EXTRA_TITLE) ?: context.getString(R.string.app_name)
        val text = intent.getStringExtra(AlarmScheduler.EXTRA_TEXT).orEmpty()
        showNotification(context, title, text)
        playAlert(context)
    }

    private fun showNotification(context: Context, title: String, text: String) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                context.getString(R.string.notify_channel_name),
                NotificationManager.IMPORTANCE_HIGH,
            ).apply { setSound(null, null) }
            manager.createNotificationChannel(channel)
        }
        val contentIntent = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_prealarm)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setContentIntent(contentIntent)
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
        manager.notify(title.hashCode(), notification)
    }

    private fun playAlert(context: Context) {
        val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val ringtone = RingtoneManager.getRingtone(context, uri)
        if (ringtone != null) {
            ringtone.audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
            ringtone.play()
            Handler(Looper.getMainLooper()).postDelayed({
                runCatching { if (ringtone.isPlaying) ringtone.stop() }
            }, ALERT_DURATION_MS)
        }
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager)?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
        if (vibrator != null && vibrator.hasVibrator()) {
            vibrator.vibrate(
                VibrationEffect.createWaveform(longArrayOf(0, 250, 200, 250, 200, 250), -1)
            )
        }
    }
}
