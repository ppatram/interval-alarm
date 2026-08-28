package com.example.intervalreminder
import android.content.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val alarmId = intent?.getIntExtra("alarmId", -1) ?: -1
        if (alarmId == -1) return

        val result = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AlarmDatabase.getDatabase(context)
                val alarm = db.alarmDao().getAlarmById(alarmId)
                if (alarm != null && alarm.enabled) {
                    val days = alarm.days.split(",").map { it.toBoolean() }
                    if (days[Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1]) {
                        val serviceIntent = Intent(context, AlarmService::class.java).apply {
                            putExtra("alarmId", alarmId)
                        }
                        context.startForegroundService(serviceIntent)
                    }
                    ReminderScheduler.scheduleNext(context, alarm)
                }
            } finally {
                result.finish()
            }
        }
    }
}
