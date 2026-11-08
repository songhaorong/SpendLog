// Haorong Song  3184339
// sqlite, one table

package com.haorong.spendlog

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class ExpenseDb(context: Context) {

    private val helper = Helper(context)

    fun getAll(): ArrayList<Expense> {
        val list = ArrayList<Expense>()
        val db = helper.readableDatabase
        val c = db.rawQuery("SELECT id, name, amount, category, date, note FROM expenses ORDER BY date DESC, id DESC", null)
        while (c.moveToNext()) {
            val note = c.getString(5)
            val row = Expense(
                c.getLong(0),
                c.getString(1),
                c.getDouble(2),
                c.getString(3),
                c.getString(4),
                if (note == null) "" else note,
            )
            list.add(row)
        }
        c.close()
        return list
    }

    fun getOne(id: Long): Expense? {
        val db = helper.readableDatabase
        val c = db.rawQuery(
            "SELECT id, name, amount, category, date, note FROM expenses WHERE id = " + id,
            null,
        )
        if (!c.moveToFirst()) {
            c.close()
            return null
        }
        val note = c.getString(5)
        val row = Expense(
            c.getLong(0),
            c.getString(1),
            c.getDouble(2),
            c.getString(3),
            c.getString(4),
            if (note == null) "" else note,
        )
        c.close()
        return row
    }

    fun insert(name: String, amount: Double, category: String, date: String, note: String) {
        val db = helper.writableDatabase
        val cv = ContentValues()
        cv.put("name", name)
        cv.put("amount", amount)
        cv.put("category", category)
        cv.put("date", date)
        cv.put("note", note)
        db.insert("expenses", null, cv)
    }

    fun update(id: Long, name: String, amount: Double, category: String, date: String, note: String) {
        val db = helper.writableDatabase
        val cv = ContentValues()
        cv.put("name", name)
        cv.put("amount", amount)
        cv.put("category", category)
        cv.put("date", date)
        cv.put("note", note)
        db.update("expenses", cv, "id = ?", arrayOf(id.toString()))
    }

    fun delete(id: Long) {
        val db = helper.writableDatabase
        db.delete("expenses", "id = " + id, null)
    }

    private class Helper(context: Context) : SQLiteOpenHelper(context, "spendlog.db", null, 1) {
        override fun onCreate(db: SQLiteDatabase) {
            db.execSQL(
                "CREATE TABLE expenses (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "name TEXT, " +
                    "amount REAL, " +
                    "category TEXT, " +
                    "date TEXT, " +
                    "note TEXT)",
            )
        }

        override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
            db.execSQL("DROP TABLE IF EXISTS expenses")
            onCreate(db)
        }
    }
}
