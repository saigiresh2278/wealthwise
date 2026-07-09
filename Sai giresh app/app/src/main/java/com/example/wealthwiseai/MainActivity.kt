package com.example.wealthwiseai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.example.wealthwiseai.data.local.AppDatabase
import com.example.wealthwiseai.data.local.SessionManager
import com.example.wealthwiseai.ui.navigation.Screen
import com.example.wealthwiseai.ui.screens.*
import com.example.wealthwiseai.ui.theme.BlueAccent
import com.example.wealthwiseai.ui.theme.WealthWiseAITheme
import com.example.wealthwiseai.viewmodel.*
import kotlinx.coroutines.launch
import com.example.wealthwiseai.data.firebase.FirebaseSyncHelper

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseSyncHelper.init(applicationContext)
        setContent {
            val settingsViewModel: SettingsViewModel = viewModel()
            val isDarkMode by settingsViewModel.isDarkMode.collectAsState()

            WealthWiseAITheme(darkTheme = isDarkMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainAppContent(settingsViewModel)
                }
            }
        }
    }
}

@Composable
fun MainAppContent(settingsViewModel: SettingsViewModel) {
    val navController = rememberNavController()
    val coroutineScope = rememberCoroutineScope()

    // ViewModels
    val authViewModel: AuthViewModel = viewModel()
    val onboardingViewModel: OnboardingViewModel = viewModel()
    val dashboardViewModel: DashboardViewModel = viewModel()
    val transactionViewModel: TransactionViewModel = viewModel()
    val advisorViewModel: AdvisorViewModel = viewModel()
    val goalViewModel: GoalViewModel = viewModel()
    val riskViewModel: RiskViewModel = viewModel()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showBottomBar = currentRoute in listOf(
        Screen.Dashboard.route,
        Screen.Expenses.route,
        Screen.Goals.route,
        Screen.Advisor.route,
        Screen.Profile.route,
        Screen.Settings.route
    )

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                BottomNavigationBar(navController)
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Splash.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Splash.route) {
                SplashScreen(onAnimationFinished = {
                    coroutineScope.launch {
                        val context = navController.context
                        val sessionManager = SessionManager.getInstance(context)
                        val database = AppDatabase.getDatabase(context)
                        val loggedIn = sessionManager.isLoggedIn.value
                        val email = sessionManager.userEmail.value
                        
                        if (loggedIn && email != null) {
                            val profile = database.userProfileDao().getProfileByEmailDirect(email)
                            val target = if (profile != null) Screen.Dashboard.route else Screen.Onboarding.route
                            navController.navigate(target) {
                                popUpTo(Screen.Splash.route) { inclusive = true }
                            }
                        } else {
                            navController.navigate(Screen.Login.route) {
                                popUpTo(Screen.Splash.route) { inclusive = true }
                            }
                        }
                    }
                })
            }

            composable(Screen.Login.route) {
                LoginScreen(
                    viewModel = authViewModel,
                    onNavigateToSignup = {
                        navController.navigate(Screen.Signup.route)
                    },
                    onLoginSuccess = { email ->
                        coroutineScope.launch {
                            val context = navController.context
                            val database = AppDatabase.getDatabase(context)
                            val profile = database.userProfileDao().getProfileByEmailDirect(email)
                            val target = if (profile != null) Screen.Dashboard.route else Screen.Onboarding.route
                            navController.navigate(target) {
                                popUpTo(Screen.Login.route) { inclusive = true }
                            }
                        }
                    }
                )
            }

            composable(Screen.Signup.route) {
                SignupScreen(
                    viewModel = authViewModel,
                    onNavigateToLogin = {
                        navController.navigate(Screen.Login.route)
                    },
                    onSignupSuccess = {
                        navController.navigate(Screen.Onboarding.route) {
                            popUpTo(Screen.Signup.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Onboarding.route) {
                OnboardingScreen(
                    viewModel = onboardingViewModel,
                    onComplete = {
                        navController.navigate(Screen.Dashboard.route) {
                            popUpTo(Screen.Onboarding.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Dashboard.route) {
                DashboardScreen(
                    viewModel = dashboardViewModel,
                    navController = navController,
                    onNavigateToTransactions = {
                        navController.navigate(Screen.Expenses.route)
                    }
                )
            }

            composable(Screen.Expenses.route) {
                TransactionManagementScreen(
                    viewModel = transactionViewModel,
                    onBack = {
                        navController.popBackStack()
                    }
                )
            }

            composable(Screen.Advisor.route) {
                AdvisorScreen(
                    viewModel = advisorViewModel,
                    onNavigateToLearn = {
                        navController.navigate(Screen.Learn.route) {
                            popUpTo(Screen.Dashboard.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }

            composable("risk_quiz") {
                RiskAnalyzerScreen(
                    viewModel = riskViewModel,
                    onBack = {
                        navController.popBackStack()
                    }
                )
            }

            composable(Screen.Goals.route) {
                GoalPlannerScreen(viewModel = goalViewModel)
            }

            composable(Screen.Reports.route) {
                ReportsScreen(viewModel = dashboardViewModel)
            }

            composable(Screen.Learn.route) {
                LearningHubScreen()
            }

            composable(Screen.Profile.route) {
                val context = navController.context
                val sessionManager = SessionManager.getInstance(context)
                val userEmail by sessionManager.userEmail.collectAsState()
                ProfileScreen(
                    viewModel = settingsViewModel,
                    userEmail = userEmail ?: "",
                    onLogout = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Dashboard.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Settings.route) {
                SettingsScreen(
                    viewModel = settingsViewModel,
                    onResetCompleted = {
                        navController.navigate(Screen.Onboarding.route) {
                            popUpTo(Screen.Dashboard.route) { inclusive = true }
                        }
                    },
                    onNavigateToRiskQuiz = {
                        navController.navigate("risk_quiz")
                    },
                    onNavigateToDatabaseExplorer = {
                        navController.navigate(Screen.DatabaseExplorer.route)
                    }
                )
            }

            composable(Screen.DatabaseExplorer.route) {
                val dbViewModel: DatabaseExplorerViewModel = viewModel()
                DatabaseExplorerScreen(
                    viewModel = dbViewModel,
                    onBack = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}

data class NavigationItem(
    val screen: Screen,
    val title: String,
    val icon: ImageVector
)

@Composable
fun BottomNavigationBar(navController: NavHostController) {
    val items = listOf(
        NavigationItem(Screen.Dashboard, "Home", Icons.Default.Home),
        NavigationItem(Screen.Expenses, "Expenses", Icons.Default.AddCircle),
        NavigationItem(Screen.Goals, "Goals", Icons.Default.CheckCircle),
        NavigationItem(Screen.Advisor, "AI", Icons.Default.Star),
        NavigationItem(Screen.Profile, "Profile", Icons.Default.Person),
        NavigationItem(Screen.Settings, "Settings", Icons.Default.Settings)
    )

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
        tonalElevation = 8.dp
    ) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        items.forEach { item ->
            val isSelected = currentRoute == item.screen.route
            NavigationBarItem(
                selected = isSelected,
                alwaysShowLabel = false,
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.title
                    )
                },
                label = {
                    Text(
                        text = item.title,
                        maxLines = 1,
                        softWrap = false,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                        fontSize = 10.sp,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color.White,
                    selectedTextColor = BlueAccent,
                    indicatorColor = BlueAccent,
                    unselectedIconColor = Color.Gray,
                    unselectedTextColor = Color.Gray
                ),
                onClick = {
                    if (currentRoute != item.screen.route) {
                        navController.navigate(item.screen.route) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            )
        }
    }
}
