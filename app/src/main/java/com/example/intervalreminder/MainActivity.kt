package com.example.intervalreminder

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.*
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private lateinit var statusText: TextView
    private lateinit var alarmList: RecyclerView
    private lateinit var adapter: AlarmAdapter
    
    private val notificationPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (Build.VERSION.SDK_INT >= 33 && checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            notificationPermission.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }
        
        statusText = findViewById(R.id.statusText)
        alarmList = findViewById(R.id.alarmList)
        alarmList.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this)
        
        adapter = AlarmAdapter(emptyList(), 
            onAlarmClick = { alarm ->
                val intent = Intent(this, EditAlarmActivity::class.java)
                intent.putExtra("alarmId", alarm.id)
                startActivity(intent)
            },
            onAlarmToggle = { alarm, isEnabled ->
                toggleAlarm(alarm, isEnabled)
            }
        )
        alarmList.adapter = adapter
        
        findViewById<View>(R.id.addAlarmFab).setOnClickListener {
            startActivity(Intent(this, EditAlarmActivity::class.java))
        }

        observeAlarms()
    }

    private fun observeAlarms() {
        val db = AlarmDatabase.getDatabase(this)
        CoroutineScope(Dispatchers.IO).launch {
            db.alarmDao().getAllAlarms().collect { alarms ->
                withContext(Dispatchers.Main) {
                    adapter.updateAlarms(alarms)
                    statusText.text = if (alarms.any { it.enabled }) "Recurring Alarms" else "All Alarms Off"
                }
            }
        }
    }

    private fun toggleAlarm(alarm: Alarm, isEnabled: Boolean) {
        val updatedAlarm = alarm.copy(enabled = isEnabled)
        CoroutineScope(Dispatchers.IO).launch {
            val db = AlarmDatabase.getDatabase(this@MainActivity)
            db.alarmDao().updateAlarm(updatedAlarm)
            if (isEnabled) {
                ReminderScheduler.scheduleNext(this@MainActivity, updatedAlarm)
            } else {
                ReminderScheduler.cancel(this@MainActivity, updatedAlarm.id)
            }
        }
    }
}
