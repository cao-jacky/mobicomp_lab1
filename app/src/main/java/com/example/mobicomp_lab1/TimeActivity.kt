package com.example.mobicomp_lab1

import android.app.Activity

class TimeActivity : Activity() {
    fun pullCurrentTime(): String {
        return System.currentTimeMillis().toString()
    }
}