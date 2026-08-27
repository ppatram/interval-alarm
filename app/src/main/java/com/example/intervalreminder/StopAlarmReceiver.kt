package com.example.intervalreminder
import android.content.*
class StopAlarmReceiver:BroadcastReceiver(){override fun onReceive(context:Context,intent:Intent?){context.startService(Intent(context,AlarmService::class.java).setAction(AlarmService.STOP))}}
