package com.example.wealthwiseai.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.wealthwiseai.data.local.entity.TransactionEntity
import com.example.wealthwiseai.data.local.entity.GoalEntity
import com.example.wealthwiseai.ui.components.DisclaimerText
import com.example.wealthwiseai.ui.components.GlassCard
import com.example.wealthwiseai.ui.components.SectionHeader
import com.example.wealthwiseai.ui.theme.*
import com.example.wealthwiseai.viewmodel.DashboardViewModel

@Composable
fun ReportsScreen(
    viewModel: DashboardViewModel
) {
    val userProfile by viewModel.userProfile.collectAsState()
    val transactions by viewModel.transactions.collectAsState()
    val goals by viewModel.goals.collectAsState()
    val healthScore by viewModel.financialHealthScore.collectAsState()
    val savingsRate by viewModel.savingsRate.collectAsState()

    // Calculate aggregated metrics
    val baseIncome = userProfile?.monthlyIncome ?: 0.0
    val baseExpenses = userProfile?.monthlyExpenses ?: 0.0
    val baseSavings = baseIncome - baseExpenses

    val loggedIncome = transactions.filter { it.type == "Income" }.sumOf { it.amount }
    val loggedExpense = transactions.filter { it.type == "Expense" }.sumOf { it.amount }

    val totalIncome = baseIncome + loggedIncome
    val totalExpense = baseExpenses + loggedExpense
    val totalSavings = (totalIncome - totalExpense).coerceAtLeast(0.0)

    val actualSavingsRate = if (totalIncome > 0) (totalSavings / totalIncome) * 100 else 0.0
    val actualExpenseRatio = if (totalIncome > 0) (totalExpense / totalIncome) * 100 else 0.0

    // Calculate category breakdown
    val expenseTransactions = transactions.filter { it.type.lowercase() == "expense" }
    val loggedCategories = expenseTransactions.groupBy { it.category }
        .mapValues { it.value.sumOf { it.amount } }

    // Merge base expenses into "Others" or distribute them. Let's list base expenses as a category "Baseline Expenses"
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
            SectionHeader("Financial Reports")
            Text(
                text = "Interactive breakdown of your income vs expenses, savings rates, and categorical leaks.",
                fontSize = 12.sp,
                color = TextGray
            )
        }

        // 1. Income vs Expenses Comparison Card
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
                
                // Comparative Stacked Progress Bar
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

        // 2. Savings Rate Circular Meter
        item {
            val scoreRating = if (savingsRate >= 30.0) "Excellent" else if (savingsRate >= 20.0) "Good" else if (savingsRate >= 10.0) "Average" else "Poor"
            val scoreColor = if (savingsRate >= 30.0) GreenAccent else if (savingsRate >= 20.0) BlueAccent else if (savingsRate >= 10.0) GoldAccent else RedAccent

            GlassCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(90.dp)
                    ) {
                        CircularProgressIndicator(
                            progress = (actualSavingsRate / 100f).toFloat().coerceIn(0f, 1f),
                            strokeWidth = 8.dp,
                            color = scoreColor,
                            trackColor = Color.White.copy(alpha = 0.1f),
                            modifier = Modifier.fillMaxSize()
                        )
                        Text(
                            text = "${actualSavingsRate.toInt()}%",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(20.dp))
                    Column {
                        Text("Savings Rate: $scoreRating", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                        Text(
                            text = "WealthWise AI recommends setting aside 20% to 30% of income monthly to secure compound interest acceleration.",
                            fontSize = 11.sp,
                            color = TextGray,
                            lineHeight = 15.sp
                        )
                    }
                }
            }
        }

        // 3. Category Breakdown (Custom Bar Charts)
        item {
            Text("CATEGORY-WISE EXPENSES", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }

        if (totalCategoryExpense == 0.0) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = CardBg),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "No expenses recorded to build breakdowns.",
                        fontSize = 12.sp,
                        color = TextGray,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        } else {
            // Sort categories by expenditure size
            val sortedCategories = allCategoriesBreakdown.toList().sortedByDescending { it.second }
            items(sortedCategories) { (cat, amt) ->
                val ratio = (amt / totalCategoryExpense).toFloat()
                CategoryBreakdownRow(category = cat, amount = amt, ratio = ratio)
            }
        }

        // 4. Goal Progress List
        if (goals.isNotEmpty()) {
            item {
                Text("GOALS TRACKER STATUS", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
            items(goals) { goal ->
                GoalProgressReportRow(goal)
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
fun CategoryBreakdownRow(
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

@Composable
fun GoalProgressReportRow(goal: GoalEntity) {
    val progress = if (goal.targetAmount > 0.0) {
        (goal.currentSavedAmount / goal.targetAmount).toFloat().coerceIn(0f, 1f)
    } else {
        1f
    }
    Card(
        colors = CardDefaults.cardColors(containerColor = CardBg),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(goal.goalName, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
                Text("${(progress * 100).toInt()}% Done", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = GreenAccent)
            }
            Spacer(modifier = Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = progress,
                color = GreenAccent,
                trackColor = Color.White.copy(alpha = 0.1f),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(CircleShape)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Target: ₹${goal.targetAmount.toInt()} | Saved: ₹${goal.currentSavedAmount.toInt()}",
                fontSize = 10.sp,
                color = TextGray
            )
        }
    }
}
