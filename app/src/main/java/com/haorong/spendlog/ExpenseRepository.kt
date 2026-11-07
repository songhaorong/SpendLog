package com.haorong.spendlog

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.time.LocalDate

class ExpenseRepository(context: Context) {
    private val helper = ExpenseDbHelper(context.applicationContext)

    fun all(): List<Expense> {
        val db = helper.readableDatabase
        val cursor = db.query(
            TABLE,
            null,
            null,
            null,
            null,
            null,
            "date DESC, id DESC",
        )
        cursor.use {
            val rows = ArrayList<Expense>(it.count)
            while (it.moveToNext()) {
                rows.add(it.toExpense())
            }
            return rows
        }
    }

    fun get(id: Long): Expense? {
        val db = helper.readableDatabase
        val cursor = db.query(
            TABLE,
            null,
            "id = ?",
            arrayOf(id.toString()),
            null,
            null,
            null,
        )
        cursor.use {
            if (!it.moveToFirst()) return null
            return it.toExpense()
        }
    }

    fun insert(expense: Expense): Long {
        return helper.writableDatabase.insert(TABLE, null, expense.toValues(includeId = false))
    }

    fun update(expense: Expense) {
        helper.writableDatabase.update(
            TABLE,
            expense.toValues(includeId = false),
            "id = ?",
            arrayOf(expense.id.toString()),
        )
    }

    fun delete(id: Long) {
        helper.writableDatabase.delete(TABLE, "id = ?", arrayOf(id.toString()))
    }

    private class ExpenseDbHelper(context: Context) : SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {
        override fun onCreate(db: SQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE $TABLE (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL,
                    amount REAL NOT NULL,
                    category TEXT NOT NULL,
                    date TEXT NOT NULL,
                    note TEXT,
                    latitude REAL,
                    longitude REAL
                )
                """.trimIndent(),
            )
            seed(db)
        }

        override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
            db.execSQL("DROP TABLE IF EXISTS $TABLE")
            onCreate(db)
        }
    }

    companion object {
        const val DB_NAME = "spendlog.db"
        const val DB_VERSION = 1
        const val TABLE = "expenses"

        private fun seed(db: SQLiteDatabase) {
            val today = LocalDate.now()
            val monthStart = today.withDayOfMonth(1)
            fun onDay(daysAgo: Long): String {
                var date = today.minusDays(daysAgo)
                if (date.isBefore(monthStart)) date = monthStart
                return date.toString()
            }
            insertSeed(db, "Coffee", 3.40, "food", onDay(0), "before class", 53.33, -6.28)
            insertSeed(db, "Tesco", 14.20, "shopping", onDay(1), "", 53.3392, -6.2620)
            insertSeed(db, "Lunch", 8.50, "food", onDay(2), "", 53.3410, -6.2594)
            insertSeed(db, "Bus ticket", 2.00, "transport", onDay(3), "", null, null)
            insertSeed(db, "Lidl", 9.15, "shopping", onDay(4), "", 53.3491, -6.2644)
        }

        private fun insertSeed(
            db: SQLiteDatabase,
            name: String,
            amount: Double,
            category: String,
            date: String,
            note: String,
            latitude: Double?,
            longitude: Double?,
        ) {
            val values = ContentValues()
            values.put("name", name)
            values.put("amount", amount)
            values.put("category", category)
            values.put("date", date)
            values.put("note", note)
            if (latitude == null || longitude == null) {
                values.putNull("latitude")
                values.putNull("longitude")
            } else {
                values.put("latitude", latitude)
                values.put("longitude", longitude)
            }
            db.insert(TABLE, null, values)
        }
    }
}

private fun Expense.toValues(includeId: Boolean): ContentValues {
    val values = ContentValues()
    if (includeId) values.put("id", id)
    values.put("name", name)
    values.put("amount", amount)
    values.put("category", category)
    values.put("date", date)
    values.put("note", note)
    if (latitude == null || longitude == null) {
        values.putNull("latitude")
        values.putNull("longitude")
    } else {
        values.put("latitude", latitude)
        values.put("longitude", longitude)
    }
    return values
}

private fun Cursor.toExpense(): Expense {
    val latIndex = getColumnIndexOrThrow("latitude")
    val lngIndex = getColumnIndexOrThrow("longitude")
    return Expense(
        id = getLong(getColumnIndexOrThrow("id")),
        name = getString(getColumnIndexOrThrow("name")),
        amount = getDouble(getColumnIndexOrThrow("amount")),
        category = getString(getColumnIndexOrThrow("category")),
        date = getString(getColumnIndexOrThrow("date")),
        note = getString(getColumnIndexOrThrow("note")).orEmpty(),
        latitude = if (isNull(latIndex)) null else getDouble(latIndex),
        longitude = if (isNull(lngIndex)) null else getDouble(lngIndex),
    )
}
