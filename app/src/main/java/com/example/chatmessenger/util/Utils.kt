package com.example.chatmessenger.util

import android.annotation.SuppressLint
import java.text.SimpleDateFormat
import java.util.Date

class Utils {
    @SuppressLint("SimpleDateFormat")
    fun getTime(): String {
        val formatter = SimpleDateFormat("HH:mm:ss")
        val date = Date(System.currentTimeMillis())
        val stringDate = formatter.format(date)
        return stringDate
    }
}