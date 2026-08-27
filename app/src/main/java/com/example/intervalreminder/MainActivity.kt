package com.example.intervalreminder

import android.Manifest
import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private lateinit var intervalEdit: EditText; private lateinit var startEdit: EditText; private lateinit var endEdit: EditText
    private lateinit var messageEdit: EditText; private lateinit var vibrateSwitch: Switch; private lateinit var statusText: TextView
    private lateinit var dayBoxes: Array<CheckBox>
    private val notificationPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) {}
    override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState); setContentView(R.layout.activity_main)
        statusText=findViewById(R.id.statusText); intervalEdit=findViewById(R.id.intervalEdit); startEdit=findViewById(R.id.startEdit); endEdit=findViewById(R.id.endEdit); messageEdit=findViewById(R.id.messageEdit); vibrateSwitch=findViewById(R.id.vibrateSwitch)
        dayBoxes = arrayOf(findViewById(R.id.cbSun), findViewById(R.id.cbMon), findViewById(R.id.cbTue), findViewById(R.id.cbWed), findViewById(R.id.cbThu), findViewById(R.id.cbFri), findViewById(R.id.cbSat))
        loadSettings(); updateStatus()
        findViewById<Button>(R.id.saveStartButton).setOnClickListener { saveAndStart() }
        findViewById<Button>(R.id.stopButton).setOnClickListener {
            ReminderScheduler.cancel(this)
            stopService(Intent(this, AlarmService::class.java))
            prefs().edit().putBoolean("enabled",false).apply()
            updateStatus()
        }
        if (Build.VERSION.SDK_INT>=33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED) notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
    }
    private fun saveAndStart() {
        val interval=intervalEdit.text.toString().toLongOrNull(); if(interval==null||interval<1){Toast.makeText(this,"Interval must be at least 1 minute",Toast.LENGTH_LONG).show();return}
        val days = dayBoxes.map { it.isChecked }.joinToString(",")
        val startParts = startEdit.text.toString().split(":"); val endParts = endEdit.text.toString().split(":")
        val sh = startParts.getOrNull(0)?.toIntOrNull() ?: 7; val sm = startParts.getOrNull(1)?.toIntOrNull() ?: 0
        val eh = endParts.getOrNull(0)?.toIntOrNull() ?: 22; val em = endParts.getOrNull(1)?.toIntOrNull() ?: 0

        if(Build.VERSION.SDK_INT>=31 && !getSystemService(AlarmManager::class.java).canScheduleExactAlarms()){startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM));Toast.makeText(this,"Allow exact alarms, then press SAVE AND START again.",Toast.LENGTH_LONG).show();return}
        prefs().edit().putLong("interval",interval).putInt("startHour",sh).putInt("startMinute",sm).putInt("endHour",eh).putInt("endMinute",em).putString("message",messageEdit.text.toString().ifBlank{"Reminder"}).putBoolean("vibrate",vibrateSwitch.isChecked).putString("days",days).putBoolean("enabled",true).apply()
        ReminderScheduler.scheduleNext(this); updateStatus(); Toast.makeText(this,"Alarm reminders started",Toast.LENGTH_SHORT).show()
    }
    private fun loadSettings(){
        val p=prefs();intervalEdit.setText(p.getLong("interval",30).toString())
        startEdit.setText(String.format(Locale.getDefault(), "%02d:%02d", p.getInt("startHour", 7), p.getInt("startMinute", 0)))
        endEdit.setText(String.format(Locale.getDefault(), "%02d:%02d", p.getInt("endHour", 22), p.getInt("endMinute", 0)))
        messageEdit.setText(p.getString("message","Time to move"));vibrateSwitch.isChecked=p.getBoolean("vibrate",true)
        val days = p.getString("days", "false,true,true,true,true,true,false")?.split(",") ?: listOf("false","true","true","true","true","true","false")
        dayBoxes.forEachIndexed { i, cb -> cb.isChecked = days.getOrNull(i)?.toBoolean() ?: false }
    }
    private fun updateStatus(){statusText.text=if(prefs().getBoolean("enabled",false))"RUNNING — alarm will ring until stopped" else "STOPPED"}
    private fun prefs()=getSharedPreferences("reminder_settings",Context.MODE_PRIVATE)
}
