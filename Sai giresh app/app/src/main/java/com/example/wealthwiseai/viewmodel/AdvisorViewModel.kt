package com.example.wealthwiseai.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.wealthwiseai.ai.AdvisorRuleEngine
import com.example.wealthwiseai.data.local.AppDatabase
import com.example.wealthwiseai.data.local.SessionManager
import com.example.wealthwiseai.data.local.entity.UserProfileEntity
import com.example.wealthwiseai.data.local.entity.TransactionEntity
import com.example.wealthwiseai.data.local.entity.GoalEntity
import com.example.wealthwiseai.data.repository.UserRepository
import com.example.wealthwiseai.data.repository.TransactionRepository
import com.example.wealthwiseai.data.repository.GoalRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class AdvisorViewModel(application: Application) : AndroidViewModel(application) {

    private val userRepository: UserRepository
    private val transactionRepository: TransactionRepository
    private val goalRepository: GoalRepository
    private val sessionManager: SessionManager

    val userProfile: StateFlow<UserProfileEntity?>
    val transactions: StateFlow<List<TransactionEntity>>
    val goals: StateFlow<List<GoalEntity>>

    // Expense Sheet analysis result state
    data class ExpenseSheetAnalysisResult(
        val sheetName: String,
        val totalExpenses: Double,
        val topCategory: String,
        val budgetLeaks: List<String>,
        val recommendations: List<String>,
        val categoryBreakdown: Map<String, Double>,
        val isValid: Boolean = true,
        val errorMessage: String? = null
    )
    private val _expenseSheetAnalysis = MutableStateFlow<ExpenseSheetAnalysisResult?>(null)
    val expenseSheetAnalysis: StateFlow<ExpenseSheetAnalysisResult?> = _expenseSheetAnalysis

    // Resume analysis result state
    data class ResumeAnalysisResult(
        val role: String,
        val salaryEstimate: String,
        val skillsToLearn: List<String>,
        val summary: String
    )
    private val _resumeAnalysis = MutableStateFlow<ResumeAnalysisResult?>(null)
    val resumeAnalysis: StateFlow<ResumeAnalysisResult?> = _resumeAnalysis

    init {
        val database = AppDatabase.getDatabase(application)
        userRepository = UserRepository(database.userProfileDao())
        transactionRepository = TransactionRepository(database.transactionDao())
        goalRepository = GoalRepository(database.goalDao())
        sessionManager = SessionManager.getInstance(application)

        userProfile = sessionManager.userEmail.flatMapLatest { email ->
            if (email != null) userRepository.getUserProfile(email) else flowOf(null)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

        transactions = sessionManager.userEmail.flatMapLatest { email ->
            if (email != null) transactionRepository.getTransactions(email) else flowOf(emptyList())
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        goals = sessionManager.userEmail.flatMapLatest { email ->
            if (email != null) goalRepository.getGoals(email) else flowOf(emptyList())
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    }

    val advisoryResult: StateFlow<AdvisorRuleEngine.AdvisoryResult> = combine(
        userProfile,
        transactions,
        goals
    ) { profile, txs, gs ->
        AdvisorRuleEngine.generateAdvice(profile, txs, gs)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AdvisorRuleEngine.AdvisoryResult(
            healthScore = 0,
            savingsRate = 0.0,
            suggestions = listOf("Loading recommendations..."),
            learningRecommendations = emptyList(),
            alerts = emptyList()
        )
    )

    fun clearExpenseSheet() {
        _expenseSheetAnalysis.value = null
    }

    fun analyzeExpenseSheet(fileName: String) {
        viewModelScope.launch {
            val nameLower = fileName.lowercase()
            val isBank = nameLower.contains("bank") || nameLower.contains("statement") || 
                         nameLower.contains("hdfc") || nameLower.contains("icici") || 
                         nameLower.contains("sbi") || nameLower.contains("transaction") || 
                         nameLower.contains("salary")
            
            val isGrocery = nameLower.contains("grocery") || nameLower.contains("bill") || 
                            nameLower.contains("invoice") || nameLower.contains("receipt") || 
                            nameLower.contains("market") || nameLower.contains("daily") || 
                            nameLower.contains("grocery_list") || nameLower.contains("calculation") ||
                            nameLower.contains("math")

            if (!isBank && !isGrocery) {
                _expenseSheetAnalysis.value = ExpenseSheetAnalysisResult(
                    sheetName = fileName,
                    totalExpenses = 0.0,
                    topCategory = "",
                    budgetLeaks = emptyList(),
                    recommendations = emptyList(),
                    categoryBreakdown = emptyMap(),
                    isValid = false,
                    errorMessage = "Error: Unsupported document. Please upload a valid bank statement sheet or a grocery document containing math calculations."
                )
                return@launch
            }

            var totalExpenses = 45000.0
            var topCategory = "Rent & Utilities"
            var leaks = listOf(
                "₹3,500 spent on multiple overlapping streaming subscriptions.",
                "₹5,200 on weekend dining out (exceeding standard 10% budget recommendation)."
            )
            var recommendations = listOf(
                "Consolidate streaming services to save ₹2,00,00 monthly.",
                "Limit dining out to ₹3,000 per month.",
                "Set up auto-savings of 20% on the 1st of the month."
            )
            var breakdown = mapOf(
                "Rent & Bills" to 22000.0,
                "Food & Dining" to 12000.0,
                "Shopping & Leisure" to 8000.0,
                "Others" to 3000.0
            )

            if (isGrocery) {
                totalExpenses = 4850.0
                topCategory = "Fresh Groceries & Dairy"
                leaks = listOf(
                    "₹850 price discrepancy in pre-packaged dairy brand versus local supply.",
                    "₹1,200 spent on high-markup snack items at checkout.",
                    "₹450 math calculation variance due to unitemized tax rounding errors."
                )
                recommendations = listOf(
                    "Buy generic staples (dry grains, beans) in bulk to save ₹1,500 monthly.",
                    "Review itemized math totals at checkout to audit rounding discrepancies.",
                    "Avoid high-markup micro-snacks in the billing aisle."
                )
                breakdown = mapOf(
                    "Vegetables & Greens" to 1800.0,
                    "Dairy & Eggs" to 1400.0,
                    "Snacks & Drinks" to 1100.0,
                    "Taxes & Rounding" to 550.0
                )
            } else {
                when {
                    nameLower.contains("hdfc") || nameLower.contains("salary") -> {
                        totalExpenses = 28500.0
                        topCategory = "Shopping & Travel"
                        leaks = listOf(
                            "₹4,200 in recurring ride-hailing fees (Uber/Ola).",
                            "₹2,800 on gourmet coffee and micro-transactions."
                        )
                        recommendations = listOf(
                            "Use public transit or carpool to reduce travel expenses by 30%.",
                            "Invest in a high-quality coffee flask to save ₹2,00,00 monthly.",
                            "Implement a 24-hour cooling period for shopping cart items."
                        )
                        breakdown = mapOf(
                            "Shopping" to 11000.0,
                            "Travel" to 8500.0,
                            "Dining" to 6000.0,
                            "Others" to 3000.0
                        )
                    }
                    nameLower.contains("icici") || nameLower.contains("credit") || nameLower.contains("card") || nameLower.contains("image") -> {
                        totalExpenses = 52000.0
                        topCategory = "Online Subscriptions & Dining"
                        leaks = listOf(
                            "₹6,000 spent on premium online food delivery orders (Zomato/Swiggy).",
                            "₹4,500 on impulse clothing sales."
                        )
                        recommendations = listOf(
                            "Cook home meals on weekdays to cut dining leaks by 50%.",
                            "Unsubscribe from promotional emails to avoid flash sales.",
                            "Use cash/debit instead of credit cards for shopping to improve discipline."
                        )
                        breakdown = mapOf(
                            "Dining" to 19000.0,
                            "Shopping" to 15000.0,
                            "Utilities" to 12000.0,
                            "Others" to 6000.0
                        )
                    }
                    nameLower.contains("sbi") || nameLower.contains("home") || nameLower.contains("loan") -> {
                        totalExpenses = 68000.0
                        topCategory = "Home Loan EMI & Groceries"
                        leaks = listOf(
                            "₹2,500 on high-end organic brand grocery markups.",
                            "₹1,800 in late fees on utility bills."
                        )
                        recommendations = listOf(
                            "Set up auto-pay for all utilities to avoid late penalties.",
                            "Buy local farm produce or generic brand items.",
                            "Refinance loan if lower interest rates are available."
                        )
                        breakdown = mapOf(
                            "EMI / Rent" to 35000.0,
                            "Groceries" to 18000.0,
                            "Utilities" to 10000.0,
                            "Others" to 5000.0
                        )
                    }
                }
            }

            _expenseSheetAnalysis.value = ExpenseSheetAnalysisResult(
                sheetName = fileName,
                totalExpenses = totalExpenses,
                topCategory = topCategory,
                budgetLeaks = leaks,
                recommendations = recommendations,
                categoryBreakdown = breakdown,
                isValid = true
            )
        }
    }

    fun analyzeResume(fileName: String, fileContent: String) {
        viewModelScope.launch {
            val content = fileContent.lowercase()
            var suitableRole = "Junior software developer"
            var salaryEstimate = "₹6,00,000 - ₹9,00,000 per annum"
            var skills = listOf("Kotlin", "Git", "Java Core")
            var summary = "Entry-level technical profile. Great foundational background."

            when {
                content.contains("android") || content.contains("kotlin") || content.contains("compose") || content.contains("flutter") -> {
                    suitableRole = "Lead Android Application Engineer"
                    salaryEstimate = "₹18,00,000 - ₹26,00,000 per annum"
                    skills = listOf("Jetpack Compose", "Kotlin Multiplatform (KMP)", "Clean Architecture", "Dagger Hilt", "CI/CD pipelines")
                    summary = "Strong mobile engineering background. Your experience matches advanced client-side framework requirements. Learning KMP will significantly boost your income profile."
                }
                content.contains("python") || content.contains("data") || content.contains("machine learning") || content.contains("ai") || content.contains("model") -> {
                    suitableRole = "AI / Machine Learning Engineer"
                    salaryEstimate = "₹22,00,000 - ₹32,00,000 per annum"
                    skills = listOf("TensorFlow/PyTorch", "Large Language Model (LLM) Fine-Tuning", "Vector Databases", "MLOps", "Cloud Data Pipelines")
                    summary = "Data-centric skillset identified. Highly sought after role with premium compensation. Focus on MLOps and LLM integration to capture the highest market brackets."
                }
                content.contains("react") || content.contains("angular") || content.contains("javascript") || content.contains("typescript") || content.contains("node") || content.contains("web") -> {
                    suitableRole = "Senior Full-Stack Engineer"
                    salaryEstimate = "₹15,00,000 - ₹22,00,000 per annum"
                    skills = listOf("TypeScript", "Next.js / Serverless", "Docker & Kubernetes", "GraphQL APIs", "System Architecture")
                    summary = "Web engineering profile detected. Excellent full-stack potential. Upgrading from simple frontend development to system architecture and Next.js increases salary leverage by ~40%."
                }
                content.contains("sales") || content.contains("marketing") || content.contains("business") || content.contains("growth") -> {
                    suitableRole = "Business Development Director"
                    salaryEstimate = "₹20,00,000 - ₹30,00,000 per annum"
                    skills = listOf("Revenue Operations", "High-ticket Client Acquisition", "Digital Marketing Strategy", "Product-Led Growth", "Data Analytics")
                    summary = "Strong commercial and growth skills identified. Leadership and management roles offer top-tier commissions and compensation. Learning data analytics will help justify larger marketing allocations."
                }
                content.contains("manager") || content.contains("lead") || content.contains("product") || content.contains("agile") -> {
                    suitableRole = "Senior Technical Product Manager"
                    salaryEstimate = "₹24,00,000 - ₹35,00,000 per annum"
                    skills = listOf("Product Strategy & Roadmap", "AI Product Development", "User Analytics (Mixpanel)", "SQL & Data Querying", "Stakeholder Management")
                    summary = "Product leadership profile. Top-tier non-coding role in high demand. Transitioning into AI/ML-driven product management will increase your salary potential by 30%."
                }
            }

            _resumeAnalysis.value = ResumeAnalysisResult(
                role = suitableRole,
                salaryEstimate = salaryEstimate,
                skillsToLearn = skills,
                summary = summary
            )
        }
    }
}
