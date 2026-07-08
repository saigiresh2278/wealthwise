package com.example.wealthwiseai.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.wealthwiseai.ai.FinancialHealthCalculator
import com.example.wealthwiseai.data.local.entity.GoalEntity
import com.example.wealthwiseai.data.local.entity.TransactionEntity
import com.example.wealthwiseai.ui.components.GlassCard
import com.example.wealthwiseai.ui.components.SectionHeader
import com.example.wealthwiseai.ui.navigation.Screen
import com.example.wealthwiseai.ui.theme.*
import com.example.wealthwiseai.viewmodel.DashboardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    navController: NavController,
    onNavigateToTransactions: () -> Unit
) {
    val userProfile by viewModel.userProfile.collectAsState()
    val goals by viewModel.goals.collectAsState()
    val healthScore by viewModel.financialHealthScore.collectAsState()
    val savingsRate by viewModel.savingsRate.collectAsState()
    val recentTxs by viewModel.recentTransactions.collectAsState(initial = emptyList())

    val totalIncome by viewModel.dynamicMonthlyIncome.collectAsState()
    val totalExpense by viewModel.dynamicMonthlyExpenses.collectAsState()
    val totalSavings by viewModel.dynamicMonthlySavings.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "WealthWise AI",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        color = BlueAccent,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Hello, ${userProfile?.fullName ?: "User"}",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
                IconButton(
                    onClick = onNavigateToTransactions,
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = BlueAccent.copy(alpha = 0.15f),
                        contentColor = BlueAccent
                    ),
                    modifier = Modifier.clip(CircleShape)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add Transaction")
                }
            }
        }

        // Financial Health Score
        item {
            val rating = FinancialHealthCalculator.getHealthRating(healthScore)
            val ratingColor = when (rating) {
                "Excellent" -> GreenAccent
                "Good" -> BlueAccent
                "Average" -> GoldAccent
                else -> RedAccent
            }

            GlassCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(100.dp)
                    ) {
                        CircularProgressIndicator(
                            progress = healthScore / 100f,
                            strokeWidth = 10.dp,
                            color = ratingColor,
                            trackColor = Color.White.copy(alpha = 0.1f),
                            modifier = Modifier.fillMaxSize()
                        )
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "$healthScore",
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                            Text(
                                text = "Score",
                                fontSize = 10.sp,
                                color = TextGray,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(20.dp))
                    Column {
                        Text(
                            text = "Financial Health: $rating",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = ratingColor
                        )
                        Text(
                            text = "Based on your income, expenses, and savings habits. Savings rate is ${"%.1f".format(savingsRate)}%.",
                            fontSize = 12.sp,
                            color = TextGray,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }

        // Financial Cards Row
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Income Card
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = CardBg),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Monthly Income", fontSize = 12.sp, color = TextGray)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("₹${"%.0f".format(totalIncome)}", fontSize = 20.sp, fontWeight = FontWeight.Black, color = Color.White)
                        }
                    }
                    // Expense Card
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = CardBg),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Monthly Expenses", fontSize = 12.sp, color = TextGray)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("₹${"%.0f".format(totalExpense)}", fontSize = 20.sp, fontWeight = FontWeight.Black, color = RedAccent)
                        }
                    }
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Savings Card
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = CardBg),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Monthly Savings", fontSize = 12.sp, color = TextGray)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("₹${"%.0f".format(totalSavings)}", fontSize = 20.sp, fontWeight = FontWeight.Black, color = GreenAccent)
                        }
                    }
                    // Investment Capacity Card
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = CardBg),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Invest Capacity", fontSize = 12.sp, color = TextGray)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("₹${"%.0f".format(totalSavings * 0.8)}", fontSize = 20.sp, fontWeight = FontWeight.Black, color = BlueAccent)
                        }
                    }
                }
            }
        }

        // Quick AI recommendation Card
        item {
            GlassCard(
                modifier = Modifier.clickable { navController.navigate(Screen.Advisor.route) }
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(BlueAccent.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = BlueAccent)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("AI Advisor Insights", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.White)
                        Text("Tap to view personalized financial guidance based on your risk comfort and budget leaks.", fontSize = 11.sp, color = TextGray)
                    }
                    Icon(imageVector = Icons.Default.KeyboardArrowRight, contentDescription = null, tint = TextGray)
                }
            }
        }

        // Goals Progress Summary
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SectionHeader("Goals Progress")
                    Text(
                        text = "View All",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = BlueAccent,
                        modifier = Modifier.clickable { navController.navigate(Screen.Goals.route) }
                    )
                }
                if (goals.isEmpty()) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = CardBg),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "No financial goals created. Plan your savings goals in the Goals tab.",
                            fontSize = 12.sp,
                            color = TextGray,
                            modifier = Modifier.padding(16.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        goals.take(2).forEach { goal ->
                            GoalSummaryRow(goal)
                        }
                    }
                }
            }
        }

        // Recent Transactions
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SectionHeader("Recent Transactions")
                Text(
                    text = "Log / Edit",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = BlueAccent,
                    modifier = Modifier.clickable(onClick = onNavigateToTransactions)
                )
            }
        }

        if (recentTxs.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = CardBg),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp)
                ) {
                    Text(
                        text = "No transactions logged yet. Tap the '+' button or Log/Edit to add income or expense.",
                        fontSize = 12.sp,
                        color = TextGray,
                        modifier = Modifier.padding(16.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            items(recentTxs) { tx ->
                TransactionRow(tx)
            }
            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun GoalSummaryRow(goal: GoalEntity) {
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
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(goal.goalName, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                Text("${(progress * 100).toInt()}%", fontWeight = FontWeight.Bold, color = BlueAccent, fontSize = 14.sp)
            }
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = progress,
                color = BlueAccent,
                trackColor = Color.White.copy(alpha = 0.1f),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(CircleShape)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "₹${goal.currentSavedAmount.toInt()} saved of ₹${goal.targetAmount.toInt()}",
                fontSize = 11.sp,
                color = TextGray
            )
        }
    }
}

@Composable
fun TransactionRow(tx: TransactionEntity) {
    val isIncome = tx.type.lowercase() == "income"
    Card(
        colors = CardDefaults.cardColors(containerColor = CardBg.copy(alpha = 0.7f)),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
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
                        fontSize = 11.sp,
                        color = TextGray
                    )
                }
            }
            Text(
                text = "${if (isIncome) "+" else "-"}₹${"%.2f".format(tx.amount)}",
                fontWeight = FontWeight.Black,
                fontSize = 15.sp,
                color = if (isIncome) GreenAccent else RedAccent
            )
        }
    }
}
