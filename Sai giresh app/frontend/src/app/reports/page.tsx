"use client"

import { useEffect, useState } from "react"
import { useRouter } from "next/navigation"
import { useAuth } from "@/context/AuthContext"
import { getTransactions, getGoals } from "@/lib/store"
import { Transaction, Goal } from "@/lib/types"
import { BarChart, Bar, XAxis, YAxis, Tooltip, ResponsiveContainer, PieChart, Pie, Cell, Legend } from "recharts"
import { BarChart3, TrendingUp, TrendingDown, PiggyBank, Target } from "lucide-react"

const COLORS = ["#4f8cff", "#34d399", "#f87171", "#fbbf24", "#a78bfa", "#22d3ee", "#f472b6", "#fb923c"]

export default function ReportsPage() {
  const router = useRouter()
  const { user, isLoggedIn, profile } = useAuth()
  const [transactions, setTransactions] = useState<Transaction[]>([])
  const [goals, setGoals] = useState<Goal[]>([])
  const [view, setView] = useState<"overview" | "category" | "goals">("overview")

  useEffect(() => {
    if (!isLoggedIn) { router.replace("/login"); return }
    if (user) {
      setTransactions(getTransactions(user.email))
      setGoals(getGoals(user.email))
    }
  }, [isLoggedIn, user, router])

  const income = transactions.filter(t => t.type === "Income").reduce((s, t) => s + t.amount, 0)
  const expenses = transactions.filter(t => t.type === "Expense").reduce((s, t) => s + t.amount, 0)
  const balance = income - expenses
  const savingsRate = income > 0 ? ((balance / income) * 100).toFixed(1) : "0"

  // Monthly data
  const monthlyData: Record<string, { income: number; expense: number }> = {}
  transactions.forEach(t => {
    const month = t.date.slice(0, 7)
    if (!monthlyData[month]) monthlyData[month] = { income: 0, expense: 0 }
    if (t.type === "Income") monthlyData[month].income += t.amount
    else monthlyData[month].expense += t.amount
  })
  const monthlyChart = Object.entries(monthlyData).sort().map(([month, d]) => ({
    month: new Date(month + "-01").toLocaleDateString("en-US", { month: "short", year: "2-digit" }),
    Income: d.income,
    Expense: d.expense,
  }))

  // Category breakdown
  const incomeByCat: Record<string, number> = {}
  const expenseByCat: Record<string, number> = {}
  transactions.forEach(t => {
    if (t.type === "Income") incomeByCat[t.category] = (incomeByCat[t.category] || 0) + t.amount
    else expenseByCat[t.category] = (expenseByCat[t.category] || 0) + t.amount
  })
  const incomePie = Object.entries(incomeByCat).map(([name, value]) => ({ name, value }))
  const expensePie = Object.entries(expenseByCat).map(([name, value]) => ({ name, value }))

  return (
    <div className="max-w-6xl mx-auto space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-white">Reports</h1>
        <p className="text-gray-400 text-sm">Visual insights into your finances</p>
      </div>

      {/* Summary cards */}
      <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
        <div className="glass-card p-4">
          <TrendingUp size={18} className="text-green-400 mb-2" />
          <p className="text-xs text-gray-400">Total Income</p>
          <p className="text-lg font-bold text-white">₹{income.toLocaleString()}</p>
        </div>
        <div className="glass-card p-4">
          <TrendingDown size={18} className="text-red-400 mb-2" />
          <p className="text-xs text-gray-400">Total Expenses</p>
          <p className="text-lg font-bold text-white">₹{expenses.toLocaleString()}</p>
        </div>
        <div className="glass-card p-4">
          <PiggyBank size={18} className="text-cyan-400 mb-2" />
          <p className="text-xs text-gray-400">Balance</p>
          <p className="text-lg font-bold text-white">₹{balance.toLocaleString()}</p>
        </div>
        <div className="glass-card p-4">
          <Target size={18} className="text-purple-400 mb-2" />
          <p className="text-xs text-gray-400">Savings Rate</p>
          <p className="text-lg font-bold text-white">{savingsRate}%</p>
        </div>
      </div>

      {/* Tabs */}
      <div className="flex gap-2">
        {(["overview", "category", "goals"] as const).map(v => (
          <button key={v} onClick={() => setView(v)}
            className={`px-4 py-1.5 rounded-lg text-sm capitalize transition-colors ${view === v ? "bg-[#4f8cff] text-white" : "bg-white/5 text-gray-400 hover:text-white"}`}>
            {v === "overview" ? "Income vs Expenses" : v === "category" ? "Category Breakdown" : "Goals Progress"}
          </button>
        ))}
      </div>

      {view === "overview" && (
        <div className="glass-card p-5">
          <h2 className="text-white font-semibold mb-4">Income vs Expenses</h2>
          {monthlyChart.length === 0 ? (
            <p className="text-gray-500 text-center py-10">No transaction data available</p>
          ) : (
            <ResponsiveContainer width="100%" height={350}>
              <BarChart data={monthlyChart}>
                <XAxis dataKey="month" stroke="#8892a8" fontSize={12} />
                <YAxis stroke="#8892a8" fontSize={12} />
                <Tooltip
                  contentStyle={{ background: "#131829", border: "1px solid #1e2844", borderRadius: "12px" }}
                  labelStyle={{ color: "#e8edf5" }}
                />
                <Bar dataKey="Income" fill="#34d399" radius={[4, 4, 0, 0]} />
                <Bar dataKey="Expense" fill="#f87171" radius={[4, 4, 0, 0]} />
                <Legend />
              </BarChart>
            </ResponsiveContainer>
          )}
        </div>
      )}

      {view === "category" && (
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          <div className="glass-card p-5">
            <h2 className="text-white font-semibold mb-4">Income by Category</h2>
            {incomePie.length === 0 ? <p className="text-gray-500 text-center py-10">No income data</p> : (
              <ResponsiveContainer width="100%" height={300}>
                <PieChart>
                  <Pie data={incomePie} dataKey="value" nameKey="name" cx="50%" cy="50%" outerRadius={100} label={({ name, percent }: any) => `${name} ${((percent || 0) * 100).toFixed(0)}%`}>
                    {incomePie.map((_, i) => <Cell key={i} fill={COLORS[i % COLORS.length]} />)}
                  </Pie>
                  <Tooltip />
                </PieChart>
              </ResponsiveContainer>
            )}
          </div>
          <div className="glass-card p-5">
            <h2 className="text-white font-semibold mb-4">Expenses by Category</h2>
            {expensePie.length === 0 ? <p className="text-gray-500 text-center py-10">No expense data</p> : (
              <ResponsiveContainer width="100%" height={300}>
                <PieChart>
                  <Pie data={expensePie} dataKey="value" nameKey="name" cx="50%" cy="50%" outerRadius={100} label={({ name, percent }: any) => `${name} ${((percent || 0) * 100).toFixed(0)}%`}>
                    {expensePie.map((_, i) => <Cell key={i} fill={COLORS[i % COLORS.length]} />)}
                  </Pie>
                  <Tooltip />
                </PieChart>
              </ResponsiveContainer>
            )}
          </div>
        </div>
      )}

      {view === "goals" && (
        <div className="glass-card p-5">
          <h2 className="text-white font-semibold mb-4">Goal Progress</h2>
          {goals.length === 0 ? (
            <p className="text-gray-500 text-center py-10">No goals set yet</p>
          ) : (
            <div className="space-y-5">
              {goals.map(g => {
                const pct = g.targetAmount > 0 ? (g.currentSavedAmount / g.targetAmount) * 100 : 0
                return (
                  <div key={g.id}>
                    <div className="flex justify-between text-sm mb-1">
                      <span className="text-white">{g.goalName}</span>
                      <span className="text-gray-400">{pct.toFixed(0)}%</span>
                    </div>
                    <div className="w-full h-3 bg-white/5 rounded-full overflow-hidden">
                      <div className="h-full rounded-full transition-all" style={{ width: `${Math.min(100, pct)}%`, background: pct >= 100 ? "#34d399" : pct >= 50 ? "#fbbf24" : "#4f8cff" }} />
                    </div>
                    <p className="text-xs text-gray-500 mt-1">
                      ₹{g.currentSavedAmount.toLocaleString()} / ₹{g.targetAmount.toLocaleString()}
                      {g.currentSavedAmount >= g.targetAmount && " ✅ Goal achieved!"}
                    </p>
                  </div>
                )
              })}
            </div>
          )}
        </div>
      )}
    </div>
  )
}
