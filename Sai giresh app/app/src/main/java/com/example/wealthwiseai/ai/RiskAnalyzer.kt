package com.example.wealthwiseai.ai

object RiskAnalyzer {

    data class RiskQuestion(
        val question: String,
        val options: List<String>,
        val points: List<Int> // points corresponding to each option
    )

    val questions = listOf(
        RiskQuestion(
            question = "How stable is your monthly income?",
            options = listOf("Highly Unstable / Variable", "Relatively Stable", "Highly Stable / Guaranteed"),
            points = listOf(1, 2, 3)
        ),
        RiskQuestion(
            question = "How comfortable are you with investment risk?",
            options = listOf(
                "Very uncomfortable: I prefer keeping money safe even if returns are low.",
                "Somewhat comfortable: I can accept mild fluctuations for better returns.",
                "Very comfortable: I can accept temporary losses for high growth potential."
            ),
            points = listOf(1, 2, 3)
        ),
        RiskQuestion(
            question = "How long can you keep money invested?",
            options = listOf("Short term (less than 1 year)", "Medium term (1 to 5 years)", "Long term (more than 5 years)"),
            points = listOf(1, 2, 3)
        ),
        RiskQuestion(
            question = "Do you have emergency savings?",
            options = listOf("None at all", "Some (1 to 3 months of expenses)", "Yes (6+ months of expenses)"),
            points = listOf(1, 2, 3)
        ),
        RiskQuestion(
            question = "What is your investment knowledge level?",
            options = listOf("Beginner: Little to no experience", "Intermediate: Know the basics of mutual funds", "Advanced: Understand stocks and asset allocation"),
            points = listOf(1, 2, 3)
        )
    )

    data class AnalysisResult(
        val score: Int,
        val riskClass: String,
        val explanation: String,
        val learningPath: List<String>
    )

    fun analyzeRisk(answers: List<Int>): AnalysisResult {
        // answers contains the indices selected for each question (0, 1, or 2)
        var totalScore = 0
        for (i in answers.indices) {
            val questionIndex = answers[i]
            val q = questions.getOrNull(i)
            if (q != null && questionIndex in q.options.indices) {
                totalScore += q.points[questionIndex]
            } else {
                totalScore += 1 // fallback
            }
        }

        return when {
            totalScore <= 8 -> AnalysisResult(
                score = totalScore,
                riskClass = "Low Risk",
                explanation = "You prefer capital preservation and security. Stable return profiles and liquid instruments fit your goals best. Avoid volatile assets that cause anxiety.",
                learningPath = listOf("Budgeting basics", "Emergency fund", "Savings habit", "Fixed deposit")
            )
            totalScore <= 12 -> AnalysisResult(
                score = totalScore,
                riskClass = "Medium Risk",
                explanation = "You seek a balanced approach, accepting moderate volatility in exchange for potential growth. A diversified portfolio combining stability with market growth is suitable.",
                learningPath = listOf("Budgeting basics", "Emergency fund", "Mutual funds", "SIP", "Diversification", "Risk management")
            )
            else -> AnalysisResult(
                score = totalScore,
                riskClass = "High Risk",
                explanation = "You are focused on long-term capital appreciation and possess a strong capacity to withstand market fluctuations. You can explore volatile assets to maximize compounding benefits over a long duration.",
                learningPath = listOf("Mutual funds", "SIP", "Stocks basics", "Diversification", "Risk management", "Financial discipline")
            )
        }
    }
}
