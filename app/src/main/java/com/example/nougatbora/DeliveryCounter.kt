package com.example.nougatbora

import android.content.Context

object DeliveryCounter {
    private const val PREFS_NAME = "delivery_prefs"
    private const val KEY_COUNT = "delivery_count"
    private const val KEY_DATE = "last_date"

    fun getCount(context: Context): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val today = getTodayDate()

        val savedDate = prefs.getString(KEY_DATE, "")
        return if (savedDate == today) {
            prefs.getInt(KEY_COUNT, 0)
        } else {
            0 // reset for new day
        }
    }

    fun increment(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val today = getTodayDate()
        val savedDate = prefs.getString(KEY_DATE, "")

        val newCount = if (savedDate == today) {
            prefs.getInt(KEY_COUNT, 0) + 1
        } else {
            1 // first delivery of the new day
        }

        prefs.edit()
            .putInt(KEY_COUNT, newCount)
            .putString(KEY_DATE, today)
            .apply()
    }

    private fun getTodayDate(): String {
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
        return sdf.format(java.util.Date())
    }
}