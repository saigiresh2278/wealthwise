package com.example.wealthwiseai.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.wealthwiseai.ui.components.CustomDropdown
import com.example.wealthwiseai.ui.components.CustomTextField
import com.example.wealthwiseai.ui.components.DisclaimerText
import com.example.wealthwiseai.ui.components.SectionHeader
import com.example.wealthwiseai.ui.theme.*
import com.example.wealthwiseai.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onResetCompleted: () -> Unit,
    onNavigateToRiskQuiz: () -> Unit,
    onNavigateToDatabaseExplorer: () -> Unit
) {
    val userProfile by viewModel.userProfile.collectAsState()
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val currentThemeStyle by viewModel.themeStyle.collectAsState()

    var showResetDialog by remember { mutableStateOf(false) }
    var showEditProfileDialog by remember { mutableStateOf(false) }

    // Edit Profile form fields
    var fullName by remember { mutableStateOf("") }
    var ageStr by remember { mutableStateOf("") }
    var occupation by remember { mutableStateOf("") }
    var incomeStr by remember { mutableStateOf("") }
    var expensesStr by remember { mutableStateOf("") }
    var savingsStr by remember { mutableStateOf("") }
    var mainGoal by remember { mutableStateOf("") }
    var riskComfort by remember { mutableStateOf("Medium") }
    var investmentExperience by remember { mutableStateOf("Beginner") }

    // Validation
    var nameError by remember { mutableStateOf("") }
    var ageError by remember { mutableStateOf("") }
    var occupationError by remember { mutableStateOf("") }
    var incomeError by remember { mutableStateOf("") }
    var expensesError by remember { mutableStateOf("") }
    var savingsError by remember { mutableStateOf("") }
    var goalError by remember { mutableStateOf("") }

    val priorities = listOf("Low", "Medium", "High")
    val experiences = listOf("Beginner", "Intermediate", "Advanced")

    val scrollState = rememberScrollState()

    LaunchedEffect(showEditProfileDialog) {
        if (showEditProfileDialog && userProfile != null) {
            val p = userProfile!!
            fullName = p.fullName
            ageStr = p.age.toString()
            occupation = p.occupation
            incomeStr = p.monthlyIncome.toString()
            expensesStr = p.monthlyExpenses.toString()
            savingsStr = p.monthlySavings.toString()
            mainGoal = p.mainFinancialGoal
            riskComfort = p.riskComfort
            investmentExperience = p.investmentExperience

            nameError = ""
            ageError = ""
            occupationError = ""
            incomeError = ""
            expensesError = ""
            savingsError = ""
            goalError = ""
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
                .verticalScroll(scrollState)
                .padding(24.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            SectionHeader("Application Settings")

            // Dark Mode Switch
            Card(
                colors = CardDefaults.cardColors(containerColor = CardBg),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Dark Mode", fontWeight = FontWeight.Bold, color = TextWhite, fontSize = 15.sp)
                        Text("Toggle the application visual theme", fontSize = 11.sp, color = TextGray)
                    }
                    Switch(
                        checked = isDarkMode,
                        onCheckedChange = { viewModel.setDarkMode(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = BlueAccent,
                            uncheckedThumbColor = TextGray,
                            uncheckedTrackColor = Color.White.copy(alpha = 0.08f)
                        )
                    )
                }
            }

            // Inbuilt Themes Selector Card
            Card(
                colors = CardDefaults.cardColors(containerColor = CardBg),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column {
                        Text("App Theme Accent", fontWeight = FontWeight.Bold, color = TextWhite, fontSize = 15.sp)
                        Text("Selected: ${currentThemeStyle.displayName}", fontSize = 11.sp, color = TextGray)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ThemeStyle.values().forEach { style ->
                            val color = when (style) {
                                ThemeStyle.CLASSIC_BLUE -> Color(0xFF3F8CFF)
                                ThemeStyle.EMERALD_GREEN -> Color(0xFF00C853)
                                ThemeStyle.ROYAL_PURPLE -> Color(0xFF8B5CF6)
                                ThemeStyle.GOLDEN_LUXURY -> Color(0xFFFFB300)
                                ThemeStyle.CRIMSON_WARM -> Color(0xFFEF4444)
                            }
                            
                            val isSelected = style == currentThemeStyle
                            
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .clickable { viewModel.setThemeStyle(style) }
                                    .border(
                                        width = if (isSelected) 3.dp else 1.dp,
                                        color = if (isSelected) Color.White else Color.White.copy(alpha = 0.15f),
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Selected",
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }


            // Edit Profile Card
            Card(
                colors = CardDefaults.cardColors(containerColor = CardBg),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column {
                        Text("Edit Advisory Profile", fontWeight = FontWeight.Bold, color = TextWhite, fontSize = 15.sp)
                        Text("Update your monthly parameters and goals", fontSize = 11.sp, color = TextGray)
                    }
                    Button(
                        onClick = { showEditProfileDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = BlueAccent),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Edit", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Retake Risk Quiz Card
            Card(
                colors = CardDefaults.cardColors(containerColor = CardBg),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column {
                        Text("Risk Profile Assessment", fontWeight = FontWeight.Bold, color = TextWhite, fontSize = 15.sp)
                        Text("Re-evaluate your risk class with a 5-step quiz", fontSize = 11.sp, color = TextGray)
                    }
                    Button(
                        onClick = onNavigateToRiskQuiz,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = BlueAccent),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Retake Quiz", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Database Explorer Card
            Card(
                colors = CardDefaults.cardColors(containerColor = CardBg),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column {
                        Text("Database Explorer", fontWeight = FontWeight.Bold, color = TextWhite, fontSize = 15.sp)
                        Text("View, search, and delete all records in the local Room database", fontSize = 11.sp, color = TextGray)
                    }
                    Button(
                        onClick = onNavigateToDatabaseExplorer,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = BlueAccent),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Explore Database", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Reset Data Card
            Card(
                colors = CardDefaults.cardColors(containerColor = CardBg),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column {
                        Text("Reset All Data", fontWeight = FontWeight.Bold, color = RedAccent, fontSize = 15.sp)
                        Text("Wipe profile, goals, and transactions", fontSize = 11.sp, color = TextGray)
                    }
                    Button(
                        onClick = { showResetDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = RedAccent.copy(alpha = 0.15f)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Wipe Data", color = RedAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // About Card
            Card(
                colors = CardDefaults.cardColors(containerColor = CardBg),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("About WealthWise AI", fontWeight = FontWeight.Bold, color = TextWhite, fontSize = 15.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Version: 1.0.0", fontSize = 12.sp, color = TextGray)
                    Text("Package: com.example.wealthwiseai", fontSize = 12.sp, color = TextGray)
                    Text("Project Title: Democratizing Wealth Management Through AI-Powered Financial Advisors.", fontSize = 12.sp, color = TextGray, lineHeight = 16.sp)
                }
            }

            DisclaimerText(modifier = Modifier.padding(vertical = 12.dp))

            Spacer(modifier = Modifier.height(24.dp))
        }

        // Reset Confirmation Dialog
        if (showResetDialog) {
            AlertDialog(
                onDismissRequest = { showResetDialog = false },
                title = { Text("Wipe All Data?", fontWeight = FontWeight.Bold, color = TextWhite) },
                text = { Text("Are you sure you want to delete your profile, transactions, goals, and risk results? This cannot be undone.", color = TextGray) },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.resetAllData()
                            showResetDialog = false
                            onResetCompleted()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = RedAccent)
                    ) {
                        Text("Yes, Wipe", color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showResetDialog = false }) {
                        Text("Cancel", color = TextGray)
                    }
                },
                containerColor = CardBg,
                shape = RoundedCornerShape(24.dp)
            )
        }

        // Edit Profile Dialog
        if (showEditProfileDialog) {
            AlertDialog(
                onDismissRequest = { showEditProfileDialog = false },
                title = { Text("Edit Advisory Profile", fontWeight = FontWeight.Bold, color = TextWhite) },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                    ) {
                        CustomTextField(
                            value = fullName,
                            onValueChange = { fullName = it },
                            label = "Full Name"
                        )
                        CustomTextField(
                            value = ageStr,
                            onValueChange = { ageStr = it },
                            label = "Age",
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                        CustomTextField(
                            value = occupation,
                            onValueChange = { occupation = it },
                            label = "Occupation"
                        )
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
                            label = "Monthly Savings (₹)",
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                        )
                        CustomTextField(
                            value = mainGoal,
                            onValueChange = { mainGoal = it },
                            label = "Main Financial Goal"
                        )
                        CustomDropdown(
                            selectedOption = riskComfort,
                            onOptionSelected = { riskComfort = it },
                            options = priorities,
                            label = "Risk Comfort"
                        )
                        CustomDropdown(
                            selectedOption = investmentExperience,
                            onOptionSelected = { investmentExperience = it },
                            options = experiences,
                            label = "Investment Experience"
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val age = ageStr.toIntOrNull() ?: 18
                            val income = incomeStr.toDoubleOrNull() ?: 0.0
                            val expenses = expensesStr.toDoubleOrNull() ?: 0.0
                            val savings = savingsStr.toDoubleOrNull() ?: 0.0

                            viewModel.updateProfile(
                                name = fullName,
                                age = age,
                                occupation = occupation,
                                income = income,
                                expenses = expenses,
                                savings = savings,
                                goal = mainGoal,
                                risk = riskComfort,
                                experience = investmentExperience
                            )
                            showEditProfileDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BlueAccent)
                    ) {
                        Text("Save Changes", color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showEditProfileDialog = false }) {
                        Text("Cancel", color = TextGray)
                    }
                },
                containerColor = CardBg,
                shape = RoundedCornerShape(24.dp)
            )
        }
    }
}
