package com.example.intervalreminder

import android.app.*
import android.content.*
import java.util.Calendar

object ReminderScheduler {
    private const val REQUEST_CODE=1001
    fun scheduleNext(context:Context){val p=context.getSharedPreferences("reminder_settings",Context.MODE_PRIVATE);if(!p.getBoolean("enabled",false))return
        val interval=p.getLong("interval",30);val sh=p.getInt("startHour",7);val sm=p.getInt("startMinute",0);val eh=p.getInt("endHour",22);val em=p.getInt("endMinute",0)
        val days=p.getString("days","false,true,true,true,true,true,false")?.split(",")?.map{it.toBoolean()}?:listOf(false,true,true,true,true,true,false)
        val now=Calendar.getInstance();val start=Calendar.getInstance().apply{set(Calendar.HOUR_OF_DAY,sh);set(Calendar.MINUTE,sm);set(Calendar.SECOND,0);set(Calendar.MILLISECOND,0)};val end=Calendar.getInstance().apply{set(Calendar.HOUR_OF_DAY,eh);set(Calendar.MINUTE,em);set(Calendar.SECOND,0);set(Calendar.MILLISECOND,0)}
        if(end<=start)end.add(Calendar.DAY_OF_YEAR,1);val next=Calendar.getInstance()
        if(now<start) next.timeInMillis=start.timeInMillis else if(now>=end){start.add(Calendar.DAY_OF_YEAR,1);next.timeInMillis=start.timeInMillis}else{next.timeInMillis=now.timeInMillis;next.add(Calendar.MINUTE,interval.toInt());if(next>end){start.add(Calendar.DAY_OF_YEAR,1);next.timeInMillis=start.timeInMillis}}
        var s=0;while(!days[next.get(Calendar.DAY_OF_WEEK)-1]&&s<8){next.set(Calendar.HOUR_OF_DAY,sh);next.set(Calendar.MINUTE,sm);next.set(Calendar.SECOND,0);next.add(Calendar.DAY_OF_YEAR,1);s++}
        val am=context.getSystemService(AlarmManager::class.java);am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,next.timeInMillis,pending(context))
    }
    fun cancel(context:Context){context.getSystemService(AlarmManager::class.java).cancel(pending(context))}
    private fun pending(context:Context)=PendingIntent.getBroadcast(context,REQUEST_CODE,Intent(context,AlarmReceiver::class.java),PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
}
