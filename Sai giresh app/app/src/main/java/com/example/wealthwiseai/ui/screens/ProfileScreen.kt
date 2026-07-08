package com.example.wealthwiseai.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.wealthwiseai.data.local.entity.UserProfileEntity
import com.example.wealthwiseai.ui.components.*
import com.example.wealthwiseai.ui.theme.*
import com.example.wealthwiseai.viewmodel.SettingsViewModel

// Photo Picker imports
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri

fun saveUriToInternalStorage(context: android.content.Context, uri: Uri): String? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri)
        val file = java.io.File(context.filesDir, "profile_photo.jpg")
        val outputStream = java.io.FileOutputStream(file)
        inputStream?.use { input ->
            outputStream.use { output ->
                input.copyTo(output)
            }
        }
        file.absolutePath
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: SettingsViewModel,
    userEmail: String,
    onLogout: () -> Unit
) {
    val profile by viewModel.userProfile.collectAsState()
    val transactions by viewModel.transactions.collectAsState()
    var showEditDialog by remember { mutableStateOf(false) }

    val profilePhotoUri by viewModel.profilePhotoUri.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current
    var profileBitmap by remember { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(profilePhotoUri) {
        if (!profilePhotoUri.isNullOrEmpty()) {
            profileBitmap = try {
                BitmapFactory.decodeFile(profilePhotoUri)
            } catch (e: Exception) {
                null
            }
        } else {
            profileBitmap = null
        }
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val localPath = saveUriToInternalStorage(context, it)
            if (localPath != null) {
                viewModel.saveProfilePhoto(localPath)
            }
        }
    }

    // Dialog form states
    var name by remember { mutableStateOf("") }
    var ageStr by remember { mutableStateOf("") }
    var occupation by remember { mutableStateOf("") }
    var incomeStr by remember { mutableStateOf("") }
    var expensesStr by remember { mutableStateOf("") }
    var savingsStr by remember { mutableStateOf("") }
    var goal by remember { mutableStateOf("") }
    var risk by remember { mutableStateOf("Medium") }
    var experience by remember { mutableStateOf("Intermediate") }

    LaunchedEffect(profile) {
        profile?.let {
            name = it.fullName
            ageStr = it.age.toString()
            occupation = it.occupation
            incomeStr = it.monthlyIncome.toString()
            expensesStr = it.monthlyExpenses.toString()
            savingsStr = it.monthlySavings.toString()
            goal = it.mainFinancialGoal
            risk = it.riskComfort
            experience = it.investmentExperience
        }
    }

    // Dynamic Daily & Monthly Expenses calculation
    val calendar = java.util.Calendar.getInstance()
    val currentMonth = calendar.get(java.util.Calendar.MONTH)
    val currentYear = calendar.get(java.util.Calendar.YEAR)
    val currentDay = calendar.get(java.util.Calendar.DAY_OF_MONTH)
    val txCalendar = java.util.Calendar.getInstance()

    val dailyExpenses = transactions.filter { tx ->
        if (tx.type.lowercase() == "expense") {
            txCalendar.timeInMillis = tx.date
            txCalendar.get(java.util.Calendar.DAY_OF_MONTH) == currentDay &&
            txCalendar.get(java.util.Calendar.MONTH) == currentMonth &&
            txCalendar.get(java.util.Calendar.YEAR) == currentYear
        } else {
            false
        }
    }.sumOf { it.amount }

    val monthlyExpenses = transactions.filter { tx ->
        if (tx.type.lowercase() == "expense") {
            txCalendar.timeInMillis = tx.date
            txCalendar.get(java.util.Calendar.MONTH) == currentMonth &&
            txCalendar.get(java.util.Calendar.YEAR) == currentYear
        } else {
            false
        }
    }.sumOf { it.amount }

    // Calculate dynamic savings rate for Financial Status
    val baseIncome = profile?.monthlyIncome ?: 0.0
    val loggedIncome = transactions.filter { it.type == "Income" }.sumOf { it.amount }
    val totalIncome = baseIncome + loggedIncome
    val totalExpense = (profile?.monthlyExpenses ?: 0.0) + monthlyExpenses
    val netSavings = (totalIncome - totalExpense).coerceAtLeast(0.0)
    val savingsRate = if (totalIncome > 0) (netSavings / totalIncome) * 100 else 0.0

    val financialStatus = when {
        savingsRate >= 30.0 -> "Elite Wealth Builder"
        savingsRate >= 20.0 -> "Disciplined Saver"
        savingsRate >= 10.0 -> "Steady Capital Accumulator"
        else -> "Budget Optimizer Required"
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "WealthWise AI",
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                color = BlueAccent,
                letterSpacing = 1.sp
            )
            SectionHeader("User Profile")
            Text(
                text = "Overview of your personal, professional, and financial risk profiles.",
                fontSize = 12.sp,
                color = TextGray
            )
        }

        // Profile Details Card
        item {
            GlassCard {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Avatar Circle
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clickable {
                                launcher.launch("image/*")
                            }
                            .background(
                                brush = Brush.linearGradient(listOf(BlueAccent, GreenAccent)),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (profileBitmap != null) {
                            Image(
                                bitmap = profileBitmap!!.asImageBitmap(),
                                contentDescription = "Profile Photo",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Text(
                                text = (profile?.fullName?.take(1) ?: "U").uppercase(),
                                color = Color.White,
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                        
                        // Subtle edit icon overlay at bottom right
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .size(24.dp)
                                .background(BlueAccent, CircleShape)
                                .padding(4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Change Photo",
                                tint = Color.White,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = profile?.fullName ?: "Guest User",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = userEmail,
                        fontSize = 13.sp,
                        color = TextGray
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
                Divider(color = Color.White.copy(alpha = 0.08f))
                Spacer(modifier = Modifier.height(16.dp))

                // Detail Items
                ProfileDetailRow(label = "Age", value = "${profile?.age ?: 0} years")
                ProfileDetailRow(label = "Occupation", value = profile?.occupation ?: "Not set")
                ProfileDetailRow(label = "Monthly Income", value = "₹${"%.2f".format(profile?.monthlyIncome ?: 0.0)}")
                ProfileDetailRow(label = "Daily Expenses (Today)", value = "₹${"%.2f".format(dailyExpenses)}", valueColor = RedAccent)
                ProfileDetailRow(label = "Monthly Expenses (Total)", value = "₹${"%.2f".format(monthlyExpenses)}", valueColor = RedAccent)
                ProfileDetailRow(label = "Financial Status", value = financialStatus, valueColor = if (savingsRate >= 20.0) GreenAccent else BlueAccent)
                ProfileDetailRow(label = "Risk Preference", value = profile?.riskComfort ?: "Not set", valueColor = if (profile?.riskComfort == "High") RedAccent else if (profile?.riskComfort == "Low") GreenAccent else BlueAccent)
                ProfileDetailRow(label = "Main Financial Target", value = profile?.mainFinancialGoal ?: "Not set")
                ProfileDetailRow(label = "Investment History", value = profile?.investmentExperience ?: "Not set")
            }
        }

        // Referral Card
        item {
            val context = androidx.compose.ui.platform.LocalContext.current
            val referralLink = "https://wealthwise.ai/download/wealthwiseai-v1.0.apk?ref=GIRESH50"
            
            GlassCard {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Spread the WealthWise Word",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Share the app with your friends to help them manage their investments and track expenses.",
                        fontSize = 11.sp,
                        color = TextGray
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White.copy(alpha = 0.05f))
                            .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = referralLink,
                            color = BlueAccent,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                        Text(
                            text = "Copy",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier
                                .clickable {
                                    val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                    val clip = android.content.ClipData.newPlainText("WealthWise Referral", referralLink)
                                    clipboard.setPrimaryClip(clip)
                                    android.widget.Toast.makeText(context, "Referral link copied to clipboard!", android.widget.Toast.LENGTH_SHORT).show()
                                }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }

        // Actions
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { showEditDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = BlueAccent),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Edit Profile", color = Color.White, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = {
                        viewModel.logout()
                        onLogout()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Logout", color = MaterialTheme.colorScheme.onErrorContainer, fontWeight = FontWeight.Bold)
                }
            }
        }

        item {
            DisclaimerText(modifier = Modifier.padding(bottom = 24.dp))
        }
    }

    // Edit Profile Modal Dialog
    if (showEditDialog) {
        val categories = listOf("Low", "Medium", "High")
        val experiences = listOf("Beginner", "Intermediate", "Advanced")

        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = {
                Text(
                    text = "Edit Financial Profile",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 450.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CustomTextField(value = name, onValueChange = { name = it }, label = "Full Name")
                    CustomTextField(
                        value = ageStr,
                        onValueChange = { ageStr = it },
                        label = "Age",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    CustomTextField(value = occupation, onValueChange = { occupation = it }, label = "Occupation")
                    CustomTextField(
                        value = incomeStr,
                        onValueChange = { incomeStr = it },
                        label = "Monthly Income (₹)",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                    CustomTextField(
                        value = expensesStr,
                        onValueChange = { expensesStr = it },
                        label = "Monthly Expenses (₹)",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                    CustomTextField(
                        value = savingsStr,
                        onValueChange = { savingsStr = it },
                        label = "Monthly Savings Goal (₹)",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                    CustomTextField(value = goal, onValueChange = { goal = it }, label = "Main Goal Description")
                    
                    CustomDropdown(
                        selectedOption = risk,
                        onOptionSelected = { risk = it },
                        options = categories,
                        label = "Risk Comfort"
                    )

                    CustomDropdown(
                        selectedOption = experience,
                        onOptionSelected = { experience = it },
                        options = experiences,
                        label = "Investment Experience"
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val finalAge = ageStr.toIntOrNull() ?: 18
                        val finalIncome = incomeStr.toDoubleOrNull() ?: 0.0
                        val finalExpenses = expensesStr.toDoubleOrNull() ?: 0.0
                        val finalSavings = savingsStr.toDoubleOrNull() ?: 0.0

                        viewModel.updateProfile(
                            name = name,
                            age = finalAge,
                            occupation = occupation,
                            income = finalIncome,
                            expenses = finalExpenses,
                            savings = finalSavings,
                            goal = goal,
                            risk = risk,
                            experience = experience
                        )
                        showEditDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BlueAccent)
                ) {
                    Text("Save Changes", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("Cancel", color = TextGray)
                }
            },
            containerColor = CardBg,
            shape = RoundedCornerShape(24.dp)
        )
    }
}

@Composable
fun ProfileDetailRow(
    label: String,
    value: String,
    valueColor: Color = Color.White
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, color = TextGray, fontSize = 13.sp)
        Text(
            text = value,
            color = valueColor,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
    }
}
