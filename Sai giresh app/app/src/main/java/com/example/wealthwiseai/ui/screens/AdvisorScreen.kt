package com.example.wealthwiseai.ui.screens

import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.wealthwiseai.data.local.entity.TransactionEntity
import com.example.wealthwiseai.ui.components.CustomTextField
import com.example.wealthwiseai.ui.components.DisclaimerText
import com.example.wealthwiseai.ui.components.GlassCard
import com.example.wealthwiseai.ui.components.SectionHeader
import com.example.wealthwiseai.ui.theme.*
import com.example.wealthwiseai.viewmodel.AdvisorViewModel

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AdvisorScreen(
    viewModel: AdvisorViewModel,
    onNavigateToLearn: () -> Unit
) {
    val userProfile by viewModel.userProfile.collectAsState()
    val advisoryResult by viewModel.advisoryResult.collectAsState()
    val transactions by viewModel.transactions.collectAsState()
    val expenseSheetAnalysis by viewModel.expenseSheetAnalysis.collectAsState()
    val resumeAnalysis by viewModel.resumeAnalysis.collectAsState()

    val context = LocalContext.current
    val contentResolver = context.contentResolver

    // File picker launcher for resume upload
    var uploadedFileName by remember { mutableStateOf<String?>(null) }
    val fileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            var fileName = "resume.pdf"
            contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1 && cursor.moveToFirst()) {
                    fileName = cursor.getString(nameIndex)
                }
            }
            uploadedFileName = fileName
            viewModel.analyzeResume(fileName, fileName) // Triggers rule parsing based on keywords in name
        }
    }

    // File picker launcher for expense sheet upload
    var uploadedSheetName by remember { mutableStateOf<String?>(null) }
    val sheetLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            var fileName = "bank_statement.pdf"
            contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1 && cursor.moveToFirst()) {
                    fileName = cursor.getString(nameIndex)
                }
            }
            uploadedSheetName = fileName
            viewModel.analyzeExpenseSheet(fileName)
        }
    }

    // Calculations for monthly report
    val baseIncome = userProfile?.monthlyIncome ?: 0.0
    val baseExpenses = userProfile?.monthlyExpenses ?: 0.0

    val loggedIncome = transactions.filter { it.type == "Income" }.sumOf { it.amount }
    val loggedExpense = transactions.filter { it.type == "Expense" }.sumOf { it.amount }

    val totalIncome = baseIncome + loggedIncome
    val totalExpense = baseExpenses + loggedExpense
    val totalSavings = (totalIncome - totalExpense).coerceAtLeast(0.0)

    val actualSavingsRate = if (totalIncome > 0) (totalSavings / totalIncome) * 100 else 0.0
    val actualExpenseRatio = if (totalIncome > 0) (totalExpense / totalIncome) * 100 else 0.0

    val expenseTransactions = transactions.filter { it.type.lowercase() == "expense" }
    val loggedCategories = expenseTransactions.groupBy { it.category }
        .mapValues { it.value.sumOf { it.amount } }

    val allCategoriesBreakdown = mutableMapOf<String, Double>()
    if (baseExpenses > 0.0) {
        allCategoriesBreakdown["Baseline (Onboarded)"] = baseExpenses
    }
    loggedCategories.forEach { (cat, amt) ->
        allCategoriesBreakdown[cat] = (allCategoriesBreakdown[cat] ?: 0.0) + amt
    }

    val totalCategoryExpense = allCategoriesBreakdown.values.sum()

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
            SectionHeader("AI Advisor & Expense Reports")
            Text(
                text = "Personalized financial recommendations combined with a breakdown of monthly expenses and leaks.",
                fontSize = 12.sp,
                color = TextGray
            )
        }

        // Dynamic Monthly Overview Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CardBg),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Dynamic Monthly Overview",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Monthly Salary", fontSize = 11.sp, color = TextGray)
                            Text("₹${"%.2f".format(totalIncome)}", fontSize = 16.sp, fontWeight = FontWeight.Black, color = Color.White)
                        }
                        Column {
                            Text("Current Expenses", fontSize = 11.sp, color = TextGray)
                            Text("₹${"%.2f".format(totalExpense)}", fontSize = 16.sp, fontWeight = FontWeight.Black, color = RedAccent)
                        }
                        Column {
                            Text("Remaining Savings", fontSize = 11.sp, color = TextGray)
                            Text("₹${"%.2f".format(totalSavings)}", fontSize = 16.sp, fontWeight = FontWeight.Black, color = GreenAccent)
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    
                    val progress = if (totalIncome > 0.0) {
                        (totalExpense / totalIncome).toFloat().coerceIn(0f, 1f)
                    } else {
                        1f
                    }
                    LinearProgressIndicator(
                        progress = progress,
                        color = if (progress > 0.8f) RedAccent else BlueAccent,
                        trackColor = Color.White.copy(alpha = 0.1f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(CircleShape)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${(progress * 100).toInt()}% of salary spent",
                            fontSize = 10.sp,
                            color = TextGray
                        )
                        Text(
                            text = "${((1f - progress) * 100).toInt()}% remaining",
                            fontSize = 10.sp,
                            color = TextGray
                        )
                    }
                }
            }
        }

        // Active Alerts (if any)
        if (advisoryResult.alerts.isNotEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = RedAccent.copy(alpha = 0.08f)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = RedAccent)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "AI Risk Warnings & Leaks",
                                fontWeight = FontWeight.Bold,
                                color = RedAccent,
                                fontSize = 14.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        advisoryResult.alerts.forEach { alert ->
                            Text(
                                text = "• $alert",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.padding(vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }

        // AI Expense Sheet Analyser
        item {
            Text(
                text = "AI Expense Sheet Analyser",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color.White
            )
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CardBg),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Upload your bank monthly transactions sheet (PDF or Image) to analyze your spending habits, identify leaks, and generate high-fidelity reports.",
                        fontSize = 11.sp,
                        color = TextGray
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = { sheetLauncher.launch("application/pdf,image/*") },
                            colors = ButtonDefaults.buttonColors(containerColor = BlueAccent),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = if (uploadedSheetName != null) "Uploaded Sheet: $uploadedSheetName" else "Upload Bank Sheet",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                        }

                        if (uploadedSheetName != null) {
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(
                                onClick = {
                                    uploadedSheetName = null
                                    viewModel.clearExpenseSheet()
                                },
                                colors = IconButtonDefaults.iconButtonColors(
                                    containerColor = Color.White.copy(alpha = 0.08f),
                                    contentColor = RedAccent
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear Upload"
                                )
                            }
                        }
                    }

                    expenseSheetAnalysis?.let { analysis ->
                        if (!analysis.isValid) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Card(
                                colors = CardDefaults.cardColors(containerColor = RedAccent.copy(alpha = 0.08f)),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = "Error",
                                        tint = RedAccent
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = analysis.errorMessage ?: "Validation error.",
                                        fontSize = 12.sp,
                                        color = RedAccent,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        } else {
                            Spacer(modifier = Modifier.height(16.dp))
                            Divider(color = Color.White.copy(alpha = 0.08f))
                            Spacer(modifier = Modifier.height(16.dp))

                            Text("AI EXPENSE SHEET ANALYSIS REPORT", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextGray)
                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "Total Analyzed Expenses",
                                fontSize = 11.sp,
                                color = TextGray
                            )
                            Text(
                                text = "₹${"%.2f".format(analysis.totalExpenses)}",
                                fontWeight = FontWeight.Black,
                                fontSize = 20.sp,
                                color = RedAccent
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Primary Spending Driver",
                                fontSize = 11.sp,
                                color = TextGray
                            )
                            Text(
                                text = analysis.topCategory,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "Discovered Category Breakdown",
                                fontSize = 11.sp,
                                color = TextGray
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                analysis.categoryBreakdown.forEach { (cat, amt) ->
                                    val ratio = (amt / analysis.totalExpenses).toFloat().coerceIn(0f, 1f)
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.03f)),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(cat, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                                Text("₹${"%.0f".format(amt)} (${(ratio * 100).toInt()}%)", fontSize = 11.sp, color = BlueAccent, fontWeight = FontWeight.Bold)
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            LinearProgressIndicator(
                                                progress = ratio,
                                                color = BlueAccent,
                                                trackColor = Color.White.copy(alpha = 0.05f),
                                                modifier = Modifier.fillMaxWidth().height(4.dp).clip(CircleShape)
                                            )
                                        }
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "Identified Budget Leaks",
                                fontSize = 11.sp,
                                color = TextGray
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            analysis.budgetLeaks.forEach { leak ->
                                Row(
                                    modifier = Modifier.padding(vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(modifier = Modifier.size(6.dp).background(RedAccent, CircleShape))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(text = leak, fontSize = 12.sp, color = Color.White)
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "AI Saving Suggestions",
                                fontSize = 11.sp,
                                color = TextGray
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            analysis.recommendations.forEach { recommendation ->
                                Row(
                                    modifier = Modifier.padding(vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(modifier = Modifier.size(6.dp).background(GreenAccent, CircleShape))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(text = recommendation, fontSize = 12.sp, color = Color.White)
                                }
                            }
                        }
                    }
                }
            }
        }

        // AI Career Booster & Salary Maximizer
        item {
            Text(
                text = "AI Career & Salary Booster",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color.White
            )
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CardBg),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Upload your resume (PDF/TXT) to discover matching high-paying roles, target salaries, and the exact skills needed to increase your income.",
                        fontSize = 11.sp,
                        color = TextGray
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { fileLauncher.launch("application/pdf,text/plain") },
                        colors = ButtonDefaults.buttonColors(containerColor = GreenAccent),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (uploadedFileName != null) "Uploaded: $uploadedFileName" else "Upload Resume",
                            color = Color.Black,
                            fontWeight = FontWeight.Black
                        )
                    }

                    resumeAnalysis?.let { analysis ->
                        Spacer(modifier = Modifier.height(16.dp))
                        Divider(color = Color.White.copy(alpha = 0.08f))
                        Spacer(modifier = Modifier.height(16.dp))

                        Text("AI RESUME MATCH REPORT", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextGray)
                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Suitable Career Role",
                            fontSize = 11.sp,
                            color = TextGray
                        )
                        Text(
                            text = analysis.role,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Top Target Income Potential",
                            fontSize = 11.sp,
                            color = TextGray
                        )
                        Text(
                            text = analysis.salaryEstimate,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = GreenAccent
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Key Skills to Master",
                            fontSize = 11.sp,
                            color = TextGray
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            analysis.skillsToLearn.forEach { skill ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(BlueAccent.copy(alpha = 0.15f))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(text = skill, color = BlueAccent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Career Strategy Summary",
                            fontSize = 11.sp,
                            color = TextGray
                        )
                        Text(
                            text = analysis.summary,
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.9f),
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }

        // Monthly Expense Analysis Section
        item {
            Text(
                text = "Monthly Expense Analysis",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color.White
            )
        }

        // Income vs Expenses Comparison Card
        item {
            GlassCard {
                Text("INCOME VS EXPENSES COMPARISON", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextGray)
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Total Income", fontSize = 12.sp, color = TextGray)
                        Text("₹${"%.2f".format(totalIncome)}", fontSize = 18.sp, fontWeight = FontWeight.Black, color = GreenAccent)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Total Expenses", fontSize = 12.sp, color = TextGray)
                        Text("₹${"%.2f".format(totalExpense)}", fontSize = 18.sp, fontWeight = FontWeight.Black, color = RedAccent)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                
                val expenseWeight = if (totalIncome > 0) (totalExpense / totalIncome).toFloat().coerceIn(0f, 1f) else 0f
                val savingsWeight = (1f - expenseWeight).coerceIn(0f, 1f)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color.White.copy(alpha = 0.1f))
                ) {
                    if (expenseWeight > 0f) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .weight(expenseWeight.coerceAtLeast(0.001f))
                                .background(RedAccent)
                        )
                    }
                    if (savingsWeight > 0f) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .weight(savingsWeight.coerceAtLeast(0.001f))
                                .background(GreenAccent)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(8.dp).background(RedAccent, CircleShape))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Expenses (${"%.1f".format(actualExpenseRatio)}%)", fontSize = 11.sp, color = Color.White)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(8.dp).background(GreenAccent, CircleShape))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Net Savings (${"%.1f".format(actualSavingsRate)}%)", fontSize = 11.sp, color = Color.White)
                    }
                }
            }
        }

        // Category-wise Breakdown Cards
        item {
            Text(
                text = "Category-Wise Expenditure",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color.White
            )
        }

        if (totalCategoryExpense == 0.0) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = CardBg),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "No expenses recorded to build categorical leaks analysis.",
                        fontSize = 12.sp,
                        color = TextGray,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        } else {
            val sortedCategories = allCategoriesBreakdown.toList().sortedByDescending { it.second }
            items(sortedCategories) { (cat, amt) ->
                val ratio = (amt / totalCategoryExpense).toFloat()
                AdvisorCategoryBreakdownRow(category = cat, amount = amt, ratio = ratio)
            }
        }

        // AI Investment Strategy Recommendation
        item {
            Text(
                text = "AI Investment Recommendation",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color.White
            )
        }

        item {
            GlassCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(GreenAccent.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = GreenAccent,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "AI Portfolio Strategy (Risk: ${userProfile?.riskComfort ?: "Medium"})",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 14.sp
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = advisoryResult.investmentRecommendation,
                    fontSize = 13.sp,
                    color = Color.White.copy(alpha = 0.9f),
                    lineHeight = 20.sp
                )
            }
        }

        // Suggestions
        item {
            Text(
                text = "Advisory Guidance",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color.White
            )
        }

        items(advisoryResult.suggestions) { suggestion ->
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
                            .size(6.dp)
                            .background(BlueAccent, RoundedCornerShape(2.dp))
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = suggestion,
                        fontSize = 13.sp,
                        color = Color.White,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        // AI Ideas to Increase Savings
        item {
            Text(
                text = "AI Ideas to Increase Savings",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color.White
            )
        }

        items(advisoryResult.savingsIncreaseIdeas) { idea ->
            Card(
                colors = CardDefaults.cardColors(containerColor = CardBg.copy(alpha = 0.8f)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .size(6.dp)
                            .background(BlueAccent, RoundedCornerShape(2.dp))
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = idea,
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.85f),
                        lineHeight = 18.sp
                    )
                }
            }
        }

        // AI Ideas to Increase Income
        item {
            Text(
                text = "AI Ideas to Increase Income",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color.White
            )
        }

        items(advisoryResult.incomeIncreaseIdeas) { idea ->
            Card(
                colors = CardDefaults.cardColors(containerColor = CardBg.copy(alpha = 0.8f)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .size(6.dp)
                            .background(GreenAccent, RoundedCornerShape(2.dp))
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = idea,
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.85f),
                        lineHeight = 18.sp
                    )
                }
            }
        }

        // Recommended Learning Topics
        item {
            Text(
                text = "Recommended Learning Path",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color.White
            )
        }

        item {
            GlassCard {
                Text(
                    text = "Based on your risk tolerance: ${userProfile?.riskComfort ?: "Unknown"}",
                    fontSize = 12.sp,
                    color = TextGray,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(12.dp))
                
                advisoryResult.learningRecommendations.forEach { rec ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = GreenAccent,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = rec,
                                fontSize = 13.sp,
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        TextButton(
                            onClick = onNavigateToLearn,
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("Learn", fontSize = 12.sp, color = GreenAccent)
                        }
                    }
                }
            }
        }

        item {
            DisclaimerText(modifier = Modifier.padding(vertical = 12.dp))
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun AdvisorCategoryBreakdownRow(
    category: String,
    amount: Double,
    ratio: Float
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CardBg.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(category, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
                Text(
                    text = "₹${"%.2f".format(amount)} (${(ratio * 100).toInt()}%)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = BlueAccent
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = ratio.coerceIn(0f, 1f),
                color = BlueAccent,
                trackColor = Color.White.copy(alpha = 0.1f),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(CircleShape)
            )
        }
    }
}
