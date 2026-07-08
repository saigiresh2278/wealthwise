"use client"

import { useState } from "react"
import { useRouter } from "next/navigation"
import { useAuth } from "@/context/AuthContext"
import { UserProfile } from "@/lib/types"
import { TrendingUp } from "lucide-react"

export default function OnboardingPage() {
  const router = useRouter()
  const { user, updateProfile } = useAuth()
  const [form, setForm] = useState({
    age: "", occupation: "", monthlyIncome: "", monthlyExpenses: "",
    monthlySavings: "", mainFinancialGoal: "", riskComfort: "Moderate",
    investmentExperience: "Beginner",
  })
  const [error, setError] = useState("")

  const update = (key: string, value: string) => setForm(prev => ({ ...prev, [key]: value }))

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault()
    setError("")
    if (!form.age || !form.occupation || !form.monthlyIncome) {
      setError("Please fill in all required fields")
      return
    }
    if (!user) return
    const profile: UserProfile = {
      email: user.email,
      fullName: user.fullName,
      age: parseInt(form.age),
      occupation: form.occupation,
      monthlyIncome: parseFloat(form.monthlyIncome) || 0,
      monthlyExpenses: parseFloat(form.monthlyExpenses) || 0,
      monthlySavings: parseFloat(form.monthlySavings) || 0,
      mainFinancialGoal: form.mainFinancialGoal,
      riskComfort: form.riskComfort,
      investmentExperience: form.investmentExperience,
    }
    updateProfile(profile)
    router.replace("/dashboard")
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-[#0a0e1a] p-4">
      <div className="w-full max-w-lg">
        <div className="text-center mb-6">
          <div className="w-12 h-12 rounded-2xl bg-gradient-to-br from-[#4f8cff] to-[#6366f1] flex items-center justify-center mx-auto mb-3">
            <TrendingUp size={24} className="text-white" />
          </div>
          <h1 className="text-2xl font-bold text-white">Set Up Your Profile</h1>
          <p className="text-gray-400 mt-1">Tell us about yourself for personalized advice</p>
        </div>

        <form onSubmit={handleSubmit} className="glass-card p-6 space-y-4">
          {error && <div className="bg-red-500/10 border border-red-500/20 text-red-400 text-sm px-4 py-2.5 rounded-xl">{error}</div>}

          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="text-sm text-gray-400 mb-1.5 block">Age *</label>
              <input type="number" value={form.age} onChange={e => update("age", e.target.value)} placeholder="25" />
            </div>
            <div>
              <label className="text-sm text-gray-400 mb-1.5 block">Occupation *</label>
              <input type="text" value={form.occupation} onChange={e => update("occupation", e.target.value)} placeholder="Software Engineer" />
            </div>
          </div>

          <div className="grid grid-cols-3 gap-4">
            <div>
              <label className="text-sm text-gray-400 mb-1.5 block">Monthly Income (₹)</label>
              <input type="number" value={form.monthlyIncome} onChange={e => update("monthlyIncome", e.target.value)} placeholder="50000" />
            </div>
            <div>
              <label className="text-sm text-gray-400 mb-1.5 block">Monthly Expenses (₹)</label>
              <input type="number" value={form.monthlyExpenses} onChange={e => update("monthlyExpenses", e.target.value)} placeholder="30000" />
            </div>
            <div>
              <label className="text-sm text-gray-400 mb-1.5 block">Monthly Savings (₹)</label>
              <input type="number" value={form.monthlySavings} onChange={e => update("monthlySavings", e.target.value)} placeholder="10000" />
            </div>
          </div>

          <div>
            <label className="text-sm text-gray-400 mb-1.5 block">Main Financial Goal</label>
            <input type="text" value={form.mainFinancialGoal} onChange={e => update("mainFinancialGoal", e.target.value)} placeholder="e.g., Buy a house, Retirement, Emergency fund" />
          </div>

          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="text-sm text-gray-400 mb-1.5 block">Risk Comfort</label>
              <select value={form.riskComfort} onChange={e => update("riskComfort", e.target.value)}>
                <option value="Low">Low - Safety first</option>
                <option value="Moderate">Moderate - Balanced</option>
                <option value="High">High - Growth focused</option>
              </select>
            </div>
            <div>
              <label className="text-sm text-gray-400 mb-1.5 block">Investment Experience</label>
              <select value={form.investmentExperience} onChange={e => update("investmentExperience", e.target.value)}>
                <option value="Beginner">Beginner</option>
                <option value="Intermediate">Intermediate</option>
                <option value="Advanced">Advanced</option>
              </select>
            </div>
          </div>

          <button type="submit" className="gradient-btn w-full py-2.5 rounded-xl text-white font-medium">
            Complete Setup
          </button>
        </form>
      </div>
    </div>
  )
}
