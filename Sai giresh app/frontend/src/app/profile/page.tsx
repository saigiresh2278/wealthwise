"use client"

import { useEffect, useState } from "react"
import { useRouter } from "next/navigation"
import { useAuth } from "@/context/AuthContext"
import { getTransactions, getGoals, getRiskProfile } from "@/lib/store"
import { User, Mail, Calendar, Briefcase, TrendingUp, TrendingDown, Target, Shield, Share2, Copy, Check } from "lucide-react"
import Link from "next/link"

export default function ProfilePage() {
  const router = useRouter()
  const { user, profile, isLoggedIn } = useAuth()
  const [copied, setCopied] = useState(false)
  const [stats, setStats] = useState({ transactions: 0, goals: 0, riskClass: "Not assessed" })

  useEffect(() => {
    if (!isLoggedIn) { router.replace("/login"); return }
    if (user) {
      setStats({
        transactions: getTransactions(user.email).length,
        goals: getGoals(user.email).length,
        riskClass: getRiskProfile(user.email)?.riskClass || "Not assessed",
      })
    }
  }, [isLoggedIn, user, router])

  const referralLink = typeof window !== "undefined" ? window.location.origin : ""

  const copyReferral = () => {
    navigator.clipboard.writeText(referralLink)
    setCopied(true)
    setTimeout(() => setCopied(false), 2000)
  }

  return (
    <div className="max-w-4xl mx-auto space-y-6">
      <h1 className="text-2xl font-bold text-white">Profile</h1>

      {/* Profile Card */}
      <div className="glass-card p-6">
        <div className="flex items-center gap-4 mb-6">
          <div className="w-16 h-16 rounded-full bg-gradient-to-br from-[#4f8cff] to-[#6366f1] flex items-center justify-center text-2xl font-bold text-white">
            {user?.fullName?.charAt(0) || "U"}
          </div>
          <div>
            <h2 className="text-xl text-white font-semibold">{user?.fullName}</h2>
            <p className="text-gray-400 text-sm">{user?.email}</p>
          </div>
        </div>

        {profile ? (
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div className="flex items-center gap-3 text-sm">
              <Calendar size={16} className="text-gray-500" />
              <span className="text-gray-400">Age:</span>
              <span className="text-white">{profile.age}</span>
            </div>
            <div className="flex items-center gap-3 text-sm">
              <Briefcase size={16} className="text-gray-500" />
              <span className="text-gray-400">Occupation:</span>
              <span className="text-white">{profile.occupation}</span>
            </div>
            <div className="flex items-center gap-3 text-sm">
              <TrendingUp size={16} className="text-green-500" />
              <span className="text-gray-400">Monthly Income:</span>
              <span className="text-white">₹{profile.monthlyIncome.toLocaleString()}</span>
            </div>
            <div className="flex items-center gap-3 text-sm">
              <TrendingDown size={16} className="text-red-500" />
              <span className="text-gray-400">Monthly Expenses:</span>
              <span className="text-white">₹{profile.monthlyExpenses.toLocaleString()}</span>
            </div>
            <div className="flex items-center gap-3 text-sm">
              <Target size={16} className="text-cyan-500" />
              <span className="text-gray-400">Financial Goal:</span>
              <span className="text-white">{profile.mainFinancialGoal || "Not set"}</span>
            </div>
            <div className="flex items-center gap-3 text-sm">
              <Shield size={16} className="text-purple-500" />
              <span className="text-gray-400">Risk Comfort:</span>
              <span className="text-white">{profile.riskComfort}</span>
            </div>
          </div>
        ) : (
          <div className="text-center py-4">
            <p className="text-gray-400 mb-2">Financial profile not set up</p>
            <Link href="/onboarding" className="text-[#4f8cff] text-sm hover:underline">Complete your profile</Link>
          </div>
        )}
      </div>

      {/* Stats */}
      <div className="grid grid-cols-3 gap-4">
        <div className="glass-card p-4 text-center">
          <p className="text-2xl font-bold text-white">{stats.transactions}</p>
          <p className="text-xs text-gray-400">Transactions</p>
        </div>
        <div className="glass-card p-4 text-center">
          <p className="text-2xl font-bold text-white">{stats.goals}</p>
          <p className="text-xs text-gray-400">Goals</p>
        </div>
        <div className="glass-card p-4 text-center">
          <p className="text-2xl font-bold text-white">{stats.riskClass}</p>
          <p className="text-xs text-gray-400">Risk Level</p>
        </div>
      </div>
    </div>
  )
}
