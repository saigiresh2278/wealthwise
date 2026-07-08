export interface FinancialHealthResult {
  score: number
  savingsRate: number
  expenseRatio: number
  hasEmergencyFund: boolean
  insights: string[]
}

export function calculateFinancialHealth(
  monthlyIncome: number,
  monthlyExpenses: number,
  monthlySavings: number,
  totalSavings: number,
  monthlyExpensesAvg: number
): FinancialHealthResult {
  const savingsRate = monthlyIncome > 0 ? (monthlySavings / monthlyIncome) * 100 : 0
  const expenseRatio = monthlyIncome > 0 ? (monthlyExpenses / monthlyIncome) * 100 : 0
  const hasEmergencyFund = totalSavings >= monthlyExpensesAvg * 3

  let score = 0

  if (savingsRate >= 30) score += 30
  else if (savingsRate >= 20) score += 22
  else if (savingsRate >= 10) score += 15
  else score += 5

  if (expenseRatio <= 40) score += 25
  else if (expenseRatio <= 60) score += 18
  else if (expenseRatio <= 80) score += 10
  else score += 3

  if (hasEmergencyFund) score += 20
  else score += 5

  if (monthlyIncome > 0 && monthlyExpenses > 0) score += 10

  const idealSaving = monthlyIncome * 0.3
  if (monthlySavings >= idealSaving) score += 15
  else if (monthlySavings >= idealSaving * 0.5) score += 8
  else score += 2

  score = Math.min(100, Math.max(0, score))

  const insights: string[] = []
  if (savingsRate < 10) insights.push("Your savings rate is critically low. Aim to save at least 10-20% of income.")
  else if (savingsRate < 20) insights.push("Consider increasing savings by 5-10% for a stronger financial future.")
  else insights.push("Great savings discipline! Your savings rate is healthy.")

  if (expenseRatio > 70) insights.push("High expense ratio detected. Review discretionary spending.")
  else if (expenseRatio > 50) insights.push("Moderate expense ratio. Look for areas to optimize.")

  if (!hasEmergencyFund) insights.push("Build an emergency fund covering 3-6 months of expenses.")
  else insights.push("Emergency fund looks adequate. Well done!")

  return { score, savingsRate, expenseRatio, hasEmergencyFund, insights }
}
