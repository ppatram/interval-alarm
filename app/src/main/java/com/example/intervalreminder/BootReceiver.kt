package com.example.intervalreminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val result = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AlarmDatabase.getDatabase(context)
                val alarms = db.alarmDao().getAllAlarmsList()
                alarms.filter { it.enabled }.forEach { alarm ->
                    ReminderScheduler.scheduleNext(context, alarm)
                }
            } finally {
                result.finish()
            }
        }
    }
}
