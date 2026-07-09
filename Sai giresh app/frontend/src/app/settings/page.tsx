"use client"

import { useRouter } from "next/navigation"
import { useAuth } from "@/context/AuthContext"
import { clearAll } from "@/lib/store"
import { Moon, Sun, User, Shield, AlertTriangle, Trash2, LogOut, ArrowRight, Database } from "lucide-react"
import Link from "next/link"

export default function SettingsPage() {
  const router = useRouter()
  const { darkMode, toggleDarkMode, logout, user } = useAuth()

  const handleReset = () => {
    if (confirm("Are you sure? This will delete all your financial data permanently.")) {
      if (confirm("This cannot be undone. Continue?")) {
        clearAll()
        logout()
        router.replace("/login")
      }
    }
  }

  const handleLogout = () => {
    logout()
    router.replace("/login")
  }

  return (
    <div className="max-w-3xl mx-auto space-y-6">
      <h1 className="text-2xl font-bold text-[var(--text-primary)]">Settings</h1>
 
      <div className="space-y-3">
        {/* Appearance */}
        <div className="glass-card p-5">
          <h2 className="text-[var(--text-primary)] font-semibold mb-4">Appearance</h2>
          <button onClick={toggleDarkMode}
            className="w-full flex items-center justify-between p-3 rounded-xl bg-white/5 hover:bg-white/10 transition-colors">
            <div className="flex items-center gap-3">
              {darkMode ? <Sun size={18} className="text-yellow-400" /> : <Moon size={18} className="text-[#4f8cff]" />}
              <span className="text-[var(--text-primary)] text-sm">{darkMode ? "Light Mode" : "Dark Mode"}</span>
            </div>
            <div className={`w-10 h-6 rounded-full transition-colors ${darkMode ? "bg-[#4f8cff]" : "bg-gray-600"} relative`}>
              <div className={`w-4 h-4 rounded-full bg-white absolute top-1 transition-all ${darkMode ? "left-5" : "left-1"}`} />
            </div>
          </button>
        </div>
 
        {/* Account */}
        <div className="glass-card p-5">
          <h2 className="text-[var(--text-primary)] font-semibold mb-4">Account</h2>
          <div className="space-y-2">
            <Link href="/profile"
              className="flex items-center justify-between p-3 rounded-xl bg-white/5 hover:bg-white/10 transition-colors">
              <div className="flex items-center gap-3">
                <User size={18} className="text-[#4f8cff]" />
                <span className="text-[var(--text-primary)] text-sm">Edit Profile</span>
              </div>
              <ArrowRight size={16} className="text-gray-500" />
            </Link>
            <Link href="/risk-analyzer"
              className="flex items-center justify-between p-3 rounded-xl bg-white/5 hover:bg-white/10 transition-colors">
              <div className="flex items-center gap-3">
                <Shield size={18} className="text-purple-400" />
                <span className="text-[var(--text-primary)] text-sm">Retake Risk Assessment</span>
              </div>
              <ArrowRight size={16} className="text-gray-500" />
            </Link>
          </div>
        </div>
 
        {/* Data */}
        <div className="glass-card p-5">
          <h2 className="text-[var(--text-primary)] font-semibold mb-4">Data Management</h2>
          <div className="space-y-2">
            <Link href="/database"
              className="w-full flex items-center justify-between p-3 rounded-xl bg-white/5 hover:bg-white/10 transition-colors">
              <div className="flex items-center gap-3">
                <Database size={18} className="text-[#4f8cff]" />
                <div className="text-left">
                  <span className="text-[var(--text-primary)] text-sm">Database Explorer</span>
                  <p className="text-xs text-gray-500">View, search, edit, and export database tables</p>
                </div>
              </div>
              <ArrowRight size={16} className="text-gray-500" />
            </Link>
            <button onClick={handleReset}
              className="w-full flex items-center justify-between p-3 rounded-xl bg-red-500/5 hover:bg-red-500/10 transition-colors">
              <div className="flex items-center gap-3">
                <Trash2 size={18} className="text-red-400" />
                <div className="text-left">
                  <span className="text-[var(--text-primary)] text-sm">Reset All Data</span>
                  <p className="text-xs text-gray-500">Delete all transactions, goals, and profile data</p>
                </div>
              </div>
              <AlertTriangle size={16} className="text-red-400" />
            </button>
          </div>
        </div>
 
        {/* Logout */}
        <button onClick={handleLogout}
          className="w-full glass-card p-4 flex items-center justify-center gap-2 text-red-400 hover:bg-red-500/5 transition-colors">
          <LogOut size={18} /> Sign Out
        </button>
 
        <p className="text-center text-xs text-gray-600">
          WealthWise AI v1.0 &mdash; All data is stored locally on your device
        </p>
      </div>
    </div>
  )
}
