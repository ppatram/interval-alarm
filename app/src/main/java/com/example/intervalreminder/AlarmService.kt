package com.example.intervalreminder

import android.app.*
import android.content.*
import android.media.*
import android.net.Uri
import android.os.*
import android.provider.Settings
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.*

class AlarmService : Service() {
    private var player: MediaPlayer? = null
    private var vibrator: Vibrator? = null
    private var currentAlarmId: Int = -1

    companion object {
        const val CHANNEL = "ringing_alarm"
        const val STOP = "STOP_ALARM"
        const val NOTIF = 5001
    }

    override fun onCreate() {
        super.onCreate()
        createChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == STOP) {
            stopAlarm()
            return START_NOT_STICKY
        }
        
        val alarmId = intent?.getIntExtra("alarmId", -1) ?: -1
        if (alarmId != -1) {
            currentAlarmId = alarmId
            CoroutineScope(Dispatchers.IO).launch {
                val alarm = AlarmDatabase.getDatabase(this@AlarmService).alarmDao().getAlarmById(alarmId)
                if (alarm != null) {
                    withContext(Dispatchers.Main) {
                        startForeground(NOTIF, buildNotification(alarm))
                        startRinging(alarm)
                        startActivity(Intent(this@AlarmService, AlarmActivity::class.java).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                            putExtra("alarmId", alarmId)
                        })
                    }
                }
            }
        }
        return START_NOT_STICKY
    }

    private fun createChannel() {
        val nm = getSystemService(NotificationManager::class.java)
        if (Build.VERSION.SDK_INT >= 26) {
            val c = NotificationChannel(CHANNEL, "Ringing alarms", NotificationManager.IMPORTANCE_HIGH)
            c.setSound(null, null)
            c.enableVibration(false)
            nm.createNotificationChannel(c)
        }
    }

    private fun buildNotification(alarm: Alarm): Notification {
        val stop = PendingIntent.getService(
            this, alarm.id, Intent(this, AlarmService::class.java).setAction(STOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Builder(this, CHANNEL)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(alarm.name.ifBlank { "RECURRING TIMER" })
            .setContentText(alarm.message)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setOngoing(true)
            .setFullScreenIntent(
                PendingIntent.getActivity(
                    this, alarm.id + 100,
                    Intent(this, AlarmActivity::class.java).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        putExtra("alarmId", alarm.id)
                    },
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                ), true
            )
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "STOP", stop)
            .build()
    }

    private fun startRinging(alarm: Alarm) {
        player?.release()
        val uri = alarm.soundUri?.let { Uri.parse(it) } ?: Settings.System.DEFAULT_ALARM_ALERT_URI
        player = MediaPlayer.create(this, uri)
        player?.isLooping = true
        player?.start()
        if (alarm.vibrate) {
            vibrator = if (Build.VERSION.SDK_INT >= 31) getSystemService(VibratorManager::class.java).defaultVibrator
            else @Suppress("DEPRECATION") (getSystemService(VIBRATOR_SERVICE) as Vibrator)
            vibrator?.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 500, 500), 0))
        }
    }

    private fun stopAlarm() {
        player?.stop()
        player?.release()
        player = null
        vibrator?.cancel()
        vibrator = null
        getSystemService(NotificationManager::class.java).cancel(NOTIF)
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        player?.release()
        player = null
        vibrator?.cancel()
        vibrator = null
        super.onDestroy()
    }

    override fun onBind(intent: Intent?) = null
}
