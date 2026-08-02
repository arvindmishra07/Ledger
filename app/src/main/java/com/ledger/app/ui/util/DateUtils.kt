package com.ledger.app.ui.util


import java.text.SimpleDateFormat
import java.util.*

object DateUtils {

    fun formatDate(millis: Long, pattern: String = "MMM d, yyyy"): String {
        return SimpleDateFormat(pattern, Locale.getDefault()).format(Date(millis))
    }

    fun formatDay(millis: Long): String {
        return SimpleDateFormat("MMM d", Locale.getDefault()).format(Date(millis))
    }

    fun formatMonthYear(month: Int, year: Int): String {
        val cal = Calendar.getInstance()
        cal.set(year, month - 1, 1)
        return SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(cal.time)
    }

    fun currentMonth(): Int = Calendar.getInstance().get(Calendar.MONTH) + 1

    fun currentYear(): Int = Calendar.getInstance().get(Calendar.YEAR)

    fun isToday(millis: Long): Boolean {
        val today = Calendar.getInstance()
        val date = Calendar.getInstance().apply { timeInMillis = millis }
        return today.get(Calendar.YEAR) == date.get(Calendar.YEAR) &&
                today.get(Calendar.DAY_OF_YEAR) == date.get(Calendar.DAY_OF_YEAR)
    }

    fun isYesterday(millis: Long): Boolean {
        val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
        val date = Calendar.getInstance().apply { timeInMillis = millis }
        return yesterday.get(Calendar.YEAR) == date.get(Calendar.YEAR) &&
                yesterday.get(Calendar.DAY_OF_YEAR) == date.get(Calendar.DAY_OF_YEAR)
    }

    fun relativeDayLabel(millis: Long): String {
        return when {
            isToday(millis) -> "Today"
            isYesterday(millis) -> "Yesterday"
            else -> formatDay(millis)
        }
    }

    fun monthRange(month: Int, year: Int): Pair<Long, Long> {
        val cal = Calendar.getInstance()
        cal.set(year, month - 1, 1, 0, 0, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val start = cal.timeInMillis
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        val end = cal.timeInMillis
        return start to end
    }

    fun nextMonth(month: Int, year: Int): Pair<Int, Int> {
        return if (month == 12) 1 to year + 1 else month + 1 to year
    }

    fun previousMonth(month: Int, year: Int): Pair<Int, Int> {
        return if (month == 1) 12 to year - 1 else month - 1 to year
    }
}