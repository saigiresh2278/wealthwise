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
  loading: boolean
  darkMode: boolean
  login: (email: string, password: string) => boolean
  signup: (name: string, email: string, password: string) => boolean
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
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    setDarkModeState(getDarkMode())
    const email = getCurrentUser()
    if (email) {
      const users = getUsers()
      const found = users.find(u => u.email === email)
      if (found) {
        setUser(found)
        setProfile(getProfile(email))
      }
    }
    setLoading(false)
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
      user, profile, isLoggedIn: !!user, loading, darkMode,
      login, signup, logout, updateProfile, refreshProfile, toggleDarkMode
    }}>
      {children}
    </AuthContext.Provider>
  )
}

export const useAuth = () => useContext(AuthContext)
