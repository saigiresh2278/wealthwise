"use client"

import { createContext, useContext, useState, useEffect, ReactNode } from "react"
import { User, UserProfile } from "@/lib/types"
import {
  getUsers, saveUsers, getCurrentUser, setCurrentUser,
  hashPassword, getProfile, saveProfile, getDarkMode, saveDarkMode
} from "@/lib/store"

interface AuthContextType {
  user: User | null
  profile: UserProfile | null
  isLoggedIn: boolean
  darkMode: boolean
  login: (email: string, password: string) => boolean
  signup: (name: string, email: string, password: string) => boolean
  loginWithGoogle: () => Promise<boolean>
  logout: () => void
  updateProfile: (p: UserProfile) => void
  refreshProfile: () => void
  toggleDarkMode: () => void
}

const AuthContext = createContext<AuthContextType>({} as AuthContextType)

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(null)
  const [profile, setProfile] = useState<UserProfile | null>(null)
  const [darkMode, setDarkModeState] = useState(true)

  useEffect(() => {
    setDarkModeState(getDarkMode())
    
    const syncWithFirebase = async () => {
      // 1. Sync all registered users list from auth_users in Realtime Database
      try {
        const { db } = await import("@/lib/firebase")
        const { ref, get } = await import("firebase/database")
        if (db) {
          const snapshot = await get(ref(db, "auth_users"))
          const fbUsers: any[] = []
          if (snapshot.exists()) {
            const val = snapshot.val()
            Object.keys(val).forEach(key => {
              if (val[key]) {
                fbUsers.push({
                  email: val[key].email || key.replace(/,/g, "."),
                  fullName: val[key].fullName || "",
                  passwordHash: val[key].passwordHash || ""
                })
              }
            })
          }
          if (fbUsers.length > 0) {
            saveUsers(fbUsers)
          }
        }
      } catch (e) {
        console.error("Failed to sync registered users from Realtime Database: ", e)
      }

      const email = getCurrentUser()
      if (email) {
        const { pullDataFromFirestore } = await import("@/lib/store")
        // 2. Fetch latest data from Realtime Database for active user
        await pullDataFromFirestore(email)
        
        const users = getUsers()
        const found = users.find(u => u.email === email)
        if (found) {
          setUser(found)
          setProfile(getProfile(email))
        }
      }
    }

    syncWithFirebase()
  }, [])

  useEffect(() => {
    document.documentElement.classList.toggle("dark", darkMode)
  }, [darkMode])

  const login = (email: string, password: string): boolean => {
    const users = getUsers()
    const found = users.find(u => u.email === email && u.passwordHash === hashPassword(password))
    if (found) {
      setUser(found)
      setCurrentUser(email)
      setProfile(getProfile(email))
      // Pull Firestore data in background and refresh profile
      import("@/lib/store").then(({ pullDataFromFirestore }) => {
        pullDataFromFirestore(email).then(() => {
          setProfile(getProfile(email))
          refreshProfile()
        })
      })
      return true
    }
    return false
  }

  const signup = (name: string, email: string, password: string): boolean => {
    const users = getUsers()
    if (users.find(u => u.email === email)) return false
    const newUser: User = { email, fullName: name, passwordHash: hashPassword(password) }
    saveUsers([...users, newUser])
    setUser(newUser)
    setCurrentUser(email)
    return true
  }

  const loginWithGoogle = async (): Promise<boolean> => {
    try {
      const { auth } = await import("@/lib/firebase")
      const { GoogleAuthProvider, signInWithPopup } = await import("firebase/auth")
      if (!auth) return false
      
      const provider = new GoogleAuthProvider()
      const result = await signInWithPopup(auth, provider)
      const fbUser = result.user
      
      if (!fbUser.email) return false
      
      const email = fbUser.email
      const fullName = fbUser.displayName || "Google User"
      
      const allUsers = getUsers()
      let existingUser = allUsers.find(u => u.email === email)
      
      if (!existingUser) {
        existingUser = { email, fullName, passwordHash: "google_oauth" }
        saveUsers([...allUsers, existingUser])
      }
      
      setUser(existingUser)
      setCurrentUser(email)
      
      // Pull existing data from Realtime Database
      const { pullDataFromFirestore, saveProfile, getProfile } = await import("@/lib/store")
      await pullDataFromFirestore(email)
      
      // If profile still doesn't exist, create a default one
      let userProfile = getProfile(email)
      if (!userProfile) {
        userProfile = {
          email,
          fullName,
          age: 25,
          occupation: "Student",
          monthlyIncome: 0,
          monthlyExpenses: 0,
          monthlySavings: 0,
          mainFinancialGoal: "Savings",
          riskComfort: "Medium",
          investmentExperience: "Beginner"
        }
        saveProfile(userProfile)
      }
      
      setProfile(userProfile)
      refreshProfile()
      return true
    } catch (e) {
      console.error("Google Sign-In failed: ", e)
      return false
    }
  }

  const logout = () => {
    setUser(null)
    setProfile(null)
    setCurrentUser(null)
  }

  const updateProfile = (p: UserProfile) => {
    saveProfile(p)
    setProfile(p)
  }

  const refreshProfile = () => {
    const email = getCurrentUser()
    if (email) setProfile(getProfile(email))
  }

  const toggleDarkMode = () => {
    const next = !darkMode
    setDarkModeState(next)
    saveDarkMode(next)
  }

  return (
    <AuthContext.Provider value={{
      user, profile, isLoggedIn: !!user, darkMode,
      login, signup, loginWithGoogle, logout, updateProfile, refreshProfile, toggleDarkMode
    }}>
      {children}
    </AuthContext.Provider>
  )
}

export const useAuth = () => useContext(AuthContext)
