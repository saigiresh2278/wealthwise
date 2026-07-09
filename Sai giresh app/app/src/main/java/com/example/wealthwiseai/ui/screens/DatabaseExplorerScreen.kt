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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.wealthwiseai.data.local.entity.*
import com.example.wealthwiseai.ui.components.SectionHeader
import com.example.wealthwiseai.ui.theme.*
import com.example.wealthwiseai.viewmodel.DatabaseExplorerViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatabaseExplorerScreen(
    viewModel: DatabaseExplorerViewModel,
    onBack: () -> Unit
) {
    val users by viewModel.users.collectAsState()
    val profiles by viewModel.profiles.collectAsState()
    val transactions by viewModel.transactions.collectAsState()
    val goals by viewModel.goals.collectAsState()
    val riskProfiles by viewModel.riskProfiles.collectAsState()

    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Users", "Profiles", "Txns", "Goals", "Risk")

    var selectedUserToDelete by remember { mutableStateOf<AuthEntity?>(null) }
    var selectedProfileToDelete by remember { mutableStateOf<UserProfileEntity?>(null) }
    var selectedTxnToDelete by remember { mutableStateOf<TransactionEntity?>(null) }
    var selectedGoalToDelete by remember { mutableStateOf<GoalEntity?>(null) }
    var selectedRiskToDelete by remember { mutableStateOf<RiskProfileEntity?>(null) }

    var recordToShowDetails by remember { mutableStateOf<Any?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Database Explorer", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBg)
            )
        },
        containerColor = DarkBg
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Tab Row
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = CardBg,
                contentColor = BlueAccent
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                text = title,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (selectedTab == index) BlueAccent else TextGray
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Main Contents
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                when (selectedTab) {
                    0 -> UsersTable(users, onDetails = { recordToShowDetails = it }, onDelete = { selectedUserToDelete = it })
                    1 -> ProfilesTable(profiles, onDetails = { recordToShowDetails = it }, onDelete = { selectedProfileToDelete = it })
                    2 -> TransactionsTable(transactions, onDetails = { recordToShowDetails = it }, onDelete = { selectedTxnToDelete = it })
                    3 -> GoalsTable(goals, onDetails = { recordToShowDetails = it }, onDelete = { selectedGoalToDelete = it })
                    4 -> RisksTable(riskProfiles, onDetails = { recordToShowDetails = it }, onDelete = { selectedRiskToDelete = it })
                }
            }
        }

        // CONFIRMATION DIALOGS
        if (selectedUserToDelete != null) {
            AlertDialog(
                onDismissRequest = { selectedUserToDelete = null },
                title = { Text("Delete User?", fontWeight = FontWeight.Bold, color = Color.White) },
                text = { Text("This will delete the user ${selectedUserToDelete!!.email} and all their linked profiles, transactions, and goals. This cannot be undone.", color = Color.LightGray) },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.deleteUser(selectedUserToDelete!!)
                            selectedUserToDelete = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = RedAccent)
                    ) { Text("Delete", color = Color.White) }
                },
                dismissButton = {
                    TextButton(onClick = { selectedUserToDelete = null }) { Text("Cancel", color = TextGray) }
                },
                containerColor = CardBg,
                shape = RoundedCornerShape(24.dp)
            )
        }

        if (selectedProfileToDelete != null) {
            AlertDialog(
                onDismissRequest = { selectedProfileToDelete = null },
                title = { Text("Delete Profile?", fontWeight = FontWeight.Bold, color = Color.White) },
                text = { Text("This will delete the advisory profile of ${selectedProfileToDelete!!.email}. Continue?", color = Color.LightGray) },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.deleteProfile(selectedProfileToDelete!!)
                            selectedProfileToDelete = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = RedAccent)
                    ) { Text("Delete", color = Color.White) }
                },
                dismissButton = {
                    TextButton(onClick = { selectedProfileToDelete = null }) { Text("Cancel", color = TextGray) }
                },
                containerColor = CardBg,
                shape = RoundedCornerShape(24.dp)
            )
        }

        if (selectedTxnToDelete != null) {
            AlertDialog(
                onDismissRequest = { selectedTxnToDelete = null },
                title = { Text("Delete Transaction?", fontWeight = FontWeight.Bold, color = Color.White) },
                text = { Text("Delete transaction of ₹${selectedTxnToDelete!!.amount} in category ${selectedTxnToDelete!!.category}?", color = Color.LightGray) },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.deleteTransaction(selectedTxnToDelete!!)
                            selectedTxnToDelete = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = RedAccent)
                    ) { Text("Delete", color = Color.White) }
                },
                dismissButton = {
                    TextButton(onClick = { selectedTxnToDelete = null }) { Text("Cancel", color = TextGray) }
                },
                containerColor = CardBg,
                shape = RoundedCornerShape(24.dp)
            )
        }

        if (selectedGoalToDelete != null) {
            AlertDialog(
                onDismissRequest = { selectedGoalToDelete = null },
                title = { Text("Delete Goal?", fontWeight = FontWeight.Bold, color = Color.White) },
                text = { Text("Delete the financial goal '${selectedGoalToDelete!!.goalName}'? (Target: ₹${selectedGoalToDelete!!.targetAmount})", color = Color.LightGray) },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.deleteGoal(selectedGoalToDelete!!)
                            selectedGoalToDelete = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = RedAccent)
                    ) { Text("Delete", color = Color.White) }
                },
                dismissButton = {
                    TextButton(onClick = { selectedGoalToDelete = null }) { Text("Cancel", color = TextGray) }
                },
                containerColor = CardBg,
                shape = RoundedCornerShape(24.dp)
            )
        }

        if (selectedRiskToDelete != null) {
            AlertDialog(
                onDismissRequest = { selectedRiskToDelete = null },
                title = { Text("Delete Risk Profile?", fontWeight = FontWeight.Bold, color = Color.White) },
                text = { Text("Delete the risk assessment for ${selectedRiskToDelete!!.email}?", color = Color.LightGray) },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.deleteRiskProfile(selectedRiskToDelete!!)
                            selectedRiskToDelete = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = RedAccent)
                    ) { Text("Delete", color = Color.White) }
                },
                dismissButton = {
                    TextButton(onClick = { selectedRiskToDelete = null }) { Text("Cancel", color = TextGray) }
                },
                containerColor = CardBg,
                shape = RoundedCornerShape(24.dp)
            )
        }

        // DETAIL DIALOG
        if (recordToShowDetails != null) {
            AlertDialog(
                onDismissRequest = { recordToShowDetails = null },
                title = { Text("Record Details", fontWeight = FontWeight.Bold, color = Color.White) },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val detailsList = getDetailsFromRecord(recordToShowDetails!!)
                        detailsList.forEach { (label, value) ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(label, color = TextGray, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                Text(value, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { recordToShowDetails = null },
                        colors = ButtonDefaults.buttonColors(containerColor = BlueAccent)
                    ) { Text("Close", color = Color.White) }
                },
                containerColor = CardBg,
                shape = RoundedCornerShape(24.dp)
            )
        }
    }
}

