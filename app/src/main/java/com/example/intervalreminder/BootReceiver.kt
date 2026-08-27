package com.example.intervalreminder
import android.content.*
class BootReceiver:BroadcastReceiver(){override fun onReceive(context:Context,intent:Intent?){val p=context.getSharedPreferences("reminder_settings",0);if(p.getBoolean("enabled",false))ReminderScheduler.scheduleNext(context)}}
