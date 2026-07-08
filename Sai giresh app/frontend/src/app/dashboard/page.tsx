"use client"

import { useEffect, useState } from "react"
import { useRouter } from "next/navigation"
import { useAuth } from "@/context/AuthContext"
import { getTransactions, getGoals, getRiskProfile } from "@/lib/store"
import { calculateFinancialHealth } from "@/lib/ai/healthCalculator"
import { Transaction, Goal } from "@/lib/types"
import HealthRing from "@/components/HealthRing"
import { TrendingUp, TrendingDown, PiggyBank, Target, AlertCircle, Plus, ArrowRight } from "lucide-react"
import Link from "next/link"

export default function DashboardPage() {
  const router = useRouter()
  const { user, profile, isLoggedIn } = useAuth()
  const [transactions, setTransactions] = useState<Transaction[]>([])
  const [goals, setGoals] = useState<Goal[]>([])
  const [health, setHealth] = useState<any>(null)

  useEffect(() => {
    if (!isLoggedIn) { router.replace("/login"); return }
    if (user) {
      const txns = getTransactions(user.email)
      const gs = getGoals(user.email)
      setTransactions(txns)
      setGoals(gs)
    }
  }, [isLoggedIn, user, router])

  useEffect(() => {
    if (profile && transactions.length > 0) {
      const monthlyAvgExpense = transactions.filter(t => t.type === "Expense").reduce((s, t) => s + t.amount, 0) / Math.max(1, transactions.length)
      const totalSavings = transactions.filter(t => t.type === "Income").reduce((s, t) => s + t.amount, 0) -
        transactions.filter(t => t.type === "Expense").reduce((s, t) => s + t.amount, 0)
      setHealth(calculateFinancialHealth(
        profile.monthlyIncome, profile.monthlyExpenses, profile.monthlySavings,
        Math.max(0, totalSavings), monthlyAvgExpense
      ))
    } else if (profile) {
      setHealth(calculateFinancialHealth(
        profile.monthlyIncome, profile.monthlyExpenses, profile.monthlySavings, 0, 0
      ))
    }
  }, [profile, transactions])

  const income = transactions.filter(t => t.type === "Income").reduce((s, t) => s + t.amount, 0)
  const expenses = transactions.filter(t => t.type === "Expense").reduce((s, t) => s + t.amount, 0)
  const balance = income - expenses
  const recentTxns = [...transactions].sort((a, b) => new Date(b.date).getTime() - new Date(a.date).getTime()).slice(0, 5)

  const onTrackGoals = goals.filter(g => g.currentSavedAmount >= g.targetAmount * 0.5).length

  return (
    <div className="max-w-6xl mx-auto space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-white">Dashboard</h1>
          <p className="text-gray-400 text-sm">Welcome back, {profile?.fullName || user?.fullName}</p>
        </div>
        <Link href="/transactions" className="gradient-btn flex items-center gap-2 px-4 py-2 rounded-xl text-white text-sm">
          <Plus size={16} /> Add Transaction
        </Link>
      </div>

      {/* Health Score + Quick Stats */}
      <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
        <div className="glass-card p-5 flex items-center justify-center md:col-span-1">
          {health ? <HealthRing score={health.score} /> : (
            <div className="text-center">
              <div className="w-20 h-20 rounded-full bg-white/5 flex items-center justify-center mx-auto mb-2">
                <AlertCircle size={32} className="text-gray-500" />
              </div>
              <p className="text-xs text-gray-400">Set up profile to see score</p>
            </div>
          )}
        </div>

        <div className="glass-card p-5">
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 rounded-xl bg-green-500/10 flex items-center justify-center">
              <TrendingUp size={20} className="text-green-400" />
            </div>
            <div>
              <p className="text-xs text-gray-400">Total Income</p>
              <p className="text-xl font-bold text-white">₹{income.toLocaleString()}</p>
            </div>
          </div>
        </div>

        <div className="glass-card p-5">
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 rounded-xl bg-red-500/10 flex items-center justify-center">
              <TrendingDown size={20} className="text-red-400" />
            </div>
            <div>
              <p className="text-xs text-gray-400">Total Expenses</p>
              <p className="text-xl font-bold text-white">₹{expenses.toLocaleString()}</p>
            </div>
          </div>
        </div>

        <div className="glass-card p-5">
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 rounded-xl bg-cyan-500/10 flex items-center justify-center">
              <PiggyBank size={20} className="text-cyan-400" />
            </div>
            <div>
              <p className="text-xs text-gray-400">Balance</p>
              <p className={`text-xl font-bold ${balance >= 0 ? "text-white" : "text-red-400"}`}>
                ₹{balance.toLocaleString()}
              </p>
            </div>
          </div>
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Recent Transactions */}
        <div className="glass-card p-5">
          <div className="flex items-center justify-between mb-4">
            <h2 className="text-white font-semibold">Recent Transactions</h2>
            <Link href="/transactions" className="text-[#4f8cff] text-sm hover:underline flex items-center gap-1">
              View All <ArrowRight size={14} />
            </Link>
          </div>
          {recentTxns.length === 0 ? (
            <div className="text-center py-8 text-gray-500 text-sm">
              <p>No transactions yet</p>
              <Link href="/transactions" className="text-[#4f8cff] hover:underline">Add your first transaction</Link>
            </div>
          ) : (
            <div className="space-y-2">
              {recentTxns.map(t => (
                <div key={t.id} className="flex items-center justify-between py-2 border-b border-white/5 last:border-0">
                  <div className="flex items-center gap-3">
                    <div className={`w-2 h-2 rounded-full ${t.type === "Income" ? "bg-green-400" : "bg-red-400"}`} />
                    <div>
                      <p className="text-sm text-white">{t.category}</p>
                      <p className="text-xs text-gray-500">{new Date(t.date).toLocaleDateString()}</p>
                    </div>
                  </div>
                  <span className={`text-sm font-medium ${t.type === "Income" ? "text-green-400" : "text-red-400"}`}>
                    {t.type === "Income" ? "+" : "-"}₹{t.amount.toLocaleString()}
                  </span>
                </div>
              ))}
            </div>
          )}
        </div>

        {/* Goals Progress */}
        <div className="glass-card p-5">
          <div className="flex items-center justify-between mb-4">
            <h2 className="text-white font-semibold">Goals Progress</h2>
            <Link href="/goals" className="text-[#4f8cff] text-sm hover:underline flex items-center gap-1">
              View All <ArrowRight size={14} />
            </Link>
          </div>
          {goals.length === 0 ? (
            <div className="text-center py-8 text-gray-500 text-sm">
              <p>No goals set yet</p>
              <Link href="/goals" className="text-[#4f8cff] hover:underline">Create your first goal</Link>
            </div>
          ) : (
            <div className="space-y-4">
              {goals.slice(0, 3).map(g => {
                const pct = Math.min(100, Math.round((g.currentSavedAmount / g.targetAmount) * 100))
                return (
                  <div key={g.id}>
                    <div className="flex justify-between text-sm mb-1">
                      <span className="text-white">{g.goalName}</span>
                      <span className="text-gray-400">₹{g.currentSavedAmount.toLocaleString()} / ₹{g.targetAmount.toLocaleString()}</span>
                    </div>
                    <div className="w-full h-2 bg-white/5 rounded-full overflow-hidden">
                      <div className="progress-bar h-full" style={{ width: `${pct}%` }} />
                    </div>
                    <p className="text-xs text-gray-500 mt-1">{pct}% completed</p>
                  </div>
                )
              })}
            </div>
          )}
        </div>
      </div>

      {/* Health Insights */}
      {health && health.insights.length > 0 && (
        <div className="glass-card p-5">
          <h2 className="text-white font-semibold mb-3">Financial Insights</h2>
          <div className="space-y-2">
            {health.insights.map((insight: string, i: number) => (
              <div key={i} className="flex items-start gap-3 text-sm">
                <div className="w-1.5 h-1.5 rounded-full bg-[#4f8cff] mt-2 shrink-0" />
                <p className="text-gray-300">{insight}</p>
              </div>
            ))}
          </div>
        </div>
      )}
    </div>
  )
}
