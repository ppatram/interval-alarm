package com.example.intervalreminder
import android.content.*
import android.os.*
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
class AlarmActivity:AppCompatActivity(){override fun onCreate(b:Bundle?){super.onCreate(b);window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);setContentView(LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;gravity=Gravity.CENTER;setPadding(48, 48, 48, 48);val p=getSharedPreferences("reminder_settings",0);addView(TextView(context).apply{text="🚨  ALARM";textSize=36f;gravity=Gravity.CENTER});addView(TextView(context).apply{text=p.getString("message","Reminder");textSize=26f;gravity=Gravity.CENTER;setPadding(24, 24, 24, 24)});addView(Button(context).apply{text="STOP ALARM";textSize=22f;setOnClickListener{stop()}})})}
private fun stop(){startService(Intent(this,AlarmService::class.java).setAction(AlarmService.STOP));finishAndRemoveTask()}
override fun onBackPressed(){/* deliberately do nothing: user must stop the alarm */}
}
