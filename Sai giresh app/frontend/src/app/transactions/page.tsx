"use client"

import { useEffect, useState } from "react"
import { useRouter } from "next/navigation"
import { useAuth } from "@/context/AuthContext"
import { getTransactions, saveTransactions, generateId } from "@/lib/store"
import { Transaction, INCOME_CATEGORIES, EXPENSE_CATEGORIES } from "@/lib/types"
import { Plus, Pencil, Trash2, TrendingUp, TrendingDown, X } from "lucide-react"

export default function TransactionsPage() {
  const router = useRouter()
  const { user, isLoggedIn } = useAuth()
  const [transactions, setTransactions] = useState<Transaction[]>([])
  const [showModal, setShowModal] = useState(false)
  const [editing, setEditing] = useState<Transaction | null>(null)
  const [filter, setFilter] = useState<"All" | "Income" | "Expense">("All")
  const [form, setForm] = useState({ amount: "", category: "", note: "", date: new Date().toISOString().split("T")[0], type: "Expense" as "Income" | "Expense" })

  useEffect(() => {
    if (!isLoggedIn) { router.replace("/login"); return }
    if (user) setTransactions(getTransactions(user.email))
  }, [isLoggedIn, user, router])

  const filtered = filter === "All" ? transactions : transactions.filter(t => t.type === filter)
  const sorted = [...filtered].sort((a, b) => new Date(b.date).getTime() - new Date(a.date).getTime())

  const income = transactions.filter(t => t.type === "Income").reduce((s, t) => s + t.amount, 0)
  const expenses = transactions.filter(t => t.type === "Expense").reduce((s, t) => s + t.amount, 0)

  const openAdd = () => {
    setEditing(null)
    setForm({ amount: "", category: "", note: "", date: new Date().toISOString().split("T")[0], type: "Expense" })
    setShowModal(true)
  }

  const openEdit = (t: Transaction) => {
    setEditing(t)
    setForm({ amount: t.amount.toString(), category: t.category, note: t.note, date: t.date, type: t.type })
    setShowModal(true)
  }

  const handleDelete = (id: string) => {
    if (!confirm("Delete this transaction?")) return
    if (!user) return
    const updated = transactions.filter(t => t.id !== id)
    setTransactions(updated)
    saveTransactions(user.email, updated)
  }

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault()
    if (!user) return
    const amount = parseFloat(form.amount)
    if (!amount || amount <= 0 || !form.category || !form.date) return

    let updated: Transaction[]
    if (editing) {
      updated = transactions.map(t => t.id === editing.id ? { ...t, amount, category: form.category, note: form.note, date: form.date, type: form.type } : t)
    } else {
      const txn: Transaction = { id: generateId(), userEmail: user.email, amount, category: form.category, note: form.note, date: form.date, type: form.type }
      updated = [...transactions, txn]
    }
    setTransactions(updated)
    saveTransactions(user.email, updated)
    setShowModal(false)
  }

  const categories = form.type === "Income" ? INCOME_CATEGORIES : EXPENSE_CATEGORIES

  return (
    <div className="max-w-6xl mx-auto space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-white">Transactions</h1>
          <p className="text-gray-400 text-sm">Track your income and expenses</p>
        </div>
        <button onClick={openAdd} className="gradient-btn flex items-center gap-2 px-4 py-2 rounded-xl text-white text-sm">
          <Plus size={16} /> Add Transaction
        </button>
      </div>

      {/* Summary */}
      <div className="grid grid-cols-2 gap-4">
        <div className="glass-card p-4 flex items-center gap-3">
          <div className="w-10 h-10 rounded-xl bg-green-500/10 flex items-center justify-center">
            <TrendingUp size={20} className="text-green-400" />
          </div>
          <div>
            <p className="text-xs text-gray-400">Total Income</p>
            <p className="text-lg font-bold text-green-400">₹{income.toLocaleString()}</p>
          </div>
        </div>
        <div className="glass-card p-4 flex items-center gap-3">
          <div className="w-10 h-10 rounded-xl bg-red-500/10 flex items-center justify-center">
            <TrendingDown size={20} className="text-red-400" />
          </div>
          <div>
            <p className="text-xs text-gray-400">Total Expenses</p>
            <p className="text-lg font-bold text-red-400">₹{expenses.toLocaleString()}</p>
          </div>
        </div>
      </div>

      {/* Filter tabs */}
      <div className="flex gap-2">
        {(["All", "Income", "Expense"] as const).map(f => (
          <button key={f} onClick={() => setFilter(f)}
            className={`px-4 py-1.5 rounded-lg text-sm transition-colors ${filter === f ? "bg-[#4f8cff] text-white" : "bg-white/5 text-gray-400 hover:text-white"}`}>
            {f}
          </button>
        ))}
      </div>

      {/* Transaction list */}
      <div className="space-y-2">
        {sorted.length === 0 ? (
          <div className="glass-card p-8 text-center text-gray-500">
            <p>No transactions found</p>
            <button onClick={openAdd} className="text-[#4f8cff] text-sm hover:underline mt-1">Add your first transaction</button>
          </div>
        ) : sorted.map(t => (
          <div key={t.id} className="glass-card px-4 py-3 flex items-center justify-between group">
            <div className="flex items-center gap-3">
              <div className={`w-10 h-10 rounded-xl flex items-center justify-center ${t.type === "Income" ? "bg-green-500/10" : "bg-red-500/10"}`}>
                {t.type === "Income" ? <TrendingUp size={18} className="text-green-400" /> : <TrendingDown size={18} className="text-red-400" />}
              </div>
              <div>
                <p className="text-white text-sm font-medium">{t.category}</p>
                <p className="text-xs text-gray-500">{t.note || new Date(t.date).toLocaleDateString()}</p>
              </div>
            </div>
            <div className="flex items-center gap-3">
              <span className={`text-sm font-medium ${t.type === "Income" ? "text-green-400" : "text-red-400"}`}>
                {t.type === "Income" ? "+" : "-"}₹{t.amount.toLocaleString()}
              </span>
              <div className="flex gap-1 opacity-0 group-hover:opacity-100 transition-opacity">
                <button onClick={() => openEdit(t)} className="p-1.5 rounded-lg hover:bg-white/10 text-gray-400"><Pencil size={14} /></button>
                <button onClick={() => handleDelete(t.id)} className="p-1.5 rounded-lg hover:bg-red-500/10 text-gray-400 hover:text-red-400"><Trash2 size={14} /></button>
              </div>
            </div>
          </div>
        ))}
      </div>

      {/* Modal */}
      {showModal && (
        <div className="fixed inset-0 bg-black/60 flex items-center justify-center z-50 p-4" onClick={() => setShowModal(false)}>
          <div className="glass-card p-6 w-full max-w-md" onClick={e => e.stopPropagation()}>
            <div className="flex justify-between items-center mb-4">
              <h2 className="text-white font-semibold">{editing ? "Edit" : "Add"} Transaction</h2>
              <button onClick={() => setShowModal(false)} className="text-gray-400"><X size={20} /></button>
            </div>
            <form onSubmit={handleSubmit} className="space-y-4">
              <div className="flex gap-2">
                <button type="button" onClick={() => setForm(prev => ({ ...prev, type: "Income" }))}
                  className={`flex-1 py-2 rounded-lg text-sm transition-colors ${form.type === "Income" ? "bg-green-500/20 text-green-400 border border-green-500/30" : "bg-white/5 text-gray-400"}`}>
                  Income
                </button>
                <button type="button" onClick={() => setForm(prev => ({ ...prev, type: "Expense" }))}
                  className={`flex-1 py-2 rounded-lg text-sm transition-colors ${form.type === "Expense" ? "bg-red-500/20 text-red-400 border border-red-500/30" : "bg-white/5 text-gray-400"}`}>
                  Expense
                </button>
              </div>

              <div>
                <label className="text-sm text-gray-400 mb-1.5 block">Amount (₹)</label>
                <input type="number" value={form.amount} onChange={e => setForm(prev => ({ ...prev, amount: e.target.value }))} placeholder="1000" required />
              </div>

              <div>
                <label className="text-sm text-gray-400 mb-1.5 block">Category</label>
                <select value={form.category} onChange={e => setForm(prev => ({ ...prev, category: e.target.value }))} required>
                  <option value="">Select category</option>
                  {categories.map(c => <option key={c} value={c}>{c}</option>)}
                </select>
              </div>

              <div>
                <label className="text-sm text-gray-400 mb-1.5 block">Note (optional)</label>
                <input type="text" value={form.note} onChange={e => setForm(prev => ({ ...prev, note: e.target.value }))} placeholder="Grocery shopping" />
              </div>

              <div>
                <label className="text-sm text-gray-400 mb-1.5 block">Date</label>
                <input type="date" value={form.date} onChange={e => setForm(prev => ({ ...prev, date: e.target.value }))} required />
              </div>

              <button type="submit" className="gradient-btn w-full py-2.5 rounded-xl text-white font-medium">
                {editing ? "Update" : "Add"} Transaction
              </button>
            </form>
          </div>
        </div>
      )}
    </div>
  )
}
