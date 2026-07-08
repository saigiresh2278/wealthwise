"use client"

import { useEffect, useState } from "react"
import { useRouter } from "next/navigation"
import { useAuth } from "@/context/AuthContext"
import { getGoals, saveGoals, generateId, getProfile } from "@/lib/store"
import { Goal } from "@/lib/types"
import { analyzeGoal } from "@/lib/ai/goalPlanner"
import { Plus, Pencil, Trash2, Target, X } from "lucide-react"

export default function GoalsPage() {
  const router = useRouter()
  const { user, isLoggedIn } = useAuth()
  const [goals, setGoals] = useState<Goal[]>([])
  const [showModal, setShowModal] = useState(false)
  const [editing, setEditing] = useState<Goal | null>(null)
  const [form, setForm] = useState({ goalName: "", targetAmount: "", currentSavedAmount: "0", targetDate: "", priority: "Medium" as "Low" | "Medium" | "High" })

  useEffect(() => {
    if (!isLoggedIn) { router.replace("/login"); return }
    if (user) setGoals(getGoals(user.email))
  }, [isLoggedIn, user, router])

  const openAdd = () => {
    setEditing(null)
    setForm({ goalName: "", targetAmount: "", currentSavedAmount: "0", targetDate: "", priority: "Medium" })
    setShowModal(true)
  }

  const openEdit = (g: Goal) => {
    setEditing(g)
    setForm({ goalName: g.goalName, targetAmount: g.targetAmount.toString(), currentSavedAmount: g.currentSavedAmount.toString(), targetDate: g.targetDate, priority: g.priority })
    setShowModal(true)
  }

  const handleDelete = (id: string) => {
    if (!confirm("Delete this goal?")) return
    if (!user) return
    const updated = goals.filter(g => g.id !== id)
    setGoals(updated)
    saveGoals(user.email, updated)
  }

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault()
    if (!user) return
    const targetAmount = parseFloat(form.targetAmount)
    const currentSaved = parseFloat(form.currentSavedAmount) || 0
    if (!targetAmount || targetAmount <= 0 || !form.goalName || !form.targetDate) return

    let updated: Goal[]
    if (editing) {
      updated = goals.map(g => g.id === editing.id ? { ...g, goalName: form.goalName, targetAmount, currentSavedAmount: currentSaved, targetDate: form.targetDate, priority: form.priority } : g)
    } else {
      const goal: Goal = { id: generateId(), userEmail: user.email, goalName: form.goalName, targetAmount, currentSavedAmount: currentSaved, targetDate: form.targetDate, priority: form.priority }
      updated = [...goals, goal]
    }
    setGoals(updated)
    saveGoals(user.email, updated)
    setShowModal(false)
  }

  const profile = user ? getProfile(user.email) : null

  return (
    <div className="max-w-6xl mx-auto space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-white">Financial Goals</h1>
          <p className="text-gray-400 text-sm">Plan and track your financial targets</p>
        </div>
        <button onClick={openAdd} className="gradient-btn flex items-center gap-2 px-4 py-2 rounded-xl text-white text-sm">
          <Plus size={16} /> Add Goal
        </button>
      </div>

      {goals.length === 0 ? (
        <div className="glass-card p-12 text-center">
          <Target size={48} className="text-gray-600 mx-auto mb-3" />
          <p className="text-gray-400">No financial goals yet</p>
          <button onClick={openAdd} className="text-[#4f8cff] text-sm hover:underline mt-1">Create your first goal</button>
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          {goals.map(g => {
            const pct = g.targetAmount > 0 ? Math.min(100, Math.round((g.currentSavedAmount / g.targetAmount) * 100)) : 0
            const analysis = profile ? analyzeGoal(g.targetAmount, g.currentSavedAmount, g.targetDate, profile.monthlyIncome, profile.monthlyExpenses) : null
            const priorityColors = { Low: "bg-blue-500/10 text-blue-400", Medium: "bg-yellow-500/10 text-yellow-400", High: "bg-red-500/10 text-red-400" }
            return (
              <div key={g.id} className="glass-card p-5 space-y-3">
                <div className="flex items-start justify-between">
                  <div>
                    <h3 className="text-white font-semibold">{g.goalName}</h3>
                    <span className={`text-xs px-2 py-0.5 rounded-full ${priorityColors[g.priority]}`}>{g.priority} Priority</span>
                  </div>
                  <div className="flex gap-1">
                    <button onClick={() => openEdit(g)} className="p-1.5 rounded-lg hover:bg-white/10 text-gray-400"><Pencil size={14} /></button>
                    <button onClick={() => handleDelete(g.id)} className="p-1.5 rounded-lg hover:bg-red-500/10 text-gray-400 hover:text-red-400"><Trash2 size={14} /></button>
                  </div>
                </div>

                <div className="flex justify-between text-sm">
                  <span className="text-gray-400">₹{g.currentSavedAmount.toLocaleString()} / ₹{g.targetAmount.toLocaleString()}</span>
                  <span className="text-white">{pct}%</span>
                </div>
                <div className="w-full h-2.5 bg-white/5 rounded-full overflow-hidden">
                  <div className="progress-bar h-full" style={{ width: `${pct}%` }} />
                </div>

                <div className="flex justify-between text-xs text-gray-500">
                  <span>Due: {new Date(g.targetDate).toLocaleDateString()}</span>
                  {analysis && (
                    <span className={analysis.achievable ? "text-green-400" : "text-yellow-400"}>
                      {analysis.achievable ? "On track" : "Needs attention"}
                    </span>
                  )}
                </div>

                {analysis && (
                  <div className="bg-white/5 rounded-xl p-3 text-xs space-y-1">
                    <p className="text-gray-400">Monthly required: <span className="text-white">₹{analysis.monthlyRequired.toLocaleString()}</span></p>
                    <p className="text-gray-400">Savings capacity: <span className="text-white">₹{analysis.savingsCapacity.toLocaleString()}/mo</span></p>
                    {analysis.suggestions.slice(0, 1).map((s, i) => (
                      <p key={i} className="text-yellow-400">{s}</p>
                    ))}
                  </div>
                )}
              </div>
            )
          })}
        </div>
      )}

      {/* Modal */}
      {showModal && (
        <div className="fixed inset-0 bg-black/60 flex items-center justify-center z-50 p-4" onClick={() => setShowModal(false)}>
          <div className="glass-card p-6 w-full max-w-md" onClick={e => e.stopPropagation()}>
            <div className="flex justify-between items-center mb-4">
              <h2 className="text-white font-semibold">{editing ? "Edit" : "Add"} Goal</h2>
              <button onClick={() => setShowModal(false)} className="text-gray-400"><X size={20} /></button>
            </div>
            <form onSubmit={handleSubmit} className="space-y-4">
              <div>
                <label className="text-sm text-gray-400 mb-1.5 block">Goal Name</label>
                <input type="text" value={form.goalName} onChange={e => setForm(prev => ({ ...prev, goalName: e.target.value }))} placeholder="e.g., Buy a car" required />
              </div>

              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="text-sm text-gray-400 mb-1.5 block">Target Amount (₹)</label>
                  <input type="number" value={form.targetAmount} onChange={e => setForm(prev => ({ ...prev, targetAmount: e.target.value }))} placeholder="500000" required />
                </div>
                <div>
                  <label className="text-sm text-gray-400 mb-1.5 block">Saved So Far (₹)</label>
                  <input type="number" value={form.currentSavedAmount} onChange={e => setForm(prev => ({ ...prev, currentSavedAmount: e.target.value }))} placeholder="0" />
                </div>
              </div>

              <div>
                <label className="text-sm text-gray-400 mb-1.5 block">Target Date</label>
                <input type="date" value={form.targetDate} onChange={e => setForm(prev => ({ ...prev, targetDate: e.target.value }))} required />
              </div>

              <div>
                <label className="text-sm text-gray-400 mb-1.5 block">Priority</label>
                <select value={form.priority} onChange={e => setForm(prev => ({ ...prev, priority: e.target.value as any }))}>
                  <option value="Low">Low</option>
                  <option value="Medium">Medium</option>
                  <option value="High">High</option>
                </select>
              </div>

              <button type="submit" className="gradient-btn w-full py-2.5 rounded-xl text-white font-medium">
                {editing ? "Update" : "Add"} Goal
              </button>
            </form>
          </div>
        </div>
      )}
    </div>
  )
}
