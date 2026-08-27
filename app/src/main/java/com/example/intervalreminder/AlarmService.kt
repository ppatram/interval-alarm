package com.example.intervalreminder

import android.app.*
import android.content.*
import android.media.*
import android.os.*
import android.provider.Settings
import androidx.core.app.NotificationCompat

class AlarmService:Service(){
    private var player:MediaPlayer?=null
    private var vibrator:Vibrator?=null
    companion object{const val CHANNEL="ringing_alarm";const val STOP="STOP_ALARM";const val NOTIF=5001}
    override fun onCreate(){super.onCreate();createChannel();startForeground(NOTIF,buildNotification());startRinging();startActivity(Intent(this,AlarmActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP))}
    override fun onStartCommand(intent:Intent?,flags:Int,startId:Int):Int{if(intent?.action==STOP)stopAlarm();return START_NOT_STICKY}
    private fun createChannel(){val nm=getSystemService(NotificationManager::class.java);if(Build.VERSION.SDK_INT>=26){val c=NotificationChannel(CHANNEL,"Ringing alarms",NotificationManager.IMPORTANCE_HIGH);c.setSound(null,null);c.enableVibration(false);nm.createNotificationChannel(c)}}
    private fun buildNotification():Notification{val stop=PendingIntent.getService(this,7,Intent(this,AlarmService::class.java).setAction(STOP),PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE);val p=getSharedPreferences("reminder_settings",0);return NotificationCompat.Builder(this,CHANNEL).setSmallIcon(android.R.drawable.ic_lock_idle_alarm).setContentTitle("ALARM REMINDER").setContentText(p.getString("message","Reminder")).setCategory(NotificationCompat.CATEGORY_ALARM).setPriority(NotificationCompat.PRIORITY_MAX).setOngoing(true).setFullScreenIntent(PendingIntent.getActivity(this,8,Intent(this,AlarmActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP),PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE),true).addAction(android.R.drawable.ic_menu_close_clear_cancel,"STOP",stop).build()}
    private fun startRinging(){
        val p=getSharedPreferences("reminder_settings",0)
        player=MediaPlayer.create(this,Settings.System.DEFAULT_ALARM_ALERT_URI)
        player?.isLooping=true
        player?.start()
        if(p.getBoolean("vibrate",true)){
            vibrator=if(Build.VERSION.SDK_INT>=31)getSystemService(VibratorManager::class.java).defaultVibrator else @Suppress("DEPRECATION")(getSystemService(VIBRATOR_SERVICE)as Vibrator)
            vibrator?.vibrate(VibrationEffect.createWaveform(longArrayOf(0,500,500),0))
        }
    }
    private fun stopAlarm(){player?.stop();player?.release();player=null;vibrator?.cancel();vibrator=null;getSystemService(NotificationManager::class.java).cancel(NOTIF);stopForeground(STOP_FOREGROUND_REMOVE);stopSelf()}
    override fun onDestroy(){player?.release();player=null;vibrator?.cancel();vibrator=null;super.onDestroy()}
    override fun onBind(intent:Intent?)=null
}
