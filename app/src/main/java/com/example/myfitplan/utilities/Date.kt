package com.example.myfitplan.utilities

import java.text.SimpleDateFormat
import java.util.*

object DateUtils {
    fun getToday(): Date {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.time
    }

    fun formattedDate(date: Date): String {
        val format = SimpleDateFormat("EEEE dd MMMM yyyy", Locale.getDefault())
        return format.format(date).replaceFirstChar {
            if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
        }
    }

    fun getTodayDate(): String {
        return com.example.myfitplan.utilities.DateUtils.formattedDate(
            com.example.myfitplan.utilities.DateUtils.getToday()
        )
    }
}
