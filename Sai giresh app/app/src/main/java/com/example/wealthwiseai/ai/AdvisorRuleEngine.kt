package com.example.wealthwiseai.ai

import com.example.wealthwiseai.data.local.entity.UserProfileEntity
import com.example.wealthwiseai.data.local.entity.TransactionEntity
import com.example.wealthwiseai.data.local.entity.GoalEntity
import java.util.Calendar

object AdvisorRuleEngine {

    data class AdvisoryResult(
        val healthScore: Int,
        val savingsRate: Double,
        val suggestions: List<String>,
        val learningRecommendations: List<String>,
        val alerts: List<String>,
        val salary: Double = 0.0,
        val monthlyExpenses: Double = 0.0,
        val savings: Double = 0.0,
        val investmentRecommendation: String = "",
        val savingsIncreaseIdeas: List<String> = emptyList(),
        val incomeIncreaseIdeas: List<String> = emptyList()
    )

    fun generateAdvice(
        profile: UserProfileEntity?,
        transactions: List<TransactionEntity>,
        goals: List<GoalEntity>
    ): AdvisoryResult {
        if (profile == null) {
            return AdvisoryResult(
                healthScore = 0,
                savingsRate = 0.0,
                suggestions = listOf("Please complete your onboarding profile setup to receive personalized AI advice."),
                learningRecommendations = listOf("Budgeting basics", "Savings habit"),
                alerts = emptyList()
            )
        }

        val suggestions = mutableListOf<String>()
        val learningRecs = mutableListOf<String>()
        val alerts = mutableListOf<String>()

        val salary = profile.monthlyIncome

        // 1. Calculate dynamic monthly expenses from transactions in the current calendar month
        val calendar = Calendar.getInstance()
        val currentMonth = calendar.get(Calendar.MONTH)
        val currentYear = calendar.get(Calendar.YEAR)
        val txCalendar = Calendar.getInstance()

        val currentMonthExpenses = transactions.filter { tx ->
            if (tx.type.lowercase() == "expense") {
                txCalendar.timeInMillis = tx.date
                txCalendar.get(Calendar.MONTH) == currentMonth && txCalendar.get(Calendar.YEAR) == currentYear
            } else {
                false
            }
        }
        val monthlyExpenses = currentMonthExpenses.sumOf { it.amount }
        val savings = (salary - monthlyExpenses).coerceAtLeast(0.0)

        val savingsRate = FinancialHealthCalculator.calculateSavingsRate(salary, monthlyExpenses)
        val expenseRatio = FinancialHealthCalculator.calculateExpenseToIncomeRatio(salary, monthlyExpenses)

        // Health Score
        val healthScore = FinancialHealthCalculator.calculateHealthScore(salary, monthlyExpenses, savings)

        // AI Recommendations & Ideas
        var investmentRec = ""
        val savingsIncreaseIdeas = mutableListOf<String>()
        val incomeIncreaseIdeas = mutableListOf<String>()

        if (savings > 0.0) {
            // Investment recommendation based on risk comfort
            investmentRec = when (profile.riskComfort.lowercase()) {
                "low" -> {
                    "Since you prefer low-risk investments, we suggest securing your savings in:\n" +
                    "1. High-Yield Savings Accounts (yielding 4-5% APY with zero risk and maximum liquidity).\n" +
                    "2. Government or Treasury Bonds (guaranteed capital preservation with stable yields).\n" +
                    "3. Fixed Deposits (CDs) (great for locking in interest rates over 6-12 months)."
                }
                "medium" -> {
                    "For medium-risk tolerance, we suggest a balanced growth portfolio:\n" +
                    "1. Index Mutual Funds (e.g., S&P 500, historically yielding 10-12% over the long term).\n" +
                    "2. Balanced Hybrid Mutual Funds (allocating ~60% to stocks and ~40% to stable bonds).\n" +
                    "3. Setting up a Systematic Investment Plan (SIP) to automate monthly equity/debt fund investments."
                }
                "high" -> {
                    "With high-risk tolerance, you can capture maximum compounding growth in:\n" +
                    "1. Aggressive Growth Stocks (focusing on technology, AI, or renewable energy).\n" +
                    "2. Small-cap or Mid-cap Equity Mutual Funds (higher volatility but potentially outsized long-term returns).\n" +
                    "3. Diversified Global ETFs (broader market exposure to international tech leaders)."
                }
                else -> {
                    "Please retake the risk profile quiz in Settings to get tailored investment advice. General suggestion: start with a diversified low-cost Index Fund."
                }
            }

            // Savings rate analysis
            if (savingsRate < 20.0) {
                suggestions.add("Your remaining savings rate is ${"%.1f".format(savingsRate)}%, which is below the recommended 20% threshold. Consider scaling back on discretionary spending.")
                alerts.add("Low savings warning: your savings rate is currently under 20% of your salary.")
            } else {
                suggestions.add("Outstanding job! Your dynamic savings rate is ${"%.1f".format(savingsRate)}%. You are in a strong position to build wealth.")
            }
        } else {
            investmentRec = "You currently have no savings remaining from your salary this month. Prioritize minimizing discretionary expenses and setting up a basic emergency fund before investing."
            alerts.add("Deficit warning: Monthly expenses have exceeded or consumed your entire salary.")
        }

        // Ideas to increase savings (budgeting/cost-cutting)
        savingsIncreaseIdeas.addAll(listOf(
            "Adopt the 50/30/20 budget framework (Needs/Wants/Savings) and try automating 20% of your salary first thing on payday.",
            "Track daily micro-transactions (e.g., premium coffees, subscription service bundles) which could save up to ₹8000+ monthly.",
            "Review recurring subscriptions and gym memberships; cancel any services unused over the last 30 days.",
            "Plan meals and cook at home; dining out or ordering delivery is typically 3x more expensive than home-cooked food.",
            "Use the 24-hour rule: pause for a day before purchasing non-essential items to eliminate impulse purchases."
        ))

        // Ideas to increase income/salary
        val occupationWord = if (profile.occupation.isNotBlank()) profile.occupation else "your domain"
        incomeIncreaseIdeas.addAll(listOf(
            "Upskill for career leverage: Allocate 3 hours a week to learn advanced certifications or high-income skills relevant to $occupationWord.",
            "Freelance or consult: Offer freelance services or part-time consulting on digital platforms like Upwork or Fiverr.",
            "Monetize assets or unused goods: Rent out spare space, or sell items you no longer use (old electronics, books, clothes).",
            "Prepare a portfolio of your recent accomplishments and milestones, and negotiate a merit-based raise with your manager.",
            "Build passive streams: Develop online courses, write guides, or create digital assets that earn royalties over time."
        ))

        // Rule-based generic checks
        if (expenseRatio > 70.0) {
            suggestions.add("Expenses consume ${"%.1f".format(expenseRatio)}% of your salary. We recommend cutting back on discretionary spending.")
        }

        // Emergency buffer assessment
        val estimatedEmergencyBuffer = savings * 3
        if (estimatedEmergencyBuffer < (monthlyExpenses * 6.0)) {
            suggestions.add("Your liquid emergency reserve is below the recommended 6-month buffer. Prioritize saving towards an Emergency Shield.")
            learningRecs.add("Emergency fund")
        }

        // Category-based leaks check
        val totalRecordedExpenses = currentMonthExpenses.sumOf { it.amount }
        if (totalRecordedExpenses > 0.0) {
            val categoryTotals = currentMonthExpenses.groupBy { it.category }
                .mapValues { entry -> entry.value.sumOf { it.amount } }

            categoryTotals.forEach { (category, amount) ->
                val categoryRatio = (amount / totalRecordedExpenses) * 100.0
                if (categoryRatio > 25.0 && category.lowercase() != "rent" && category.lowercase() != "others") {
                    alerts.add("Budget Leak Alert: Spending on $category constitutes ${"%.1f".format(categoryRatio)}% of this month's expenses. Consider scaling back.")
                    savingsIncreaseIdeas.add(0, "Scale back on $category: it constitutes ${"%.1f".format(categoryRatio)}% of your expenses this month. Target a 15% reduction here.")
                }
            }
        }

        // Learning path suggestions
        when (profile.riskComfort.lowercase()) {
            "low" -> {
                learningRecs.addAll(listOf("Budgeting basics", "Savings habit", "Fixed deposit", "Risk management"))
            }
            "medium", "high" -> {
                learningRecs.addAll(listOf("Mutual funds", "SIP", "Stocks basics", "Diversification", "Risk management"))
            }
        }

        // Goal-based suggestion
        if (goals.isNotEmpty()) {
            val pendingGoals = goals.filter { it.currentSavedAmount < it.targetAmount }
            if (pendingGoals.isNotEmpty()) {
                val totalRequiredMonthly = pendingGoals.sumOf { goal ->
                    val remaining = goal.targetAmount - goal.currentSavedAmount
                    val timeLeftMonths = ((goal.targetDate - System.currentTimeMillis()) / (30L * 24 * 60 * 60 * 1000)).coerceAtLeast(1)
                    remaining / timeLeftMonths
                }
                if (totalRequiredMonthly > savings) {
                    suggestions.add("To achieve active goals, you need ₹${"%.2f".format(totalRequiredMonthly)}/month, which exceeds your dynamic monthly savings (₹${"%.2f".format(savings)}). Extend target dates or prioritize.")
                } else {
                    suggestions.add("Your dynamic savings of ₹${"%.2f".format(savings)} cover your active goals (₹${"%.2f".format(totalRequiredMonthly)}/month required). Keep up the great pace!")
                }
            }
        }

        return AdvisoryResult(
            healthScore = healthScore,
            savingsRate = savingsRate,
            suggestions = suggestions,
            learningRecommendations = learningRecs.distinct(),
            alerts = alerts,
            salary = salary,
            monthlyExpenses = monthlyExpenses,
            savings = savings,
            investmentRecommendation = investmentRec,
            savingsIncreaseIdeas = savingsIncreaseIdeas,
            incomeIncreaseIdeas = incomeIncreaseIdeas
        )
    }
}
