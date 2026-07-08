export interface GoalAnalysis {
  monthlyRequired: number
  achievable: boolean
  monthsRemaining: number
  savingsCapacity: number
  suggestions: string[]
}

export function analyzeGoal(
  targetAmount: number,
  currentSaved: number,
  targetDate: string,
  monthlyIncome: number,
  monthlyExpenses: number
): GoalAnalysis {
  const now = new Date()
  const target = new Date(targetDate)
  const monthsRemaining = Math.max(1, (target.getFullYear() - now.getFullYear()) * 12 + (target.getMonth() - now.getMonth()))
  const remaining = Math.max(0, targetAmount - currentSaved)
  const monthlyRequired = Math.ceil(remaining / monthsRemaining)
  const savingsCapacity = Math.max(0, monthlyIncome - monthlyExpenses)

  const achievable = monthlyRequired <= savingsCapacity

  const suggestions: string[] = []
  if (!achievable) {
    suggestions.push("Consider extending the target date to reduce monthly savings requirement.")
    suggestions.push("Look for ways to increase income or reduce expenses.")
    if (savingsCapacity > 0) {
      const extendedMonths = Math.ceil(remaining / savingsCapacity)
      const extendedDate = new Date(now)
      extendedDate.setMonth(extendedDate.getMonth() + extendedMonths)
      suggestions.push(`At current savings capacity, you could reach this goal by ${extendedDate.toLocaleDateString()}.`)
    }
  } else {
    suggestions.push("This goal is achievable with your current savings capacity.")
    suggestions.push("Consider automating monthly transfers to stay on track.")
  }

  return { monthlyRequired, achievable, monthsRemaining, savingsCapacity, suggestions }
}
