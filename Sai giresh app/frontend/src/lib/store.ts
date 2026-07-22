import { User, UserProfile, Transaction, Goal, RiskProfile } from "./types"

const KEYS = {
  users: "ww_users",
  currentUser: "ww_current_user",
  profile: "ww_profile_",
  transactions: "ww_transactions_",
  goals: "ww_goals_",
  riskProfile: "ww_risk_",
  darkMode: "ww_dark_mode",
}

function getItem<T>(key: string): T | null {
  if (typeof window === "undefined") return null
  try {
    const item = localStorage.getItem(key)
    return item ? JSON.parse(item) : null
  } catch {
    return null
  }
}

function setItem<T>(key: string, value: T): void {
  if (typeof window === "undefined") return
  localStorage.setItem(key, JSON.stringify(value))
}

function encodeEmail(email: string): string {
  return email.trim().toLowerCase().replace(/\./g, ",")
}

// Users
export function getUsers(): User[] {
  return getItem<User[]>(KEYS.users) || []
}

export function saveUsers(users: User[]): void {
  setItem(KEYS.users, users)
  // Sync each user to Realtime Database auth_users
  users.forEach(u => {
    syncAuthUserToFirebase(u)
  })
}

export function getCurrentUser(): string | null {
  return getItem<string>(KEYS.currentUser)
}

export function setCurrentUser(email: string | null): void {
  if (email) setItem(KEYS.currentUser, email)
  else localStorage.removeItem(KEYS.currentUser)
}

export function hashPassword(password: string): string {
  let hash = 0
  for (let i = 0; i < password.length; i++) {
    const char = password.charCodeAt(i)
    hash = ((hash << 5) - hash) + char
    hash = hash & hash
  }
  return "hash_" + Math.abs(hash).toString(16)
}

// Profile
export function getProfile(email: string): UserProfile | null {
  return getItem<UserProfile>(KEYS.profile + email)
}

export function saveProfile(profile: UserProfile): void {
  setItem(KEYS.profile + profile.email, profile)
  syncUserProfileToFirebase(profile)
}

// Transactions
export function getTransactions(email: string): Transaction[] {
  return getItem<Transaction[]>(KEYS.transactions + email) || []
}

export function saveTransactions(email: string, transactions: Transaction[]): void {
  setItem(KEYS.transactions + email, transactions)
  syncTransactionsToFirebase(email, transactions)
}

// Goals
export function getGoals(email: string): Goal[] {
  return getItem<Goal[]>(KEYS.goals + email) || []
}

export function saveGoals(email: string, goals: Goal[]): void {
  setItem(KEYS.goals + email, goals)
  syncGoalsToFirebase(email, goals)
}

// Risk Profile
export function getRiskProfile(email: string): RiskProfile | null {
  return getItem<RiskProfile>(KEYS.riskProfile + email)
}

export function saveRiskProfile(profile: RiskProfile): void {
  setItem(KEYS.riskProfile + profile.email, profile)
  syncRiskProfileToFirebase(profile)
}

// Dark Mode
export function getDarkMode(): boolean {
  return getItem<boolean>(KEYS.darkMode) ?? true
}

export function saveDarkMode(dark: boolean): void {
  setItem(KEYS.darkMode, dark)
}

// Utility
export function generateId(): string {
  return Date.now().toString(36) + Math.random().toString(36).slice(2, 8)
}

export function clearAll(): void {
  const email = getCurrentUser()
  if (email) {
    localStorage.removeItem(KEYS.profile + email)
    localStorage.removeItem(KEYS.transactions + email)
    localStorage.removeItem(KEYS.goals + email)
    localStorage.removeItem(KEYS.riskProfile + email)
    
    // Clear user data on Realtime Database if requested
    clearUserDataFromFirebase(email)
  }
  localStorage.removeItem(KEYS.currentUser)
}

/* ==========================================================================
   BACKEND SERVER SYNC HELPER FUNCTIONS
   ========================================================================== */

const BACKEND_URL = process.env.NEXT_PUBLIC_API_URL || "http://localhost:5000/api"

export async function syncAuthUserToFirebase(user: User) {
  try {
    await fetch(`${BACKEND_URL}/auth/register`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        email: user.email,
        fullName: user.fullName,
        passwordHash: user.passwordHash
      })
    })
  } catch (e) {
    console.error("Error syncing auth user to Backend: ", e)
  }
}

export async function syncUserProfileToFirebase(profile: UserProfile) {
  try {
    await fetch(`${BACKEND_URL}/auth/profile/${encodeURIComponent(profile.email)}`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(profile)
    })
  } catch (e) {
    console.error("Error syncing profile to Backend: ", e)
  }
}

export async function syncTransactionsToFirebase(email: string, transactions: Transaction[]) {
  try {
    await fetch(`${BACKEND_URL}/transactions/${encodeURIComponent(email)}`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(transactions)
    })
  } catch (e) {
    console.error("Error syncing transactions to Backend: ", e)
  }
}

export async function syncGoalsToFirebase(email: string, goals: Goal[]) {
  try {
    await fetch(`${BACKEND_URL}/goals/${encodeURIComponent(email)}`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(goals)
    })
  } catch (e) {
    console.error("Error syncing goals to Backend: ", e)
  }
}

export async function syncRiskProfileToFirebase(profile: RiskProfile) {
  try {
    await fetch(`${BACKEND_URL}/auth/risk-profile/${encodeURIComponent(profile.email)}`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(profile)
    })
  } catch (e) {
    console.error("Error syncing risk profile to Backend: ", e)
  }
}

