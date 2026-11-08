package com.haorong.spendlog

// one row in the table
data class Expense(
    var id: Long,
    var name: String,
    var amount: Double,
    var category: String,
    var date: String,
    var note: String,
)

fun euro(n: Double): String {
    return "€" + String.format(java.util.Locale.US, "%.2f", n)
}

// 2026-11-07 -> 7 Nov 2026
fun showDate(iso: String): String {
    val p = iso.split("-")
    val months = arrayOf(
        "Jan", "Feb", "Mar", "Apr", "May", "Jun",
        "Jul", "Aug", "Sep", "Oct", "Nov", "Dec",
    )
    val day = p[2].toInt()
    val month = months[p[1].toInt() - 1]
    return day.toString() + " " + month + " " + p[0]
}

fun categoryName(value: String): String {
    if (value == "food") return "Food"
    if (value == "transport") return "Transport"
    if (value == "shopping") return "Shopping"
    if (value == "other") return "Other"
    return value
}
