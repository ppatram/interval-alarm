package com.example.intervalreminder

import android.app.*
import android.content.*
import java.util.Calendar

object ReminderScheduler {
    fun scheduleNext(context: Context, alarm: Alarm) {
        if (!alarm.enabled) return
        val interval = alarm.intervalMinutes
        val sh = alarm.startHour; val sm = alarm.startMinute; val eh = alarm.endHour; val em = alarm.endMinute
        val days = alarm.days.split(",").map { it.toBoolean() }
        
        val now = Calendar.getInstance()
        val start = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, sh); set(Calendar.MINUTE, sm); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }
        val end = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, eh); set(Calendar.MINUTE, em); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }
        
        if (end <= start) end.add(Calendar.DAY_OF_YEAR, 1)
        val next = Calendar.getInstance()
        
        if (now < start) {
            next.timeInMillis = start.timeInMillis
        } else if (now >= end) {
            start.add(Calendar.DAY_OF_YEAR, 1)
            next.timeInMillis = start.timeInMillis
        } else {
            next.timeInMillis = now.timeInMillis
            next.add(Calendar.MINUTE, interval.toInt())
            if (next > end) {
                start.add(Calendar.DAY_OF_YEAR, 1)
                next.timeInMillis = start.timeInMillis
            }
        }
        
        var s = 0
        while (!days[next.get(Calendar.DAY_OF_WEEK) - 1] && s < 8) {
            next.set(Calendar.HOUR_OF_DAY, sh)
            next.set(Calendar.MINUTE, sm)
            next.set(Calendar.SECOND, 0)
            next.add(Calendar.DAY_OF_YEAR, 1)
            s++
        }
        
        val am = context.getSystemService(AlarmManager::class.java)
        am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, next.timeInMillis, pending(context, alarm.id))
    }

    fun cancel(context: Context, alarmId: Int) {
        context.getSystemService(AlarmManager::class.java).cancel(pending(context, alarmId))
    }

    private fun pending(context: Context, alarmId: Int) = PendingIntent.getBroadcast(
        context, alarmId, Intent(context, AlarmReceiver::class.java).putExtra("alarmId", alarmId),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
}
