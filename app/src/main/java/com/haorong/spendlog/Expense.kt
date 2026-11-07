package com.haorong.spendlog

data class Expense(
    val id: Long,
    val name: String,
    val amount: Double,
    val category: String,
    val date: String,
    val note: String,
    val latitude: Double?,
    val longitude: Double?,
)

val CATEGORIES = listOf(
    "food" to "Food",
    "transport" to "Transport",
    "shopping" to "Shopping",
    "other" to "Other",
)

fun categoryLabel(value: String): String {
    return CATEGORIES.firstOrNull { it.first == value }?.second ?: value
}
