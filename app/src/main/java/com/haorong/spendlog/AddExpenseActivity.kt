// Haorong Song  3184339
// One form for adding and editing. Started by an explicit intent.
// A missing extra means "add". An expense id means "edit".

package com.haorong.spendlog

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.CancellationSignal
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

class AddExpenseActivity : ComponentActivity() {
    private val repository by lazy { ExpenseRepository(this) }
    private val form = ExpenseForm()
    private var editingId: Long = -1L
    private var keptLat: Double? = null
    private var keptLng: Double? = null
    private var pending: PendingExpense? = null
    private var locationDone = false
    private var locationSignal: CancellationSignal? = null
    private val handler = Handler(Looper.getMainLooper())

    private val locationPermission = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { grants ->
        val granted = grants[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            grants[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            captureAndSave()
        } else {
            saveExpense(null, null)
        }
    }

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
            keptLat = existing.latitude
            keptLng = existing.longitude
        }
        if (savedInstanceState != null) {
            form.name = savedInstanceState.getString(KEY_NAME, form.name)
            form.amount = savedInstanceState.getString(KEY_AMOUNT, form.amount)
            form.category = savedInstanceState.getString(KEY_CATEGORY, form.category)
            form.note = savedInstanceState.getString(KEY_NOTE, form.note)
            val epochDay = savedInstanceState.getLong(KEY_DATE, form.date.toEpochDay())
            form.date = LocalDate.ofEpochDay(epochDay)
        }
        title = if (editingId < 0L) "Add expense" else "Edit expense"

        setContent {
            SpendLogTheme {
                AddExpenseScreen(
                    form = form,
                    title = if (editingId < 0L) "Add expense" else "Edit expense",
                    onBack = { finish() },
                    onSave = { onSaveClicked() },
                )
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(KEY_NAME, form.name)
        outState.putString(KEY_AMOUNT, form.amount)
        outState.putString(KEY_CATEGORY, form.category)
        outState.putString(KEY_NOTE, form.note)
        outState.putLong(KEY_DATE, form.date.toEpochDay())
    }

    override fun onDestroy() {
        locationSignal?.cancel()
        handler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }

    private fun onSaveClicked() {
        if (form.saving) return
        val name = form.name.trim()
        if (name.isEmpty()) {
            form.error = "Enter a name."
            return
        }
        val amount = form.amount.trim().toDoubleOrNull()
        if (amount == null || amount <= 0.0) {
            form.error = "Enter an amount greater than 0."
            return
        }
        pending = PendingExpense(
            name = name,
            amount = amount,
            category = form.category,
            date = form.date.toString(),
            note = form.note.trim(),
        )
        form.error = null
        form.saving = true
        val fine = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
        val coarse = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
        if (fine == PackageManager.PERMISSION_GRANTED || coarse == PackageManager.PERMISSION_GRANTED) {
            captureAndSave()
        } else {
            locationPermission.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                ),
            )
        }
    }

    @SuppressLint("MissingPermission")
    private fun captureAndSave() {
        val manager = getSystemService(LocationManager::class.java)
        val recent = latestKnown(manager)
        if (recent != null && System.currentTimeMillis() - recent.time < RECENT_FIX_MS) {
            saveExpense(recent.latitude, recent.longitude)
            return
        }
        val fine = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED
        val provider = when {
            fine && manager.isProviderEnabled(LocationManager.GPS_PROVIDER) -> LocationManager.GPS_PROVIDER
            manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) -> LocationManager.NETWORK_PROVIDER
            else -> null
        }
        if (provider == null) {
            saveExpense(recent?.latitude, recent?.longitude)
            return
        }

        locationDone = false
        val signal = CancellationSignal()
        locationSignal = signal
        handler.postDelayed({
            val fallback = latestKnown(manager)
            deliverLocation(fallback?.latitude, fallback?.longitude)
        }, LOCATION_TIMEOUT_MS)

        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                manager.getCurrentLocation(provider, signal, mainExecutor) { location ->
                    if (location != null) {
                        deliverLocation(location.latitude, location.longitude)
                    } else {
                        val fallback = latestKnown(manager)
                        deliverLocation(fallback?.latitude, fallback?.longitude)
                    }
                }
            } else {
                @Suppress("DEPRECATION")
                manager.requestSingleUpdate(
                    provider,
                    object : LocationListener {
                        override fun onLocationChanged(location: Location) {
                            deliverLocation(location.latitude, location.longitude)
                        }

                        @Deprecated("Deprecated in Java")
                        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) = Unit

                        override fun onProviderEnabled(provider: String) = Unit
                        override fun onProviderDisabled(provider: String) = Unit
                    },
                    Looper.getMainLooper(),
                )
            }
        } catch (_: SecurityException) {
            deliverLocation(null, null)
        }
    }

    private fun deliverLocation(latitude: Double?, longitude: Double?) {
        if (locationDone || isDestroyed) return
        locationDone = true
        locationSignal?.cancel()
        handler.removeCallbacksAndMessages(null)
        saveExpense(latitude, longitude)
    }

    @SuppressLint("MissingPermission")
    private fun latestKnown(manager: LocationManager): Location? {
        var best: Location? = null
        val providers = try {
            manager.getProviders(true)
        } catch (_: Exception) {
            emptyList()
        }
        for (provider in providers) {
            val location = try {
                manager.getLastKnownLocation(provider)
            } catch (_: SecurityException) {
                null
            } ?: continue
            if (best == null || location.time > best.time) best = location
        }
        return best
    }

    private fun saveExpense(latitude: Double?, longitude: Double?) {
        val draft = pending
        if (draft == null) {
            form.saving = false
            return
        }
        val useLat = latitude ?: keptLat
        val useLng = longitude ?: keptLng
        val expense = Expense(
            id = editingId,
            name = draft.name,
            amount = draft.amount,
            category = draft.category,
            date = draft.date,
            note = draft.note,
            latitude = useLat,
            longitude = useLng,
        )
        if (editingId < 0L) {
            repository.insert(expense)
        } else {
            repository.update(expense)
        }
        val message = when {
            latitude != null && longitude != null -> "Saved"
            useLat != null && useLng != null -> "Saved. Location unchanged."
            else -> "Saved without a location"
        }
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        form.saving = false
        finish()
    }

    companion object {
        const val EXTRA_ID = "expense_id"
        private const val KEY_NAME = "name"
        private const val KEY_AMOUNT = "amount"
        private const val KEY_CATEGORY = "category"
        private const val KEY_NOTE = "note"
        private const val KEY_DATE = "date"
        private const val RECENT_FIX_MS = 2 * 60 * 1000L
        private const val LOCATION_TIMEOUT_MS = 8_000L
    }
}

