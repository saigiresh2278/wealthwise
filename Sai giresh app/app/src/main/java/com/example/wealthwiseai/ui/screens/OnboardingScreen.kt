package com.example.wealthwiseai.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.wealthwiseai.ui.components.CustomDropdown
import com.example.wealthwiseai.ui.components.CustomTextField
import com.example.wealthwiseai.ui.components.DisclaimerText
import com.example.wealthwiseai.ui.components.GradientButton
import com.example.wealthwiseai.ui.theme.BlueAccent
import com.example.wealthwiseai.viewmodel.OnboardingViewModel

@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel,
    onComplete: () -> Unit
) {
    var fullName by remember { mutableStateOf("") }
    var ageStr by remember { mutableStateOf("") }
    var occupation by remember { mutableStateOf("") }
    var incomeStr by remember { mutableStateOf("") }
    var expensesStr by remember { mutableStateOf("") }
    var savingsStr by remember { mutableStateOf("") }
    var mainGoal by remember { mutableStateOf("") }
    
    var riskComfort by remember { mutableStateOf("Medium") }
    var investmentExperience by remember { mutableStateOf("Beginner") }

    // Validation States
    var nameError by remember { mutableStateOf("") }
    var ageError by remember { mutableStateOf("") }
    var occupationError by remember { mutableStateOf("") }
    var incomeError by remember { mutableStateOf("") }
    var expensesError by remember { mutableStateOf("") }
    var savingsError by remember { mutableStateOf("") }
    var goalError by remember { mutableStateOf("") }

    val scrollState = rememberScrollState()

    // Auto-calculate savings if income and expenses are entered
    LaunchedEffect(incomeStr, expensesStr) {
        val inc = incomeStr.toDoubleOrNull() ?: 0.0
        val exp = expensesStr.toDoubleOrNull() ?: 0.0
        if (inc > 0.0 && exp >= 0.0) {
            val calcSavings = inc - exp
            if (calcSavings >= 0.0) {
                savingsStr = calcSavings.toString()
            }
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
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))
            
            Text(
                text = "Welcome to WealthWise AI",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )
            
            Text(
                text = "Democratizing Wealth Management Through AI",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
            )

            // Name
            CustomTextField(
                value = fullName,
                onValueChange = {
                    fullName = it
                    nameError = if (it.trim().isEmpty()) "Name is required" else ""
                },
                label = "Full Name",
                placeholder = "John Doe",
                isError = nameError.isNotEmpty(),
                errorMessage = nameError
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                // Age
                CustomTextField(
                    value = ageStr,
                    onValueChange = {
                        ageStr = it
                        val age = it.toIntOrNull()
                        ageError = when {
                            it.isEmpty() -> "Age is required"
                            age == null || age < 18 -> "Must be 18+"
                            else -> ""
                        }
                    },
                    label = "Age",
                    placeholder = "22",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = ageError.isNotEmpty(),
                    errorMessage = ageError,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(12.dp))
                // Occupation
                CustomTextField(
                    value = occupation,
                    onValueChange = {
                        occupation = it
                        occupationError = if (it.trim().isEmpty()) "Occupation is required" else ""
                    },
                    label = "Occupation",
                    placeholder = "Student / Developer",
                    isError = occupationError.isNotEmpty(),
                    errorMessage = occupationError,
                    modifier = Modifier.weight(1.5f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Income
            CustomTextField(
                value = incomeStr,
                onValueChange = {
                    incomeStr = it
                    val inc = it.toDoubleOrNull()
                    incomeError = when {
                        it.isEmpty() -> "Income is required"
                        inc == null || inc < 0.0 -> "Invalid amount"
                        else -> ""
                    }
                },
                label = "Monthly Income (₹)",
                placeholder = "5000",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                isError = incomeError.isNotEmpty(),
                errorMessage = incomeError
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Expenses
            CustomTextField(
                value = expensesStr,
                onValueChange = {
                    expensesStr = it
                    val exp = it.toDoubleOrNull()
                    expensesError = when {
                        it.isEmpty() -> "Expenses required"
                        exp == null || exp < 0.0 -> "Invalid amount"
                        else -> ""
                    }
                },
                label = "Monthly Expenses (₹)",
                placeholder = "3000",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                isError = expensesError.isNotEmpty(),
                errorMessage = expensesError
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Savings
            CustomTextField(
                value = savingsStr,
                onValueChange = {
                    savingsStr = it
                    val sav = it.toDoubleOrNull()
                    savingsError = when {
                        it.isEmpty() -> "Savings required"
                        sav == null || sav < 0.0 -> "Invalid amount"
                        else -> ""
                    }
                },
                label = "Monthly Savings (₹)",
                placeholder = "2000",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                isError = savingsError.isNotEmpty(),
                errorMessage = savingsError
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Main Financial Goal
            CustomTextField(
                value = mainGoal,
                onValueChange = {
                    mainGoal = it
                    goalError = if (it.trim().isEmpty()) "Financial goal is required" else ""
                },
                label = "Main Financial Goal",
                placeholder = "Buy a home / Build emergency fund",
                isError = goalError.isNotEmpty(),
                errorMessage = goalError
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Dropdowns
            Row(modifier = Modifier.fillMaxWidth()) {
                CustomDropdown(
                    selectedOption = riskComfort,
                    onOptionSelected = { riskComfort = it },
                    options = listOf("Low", "Medium", "High"),
                    label = "Risk Comfort",
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(12.dp))
                CustomDropdown(
                    selectedOption = investmentExperience,
                    onOptionSelected = { investmentExperience = it },
                    options = listOf("Beginner", "Intermediate", "Advanced"),
                    label = "Experience",
                    modifier = Modifier.weight(1.2f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            DisclaimerText(modifier = Modifier.padding(bottom = 24.dp))

            GradientButton(
                text = "Create Profile",
                onClick = {
                    // Final Validation check
                    val isNameValid = fullName.trim().isNotEmpty()
                    val isAgeValid = ageStr.toIntOrNull() != null && ageStr.toInt() >= 18
                    val isOccValid = occupation.trim().isNotEmpty()
                    val isIncValid = incomeStr.toDoubleOrNull() != null && incomeStr.toDouble() >= 0.0
                    val isExpValid = expensesStr.toDoubleOrNull() != null && expensesStr.toDouble() >= 0.0
                    val isSavValid = savingsStr.toDoubleOrNull() != null && savingsStr.toDouble() >= 0.0
                    val isGoalValid = mainGoal.trim().isNotEmpty()

                    if (!isNameValid) nameError = "Name is required"
                    if (!isAgeValid) ageError = "Must be 18+"
                    if (!isOccValid) occupationError = "Occupation is required"
                    if (!isIncValid) incomeError = "Invalid income"
                    if (!isExpValid) expensesError = "Invalid expenses"
                    if (!isSavValid) savingsError = "Invalid savings"
                    if (!isGoalValid) goalError = "Goal is required"

                    if (isNameValid && isAgeValid && isOccValid && isIncValid && isExpValid && isSavValid && isGoalValid) {
                        viewModel.saveProfile(
                            name = fullName.trim(),
                            age = ageStr.toInt(),
                            occupation = occupation.trim(),
                            monthlyIncome = incomeStr.toDouble(),
                            monthlyExpenses = expensesStr.toDouble(),
                            monthlySavings = savingsStr.toDouble(),
                            mainFinancialGoal = mainGoal.trim(),
                            riskComfort = riskComfort,
                            investmentExperience = investmentExperience
                        )
                        onComplete()
                    }
                }
            )

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
