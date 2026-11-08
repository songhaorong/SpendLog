// Haorong Song  3184339

package com.haorong.spendlog

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    private val repository by lazy { ExpenseRepository(this) }
    private var expenses by mutableStateOf<List<Expense>>(emptyList())
    private var monthTotal by mutableStateOf(0.0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        reload()
        setContent {
            SpendLogTheme {
                HomeScreen(
                    expenses = expenses,
                    monthTotal = monthTotal,
                    monthTitle = currentMonthTitle(),
                    onAdd = { openAdd() },
                    onOpen = { openDetail(it) },
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        reload()
    }

    private fun reload() {
        val rows = repository.all()
        expenses = rows
        monthTotal = rows.filter { isInMonth(it.date) }.sumOf { it.amount }
    }

    private fun openAdd() {
        startActivity(Intent(this, AddExpenseActivity::class.java))
    }

    private fun openDetail(id: Long) {
        val intent = Intent(this, ExpenseDetailActivity::class.java)
        intent.putExtra(ExpenseDetailActivity.EXTRA_ID, id)
        startActivity(intent)
    }
}

@Composable
private fun HomeScreen(
    expenses: List<Expense>,
    monthTotal: Double,
    monthTitle: String,
    onAdd: () -> Unit,
    onOpen: (Long) -> Unit,
) {
    Scaffold(containerColor = MaterialTheme.colorScheme.background) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
                Text(
                    text = "SpendLog",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 16.dp),
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primary, MaterialTheme.shapes.large)
                        .padding(18.dp),
                ) {
                    Text(
                        text = "Spent this month",
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                    )
                    Text(
                        text = monthTitle,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                        fontSize = 13.sp,
                    )
                    Text(
                        text = formatMoney(monthTotal),
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontSize = 36.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(top = 6.dp, bottom = 14.dp),
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
            }

            if (expenses.isEmpty()) {
                Text(
                    text = "No expenses yet.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 20.dp),
                )
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 28.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(expenses, key = { it.id }) { expense ->
                        ExpenseRow(expense = expense, onOpen = { onOpen(expense.id) })
                    }
                }
            }
        }
    }
}

@Composable
private fun ExpenseRow(expense: Expense, onOpen: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface, MaterialTheme.shapes.medium)
            .clickable(onClick = onOpen)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = expense.name, fontWeight = FontWeight.Medium, fontSize = 17.sp)
            Text(
                text = categoryLabel(expense.category) + "  ·  " + formatDate(expense.date),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 13.sp,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
        Text(
            text = formatMoney(expense.amount),
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
            fontSize = 16.sp,
        )
    }
}
