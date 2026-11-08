package com.haorong.spendlog

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class ExpenseRepository(context: Context) {
    private val helper = ExpenseDbHelper(context.applicationContext)

    fun all(): List<Expense> {
        val cursor = helper.readableDatabase.query(
            TABLE,
            null,
            null,
            null,
            null,
            null,
            "date DESC, id DESC",
        )
        cursor.use {
            val rows = ArrayList<Expense>()
            while (it.moveToNext()) {
                rows.add(it.toExpense())
            }
            return rows
        }
    }

    fun get(id: Long): Expense? {
        val cursor = helper.readableDatabase.query(
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

    fun insert(expense: Expense) {
        helper.writableDatabase.insert(TABLE, null, expense.toValues())
    }

    fun update(expense: Expense) {
        helper.writableDatabase.update(
            TABLE,
            expense.toValues(),
            "id = ?",
            arrayOf(expense.id.toString()),
        )
    }

    fun delete(id: Long) {
        helper.writableDatabase.delete(TABLE, "id = ?", arrayOf(id.toString()))
    }

    private class ExpenseDbHelper(context: Context) : SQLiteOpenHelper(context, "spendlog.db", null, 1) {
        override fun onCreate(db: SQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE $TABLE (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL,
                    amount REAL NOT NULL,
                    category TEXT NOT NULL,
                    date TEXT NOT NULL,
                    note TEXT
                )
                """.trimIndent(),
            )
        }

        override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
            db.execSQL("DROP TABLE IF EXISTS $TABLE")
            onCreate(db)
        }
    }

    companion object {
        const val TABLE = "expenses"
    }
}

private fun Expense.toValues(): ContentValues {
    val values = ContentValues()
    values.put("name", name)
    values.put("amount", amount)
    values.put("category", category)
    values.put("date", date)
    values.put("note", note)
    return values
}

private fun Cursor.toExpense(): Expense {
    return Expense(
        id = getLong(getColumnIndexOrThrow("id")),
        name = getString(getColumnIndexOrThrow("name")),
        amount = getDouble(getColumnIndexOrThrow("amount")),
        category = getString(getColumnIndexOrThrow("category")),
        date = getString(getColumnIndexOrThrow("date")),
        note = getString(getColumnIndexOrThrow("note")).orEmpty(),
    )
}
