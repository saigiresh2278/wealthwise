package com.example.wealthwiseai.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.wealthwiseai.data.local.entity.TransactionEntity
import com.example.wealthwiseai.ui.components.CustomDropdown
import com.example.wealthwiseai.ui.components.CustomTextField
import com.example.wealthwiseai.ui.components.SectionHeader
import com.example.wealthwiseai.ui.theme.*
import com.example.wealthwiseai.viewmodel.TransactionViewModel
import java.text.SimpleDateFormat
import java.util.*
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import com.example.wealthwiseai.util.ReceiptOcrHelper
import com.example.wealthwiseai.util.ReceiptParser
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun TransactionManagementScreen(
    viewModel: TransactionViewModel,
    onBack: () -> Unit
) {
    val transactions by viewModel.transactions.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var editingTransaction by remember { mutableStateOf<TransactionEntity?>(null) }

    // Dialog state fields
    var amountStr by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Food") }
    var note by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("Expense") }
    
    // Dialog validations
    var amountError by remember { mutableStateOf("") }

    val categories = listOf("Food", "Travel", "Rent", "Education", "Shopping", "Bills", "Health", "Investment", "Salary", "Freelance", "Others")
    val types = listOf("Income", "Expense")

    // Scanning states and helper launch tools
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var isScanning by remember { mutableStateOf(false) }
    var isOcrPopulated by remember { mutableStateOf(false) }
    var scanningError by remember { mutableStateOf<String?>(null) }
    var showScanOptionDialog by remember { mutableStateOf(false) }
    var tempPhotoUri by remember { mutableStateOf<Uri?>(null) }

    fun processReceiptUri(uri: Uri) {
        isScanning = true
        scanningError = null
        coroutineScope.launch {
            try {
                val rawText = ReceiptOcrHelper.recognizeText(context, uri)
                if (rawText.trim().isNotEmpty()) {
                    val parsed = ReceiptParser.parse(rawText)
                    amountStr = if (parsed.amount > 0) parsed.amount.toString() else ""
                    category = parsed.category
                    note = parsed.merchant
                    type = "Expense"
                    amountError = ""
                    isOcrPopulated = true
                    showAddDialog = true
                } else {
                    scanningError = "No text could be extracted from the receipt. Please try again with a clearer image."
                }
            } catch (e: Exception) {
                scanningError = "Failed to parse receipt: ${e.localizedMessage}"
            } finally {
                isScanning = false
            }
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            processReceiptUri(uri)
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        if (success) {
            tempPhotoUri?.let { uri ->
                processReceiptUri(uri)
            }
        }
    }

    fun createTempPhotoUri(): Uri {
        val tempFile = File(context.cacheDir, "receipt_${System.currentTimeMillis()}.jpg")
        return FileProvider.getUriForFile(
            context,
            "com.company.finanicaltracker.wealthai.fileprovider",
            tempFile
        )
    }

    LaunchedEffect(showAddDialog, editingTransaction) {
        if (isOcrPopulated) {
            isOcrPopulated = false
        } else if (editingTransaction != null) {
            amountStr = editingTransaction!!.amount.toString()
            category = editingTransaction!!.category
            note = editingTransaction!!.note
            type = editingTransaction!!.type
            amountError = ""
        } else {
            amountStr = ""
            category = "Food"
            note = ""
            type = "Expense"
            amountError = ""
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SectionHeader("Manage Transactions")
                Button(
                    onClick = { showScanOptionDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = BlueAccent)
                ) {
                    Text("Add Expense", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (transactions.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No transactions logged.\nTap 'Add New' to record income or expenses.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextGray,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(transactions) { tx ->
                        TransactionManageRow(
                            tx = tx,
                            onEdit = { editingTransaction = tx },
                            onDelete = { viewModel.deleteTransaction(tx) }
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Add / Edit Dialog
        if (showAddDialog || editingTransaction != null) {
            val isEditing = editingTransaction != null
            AlertDialog(
                onDismissRequest = {
                    showAddDialog = false
                    editingTransaction = null
                },
                title = {
                    Text(
                        text = if (isEditing) "Edit Transaction" else "Add Transaction",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        CustomDropdown(
                            selectedOption = type,
                            onOptionSelected = { type = it },
                            options = types,
                            label = "Type"
                        )
                        CustomTextField(
                            value = amountStr,
                            onValueChange = {
                                amountStr = it
                                val amt = it.toDoubleOrNull()
                                amountError = when {
                                    it.trim().isEmpty() -> "Amount is required"
                                    amt == null || amt <= 0.0 -> "Amount must be positive"
                                    else -> ""
                                }
                            },
                            label = "Amount (₹)",
                            placeholder = "15.50",
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            isError = amountError.isNotEmpty(),
                            errorMessage = amountError
                        )
                        CustomDropdown(
                            selectedOption = category,
                            onOptionSelected = { category = it },
                            options = categories,
                            label = "Category"
                        )
                        CustomTextField(
                            value = note,
                            onValueChange = { note = it },
                            label = "Note (Optional)",
                            placeholder = "Lunch at cafe"
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val amt = amountStr.toDoubleOrNull()
                            if (amt == null || amt <= 0.0) {
                                amountError = "Amount must be a positive number"
                            } else {
                                if (isEditing) {
                                    viewModel.updateTransaction(
                                        id = editingTransaction!!.id,
                                        amount = amt,
                                        category = category,
                                        note = note,
                                        type = type,
                                        date = editingTransaction!!.date
                                    )
                                } else {
                                    viewModel.addTransaction(
                                        amount = amt,
                                        category = category,
                                        note = note,
                                        type = type,
                                        date = System.currentTimeMillis()
                                    )
                                }
                                showAddDialog = false
                                editingTransaction = null
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BlueAccent)
                    ) {
                        Text("Save", color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            showAddDialog = false
                            editingTransaction = null
                        }
                    ) {
                        Text("Cancel", color = TextGray)
                    }
                },
                containerColor = CardBg,
                shape = RoundedCornerShape(24.dp)
            )
        }

        // OCR Receipt Scanning Dialogs
        if (showScanOptionDialog) {
            AlertDialog(
                onDismissRequest = { showScanOptionDialog = false },
                title = {
                    Text(
                        text = "Add Expense",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                    ) {
                        Text(
                            text = "Choose how you want to add the transaction:",
                            color = TextGray,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Button(
                            onClick = {
                                showScanOptionDialog = false
                                amountStr = ""
                                category = "Food"
                                note = ""
                                type = "Expense"
                                amountError = ""
                                showAddDialog = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = BlueAccent),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Add Manually", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = {
                                showScanOptionDialog = false
                                try {
                                    val uri = createTempPhotoUri()
                                    tempPhotoUri = uri
                                    cameraLauncher.launch(uri)
                                } catch (e: Exception) {
                                    scanningError = "Failed to launch camera: ${e.localizedMessage}"
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CardBg),
                            border = androidx.compose.foundation.BorderStroke(1.dp, BlueAccent),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Scan Receipt (Camera)", color = BlueAccent, fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = {
                                showScanOptionDialog = false
                                galleryLauncher.launch("image/*")
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CardBg),
                            border = androidx.compose.foundation.BorderStroke(1.dp, BlueAccent),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Scan Receipt (Gallery)", color = BlueAccent, fontWeight = FontWeight.Bold)
                        }
                    }
                },
                confirmButton = {},
                dismissButton = {
                    TextButton(onClick = { showScanOptionDialog = false }) {
                        Text("Cancel", color = TextGray)
                    }
                },
                containerColor = CardBg,
                shape = RoundedCornerShape(24.dp)
            )
        }

        if (isScanning) {
            AlertDialog(
                onDismissRequest = {},
                confirmButton = {},
                title = {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = BlueAccent)
                    }
                },
                text = {
                    Text(
                        text = "Scanning receipt using ML Kit OCR...",
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                        color = Color.White
                    )
                },
                containerColor = CardBg,
                shape = RoundedCornerShape(16.dp)
            )
        }

        if (scanningError != null) {
            AlertDialog(
                onDismissRequest = { scanningError = null },
                title = {
                    Text(
                        text = "Scanning Failed",
                        fontWeight = FontWeight.Bold,
                        color = RedAccent
                    )
                },
                text = {
                    Text(text = scanningError ?: "", color = Color.White)
                },
                confirmButton = {
                    Button(
                        onClick = { scanningError = null },
                        colors = ButtonDefaults.buttonColors(containerColor = BlueAccent)
                    ) {
                        Text("OK", color = Color.White)
                    }
                },
                containerColor = CardBg,
                shape = RoundedCornerShape(16.dp)
            )
        }
    }
}

@Composable
fun TransactionManageRow(
    tx: TransactionEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val isIncome = tx.type.lowercase() == "income"
    val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    val formattedDate = sdf.format(Date(tx.date))

    Card(
        colors = CardDefaults.cardColors(containerColor = CardBg),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(if (isIncome) GreenAccent else RedAccent, CircleShape)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = tx.category,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color.White
                )
                if (tx.note.isNotEmpty()) {
                    Text(
                        text = tx.note,
                        fontSize = 12.sp,
                        color = TextGray
                    )
                }
                Text(
                    text = formattedDate,
                    fontSize = 10.sp,
                    color = TextGray.copy(alpha = 0.8f)
                )
            }
            Text(
                text = "${if (isIncome) "+" else "-"}₹${"%.2f".format(tx.amount)}",
                fontWeight = FontWeight.Black,
                fontSize = 14.sp,
                color = if (isIncome) GreenAccent else RedAccent,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            IconButton(onClick = onEdit) {
                Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit", tint = BlueAccent, modifier = Modifier.size(20.dp))
            }
            IconButton(onClick = onDelete) {
                Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = RedAccent, modifier = Modifier.size(20.dp))
            }
        }
    }
}
