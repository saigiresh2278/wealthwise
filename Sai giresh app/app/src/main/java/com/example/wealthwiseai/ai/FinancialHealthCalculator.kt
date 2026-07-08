package com.example.wealthwiseai.ai

object FinancialHealthCalculator {

    fun calculateSavingsRate(income: Double, expenses: Double): Double {
        if (income <= 0.0) return 0.0
        val savings = income - expenses
        return (savings / income) * 100.0
    }

    fun calculateExpenseToIncomeRatio(income: Double, expenses: Double): Double {
        if (income <= 0.0) return 0.0
        return (expenses / income) * 100.0
    }

    /**
     * Calculates a health score out of 100 based on:
     * - Savings Rate (max 40 points)
     * - Expense-to-Income Ratio (max 30 points)
     * - Emergency Fund Status (max 30 points)
     */
    fun calculateHealthScore(
        income: Double,
        expenses: Double,
        currentSavedAmountForEmergency: Double
    ): Int {
        if (income <= 0.0) return 0

        var score = 0

        // 1. Savings Rate points (Max 40)
        val savingsRate = calculateSavingsRate(income, expenses)
        when {
            savingsRate >= 30.0 -> score += 40
            savingsRate >= 20.0 -> score += 30
            savingsRate >= 10.0 -> score += 20
            savingsRate > 0.0 -> score += 10
            else -> score += 0
        }

        // 2. Expense-to-Income points (Max 30)
        val expenseRatio = calculateExpenseToIncomeRatio(income, expenses)
        when {
            expenseRatio < 50.0 -> score += 30
            expenseRatio <= 70.0 -> score += 20
            expenseRatio <= 90.0 -> score += 10
            else -> score += 0
        }

        // 3. Emergency Fund points (Max 30)
        // Ideal Emergency Fund = 6 months of expenses
        val targetEmergencyFund = expenses * 6
        if (targetEmergencyFund > 0.0) {
            val monthsCovered = currentSavedAmountForEmergency / expenses
            when {
                monthsCovered >= 6.0 -> score += 30
                monthsCovered >= 3.0 -> score += 20
                monthsCovered >= 1.0 -> score += 10
                else -> score += 0
            }
        } else {
            score += 30 // No expenses implies no immediate emergency fund need
        }

        return score.coerceIn(0, 100)
    }

    fun getHealthRating(score: Int): String {
        return when {
            score >= 80 -> "Excellent"
            score >= 60 -> "Good"
            score >= 40 -> "Average"
            else -> "Poor"
        }
    }
}
