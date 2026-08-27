package com.example.intervalreminder
import android.content.*
import java.util.Calendar
class AlarmReceiver:BroadcastReceiver(){override fun onReceive(context:Context,intent:Intent?){val p=context.getSharedPreferences("reminder_settings",Context.MODE_PRIVATE);if(!p.getBoolean("enabled",false))return
    val days=p.getString("days","false,true,true,true,true,true,false")?.split(",")?.map{it.toBoolean()}?:listOf(false,true,true,true,true,true,false)
    if(days[Calendar.getInstance().get(Calendar.DAY_OF_WEEK)-1]) context.startForegroundService(Intent(context,AlarmService::class.java))
    ReminderScheduler.scheduleNext(context)}}