private fun getDetailsFromRecord(record: Any): List<Pair<String, String>> {
    return when (record) {
        is AuthEntity -> listOf(
            "Email" to record.email,
            "Name" to record.fullName,
            "Password Hash" to record.passwordHash
        )
        is UserProfileEntity -> listOf(
            "Email" to record.email,
            "Name" to record.fullName,
            "Age" to record.age.toString(),
            "Occupation" to record.occupation,
            "Income" to "₹${record.monthlyIncome}",
            "Expenses" to "₹${record.monthlyExpenses}",
            "Savings" to "₹${record.monthlySavings}",
            "Main Goal" to record.mainFinancialGoal,
            "Risk Comfort" to record.riskComfort,
            "Experience" to record.investmentExperience
        )
        is TransactionEntity -> listOf(
            "ID" to record.id.toString(),
            "User Email" to record.userEmail,
            "Amount" to "₹${record.amount}",
            "Category" to record.category,
            "Note" to record.note,
            "Type" to record.type,
            "Date" to SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()).format(Date(record.date))
        )
        is GoalEntity -> listOf(
            "ID" to record.id.toString(),
            "User Email" to record.userEmail,
            "Goal Name" to record.goalName,
            "Target Amount" to "₹${record.targetAmount}",
            "Saved So Far" to "₹${record.currentSavedAmount}",
            "Due Date" to SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(record.targetDate)),
            "Priority" to record.priority
        )
        is RiskProfileEntity -> listOf(
            "Email" to record.email,
            "Risk Score" to record.score.toString(),
            "Risk Class" to record.riskClass,
            "Assessment Date" to SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()).format(Date(record.lastAssessmentDate))
        )
        else -> emptyList()
    }
}

