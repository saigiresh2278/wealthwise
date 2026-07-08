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

// Users
export function getUsers(): User[] {
  return getItem<User[]>(KEYS.users) || []
}

export function saveUsers(users: User[]): void {
  setItem(KEYS.users, users)
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
}

// Transactions
export function getTransactions(email: string): Transaction[] {
  return getItem<Transaction[]>(KEYS.transactions + email) || []
}

export function saveTransactions(email: string, transactions: Transaction[]): void {
  setItem(KEYS.transactions + email, transactions)
}

// Goals
export function getGoals(email: string): Goal[] {
  return getItem<Goal[]>(KEYS.goals + email) || []
}

export function saveGoals(email: string, goals: Goal[]): void {
  setItem(KEYS.goals + email, goals)
}

// Risk Profile
export function getRiskProfile(email: string): RiskProfile | null {
  return getItem<RiskProfile>(KEYS.riskProfile + email)
}

export function saveRiskProfile(profile: RiskProfile): void {
  setItem(KEYS.riskProfile + profile.email, profile)
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
  }
  localStorage.removeItem(KEYS.currentUser)
}
