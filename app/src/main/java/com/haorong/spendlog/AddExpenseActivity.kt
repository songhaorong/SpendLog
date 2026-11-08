// Haorong Song  3184339
// add and edit use this same page
// if expense_id is missing, it's a new one

package com.haorong.spendlog

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

class AddExpenseActivity : ComponentActivity() {

    private lateinit var db: ExpenseDb
    private var editingId: Long = -1

    private var name by mutableStateOf("")
    private var amountText by mutableStateOf("")
    private var category by mutableStateOf("food")
    private var date by mutableStateOf(LocalDate.now())
    private var note by mutableStateOf("")
    private var showPicker by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        db = ExpenseDb(this)

        editingId = intent.getLongExtra("expense_id", -1)
        if (editingId >= 0) {
            val old = db.getOne(editingId)
            if (old == null) {
                finish()
                return
            }
            name = old.name
            amountText = String.format(java.util.Locale.US, "%.2f", old.amount)
            category = old.category
            date = LocalDate.parse(old.date)
            note = old.note
        }

        val title = if (editingId < 0) "Add expense" else "Edit expense"
        setContent {
            SpendLogTheme {
                FormPage(
                    title = title,
                    name = name,
                    amountText = amountText,
                    category = category,
                    dateText = showDate(date.toString()),
                    note = note,
                    onName = { name = it },
                    onAmount = { amountText = keepAmount(it) },
                    onCategory = { category = it },
                    onNote = { note = it },
                    onChangeDate = { showPicker = true },
                    onClose = { finish() },
                    onSave = { save() },
                )
                if (showPicker) {
                    DateBox(
                        start = date,
                        onOk = { picked ->
                            date = picked
                            showPicker = false
                        },
                        onCancel = { showPicker = false },
                    )
                }
            }
        }
    }

    private fun save() {
        val cleanName = name.trim()
        if (cleanName == "") {
            Toast.makeText(this, "Enter a name", Toast.LENGTH_SHORT).show()
            return
        }
        val amount = amountText.toDoubleOrNull()
        if (amount == null || amount <= 0) {
            Toast.makeText(this, "Enter an amount", Toast.LENGTH_SHORT).show()
            return
        }

        val day = date.toString()
        val text = note.trim()
        if (editingId < 0) {
            db.insert(cleanName, amount, category, day, text)
        } else {
            db.update(editingId, cleanName, amount, category, day, text)
        }
        finish()
    }
}

// only digits and one dot, two numbers after the dot
private fun keepAmount(raw: String): String {
    var s = ""
    var dot = false
    var after = 0
    for (i in raw.indices) {
        val ch = raw[i]
        if (ch == ',' || ch == '.') {
            if (!dot) {
                s = s + "."
                dot = true
            }
        } else if (ch.isDigit()) {
            if (dot) {
                if (after < 2) {
                    s = s + ch
                    after = after + 1
                }
            } else {
                s = s + ch
            }
        }
    }
    return s
}

@Composable
private fun FormPage(
    title: String,
    name: String,
    amountText: String,
    category: String,
    dateText: String,
    note: String,
    onName: (String) -> Unit,
    onAmount: (String) -> Unit,
    onCategory: (String) -> Unit,
    onNote: (String) -> Unit,
    onChangeDate: () -> Unit,
    onClose: () -> Unit,
    onSave: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
    ) {
        TextButton(onClick = onClose) { Text("Close") }
        Text(text = title, fontSize = 26.sp, modifier = Modifier.padding(bottom = 8.dp))

        OutlinedTextField(
            value = name,
            onValueChange = onName,
            label = { Text("What") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = amountText,
            onValueChange = onAmount,
            label = { Text("Amount") },
            prefix = { Text("€") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
        )

        Text(text = "Category", modifier = Modifier.padding(top = 12.dp, bottom = 4.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            TextButton(onClick = { onCategory("food") }) { Text("Food") }
            TextButton(onClick = { onCategory("transport") }) { Text("Transport") }
        }
        Row(modifier = Modifier.fillMaxWidth()) {
            TextButton(onClick = { onCategory("shopping") }) { Text("Shopping") }
            TextButton(onClick = { onCategory("other") }) { Text("Other") }
        }
        Text(text = "picked: " + categoryName(category))

        OutlinedTextField(
            value = dateText,
            onValueChange = {},
            readOnly = true,
            label = { Text("Date") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
        )
        TextButton(onClick = onChangeDate) { Text("Change date") }

        OutlinedTextField(
            value = note,
            onValueChange = onNote,
            label = { Text("Note") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            minLines = 3,
        )
        Button(
            onClick = onSave,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
        ) {
            Text("Save")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateBox(start: LocalDate, onOk: (LocalDate) -> Unit, onCancel: () -> Unit) {
    val millis = start.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
    val state = rememberDatePickerState(initialSelectedDateMillis = millis)
    DatePickerDialog(
        onDismissRequest = onCancel,
        confirmButton = {
            TextButton(onClick = {
                val picked = state.selectedDateMillis
                if (picked != null) {
                    onOk(Instant.ofEpochMilli(picked).atZone(ZoneOffset.UTC).toLocalDate())
                } else {
                    onCancel()
                }
            }) { Text("OK") }
        },
        dismissButton = {
            TextButton(onClick = onCancel) { Text("Cancel") }
        },
    ) {
        DatePicker(state = state)
    }
}
