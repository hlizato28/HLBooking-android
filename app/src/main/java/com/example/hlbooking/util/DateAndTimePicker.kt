package com.example.hlbooking.util

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import com.example.hlbooking.R
import com.google.android.material.textfield.TextInputEditText
import java.util.Calendar

fun setupDateAndTimePickers(
    context: Context,
    etDate: TextInputEditText,
    etStartTime: TextInputEditText,
    etEndTime: TextInputEditText
) {
    val calendar = Calendar.getInstance()

    etDate.setOnClickListener {
        val datePickerDialog = DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                etDate.setText(context.getString(R.string.date_format, year, month + 1, dayOfMonth))
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }

    val startTimeSetListener = TimePickerDialog.OnTimeSetListener { _, hourOfDay, _ ->
        etStartTime.setText(context.getString(R.string.time_format, hourOfDay))
    }

    val endTimeSetListener = TimePickerDialog.OnTimeSetListener { _, hourOfDay, _ ->
        etEndTime.setText(context.getString(R.string.time_format, hourOfDay))
    }

    etStartTime.setOnClickListener {
        TimePickerDialog(context, startTimeSetListener, calendar.get(Calendar.HOUR_OF_DAY), 0, true).show()
    }

    etEndTime.setOnClickListener {
        TimePickerDialog(context, endTimeSetListener, calendar.get(Calendar.HOUR_OF_DAY), 0, true).show()
    }
}