@Composable
fun UsersTable(users: List<AuthEntity>, onDetails: (AuthEntity) -> Unit, onDelete: (AuthEntity) -> Unit) {
    if (users.isEmpty()) {
        EmptyTablePlaceholder("No users in the database.")
    } else {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(users) { u ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = CardBg),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth().clickable { onDetails(u) }
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = u.fullName, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 15.sp)
                            Text(text = u.email, color = TextGray, fontSize = 12.sp)
                        }
                        IconButton(onClick = { onDelete(u) }) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = RedAccent)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProfilesTable(profiles: List<UserProfileEntity>, onDetails: (UserProfileEntity) -> Unit, onDelete: (UserProfileEntity) -> Unit) {
    if (profiles.isEmpty()) {
        EmptyTablePlaceholder("No profiles in the database.")
    } else {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(profiles) { p ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = CardBg),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth().clickable { onDetails(p) }
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = p.fullName, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 15.sp)
                            Text(text = "Income: ₹${p.monthlyIncome} | Save: ₹${p.monthlySavings}", color = TextGray, fontSize = 12.sp)
                        }
                        IconButton(onClick = { onDelete(p) }) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = RedAccent)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TransactionsTable(transactions: List<TransactionEntity>, onDetails: (TransactionEntity) -> Unit, onDelete: (TransactionEntity) -> Unit) {
    if (transactions.isEmpty()) {
        EmptyTablePlaceholder("No transactions in the database.")
    } else {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(transactions) { t ->
                val isIncome = t.type.lowercase() == "income"
                Card(
                    colors = CardDefaults.cardColors(containerColor = CardBg),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth().clickable { onDetails(t) }
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.size(8.dp).background(if (isIncome) GreenAccent else RedAccent, CircleShape))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = t.category, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                            Text(text = t.userEmail, color = TextGray, fontSize = 11.sp)
                        }
                        Text(
                            text = "${if (isIncome) "+" else "-"}₹${t.amount}",
                            fontWeight = FontWeight.Black,
                            color = if (isIncome) GreenAccent else RedAccent,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                        IconButton(onClick = { onDelete(t) }) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = RedAccent)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GoalsTable(goals: List<GoalEntity>, onDetails: (GoalEntity) -> Unit, onDelete: (GoalEntity) -> Unit) {
    if (goals.isEmpty()) {
        EmptyTablePlaceholder("No goals in the database.")
    } else {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(goals) { g ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = CardBg),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth().clickable { onDetails(g) }
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = g.goalName, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                            Text(text = "Saved ₹${g.currentSavedAmount} of ₹${g.targetAmount}", color = TextGray, fontSize = 11.sp)
                        }
                        IconButton(onClick = { onDelete(g) }) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = RedAccent)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RisksTable(riskProfiles: List<RiskProfileEntity>, onDetails: (RiskProfileEntity) -> Unit, onDelete: (RiskProfileEntity) -> Unit) {
    if (riskProfiles.isEmpty()) {
        EmptyTablePlaceholder("No risk profiles in the database.")
    } else {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(riskProfiles) { r ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = CardBg),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth().clickable { onDetails(r) }
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = r.email, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                            Text(text = "Score: ${r.score} | Class: ${r.riskClass}", color = TextGray, fontSize = 11.sp)
                        }
                        IconButton(onClick = { onDelete(r) }) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = RedAccent)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyTablePlaceholder(message: String) {
    Box(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = TextGray, modifier = Modifier.size(36.dp))
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = message, color = TextGray, fontSize = 13.sp, modifier = Modifier.fillMaxWidth(), style = MaterialTheme.typography.bodyMedium)
        }
    }
}
