// Haorong Song  3184339
// one expense
// edit is explicit, open in maps is implicit

package com.haorong.spendlog

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class ExpenseDetailActivity : ComponentActivity() {

    private lateinit var db: ExpenseDb
    private var expenseId: Long = -1
    private var row by mutableStateOf<Expense?>(null)
    private var ask by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        db = ExpenseDb(this)
        expenseId = intent.getLongExtra("expense_id", -1)
        if (expenseId < 0) {
            finish()
            return
        }
        setContent {
            SpendLogTheme {
                val item = row
                if (item != null) {
                    DetailPage(
                        item = item,
                        onClose = { finish() },
                        onEdit = {
                            val intent = Intent(this, AddExpenseActivity::class.java)
                            intent.putExtra("expense_id", item.id)
                            startActivity(intent)
                        },
                        onDelete = { ask = true },
                        onMaps = { openMaps(item) },
                    )
                }
                if (ask && item != null) {
                    AlertDialog(
                        onDismissRequest = { ask = false },
                        title = { Text("Delete " + item.name + "?") },
                        text = { Text("This takes it off the list.") },
                        confirmButton = {
                            TextButton(onClick = {
                                db.delete(expenseId)
                                finish()
                            }) { Text("Delete") }
                        },
                        dismissButton = {
                            TextButton(onClick = { ask = false }) { Text("Cancel") }
                        },
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val found = db.getOne(expenseId)
        if (found == null) {
            finish()
            return
        }
        row = found
    }

    private fun openMaps(item: Expense) {
        // implicit, don't set a package
        val uri = Uri.parse("geo:0,0?q=" + Uri.encode(item.name))
        val intent = Intent(Intent.ACTION_VIEW, uri)
        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, "no maps app", Toast.LENGTH_SHORT).show()
        }
    }
}

@Composable
private fun DetailPage(
    item: Expense,
    onClose: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onMaps: () -> Unit,
) {
    Column(modifier = Modifier.padding(16.dp)) {
        TextButton(onClick = onClose) { Text("Close") }
        Text(text = item.name, fontSize = 28.sp)
        Text(text = euro(item.amount), fontSize = 36.sp, modifier = Modifier.padding(bottom = 12.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(12.dp),
        ) {
            Text(text = "Category", fontSize = 13.sp)
            Text(text = categoryName(item.category), fontSize = 18.sp, modifier = Modifier.padding(bottom = 8.dp))
            Text(text = "Date", fontSize = 13.sp)
            Text(text = showDate(item.date), fontSize = 18.sp, modifier = Modifier.padding(bottom = 8.dp))
            Text(text = "Note", fontSize = 13.sp)
            if (item.note == "") {
                Text(text = "none", fontSize = 18.sp)
            } else {
                Text(text = item.note, fontSize = 18.sp)
            }
        }

        Row(modifier = Modifier.padding(top = 12.dp)) {
            OutlinedButton(onClick = onEdit, modifier = Modifier.weight(1f)) {
                Text("Edit")
            }
            OutlinedButton(
                onClick = onDelete,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp),
            ) {
                Text("Delete")
            }
        }
        Button(
            onClick = onMaps,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
        ) {
            Text("Open in Maps")
        }
    }
}
