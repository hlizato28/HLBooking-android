package com.example.hlbooking.util

import android.app.Activity
import android.content.Context
import android.view.inputmethod.InputMethodManager

object HideKeyboard {
    fun hideKeyboard(activity: Activity) {
        val inputManager = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val view = activity.currentFocus
        if (view != null) {
            inputManager.hideSoftInputFromWindow(view.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
        }
    }
}