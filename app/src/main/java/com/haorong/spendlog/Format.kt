package com.haorong.spendlog

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private val displayDate = DateTimeFormatter.ofPattern("d MMM yyyy", Locale.ENGLISH)
private val monthTitle = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.ENGLISH)

fun formatMoney(amount: Double): String {
    return "€" + String.format(Locale.US, "%.2f", amount)
}

fun formatDate(iso: String): String {
    return LocalDate.parse(iso).format(displayDate)
}

fun currentMonthTitle(): String {
    return LocalDate.now().format(monthTitle)
}

fun isInMonth(iso: String): Boolean {
    val date = LocalDate.parse(iso)
    val today = LocalDate.now()
    return date.year == today.year && date.month == today.month
}
