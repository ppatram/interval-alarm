package com.example.intervalreminder

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.materialswitch.MaterialSwitch
import java.util.Locale

class AlarmAdapter(
    private var alarms: List<Alarm>,
    private val onAlarmClick: (Alarm) -> Unit,
    private val onAlarmToggle: (Alarm, Boolean) -> Unit
) : RecyclerView.Adapter<AlarmAdapter.AlarmViewHolder>() {

    class AlarmViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nameText: TextView = view.findViewById(R.id.alarmName)
        val detailsText: TextView = view.findViewById(R.id.alarmDetails)
        val messageText: TextView = view.findViewById(R.id.alarmMessage)
        val enabledSwitch: MaterialSwitch = view.findViewById(R.id.alarmEnabled)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlarmViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_alarm, parent, false)
        return AlarmViewHolder(view)
    }

    override fun onBindViewHolder(holder: AlarmViewHolder, position: Int) {
        val alarm = alarms[position]
        holder.nameText.text = alarm.name.ifBlank { "Alarm" }
        holder.messageText.text = alarm.message
        holder.detailsText.text = String.format(
            Locale.getDefault(),
            "%02d:%02d - %02d:%02d | %d mins",
            alarm.startHour, alarm.startMinute,
            alarm.endHour, alarm.endMinute,
            alarm.intervalMinutes
        )
        
        // Remove listener before setting checked to avoid triggering onAlarmToggle
        holder.enabledSwitch.setOnCheckedChangeListener(null)
        holder.enabledSwitch.isChecked = alarm.enabled
        
        holder.itemView.setOnClickListener { onAlarmClick(alarm) }
        holder.enabledSwitch.setOnCheckedChangeListener { _, isChecked ->
            onAlarmToggle(alarm, isChecked)
        }
    }

    override fun getItemCount() = alarms.size

    fun updateAlarms(newAlarms: List<Alarm>) {
        alarms = newAlarms
        notifyDataSetChanged()
    }
}
