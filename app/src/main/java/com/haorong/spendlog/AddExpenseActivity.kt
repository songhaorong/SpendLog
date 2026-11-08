// Haorong Song  3184339

package com.haorong.spendlog

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

class AddExpenseActivity : ComponentActivity() {
    private val repository by lazy { ExpenseRepository(this) }
    private val form = ExpenseForm()
    private var editingId: Long = -1L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        editingId = intent.getLongExtra(EXTRA_ID, -1L)
        if (editingId >= 0L) {
            val existing = repository.get(editingId)
            if (existing == null) {
                finish()
                return
            }
            form.name = existing.name
            form.amount = String.format(java.util.Locale.US, "%.2f", existing.amount)
            form.category = existing.category
            form.date = LocalDate.parse(existing.date)
            form.note = existing.note
        }

        setContent {
            SpendLogTheme {
                AddExpenseScreen(
                    form = form,
                    title = if (editingId < 0L) "Add expense" else "Edit expense",
                    onBack = { finish() },
                    onSave = { save() },
                )
            }
        }
    }

    private fun save() {
        val name = form.name.trim()
        val amount = form.amount.trim().toDoubleOrNull()
        if (name.isEmpty()) {
            Toast.makeText(this, "Enter a name", Toast.LENGTH_SHORT).show()
            return
        }
        if (amount == null || amount <= 0.0) {
            Toast.makeText(this, "Enter an amount", Toast.LENGTH_SHORT).show()
            return
        }
        val expense = Expense(
            id = editingId,
            name = name,
            amount = amount,
            category = form.category,
            date = form.date.toString(),
            note = form.note.trim(),
        )
        if (editingId < 0L) {
            repository.insert(expense)
        } else {
            repository.update(expense)
        }
        finish()
    }

    companion object {
        const val EXTRA_ID = "expense_id"
    }
}

private class ExpenseForm {
    var name by mutableStateOf("")
    var amount by mutableStateOf("")
    var category by mutableStateOf("food")
    var date by mutableStateOf(LocalDate.now())
    var note by mutableStateOf("")
    var showDatePicker by mutableStateOf(false)
    var categoryOpen by mutableStateOf(false)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddExpenseScreen(
    form: ExpenseForm,
    title: String,
    onBack: () -> Unit,
    onSave: () -> Unit,
) {
    Scaffold(containerColor = MaterialTheme.colorScheme.background) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            TextButton(onClick = onBack) { Text("Close") }
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.SemiBold,
            )
            OutlinedTextField(
                value = form.name,
                onValueChange = { form.name = it },
                label = { Text("What") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = form.amount,
                onValueChange = { form.amount = cleanAmount(it) },
                label = { Text("Amount") },
                prefix = { Text("€") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            CategoryField(
                category = form.category,
                expanded = form.categoryOpen,
                onExpanded = { form.categoryOpen = it },
                onSelected = { form.category = it },
            )
            OutlinedTextField(
                value = formatDate(form.date.toString()),
                onValueChange = {},
                readOnly = true,
                label = { Text("Date") },
                modifier = Modifier.fillMaxWidth(),
            )
            TextButton(onClick = { form.showDatePicker = true }) {
                Text("Change date")
            }
            OutlinedTextField(
                value = form.note,
                onValueChange = { form.note = it },
                label = { Text("Note") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
            )
            Button(onClick = onSave, modifier = Modifier.fillMaxWidth()) {
                Text("Save")
            }
        }
    }

    if (form.showDatePicker) {
        val initial = form.date.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        val pickerState = rememberDatePickerState(initialSelectedDateMillis = initial)
        DatePickerDialog(
            onDismissRequest = { form.showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let { millis ->
                        form.date = Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()
                    }
                    form.showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { form.showDatePicker = false }) { Text("Cancel") }
            },
        ) {
            DatePicker(state = pickerState)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryField(
    category: String,
    expanded: Boolean,
    onExpanded: (Boolean) -> Unit,
    onSelected: (String) -> Unit,
) {
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = onExpanded) {
        OutlinedTextField(
            value = categoryLabel(category),
            onValueChange = {},
            readOnly = true,
            label = { Text("Category") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth(),
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { onExpanded(false) }) {
            CATEGORIES.forEach { (value, label) ->
                DropdownMenuItem(
                    text = { Text(label) },
                    onClick = {
                        onSelected(value)
                        onExpanded(false)
                    },
                )
            }
        }
    }
}

private fun cleanAmount(raw: String): String {
    val cleaned = raw.replace(',', '.').filter { it.isDigit() || it == '.' }
    val dot = cleaned.indexOf('.')
    if (dot < 0) return cleaned
    val fraction = cleaned.substring(dot + 1).replace(".", "").take(2)
    return cleaned.substring(0, dot) + "." + fraction
}
