package com.haorong.spendlog

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private val displayDate: DateTimeFormatter =
    DateTimeFormatter.ofPattern("d MMM yyyy", Locale.ENGLISH)

private val monthTitle: DateTimeFormatter =
    DateTimeFormatter.ofPattern("MMMM yyyy", Locale.ENGLISH)

fun formatMoney(amount: Double): String {
    return "€" + String.format(Locale.US, "%.2f", amount)
}

fun formatDate(iso: String): String {
    return LocalDate.parse(iso).format(displayDate)
}

fun currentMonthTitle(today: LocalDate = LocalDate.now()): String {
    return today.format(monthTitle)
}

fun isInMonth(iso: String, today: LocalDate = LocalDate.now()): Boolean {
    val date = LocalDate.parse(iso)
    return date.year == today.year && date.month == today.month
}

fun formatLocation(latitude: Double?, longitude: Double?): String {
    if (latitude == null || longitude == null) return "Not saved"
    return String.format(Locale.US, "%.5f, %.5f", latitude, longitude)
}
