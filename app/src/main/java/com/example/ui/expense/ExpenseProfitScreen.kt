package com.example.ui.expense

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.AppDatabase
import com.example.data.local.entity.ExpenseEntity
import com.example.data.model.AppLanguage
import com.example.data.model.ExpenseCategory
import com.example.ui.theme.AgriAlertRed
import com.example.ui.theme.AgriGreenPrimary
import com.example.ui.theme.AgriTextPrimary
import com.example.ui.theme.AgriTextSecondary
import com.example.util.isCompactNarrowScreen
import com.example.util.toLocalizedDoubleOrNull
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseProfitScreen(
    currentLanguage: AppLanguage,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getInstance(context) }
    val dao = db.expenseDao()
    val coroutineScope = rememberCoroutineScope()

    val expenses by dao.getAllExpenses().collectAsState(initial = emptyList())
    val totalExpense by dao.getTotalExpenses().collectAsState(initial = 0.0)

    var showAddDialog by remember { mutableStateOf(false) }
    var amountInput by remember { mutableStateOf("") }
    var noteInput by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(ExpenseCategory.FERTILIZER) }
    var categoryDropdownExpanded by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }
    val isNarrow = isCompactNarrowScreen()

    // Approximate production sales for profit calculator demonstration
    val estimatedSalesRevenue = 35000.0
    val totalExp = totalExpense ?: 0.0
    val estimatedProfit = estimatedSalesRevenue - totalExp

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            Surface(color = AgriGreenPrimary, modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("expense_back_btn")
                    ) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = when (currentLanguage) {
                            AppLanguage.BENGALI -> "💵 চাষের খরচ ও লাভ হিসাব"
                            AppLanguage.HINDI -> "💵 खेती की लागत व लाभ ट्रैकर"
                            AppLanguage.ENGLISH -> "Expense & Profit Tracker"
                        },
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = AgriGreenPrimary,
                contentColor = Color.White,
                modifier = Modifier.testTag("add_expense_fab")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Expense")
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .imePadding(),
            contentAlignment = Alignment.TopCenter
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 760.dp),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 88.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Summary Profit & Loss Card
                item {
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = AgriGreenPrimary),
                        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = when (currentLanguage) {
                                    AppLanguage.BENGALI -> "📊 ফসলভিত্তিক আর্থিক সংক্ষিপ্তসার (Profit & Loss):"
                                    AppLanguage.HINDI -> "📊 फसलवार वित्तीय सारांश (लाभ व हानि):"
                                    AppLanguage.ENGLISH -> "📊 Crop-wise Financial Summary (Profit & Loss):"
                                },
                                color = com.example.ui.theme.RoyalTextSecondary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            if (isNarrow) {
                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Column {
                                        Text(
                                            text = when (currentLanguage) {
                                                AppLanguage.BENGALI -> "মোট উৎপাদন খরচ"
                                                AppLanguage.HINDI -> "कुल उत्पादन लागत"
                                                AppLanguage.ENGLISH -> "Total Production Cost"
                                            },
                                            color = Color.White,
                                            fontSize = 13.sp
                                        )
                                        Text(
                                            text = "₹${"%.0f".format(totalExp)}",
                                            color = com.example.ui.theme.RoyalAlertText,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 22.sp
                                        )
                                    }
                                    Column {
                                        Text(
                                            text = when (currentLanguage) {
                                                AppLanguage.BENGALI -> "আনুমানিক ফসল বিক্রি"
                                                AppLanguage.HINDI -> "अनुमानित फसल बिक्री"
                                                AppLanguage.ENGLISH -> "Estimated Crop Sales"
                                            },
                                            color = Color.White,
                                            fontSize = 13.sp
                                        )
                                        Text(
                                            text = "₹${"%.0f".format(estimatedSalesRevenue)}",
                                            color = com.example.ui.theme.RoyalSoftGold,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 22.sp
                                        )
                                    }
                                }
                            } else {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = when (currentLanguage) {
                                                AppLanguage.BENGALI -> "মোট উৎপাদন খরচ"
                                                AppLanguage.HINDI -> "कुल उत्पादन लागत"
                                                AppLanguage.ENGLISH -> "Total Production Cost"
                                            },
                                            color = Color.White,
                                            fontSize = 13.sp
                                        )
                                        Text(
                                            text = "₹${"%.0f".format(totalExp)}",
                                            color = com.example.ui.theme.RoyalAlertText,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 22.sp
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = when (currentLanguage) {
                                                AppLanguage.BENGALI -> "আনুমানিক ফসল বিক্রি"
                                                AppLanguage.HINDI -> "अनुमानित फसल बिक्री"
                                                AppLanguage.ENGLISH -> "Estimated Crop Sales"
                                            },
                                            color = Color.White,
                                            fontSize = 13.sp
                                        )
                                        Text(
                                            text = "₹${"%.0f".format(estimatedSalesRevenue)}",
                                            color = com.example.ui.theme.RoyalSoftGold,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 22.sp
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (estimatedProfit >= 0) com.example.ui.theme.RoyalGreenSecondaryBg else com.example.ui.theme.RoyalAlertBg,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (estimatedProfit >= 0) {
                                            when (currentLanguage) {
                                                AppLanguage.BENGALI -> "প্রত্যাশিত লাভ (Estimated Profit):"
                                                AppLanguage.HINDI -> "अनुमानित लाभ (Estimated Profit):"
                                                AppLanguage.ENGLISH -> "Estimated Profit:"
                                            }
                                        } else {
                                            when (currentLanguage) {
                                                AppLanguage.BENGALI -> "লোকসান (Loss):"
                                                AppLanguage.HINDI -> "हानि (Loss):"
                                                AppLanguage.ENGLISH -> "Loss:"
                                            }
                                        },
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "₹${"%.0f".format(estimatedProfit)}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 20.sp,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }

                // Quick Add Expense Inline Form
                if (showAddDialog) {
                    item {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text(
                                    text = when (currentLanguage) {
                                        AppLanguage.BENGALI -> "নতুন খরচ যোগ করুন:"
                                        AppLanguage.HINDI -> "नया खर्च जोड़ें:"
                                        AppLanguage.ENGLISH -> "Add New Expense:"
                                    },
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )

                                // Category
                                ExposedDropdownMenuBox(
                                    expanded = categoryDropdownExpanded,
                                    onExpandedChange = { categoryDropdownExpanded = !categoryDropdownExpanded }
                                ) {
                                    OutlinedTextField(
                                        value = "${selectedCategory.icon} ${selectedCategory.getLabel(currentLanguage)}",
                                        onValueChange = {},
                                        readOnly = true,
                                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryDropdownExpanded) },
                                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    ExposedDropdownMenu(
                                        expanded = categoryDropdownExpanded,
                                        onDismissRequest = { categoryDropdownExpanded = false }
                                    ) {
                                        ExpenseCategory.values().forEach { cat ->
                                            DropdownMenuItem(
                                                text = { Text("${cat.icon} ${cat.getLabel(currentLanguage)}") },
                                                onClick = {
                                                    selectedCategory = cat
                                                    categoryDropdownExpanded = false
                                                }
                                            )
                                        }
                                    }
                                }

                                // Amount
                                OutlinedTextField(
                                    value = amountInput,
                                    onValueChange = { amountInput = it },
                                    label = {
                                        Text(
                                            when (currentLanguage) {
                                                AppLanguage.BENGALI -> "টাকার পরিমাণ (₹)"
                                                AppLanguage.HINDI -> "राशि (₹ Amount)"
                                                AppLanguage.ENGLISH -> "Amount (₹)"
                                            }
                                        )
                                    },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.fillMaxWidth().testTag("expense_amount_input"),
                                    shape = RoundedCornerShape(12.dp)
                                )

                                // Note
                                OutlinedTextField(
                                    value = noteInput,
                                    onValueChange = { noteInput = it },
                                    label = {
                                        Text(
                                            when (currentLanguage) {
                                                AppLanguage.BENGALI -> "বিবরণ / নোট (ঐচ্ছিক)"
                                                AppLanguage.HINDI -> "विवरण / नोट (वैकल्पिक)"
                                                AppLanguage.ENGLISH -> "Description / Note (Optional)"
                                            }
                                        )
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp)
                                )

                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Button(
                                        enabled = !isSaving,
                                        onClick = {
                                            val amt = amountInput.toLocalizedDoubleOrNull()
                                            if (amt == null || amt <= 0.0) {
                                                val errMsg = when (currentLanguage) {
                                                    AppLanguage.BENGALI -> "অনুগ্রহ করে সঠিক টাকার পরিমাণ লিখুন।"
                                                    AppLanguage.HINDI -> "कृपया सही राशि दर्ज करें।"
                                                    AppLanguage.ENGLISH -> "Please enter a valid positive amount."
                                                }
                                                Toast.makeText(context, errMsg, Toast.LENGTH_SHORT).show()
                                                return@Button
                                            }
                                            coroutineScope.launch {
                                                isSaving = true
                                                try {
                                                    dao.insertExpense(
                                                        ExpenseEntity(
                                                            category = selectedCategory.code,
                                                            amount = amt,
                                                            note = noteInput
                                                        )
                                                    )
                                                    amountInput = ""
                                                    noteInput = ""
                                                    showAddDialog = false
                                                } catch (e: CancellationException) {
                                                    throw e
                                                } catch (_: Exception) {
                                                    Toast.makeText(context, "Error saving expense", Toast.LENGTH_SHORT).show()
                                                } finally {
                                                    isSaving = false
                                                }
                                            }
                                        },
                                        modifier = Modifier.weight(1f).heightIn(min = 48.dp).testTag("save_expense_btn"),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = AgriGreenPrimary,
                                            contentColor = Color.White
                                        ),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Text(
                                            when (currentLanguage) {
                                                AppLanguage.BENGALI -> "খরচ যোগ করুন"
                                                AppLanguage.HINDI -> "खर्च जोड़ें"
                                                AppLanguage.ENGLISH -> "Add Expense"
                                            },
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    Button(
                                        onClick = { showAddDialog = false },
                                        modifier = Modifier.weight(0.6f).heightIn(min = 48.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = AgriTextPrimary),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Text(
                                            when (currentLanguage) {
                                                AppLanguage.BENGALI -> "বাতিল"
                                                AppLanguage.HINDI -> "रद्द करें"
                                                AppLanguage.ENGLISH -> "Cancel"
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    Text(
                        text = when (currentLanguage) {
                            AppLanguage.BENGALI -> "খরচের তালিকা (${expenses.size}টি ভুক্তি):"
                            AppLanguage.HINDI -> "खर्च सूची (${expenses.size} प्रविष्टियां):"
                            AppLanguage.ENGLISH -> "Expense List (${expenses.size} entries):"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = AgriTextPrimary
                    )
                }

                if (expenses.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Text(
                                text = when (currentLanguage) {
                                    AppLanguage.BENGALI -> "কোনো খরচ যোগ করা হয়নি। '+' বোতাম চেপে বীজ, সার, কীটনাশক বা শ্রমিক খরচের হিসাব রাখুন।"
                                    AppLanguage.HINDI -> "कोई खर्च नहीं जोड़ा गया है। बीज, खाद, कीटनाशक या मजदूरी का हिसाब रखने के लिए '+' बटन दबाएं।"
                                    AppLanguage.ENGLISH -> "No expenses added yet. Tap '+' to record seed, fertilizer, pesticide, or labor expenses."
                                },
                                modifier = Modifier.padding(20.dp),
                                color = AgriTextSecondary
                            )
                        }
                    }
                } else {
                    items(expenses) { exp ->
                        val matchedCategory = ExpenseCategory.values().find {
                            it.code.equals(exp.category, true) ||
                                it.bnName == exp.category ||
                                it.hiName == exp.category ||
                                it.enName == exp.category
                        }
                        val catDisplay = matchedCategory?.let { "${it.icon} ${it.getLabel(currentLanguage)}" } ?: exp.category

                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = catDisplay, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = AgriTextPrimary)
                                    if (exp.note.isNotBlank()) {
                                        Text(text = exp.note, fontSize = 13.sp, color = AgriTextSecondary)
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "₹${"%.0f".format(exp.amount)}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    IconButton(
                                        modifier = Modifier.size(48.dp),
                                        onClick = {
                                            coroutineScope.launch {
                                                try {
                                                    dao.deleteExpense(exp.localId)
                                                } catch (e: CancellationException) {
                                                    throw e
                                                } catch (_: Exception) {
                                                    Toast.makeText(context, "Could not delete entry", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        }
                                    ) {
                                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFF9E9E9E))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun ExpenseCategory.getLabel(lang: AppLanguage): String = when (lang) {
    AppLanguage.BENGALI -> bnName
    AppLanguage.HINDI -> hiName
    AppLanguage.ENGLISH -> enName
}
