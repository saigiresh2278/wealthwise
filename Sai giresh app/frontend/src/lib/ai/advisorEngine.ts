import { UserProfile, Transaction, Goal, RiskProfile } from "../types"

export function generateAdvice(
  profile: UserProfile | null,
  transactions: Transaction[],
  goals: Goal[],
  riskProfile: RiskProfile | null
) {
  if (!profile) return null

  const recentTxns = transactions.filter(t => {
    const d = new Date(t.date)
    const m = new Date()
    m.setMonth(m.getMonth() - 3)
    return d >= m
  })
  const totalIncome = recentTxns.filter(t => t.type === "Income").reduce((s, t) => s + t.amount, 0)
  const totalExpenses = recentTxns.filter(t => t.type === "Expense").reduce((s, t) => s + t.amount, 0)
  const savingsRate = totalIncome > 0 ? ((totalIncome - totalExpenses) / totalIncome) * 100 : 0

  const alerts: string[] = []
  if (profile.monthlyExpenses > profile.monthlyIncome * 0.7) {
    alerts.push("Your expenses exceed 70% of income. Consider reducing discretionary spending.")
  }
  if (goals.some(g => {
    const t = new Date(g.targetDate)
    const now = new Date()
    const monthsLeft = (t.getFullYear() - now.getFullYear()) * 12 + (t.getMonth() - now.getMonth())
    return monthsLeft <= 3 && g.currentSavedAmount < g.targetAmount
  })) {
    alerts.push("You have goals approaching their deadline. Review progress immediately.")
  }
  if (savingsRate < 10 && totalIncome > 0) {
    alerts.push("Your savings rate is below 10%. Aim for at least 20%.")
  }
  if (!riskProfile) {
    alerts.push("Take the risk assessment quiz for personalized investment advice.")
  }

  const recommendations: string[] = []
  if (riskProfile) {
    if (riskProfile.riskClass === "Low") {
      recommendations.push("Consider fixed deposits, debt funds, and government bonds.")
      recommendations.push("Build a diversified portfolio with 70% debt and 30% equity.")
    } else if (riskProfile.riskClass === "Medium") {
      recommendations.push("Consider balanced mutual funds with 50-50 debt-equity allocation.")
      recommendations.push("Explore index funds and blue-chip stocks for steady growth.")
    } else {
      recommendations.push("Consider aggressive growth funds, small-cap funds, and direct equity.")
      recommendations.push("International diversification can reduce overall portfolio risk.")
    }
  }
  recommendations.push("Maintain an emergency fund covering 3-6 months of expenses.")
  recommendations.push("Review and rebalance your portfolio every 6 months.")

  const savingsIdeas: string[] = [
    "Use the 50/30/20 rule: 50% needs, 30% wants, 20% savings.",
    "Automate transfers to a dedicated savings account on payday.",
    "Cancel unused subscriptions and memberships.",
    "Cook at home more often to reduce food expenses.",
  ]

  const incomeIdeas: string[] = [
    "Explore freelancing or consulting in your area of expertise.",
    "Invest in skill development for career growth.",
    "Consider passive income through dividend-paying stocks.",
    "Start a side hustle aligned with your interests.",
  ]

  const learningPath: string[] = [
    "Basics of Personal Finance",
    "Understanding Mutual Funds & SIPs",
    "Stock Market Fundamentals",
    "Tax Planning & Optimization",
    "Retirement Planning",
    "Estate Planning",
  ]

  return {
    overview: `Based on your profile, you have a savings rate of ${savingsRate.toFixed(1)}% with ${profile.monthlyIncome > 0 ? "a monthly income of ₹" + profile.monthlyIncome.toLocaleString() : "no recorded income"}. ${goals.length > 0 ? "You are tracking " + goals.length + " financial goal(s)." : "You haven't set any financial goals yet."} ${riskProfile ? "Your risk tolerance is " + riskProfile.riskClass + "." : "Complete the risk assessment for personalized recommendations."}`,
    alerts,
    recommendations,
    savingsIdeas,
    incomeIdeas,
    learningPath,
  }
}
