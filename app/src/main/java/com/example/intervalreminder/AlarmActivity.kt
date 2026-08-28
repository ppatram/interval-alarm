package com.example.intervalreminder
import android.content.*
import android.os.*
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.*

class AlarmActivity : AppCompatActivity() {
    private var alarmId: Int = -1

    override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        alarmId = intent.getIntExtra("alarmId", -1)
        
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(48, 48, 48, 48)
        }
        setContentView(layout)

        val titleView = TextView(this).apply {
            text = "🚨  ALARM"
            textSize = 36f
            gravity = Gravity.CENTER
        }
        layout.addView(titleView)

        val messageView = TextView(this).apply {
            text = "Reminder"
            textSize = 26f
            gravity = Gravity.CENTER
            setPadding(24, 24, 24, 24)
        }
        layout.addView(messageView)

        if (alarmId != -1) {
            CoroutineScope(Dispatchers.IO).launch {
                val alarm = AlarmDatabase.getDatabase(this@AlarmActivity).alarmDao().getAlarmById(alarmId)
                if (alarm != null) {
                    withContext(Dispatchers.Main) {
                        messageView.text = alarm.message
                    }
                }
            }
        }

        val stopButton = Button(this).apply {
            text = "STOP ALARM"
            textSize = 22f
            setOnClickListener { stop() }
        }
        layout.addView(stopButton)
    }

    private fun stop() {
        startService(Intent(this, AlarmService::class.java).setAction(AlarmService.STOP))
        finishAndRemoveTask()
    }

    override fun onBackPressed() {}
}