private class ExpenseForm {
    var name by mutableStateOf("")
    var amount by mutableStateOf("")
    var category by mutableStateOf("food")
    var date by mutableStateOf(LocalDate.now())
    var note by mutableStateOf("")
    var saving by mutableStateOf(false)
    var error by mutableStateOf<String?>(null)
    var showDatePicker by mutableStateOf(false)
    var categoryOpen by mutableStateOf(false)
}

private data class PendingExpense(
    val name: String,
    val amount: Double,
    val category: String,
    val date: String,
    val note: String,
)

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
            Text(
                text = "Name, amount, category, date and a note. Location is read once, when you save.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
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
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = formatDate(form.date.toString()),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Date") },
                    modifier = Modifier.fillMaxWidth(),
                )
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() },
                        ) { form.showDatePicker = true },
                )
            }
            OutlinedTextField(
                value = form.note,
                onValueChange = { form.note = it },
                label = { Text("Note") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
            )
            Text(
                text = "If you deny location, the expense is still stored. The place is just left empty.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall,
            )
            form.error?.let { message ->
                Text(text = message, color = MaterialTheme.colorScheme.error)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Button(
                onClick = onSave,
                enabled = !form.saving,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(if (form.saving) "Saving…" else "Save")
            }
        }
    }

    if (form.showDatePicker) {
        val initial = form.date.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        val pickerState = rememberDatePickerState(initialSelectedDateMillis = initial)
        DatePickerDialog(
            onDismissRequest = { form.showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        pickerState.selectedDateMillis?.let { millis ->
                            form.date = Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()
                        }
                        form.showDatePicker = false
                    },
                ) { Text("OK") }
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
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = onExpanded,
    ) {
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
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { onExpanded(false) },
        ) {
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
    val whole = cleaned.substring(0, dot)
    val fraction = cleaned.substring(dot + 1).replace(".", "").take(2)
    return "$whole.$fraction"
}
