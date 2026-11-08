// Haorong Song  3184339
// home: month total + list
// explicit intent -> add page, and -> detail page

package com.haorong.spendlog

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate

class MainActivity : ComponentActivity() {

    private lateinit var db: ExpenseDb
    private var rows by mutableStateOf(ArrayList<Expense>())
    private var monthTotal by mutableStateOf(0.0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        db = ExpenseDb(this)
        load()
        setContent {
            SpendLogTheme {
                HomePage(
                    rows = rows,
                    monthTotal = monthTotal,
                    onAdd = {
                        // explicit
                        val intent = Intent(this, AddExpenseActivity::class.java)
                        startActivity(intent)
                    },
                    onOpen = { id ->
                        // explicit, pass which row
                        val intent = Intent(this, ExpenseDetailActivity::class.java)
                        intent.putExtra("expense_id", id)
                        startActivity(intent)
                    },
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        load()
    }

    private fun load() {
        rows = db.getAll()
        val today = LocalDate.now()
        val prefix = today.year.toString() + "-" + String.format("%02d", today.monthValue)
        var total = 0.0
        for (i in rows.indices) {
            if (rows[i].date.startsWith(prefix)) {
                total = total + rows[i].amount
            }
        }
        monthTotal = total
    }
}

@Composable
private fun HomePage(
    rows: ArrayList<Expense>,
    monthTotal: Double,
    onAdd: () -> Unit,
    onOpen: (Long) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
    ) {
        Text(text = "SpendLog", fontSize = 28.sp, modifier = Modifier.padding(bottom = 12.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primary)
                .padding(16.dp),
        ) {
            Text(text = "Spent this month", color = Color.White)
            Text(
                text = euro(monthTotal),
                color = Color.White,
                fontSize = 34.sp,
                modifier = Modifier.padding(top = 4.dp, bottom = 12.dp),
            )
            Button(
                onClick = onAdd,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = MaterialTheme.colorScheme.primary,
                ),
            ) {
                Text("Add expense")
            }
        }

        if (rows.size == 0) {
            Text(
                text = "No expenses yet.",
                modifier = Modifier.padding(top = 16.dp),
            )
        }

        for (i in rows.indices) {
            val row = rows[i]
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp)
                    .background(Color.White)
                    .clickable { onOpen(row.id) }
                    .padding(12.dp),
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = row.name, fontSize = 18.sp)
                    Text(
                        text = categoryName(row.category) + "  " + showDate(row.date),
                        fontSize = 13.sp,
                    )
                }
                Text(text = euro(row.amount), fontSize = 16.sp)
            }
        }
    }
}
