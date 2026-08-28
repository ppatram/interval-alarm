package com.example.intervalreminder

import android.app.AlarmManager
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.materialswitch.MaterialSwitch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class EditAlarmActivity : AppCompatActivity() {
    private lateinit var nameEdit: EditText; private lateinit var intervalEdit: EditText
    private lateinit var startEdit: EditText; private lateinit var endEdit: EditText
    private lateinit var messageEdit: EditText; private lateinit var vibrateSwitch: MaterialSwitch
    private lateinit var dayBoxes: Array<CheckBox>; private lateinit var deleteBtn: Button
    private lateinit var soundText: TextView; private lateinit var enabledSwitch: MaterialSwitch
    
    private var alarmId: Int = -1
    private var selectedSoundUri: String? = null

    private val soundPicker = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val uri = result.data?.getParcelableExtra<Uri>(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
            selectedSoundUri = uri?.toString()
            updateSoundText(uri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_alarm)
        
        alarmId = intent.getIntExtra("alarmId", -1)
        
        nameEdit = findViewById(R.id.editName); intervalEdit = findViewById(R.id.editInterval)
        startEdit = findViewById(R.id.editStart); endEdit = findViewById(R.id.editEnd)
        messageEdit = findViewById(R.id.editMessage); vibrateSwitch = findViewById(R.id.switchVibrate)
        deleteBtn = findViewById(R.id.btnDelete)
        soundText = findViewById(R.id.textSelectedSound); enabledSwitch = findViewById(R.id.switchEnabled)
        
        dayBoxes = arrayOf(
            findViewById(R.id.cbSun), findViewById(R.id.cbMon), findViewById(R.id.cbTue),
            findViewById(R.id.cbWed), findViewById(R.id.cbThu), findViewById(R.id.cbFri),
            findViewById(R.id.cbSat)
        )

        if (alarmId != -1) {
            deleteBtn.visibility = View.VISIBLE
            loadAlarm()
        }

        findViewById<Button>(R.id.btnSelectSound).setOnClickListener {
            val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER).apply {
                putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALARM)
                putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Select Alarm Sound")
                putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, selectedSoundUri?.let { Uri.parse(it) })
                putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true)
                putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false)
            }
            soundPicker.launch(intent)
        }

        findViewById<Button>(R.id.btnSave).setOnClickListener { saveAlarm() }
        deleteBtn.setOnClickListener { deleteAlarm() }
    }

    private fun updateSoundText(uri: Uri?) {
        soundText.text = if (uri != null) {
            RingtoneManager.getRingtone(this, uri).getTitle(this)
        } else {
            "Default"
        }
    }

    private fun loadAlarm() {
        CoroutineScope(Dispatchers.IO).launch {
            val alarm = AlarmDatabase.getDatabase(this@EditAlarmActivity).alarmDao().getAlarmById(alarmId)
            alarm?.let { a ->
                withContext(Dispatchers.Main) {
                    nameEdit.setText(a.name)
                    intervalEdit.setText(a.intervalMinutes.toString())
                    startEdit.setText(String.format(Locale.getDefault(), "%02d:%02d", a.startHour, a.startMinute))
                    endEdit.setText(String.format(Locale.getDefault(), "%02d:%02d", a.endHour, a.endMinute))
                    messageEdit.setText(a.message)
                    vibrateSwitch.isChecked = a.vibrate
                    enabledSwitch.isChecked = a.enabled
                    selectedSoundUri = a.soundUri
                    updateSoundText(a.soundUri?.let { Uri.parse(it) })
                    val days = a.days.split(",").map { it.toBoolean() }
                    dayBoxes.forEachIndexed { i, cb -> cb.isChecked = days.getOrNull(i) ?: false }
                }
            }
        }
    }

    private fun saveAlarm() {
        if (Build.VERSION.SDK_INT >= 31 && !getSystemService(AlarmManager::class.java).canScheduleExactAlarms()) {
            startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM))
            Toast.makeText(this, "Allow exact alarms, then press SAVE again.", Toast.LENGTH_LONG).show()
            return
        }
        val name = nameEdit.text.toString().ifBlank { "Alarm" }
        val interval = intervalEdit.text.toString().toLongOrNull() ?: 30
        val startParts = startEdit.text.toString().split(":")
        val endParts = endEdit.text.toString().split(":")
        val sh = startParts.getOrNull(0)?.toIntOrNull() ?: 7
        val sm = startParts.getOrNull(1)?.toIntOrNull() ?: 0
        val eh = endParts.getOrNull(0)?.toIntOrNull() ?: 22
        val em = endParts.getOrNull(1)?.toIntOrNull() ?: 0
        val vibrate = vibrateSwitch.isChecked
        val enabled = enabledSwitch.isChecked
        val days = dayBoxes.map { it.isChecked }.joinToString(",")

        val alarm = Alarm(
            id = if (alarmId == -1) 0 else alarmId,
            name = name,
            intervalMinutes = interval,
            startHour = sh,
            startMinute = sm,
            endHour = eh,
            endMinute = em,
            message = messageEdit.text.toString().ifBlank { "Time to move" },
            vibrate = vibrate,
            days = days,
            enabled = enabled,
            soundUri = selectedSoundUri
        )

        CoroutineScope(Dispatchers.IO).launch {
            val db = AlarmDatabase.getDatabase(this@EditAlarmActivity)
            if (alarmId == -1) {
                val newId = db.alarmDao().insertAlarm(alarm)
                val newAlarm = alarm.copy(id = newId.toInt())
                ReminderScheduler.scheduleNext(this@EditAlarmActivity, newAlarm)
            } else {
                db.alarmDao().updateAlarm(alarm)
                ReminderScheduler.cancel(this@EditAlarmActivity, alarm.id)
                ReminderScheduler.scheduleNext(this@EditAlarmActivity, alarm)
            }
            withContext(Dispatchers.Main) {
                finish()
            }
        }
    }

    private fun deleteAlarm() {
        CoroutineScope(Dispatchers.IO).launch {
            val db = AlarmDatabase.getDatabase(this@EditAlarmActivity)
            val alarm = db.alarmDao().getAlarmById(alarmId)
            alarm?.let {
                ReminderScheduler.cancel(this@EditAlarmActivity, it.id)
                db.alarmDao().deleteAlarm(it)
            }
            withContext(Dispatchers.Main) {
                finish()
            }
        }
    }
}
