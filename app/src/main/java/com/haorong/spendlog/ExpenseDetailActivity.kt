// Haorong Song  3184339
// One expense. Edit is an explicit intent back to the form.
// Open in Maps is an implicit ACTION_VIEW on a geo: uri.

package com.haorong.spendlog

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class ExpenseDetailActivity : ComponentActivity() {
    private val repository by lazy { ExpenseRepository(this) }
    private var expenseId: Long = -1L
    private var expense by mutableStateOf<Expense?>(null)
    private var askDelete by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        expenseId = intent.getLongExtra(EXTRA_ID, -1L)
        expense = repository.get(expenseId)
        if (expenseId < 0L || expense == null) {
            finish()
            return
        }
        setContent {
            SpendLogTheme {
                val row = expense
                if (row == null) {
                    Scaffold(containerColor = MaterialTheme.colorScheme.background) { padding ->
                        Text(
                            text = "Loading…",
                            modifier = Modifier.padding(padding).padding(20.dp),
                        )
                    }
                } else {
                    DetailScreen(
                        expense = row,
                        onBack = { finish() },
                        onEdit = { openEdit(row.id) },
                        onDelete = { askDelete = true },
                        onMaps = { openMaps(row) },
                    )
                }
                if (askDelete && row != null) {
                    AlertDialog(
                        onDismissRequest = { askDelete = false },
                        title = { Text("Delete this expense?") },
                        text = { Text("This removes ${row.name} from the list.") },
                        confirmButton = {
                            TextButton(onClick = { deleteAndClose() }) { Text("Delete") }
                        },
                        dismissButton = {
                            TextButton(onClick = { askDelete = false }) { Text("Cancel") }
                        },
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val row = repository.get(expenseId)
        if (row == null) {
            finish()
            return
        }
        expense = row
    }

    private fun openEdit(id: Long) {
        val intent = Intent(this, AddExpenseActivity::class.java)
        intent.putExtra(AddExpenseActivity.EXTRA_ID, id)
        startActivity(intent)
    }

    private fun deleteAndClose() {
        repository.delete(expenseId)
        Toast.makeText(this, "Deleted", Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun openMaps(row: Expense) {
        val latitude = row.latitude
        val longitude = row.longitude
        if (latitude == null || longitude == null) {
            Toast.makeText(this, "No location saved for this expense", Toast.LENGTH_SHORT).show()
            return
        }
        val label = Uri.encode(row.name)
        val uri = Uri.parse("geo:$latitude,$longitude?q=$latitude,$longitude($label)")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        try {
            startActivity(intent)
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(this, "no maps app", Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        const val EXTRA_ID = "expense_id"
    }
}

@Composable
private fun DetailScreen(
    expense: Expense,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onMaps: () -> Unit,
) {
    Scaffold(containerColor = MaterialTheme.colorScheme.background) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            TextButton(onClick = onBack) { Text("Close") }
            Text(
                text = expense.name,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = formatMoney(expense.amount),
                color = MaterialTheme.colorScheme.primary,
                fontSize = 40.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface, MaterialTheme.shapes.large)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                DetailLine("Category", categoryLabel(expense.category))
                DetailLine("Date", formatDate(expense.date))
                DetailLine("Note", expense.note.ifBlank { "—" })
                DetailLine("Location", formatLocation(expense.latitude, expense.longitude))
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                OutlinedButton(onClick = onEdit, modifier = Modifier.weight(1f)) {
                    Text("Edit")
                }
                OutlinedButton(onClick = onDelete, modifier = Modifier.weight(1f)) {
                    Text("Delete")
                }
            }
            Button(onClick = onMaps, modifier = Modifier.fillMaxWidth()) {
                Text("Open in Maps")
            }
        }
    }
}

@Composable
private fun DetailLine(label: String, value: String) {
    Column {
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 13.sp,
        )
        Text(text = value, fontSize = 17.sp)
    }
}
