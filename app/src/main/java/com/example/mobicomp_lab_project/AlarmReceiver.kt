package com.example.mobicomp_lab_project

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast

class AlarmReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val reminderItem= intent.getStringExtra("Reminder")
        Log.d("debugging", reminderItem)
        Toast.makeText(context, reminderItem, Toast.LENGTH_LONG).show()
    }
}