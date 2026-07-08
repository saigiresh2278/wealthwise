package com.example.wealthwiseai.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
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
import com.example.wealthwiseai.ai.GoalPlannerEngine
import com.example.wealthwiseai.data.local.entity.GoalEntity
import com.example.wealthwiseai.ui.components.CustomDropdown
import com.example.wealthwiseai.ui.components.CustomTextField
import com.example.wealthwiseai.ui.components.SectionHeader
import com.example.wealthwiseai.ui.theme.*
import com.example.wealthwiseai.viewmodel.GoalViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun GoalPlannerScreen(
    viewModel: GoalViewModel
) {
    val goals by viewModel.goals.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var editingGoal by remember { mutableStateOf<GoalEntity?>(null) }

    // Dialog state fields
    var goalName by remember { mutableStateOf("") }
    var targetAmountStr by remember { mutableStateOf("") }
    var currentSavedStr by remember { mutableStateOf("") }
    var targetMonthsStr by remember { mutableStateOf("12") } // target date in months from now
    var priority by remember { mutableStateOf("Medium") }

    // Validation
    var nameError by remember { mutableStateOf("") }
    var targetError by remember { mutableStateOf("") }
    var currentSavedError by remember { mutableStateOf("") }
    var monthsError by remember { mutableStateOf("") }

    val priorities = listOf("Low", "Medium", "High")

    LaunchedEffect(showAddDialog, editingGoal) {
        if (editingGoal != null) {
            goalName = editingGoal!!.goalName
            targetAmountStr = editingGoal!!.targetAmount.toString()
            currentSavedStr = editingGoal!!.currentSavedAmount.toString()
            
            // Convert timestamp back to months remaining
            val remainingMs = editingGoal!!.targetDate - System.currentTimeMillis()
            val months = (remainingMs / (30L * 24 * 60 * 60 * 1000)).coerceAtLeast(1)
            targetMonthsStr = months.toString()
            priority = editingGoal!!.priority
            
            nameError = ""
            targetError = ""
            currentSavedError = ""
            monthsError = ""
        } else {
            goalName = ""
            targetAmountStr = ""
            currentSavedStr = "0"
            targetMonthsStr = "12"
            priority = "Medium"
            nameError = ""
            targetError = ""
            currentSavedError = ""
            monthsError = ""
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
                .padding(horizontal = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SectionHeader("Financial Goals")
                Button(
                    onClick = { showAddDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = BlueAccent)
                ) {
                    Text("Add Goal", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }

            Text(
                text = "Plan and track milestones with visual progress and feasibility estimates.",
                fontSize = 12.sp,
                color = TextGray,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            if (goals.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No financial goals planned yet.\nTap 'Add Goal' to create one.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextGray,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(goals) { goal ->
                        val currentSavingsCapacity = userProfile?.monthlySavings ?: 0.0
                        val analysis = GoalPlannerEngine.analyzeGoal(goal, currentSavingsCapacity)
                        
                        GoalCard(
                            goal = goal,
                            remaining = analysis.remainingAmount,
                            monthlyRequired = analysis.monthlySavingsRequired,
                            completionProgress = analysis.completionPercentage,
                            isAchievable = analysis.isAchievable,
                            onEdit = { editingGoal = goal },
                            onDelete = { viewModel.deleteGoal(goal) }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
        }

        // Add / Edit Goal Dialog
        if (showAddDialog || editingGoal != null) {
            val isEditing = editingGoal != null
            AlertDialog(
                onDismissRequest = {
                    showAddDialog = false
                    editingGoal = null
                },
                title = {
                    Text(
                        text = if (isEditing) "Edit Financial Goal" else "Create Financial Goal",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        CustomTextField(
                            value = goalName,
                            onValueChange = {
                                goalName = it
                                nameError = if (it.trim().isEmpty()) "Goal name required" else ""
                            },
                            label = "Goal Name",
                            placeholder = "Buy a laptop / Save for vacation",
                            isError = nameError.isNotEmpty(),
                            errorMessage = nameError
                        )

                        CustomTextField(
                            value = targetAmountStr,
                            onValueChange = {
                                targetAmountStr = it
                                val amt = it.toDoubleOrNull()
                                targetError = when {
                                    it.isEmpty() -> "Target amount is required"
                                    amt == null || amt <= 0.0 -> "Must be a positive amount"
                                    else -> ""
                                }
                            },
                            label = "Target Amount (₹)",
                            placeholder = "1500",
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            isError = targetError.isNotEmpty(),
                            errorMessage = targetError
                        )

                        CustomTextField(
                            value = currentSavedStr,
                            onValueChange = {
                                currentSavedStr = it
                                val sav = it.toDoubleOrNull()
                                currentSavedError = when {
                                    it.isEmpty() -> "Saved amount is required"
                                    sav == null || sav < 0.0 -> "Cannot be negative"
                                    else -> ""
                                }
                            },
                            label = "Current Saved (₹)",
                            placeholder = "200",
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            isError = currentSavedError.isNotEmpty(),
                            errorMessage = currentSavedError
                        )

                        CustomTextField(
                            value = targetMonthsStr,
                            onValueChange = {
                                targetMonthsStr = it
                                val months = it.toLongOrNull()
                                monthsError = when {
                                    it.isEmpty() -> "Months count is required"
                                    months == null || months <= 0 -> "Must be 1 month or more"
                                    else -> ""
                                }
                            },
                            label = "Months to Achieve",
                            placeholder = "12",
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            isError = monthsError.isNotEmpty(),
                            errorMessage = monthsError
                        )

                        CustomDropdown(
                            selectedOption = priority,
                            onOptionSelected = { priority = it },
                            options = priorities,
                            label = "Priority"
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val targetAmt = targetAmountStr.toDoubleOrNull()
                            val curSaved = currentSavedStr.toDoubleOrNull()
                            val months = targetMonthsStr.toLongOrNull()
                            val isNameValid = goalName.trim().isNotEmpty()

                            if (!isNameValid) nameError = "Goal name required"
                            if (targetAmt == null || targetAmt <= 0.0) targetError = "Invalid target"
                            if (curSaved == null || curSaved < 0.0) currentSavedError = "Invalid saved amount"
                            if (months == null || months <= 0) monthsError = "Invalid months duration"

                            if (isNameValid && targetAmt != null && targetAmt > 0.0 && curSaved != null && curSaved >= 0.0 && months != null && months > 0) {
                                // Calculate timestamp based on months
                                val timestamp = System.currentTimeMillis() + (months * 30L * 24 * 60 * 60 * 1000)
                                if (isEditing) {
                                    viewModel.updateGoal(
                                        id = editingGoal!!.id,
                                        name = goalName.trim(),
                                        targetAmount = targetAmt,
                                        currentSavedAmount = curSaved,
                                        targetDate = timestamp,
                                        priority = priority
                                    )
                                } else {
                                    viewModel.addGoal(
                                        name = goalName.trim(),
                                        targetAmount = targetAmt,
                                        currentSavedAmount = curSaved,
                                        targetDate = timestamp,
                                        priority = priority
                                    )
                                }
                                showAddDialog = false
                                editingGoal = null
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
                            editingGoal = null
                        }
                    ) {
                        Text("Cancel", color = TextGray)
                    }
                },
                containerColor = CardBg,
                shape = RoundedCornerShape(24.dp)
            )
        }
    }
}

@Composable
fun GoalCard(
    goal: GoalEntity,
    remaining: Double,
    monthlyRequired: Double,
    completionProgress: Float,
    isAchievable: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val sdf = SimpleDateFormat("MMM yyyy", Locale.getDefault())
    val targetDateStr = sdf.format(Date(goal.targetDate))

    Card(
        colors = CardDefaults.cardColors(containerColor = CardBg),
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(24.dp))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = goal.goalName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color.White
                    )
                    Text(
                        text = "Target Date: $targetDateStr",
                        fontSize = 11.sp,
                        color = TextGray
                    )
                }
                Row {
                    IconButton(onClick = onEdit) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit Goal", tint = BlueAccent, modifier = Modifier.size(20.dp))
                    }
                    IconButton(onClick = onDelete) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete Goal", tint = RedAccent, modifier = Modifier.size(20.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Progress Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Goal Progress",
                    fontSize = 12.sp,
                    color = TextGray
                )
                Text(
                    text = "${(completionProgress * 100).toInt()}%",
                    fontWeight = FontWeight.Black,
                    color = BlueAccent,
                    fontSize = 14.sp
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = completionProgress,
                color = BlueAccent,
                trackColor = Color.White.copy(alpha = 0.1f),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Saved: ₹${goal.currentSavedAmount.toInt()} / ₹${goal.targetAmount.toInt()}", fontSize = 12.sp, color = Color.White)
                Text("Left: ₹${remaining.toInt()}", fontSize = 12.sp, color = TextGray)
            }

            Divider(modifier = Modifier.padding(vertical = 12.dp), color = Color.White.copy(alpha = 0.08f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Monthly Savings Required", fontSize = 11.sp, color = TextGray)
                    Text("₹${"%.2f".format(monthlyRequired)}/mo", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }

                // Achievability Tag
                val tagBg = if (isAchievable) GreenAccent.copy(alpha = 0.15f) else RedAccent.copy(alpha = 0.15f)
                val tagText = if (isAchievable) "Achievable" else "At Risk"
                val tagColor = if (isAchievable) GreenAccent else RedAccent

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(tagBg)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = tagText,
                        color = tagColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
