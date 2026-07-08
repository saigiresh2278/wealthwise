export interface User {
  email: string
  fullName: string
  passwordHash: string
}

export interface UserProfile {
  email: string
  fullName: string
  age: number
  occupation: string
  monthlyIncome: number
  monthlyExpenses: number
  monthlySavings: number
  mainFinancialGoal: string
  riskComfort: string
  investmentExperience: string
}

export interface Transaction {
  id: string
  userEmail: string
  amount: number
  category: string
  note: string
  date: string
  type: "Income" | "Expense"
}

export interface Goal {
  id: string
  userEmail: string
  goalName: string
  targetAmount: number
  currentSavedAmount: number
  targetDate: string
  priority: "Low" | "Medium" | "High"
}

export interface RiskProfile {
  email: string
  score: number
  riskClass: "Low" | "Medium" | "High"
  lastAssessmentDate: string
}

export interface FinancialHealth {
  score: number
  savingsRate: number
  expenseRatio: number
  hasEmergencyFund: boolean
  insights: string[]
}

export interface AdvisorAdvice {
  overview: string
  alerts: string[]
  recommendations: string[]
  savingsIdeas: string[]
  incomeIdeas: string[]
  learningPath: string[]
}

export type TransactionType = "Income" | "Expense"

export const INCOME_CATEGORIES = [
  "Salary", "Freelance", "Investments", "Business", "Rental", "Other Income"
]

export const EXPENSE_CATEGORIES = [
  "Food & Dining", "Transportation", "Housing", "Utilities", "Entertainment",
  "Healthcare", "Shopping", "Education", "Insurance", "Debt", "Other"
]

export const DEFAULT_CURRENCY = "₹"