export async function clearUserDataFromFirebase(email: string) {
  try {
    const encoded = encodeURIComponent(email)
    await fetch(`${BACKEND_URL}/auth/profile/${encoded}`, { method: "DELETE" })
    await fetch(`${BACKEND_URL}/transactions/${encoded}`, { method: "DELETE" })
    await fetch(`${BACKEND_URL}/goals/${encoded}`, { method: "DELETE" })
  } catch (e) {
    console.error("Error clearing user data from Backend: ", e)
  }
}

export async function pullDataFromFirestore(email: string): Promise<boolean> {
  try {
    const encoded = encodeURIComponent(email)
    
    // 1. Profile
    const profileRes = await fetch(`${BACKEND_URL}/auth/profile/${encoded}`)
    if (profileRes.ok) {
      const p = await profileRes.json()
      setItem(KEYS.profile + email, {
        email: p.email || email,
        fullName: p.fullName || "",
        age: Number(p.age) || 25,
        occupation: p.occupation || "",
        monthlyIncome: Number(p.monthlyIncome) || 0,
        monthlyExpenses: Number(p.monthlyExpenses) || 0,
        monthlySavings: Number(p.monthlySavings) || 0,
        mainFinancialGoal: p.mainFinancialGoal || "",
        riskComfort: p.riskComfort || "Medium",
        investmentExperience: p.investmentExperience || "Beginner"
      })
    }

    // 2. Transactions
    const txRes = await fetch(`${BACKEND_URL}/transactions/${encoded}`)
    if (txRes.ok) {
      const txsData = await txRes.json()
      const txs: Transaction[] = txsData.map((data: any) => {
        let dateStr = ""
        if (data.date) {
          if (typeof data.date === "number") {
            dateStr = new Date(data.date).toISOString().split("T")[0]
          } else if (typeof data.date === "string") {
            if (/^\d+$/.test(data.date)) {
              dateStr = new Date(Number(data.date)).toISOString().split("T")[0]
            } else {
              const parsed = new Date(data.date)
              dateStr = !isNaN(parsed.getTime()) ? parsed.toISOString().split("T")[0] : data.date
            }
          } else {
            dateStr = new Date().toISOString().split("T")[0]
          }
        } else {
          dateStr = new Date().toISOString().split("T")[0]
        }
        return {
          id: data.id ? data.id.toString() : "",
          userEmail: data.userEmail || email,
          amount: Number(data.amount) || 0,
          category: data.category || "Others",
          note: data.note || "",
          date: dateStr,
          type: data.type === "Income" ? "Income" : "Expense"
        }
      })
      setItem(KEYS.transactions + email, txs)
    }

    // 3. Goals
    const goalRes = await fetch(`${BACKEND_URL}/goals/${encoded}`)
    if (goalRes.ok) {
      const goalsData = await goalRes.json()
      const goals: Goal[] = goalsData.map((data: any) => {
        let dateStr = ""
        if (data.targetDate) {
          if (typeof data.targetDate === "number") {
            dateStr = new Date(data.targetDate).toISOString().split("T")[0]
          } else if (typeof data.targetDate === "string") {
            if (/^\d+$/.test(data.targetDate)) {
              dateStr = new Date(Number(data.targetDate)).toISOString().split("T")[0]
            } else {
              const parsed = new Date(data.targetDate)
              dateStr = !isNaN(parsed.getTime()) ? parsed.toISOString().split("T")[0] : data.targetDate
            }
          } else {
            dateStr = new Date().toISOString().split("T")[0]
          }
        } else {
          dateStr = new Date().toISOString().split("T")[0]
        }
        return {
          id: data.id ? data.id.toString() : "",
          userEmail: data.userEmail || email,
          goalName: data.goalName || "",
          targetAmount: Number(data.targetAmount) || 0,
          currentSavedAmount: Number(data.currentSavedAmount) || 0,
          targetDate: dateStr,
          priority: data.priority === "High" ? "High" : data.priority === "Low" ? "Low" : "Medium"
        }
      })
      setItem(KEYS.goals + email, goals)
    }

    // 4. Risk Profile
    const riskRes = await fetch(`${BACKEND_URL}/auth/risk-profile/${encoded}`)
    if (riskRes.ok) {
      const r = await riskRes.json()
      let dateStr = ""
      if (r.lastAssessmentDate) {
        if (typeof r.lastAssessmentDate === "number") {
          dateStr = new Date(r.lastAssessmentDate).toLocaleDateString()
        } else if (typeof r.lastAssessmentDate === "string") {
          if (/^\d+$/.test(r.lastAssessmentDate)) {
            dateStr = new Date(Number(r.lastAssessmentDate)).toLocaleDateString()
          } else {
            const parsed = new Date(r.lastAssessmentDate)
            dateStr = !isNaN(parsed.getTime()) ? parsed.toLocaleDateString() : r.lastAssessmentDate
          }
        } else {
          dateStr = new Date().toLocaleDateString()
        }
      } else {
        dateStr = new Date().toLocaleDateString()
      }
      setItem(KEYS.riskProfile + email, {
        email: r.email || email,
        score: Number(r.score) || 50,
        riskClass: r.riskClass || "Medium",
        lastAssessmentDate: dateStr
      })
    }
    return true
  } catch (e) {
    console.error("Error pulling data from Backend: ", e)
    return false
  }
}
