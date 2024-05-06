package com.example.hlbooking.util

import android.content.Context

object SharedPreferences {
    fun getUserId(context: Context): Long {
        val sharedPreferences = context.getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        return sharedPreferences.getLong("id", -1)
    }

    fun getToken(context: Context): String {
        val sharedPreferences = context.getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        return sharedPreferences.getString("token", "") ?: ""
    }
}