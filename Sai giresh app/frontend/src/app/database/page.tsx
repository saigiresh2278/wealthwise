"use client"

import { useState, useEffect } from "react"
import { useRouter } from "next/navigation"
import { 
  Database, UserCheck, Users, DollarSign, Target, ShieldAlert, 
  Trash2, RefreshCw, Plus, Check, AlertCircle, FileText
} from "lucide-react"
import { db } from "@/lib/firebase"
import { ref, get, set, remove } from "firebase/database"

export default function DatabaseExplorerPage() {
  const router = useRouter()
  const [activeTab, setActiveTab] = useState<"auth" | "users" | "transactions" | "goals" | "risk">("auth")
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  // Database states
  const [authUsers, setAuthUsers] = useState<Record<string, any>>({})
  const [users, setUsers] = useState<Record<string, any>>({})
  const [transactions, setTransactions] = useState<Record<string, any>>({})
  const [goals, setGoals] = useState<Record<string, any>>({})
  const [riskProfiles, setRiskProfiles] = useState<Record<string, any>>({})

  // Form states
  const [authForm, setAuthForm] = useState({ email: "", fullName: "", passwordHash: "demo_hash" })
  const [userForm, setUserForm] = useState({ email: "", fullName: "", age: "25", occupation: "Student", monthlyIncome: "50000", monthlyExpenses: "20000" })
  const [txForm, setTxForm] = useState({ email: "", amount: "100", category: "Food", type: "Expense", note: "Snacks" })
  const [goalForm, setGoalForm] = useState({ email: "", goalName: "Emergency Fund", targetAmount: "100000", currentSavedAmount: "10000", priority: "Medium" })
  const [riskForm, setRiskForm] = useState({ email: "", score: "65", riskClass: "Medium" })

  const fetchData = async () => {
    setLoading(true)
    setError(null)
    try {
      if (!db) {
        throw new Error("Firebase Database is not initialized. Check your credentials.")
      }

      // Fetch Auth Users
      const authSnap = await get(ref(db, "auth_users"))
      setAuthUsers(authSnap.val() || {})

      // Fetch Users
      const usersSnap = await get(ref(db, "users"))
      setUsers(usersSnap.val() || {})

      // Fetch Transactions
      const txSnap = await get(ref(db, "transactions"))
      setTransactions(txSnap.val() || {})

      // Fetch Goals
      const goalsSnap = await get(ref(db, "goals"))
      setGoals(goalsSnap.val() || {})

      // Fetch Risk Profiles
      const riskSnap = await get(ref(db, "risk_profiles"))
      setRiskProfiles(riskSnap.val() || {})
    } catch (e: any) {
      console.error(e)
      setError(e.message || "Failed to load database nodes.")
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    fetchData()
  }, [])

  const encodeEmail = (email: string) => {
    return email.trim().toLowerCase().replace(/\./g, ",")
  }

  // Delete handlers
  const deleteAuthUser = async (key: string) => {
    if (confirm(`Delete auth user: ${key}?`)) {
      await remove(ref(db, `auth_users/${key}`))
      fetchData()
    }
  }

  const deleteUserProfile = async (key: string) => {
    if (confirm(`Delete user profile: ${key}?`)) {
      await remove(ref(db, `users/${key}`))
      fetchData()
    }
  }

  const deleteTransaction = async (emailKey: string, id: string) => {
    if (confirm(`Delete transaction ${id}?`)) {
      await remove(ref(db, `transactions/${emailKey}/${id}`))
      fetchData()
    }
  }

  const deleteGoal = async (emailKey: string, id: string) => {
    if (confirm(`Delete goal ${id}?`)) {
      await remove(ref(db, `goals/${emailKey}/${id}`))
      fetchData()
    }
  }

  const deleteRiskProfile = async (key: string) => {
    if (confirm(`Delete risk profile: ${key}?`)) {
      await remove(ref(db, `risk_profiles/${key}`))
      fetchData()
    }
  }

  // Add handlers
  const handleAddAuth = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!authForm.email) return
    const key = encodeEmail(authForm.email)
    await set(ref(db, `auth_users/${key}`), {
      email: authForm.email,
      fullName: authForm.fullName,
      passwordHash: authForm.passwordHash
    })
    setAuthForm({ email: "", fullName: "", passwordHash: "demo_hash" })
    fetchData()
  }

  const handleAddUser = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!userForm.email) return
    const key = encodeEmail(userForm.email)
    await set(ref(db, `users/${key}`), {
      email: userForm.email,
      fullName: userForm.fullName,
      age: Number(userForm.age),
      occupation: userForm.occupation,
      monthlyIncome: Number(userForm.monthlyIncome),
      monthlyExpenses: Number(userForm.monthlyExpenses),
      monthlySavings: Number(userForm.monthlyIncome) - Number(userForm.monthlyExpenses),
      mainFinancialGoal: "Savings",
      riskComfort: "Medium",
      investmentExperience: "Beginner"
    })
    setUserForm({ email: "", fullName: "", age: "25", occupation: "Student", monthlyIncome: "50000", monthlyExpenses: "20000" })
    fetchData()
  }

  const handleAddTx = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!txForm.email) return
    const key = encodeEmail(txForm.email)
    const id = Date.now().toString()
    await set(ref(db, `transactions/${key}/${id}`), {
      id,
      userEmail: txForm.email.toLowerCase(),
      amount: Number(txForm.amount),
      category: txForm.category,
      type: txForm.type,
      note: txForm.note,
      date: new Date().toISOString().split("T")[0]
    })
    setTxForm({ email: "", amount: "100", category: "Food", type: "Expense", note: "Snacks" })
    fetchData()
  }

  const handleAddGoal = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!goalForm.email) return
    const key = encodeEmail(goalForm.email)
    const id = Date.now().toString()
    await set(ref(db, `goals/${key}/${id}`), {
      id,
      userEmail: goalForm.email.toLowerCase(),
      goalName: goalForm.goalName,
      targetAmount: Number(goalForm.targetAmount),
      currentSavedAmount: Number(goalForm.currentSavedAmount),
      targetDate: new Date(Date.now() + 365*24*60*60*1000).toISOString().split("T")[0],
      priority: goalForm.priority
    })
    setGoalForm({ email: "", goalName: "Emergency Fund", targetAmount: "100000", currentSavedAmount: "10000", priority: "Medium" })
    fetchData()
  }

  const handleAddRisk = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!riskForm.email) return
    const key = encodeEmail(riskForm.email)
    await set(ref(db, `risk_profiles/${key}`), {
      email: riskForm.email.toLowerCase(),
      score: Number(riskForm.score),
      riskClass: riskForm.riskClass,
      lastAssessmentDate: new Date().toLocaleDateString()
    })
    setRiskForm({ email: "", score: "65", riskClass: "Medium" })
    fetchData()
  }

  return (
    <div className="min-h-screen bg-slate-900 text-slate-100 p-6 font-sans">
      <div className="max-w-7xl mx-auto">
        <header className="flex justify-between items-center mb-8 border-b border-slate-800 pb-5">
          <div className="flex items-center gap-3">
            <div className="p-3 bg-indigo-600/20 text-indigo-400 rounded-xl">
              <Database className="w-8 h-8" />
            </div>
            <div>
              <h1 className="text-2xl font-bold tracking-tight text-white">Database Explorer</h1>
              <p className="text-slate-400 text-sm">Realtime verification of Firebase Realtime Database nodes</p>
            </div>
          </div>
          <div className="flex gap-3">
            <button 
              onClick={() => router.push("/dashboard")}
              className="px-4 py-2 bg-slate-800 hover:bg-slate-700 text-white rounded-lg transition text-sm font-medium"
            >
              Go to Dashboard
            </button>
            <button 
              onClick={fetchData}
              className="flex items-center gap-2 px-4 py-2 bg-indigo-600 hover:bg-indigo-550 text-white rounded-lg transition text-sm font-medium"
            >
              <RefreshCw className={`w-4 h-4 ${loading ? 'animate-spin' : ''}`} />
              Refresh Nodes
            </button>
          </div>
        </header>

        {error && (
          <div className="mb-6 p-4 bg-red-900/30 border border-red-800 rounded-xl text-red-300 flex items-center gap-3">
            <AlertCircle className="w-5 h-5 shrink-0" />
            <p className="text-sm">{error}</p>
          </div>
        )}

        {/* Tab Navigation */}
        <div className="flex gap-2 mb-6 border-b border-slate-800 pb-px overflow-x-auto">
          {[
            { id: "auth", label: "auth_users", icon: UserCheck, count: Object.keys(authUsers).length },
            { id: "users", label: "users", icon: Users, count: Object.keys(users).length },
            { id: "transactions", label: "transactions", icon: DollarSign, count: Object.values(transactions).reduce((acc, t) => acc + (t ? Object.keys(t).length : 0), 0) },
            { id: "goals", label: "goals", icon: Target, count: Object.values(goals).reduce((acc, g) => acc + (g ? Object.keys(g).length : 0), 0) },
            { id: "risk", label: "risk_profiles", icon: ShieldAlert, count: Object.keys(riskProfiles).length }
          ].map(tab => (
            <button
              key={tab.id}
              onClick={() => setActiveTab(tab.id as any)}
              className={`flex items-center gap-2 px-5 py-3 border-b-2 font-medium text-sm transition shrink-0 ${
                activeTab === tab.id 
                  ? "border-indigo-500 text-indigo-400" 
                  : "border-transparent text-slate-400 hover:text-slate-200"
              }`}
            >
              <tab.icon className="w-4 h-4" />
              <span>{tab.label}</span>
              <span className="ml-1 bg-slate-800 text-slate-300 px-2 py-0.5 rounded-full text-xs font-semibold">
                {tab.count}
              </span>
            </button>
          ))}
        </div>

        {/* Explorer Content */}
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
          {/* Main List */}
          <div className="lg:col-span-2 bg-slate-800/50 border border-slate-800 rounded-2xl p-6 min-h-[500px]">
            {loading ? (
              <div className="flex flex-col items-center justify-center h-full py-20 text-slate-400 gap-3">
                <RefreshCw className="w-8 h-8 animate-spin text-indigo-500" />
                <p>Reading Realtime Database nodes...</p>
              </div>
            ) : (
              <>
                {/* auth_users tab */}
                {activeTab === "auth" && (
                  <div>
                    <h3 className="text-lg font-semibold text-white mb-4">auth_users Node</h3>
                    {Object.keys(authUsers).length === 0 ? (
                      <div className="text-center py-16 text-slate-500">No records found. Register a user in signup.</div>
                    ) : (
                      <div className="overflow-x-auto">
                        <table className="w-full text-left border-collapse">
                          <thead>
                            <tr className="border-b border-slate-850 text-slate-400 text-xs font-semibold uppercase tracking-wider">
                              <th className="py-3 px-4">Email Key</th>
                              <th className="py-3 px-4">Full Name</th>
                              <th className="py-3 px-4">Password Hash</th>
                              <th className="py-3 px-4 text-right">Actions</th>
                            </tr>
                          </thead>
                          <tbody>
                            {Object.entries(authUsers).map(([key, val]) => (
                              <tr key={key} className="border-b border-slate-850 hover:bg-slate-800/40 text-sm">
                                <td className="py-3.5 px-4 font-mono text-indigo-400">{key}</td>
                                <td className="py-3.5 px-4 text-white font-medium">{val.fullName}</td>
                                <td className="py-3.5 px-4 font-mono text-xs text-slate-400">{val.passwordHash}</td>
                                <td className="py-3.5 px-4 text-right">
                                  <button onClick={() => deleteAuthUser(key)} className="text-red-400 hover:text-red-300 p-1.5 hover:bg-red-500/10 rounded transition">
                                    <Trash2 className="w-4 h-4" />
                                  </button>
                                </td>
                              </tr>
                            ))}
                          </tbody>
                        </table>
                      </div>
                    )}
                  </div>
                )}

                {/* users tab */}
                {activeTab === "users" && (
                  <div>
                    <h3 className="text-lg font-semibold text-white mb-4">users Node</h3>
                    {Object.keys(users).length === 0 ? (
                      <div className="text-center py-16 text-slate-500">No onboarding profiles found.</div>
                    ) : (
                      <div className="overflow-x-auto">
                        <table className="w-full text-left border-collapse">
                          <thead>
                            <tr className="border-b border-slate-850 text-slate-400 text-xs font-semibold uppercase tracking-wider">
                              <th className="py-3 px-4">Email Key</th>
                              <th className="py-3 px-4">Profile Info</th>
                              <th className="py-3 px-4">Monthly Stats</th>
                              <th className="py-3 px-4 text-right">Actions</th>
                            </tr>
                          </thead>
                          <tbody>
                            {Object.entries(users).map(([key, val]) => (
                              <tr key={key} className="border-b border-slate-850 hover:bg-slate-800/40 text-sm">
                                <td className="py-3.5 px-4 font-mono text-indigo-400">{key}</td>
                                <td className="py-3.5 px-4 text-slate-200">
                                  <div className="font-semibold text-white">{val.fullName}</div>
                                  <div className="text-xs text-slate-400">Age: {val.age} | {val.occupation}</div>
                                </td>
                                <td className="py-3.5 px-4 text-slate-200">
                                  <div className="text-xs font-semibold text-emerald-400">Inc: ₹{val.monthlyIncome}</div>
                                  <div className="text-xs font-semibold text-rose-400">Exp: ₹{val.monthlyExpenses}</div>
                                </td>
                                <td className="py-3.5 px-4 text-right">
                                  <button onClick={() => deleteUserProfile(key)} className="text-red-400 hover:text-red-300 p-1.5 hover:bg-red-500/10 rounded transition">
                                    <Trash2 className="w-4 h-4" />
                                  </button>
                                </td>
                              </tr>
                            ))}
                          </tbody>
                        </table>
                      </div>
                    )}
                  </div>
                )}

                {/* transactions tab */}
                {activeTab === "transactions" && (
                  <div>
                    <h3 className="text-lg font-semibold text-white mb-4">transactions Node</h3>
                    {Object.keys(transactions).length === 0 ? (
                      <div className="text-center py-16 text-slate-500">No transactions synced.</div>
                    ) : (
                      <div className="space-y-6">
                        {Object.entries(transactions).map(([emailKey, userTxList]) => {
                          if (!userTxList) return null
                          return (
                            <div key={emailKey} className="border border-slate-800 rounded-xl p-4 bg-slate-900/30">
                              <h4 className="font-mono text-indigo-400 text-xs font-semibold mb-3 border-b border-slate-800 pb-2">
                                User: {emailKey} ({Object.keys(userTxList).length} records)
                              </h4>
                              <div className="overflow-x-auto">
                                <table className="w-full text-left border-collapse">
                                  <thead>
                                    <tr className="text-slate-500 text-[10px] font-bold uppercase tracking-wider">
                                      <th className="pb-2 px-2">ID</th>
                                      <th className="pb-2 px-2">Category</th>
                                      <th className="pb-2 px-2">Type</th>
                                      <th className="pb-2 px-2">Amount</th>
                                      <th className="pb-2 px-2">Note</th>
                                      <th className="pb-2 px-2 text-right">Actions</th>
                                    </tr>
                                  </thead>
                                  <tbody>
                                    {Object.entries(userTxList).map(([id, t]: [string, any]) => (
                                      <tr key={id} className="text-xs hover:bg-slate-850/50">
                                        <td className="py-2 px-2 font-mono text-slate-500">{id}</td>
                                        <td className="py-2 px-2 text-white font-medium">{t.category}</td>
                                        <td className="py-2 px-2">
                                          <span className={`px-2 py-0.5 rounded-full text-[10px] font-semibold ${
                                            t.type === "Income" ? "bg-emerald-500/10 text-emerald-400" : "bg-rose-500/10 text-rose-400"
                                          }`}>
                                            {t.type}
                                          </span>
                                        </td>
                                        <td className="py-2 px-2 font-semibold">₹{t.amount}</td>
                                        <td className="py-2 px-2 text-slate-400 italic">{t.note || "-"}</td>
                                        <td className="py-2 px-2 text-right">
                                          <button onClick={() => deleteTransaction(emailKey, id)} className="text-red-400 hover:text-red-300 p-1 hover:bg-red-500/10 rounded transition">
                                            <Trash2 className="w-3.5 h-3.5" />
                                          </button>
                                        </td>
                                      </tr>
                                    ))}
                                  </tbody>
                                </table>
                              </div>
                            </div>
                          )
                        })}
                      </div>
                    )}
                  </div>
                )}

                {/* goals tab */}
                {activeTab === "goals" && (
                  <div>
                    <h3 className="text-lg font-semibold text-white mb-4">goals Node</h3>
                    {Object.keys(goals).length === 0 ? (
                      <div className="text-center py-16 text-slate-500">No goals tracked.</div>
                    ) : (
                      <div className="space-y-6">
                        {Object.entries(goals).map(([emailKey, userGoals]) => {
                          if (!userGoals) return null
                          return (
                            <div key={emailKey} className="border border-slate-800 rounded-xl p-4 bg-slate-900/30">
                              <h4 className="font-mono text-indigo-400 text-xs font-semibold mb-3 border-b border-slate-800 pb-2">
                                User: {emailKey} ({Object.keys(userGoals).length} records)
                              </h4>
                              <div className="overflow-x-auto">
                                <table className="w-full text-left border-collapse">
                                  <thead>
                                    <tr className="text-slate-500 text-[10px] font-bold uppercase tracking-wider">
                                      <th className="pb-2 px-2">ID</th>
                                      <th className="pb-2 px-2">Goal Name</th>
                                      <th className="pb-2 px-2">Progress</th>
                                      <th className="pb-2 px-2">Target</th>
                                      <th className="pb-2 px-2">Priority</th>
                                      <th className="pb-2 px-2 text-right">Actions</th>
                                    </tr>
                                  </thead>
                                  <tbody>
                                    {Object.entries(userGoals).map(([id, g]: [string, any]) => (
                                      <tr key={id} className="text-xs hover:bg-slate-850/50">
                                        <td className="py-2 px-2 font-mono text-slate-500">{id}</td>
                                        <td className="py-2 px-2 text-white font-medium">{g.goalName}</td>
                                        <td className="py-2 px-2 text-slate-300">
                                          ₹{g.currentSavedAmount} / ₹{g.targetAmount}
                                        </td>
                                        <td className="py-2 px-2 text-slate-400 font-mono text-[10px]">{g.targetDate}</td>
                                        <td className="py-2 px-2">
                                          <span className={`px-1.5 py-0.5 rounded text-[9px] font-bold ${
                                            g.priority === "High" ? "bg-red-500/10 text-red-400" : g.priority === "Low" ? "bg-slate-700 text-slate-300" : "bg-yellow-500/10 text-yellow-400"
                                          }`}>
                                            {g.priority}
                                          </span>
                                        </td>
                                        <td className="py-2 px-2 text-right">
                                          <button onClick={() => deleteGoal(emailKey, id)} className="text-red-400 hover:text-red-300 p-1 hover:bg-red-500/10 rounded transition">
                                            <Trash2 className="w-3.5 h-3.5" />
                                          </button>
                                        </td>
                                      </tr>
                                    ))}
                                  </tbody>
                                </table>
                              </div>
                            </div>
                          )
                        })}
                      </div>
                    )}
                  </div>
                )}

                {/* risk_profiles tab */}
                {activeTab === "risk" && (
                  <div>
                    <h3 className="text-lg font-semibold text-white mb-4">risk_profiles Node</h3>
                    {Object.keys(riskProfiles).length === 0 ? (
                      <div className="text-center py-16 text-slate-500">No risk profiling records.</div>
                    ) : (
                      <div className="overflow-x-auto">
                        <table className="w-full text-left border-collapse">
                          <thead>
                            <tr className="border-b border-slate-850 text-slate-400 text-xs font-semibold uppercase tracking-wider">
                              <th className="py-3 px-4">Email Key</th>
                              <th className="py-3 px-4">Risk score</th>
                              <th className="py-3 px-4">Classification</th>
                              <th className="py-3 px-4">Last Assessment</th>
                              <th className="py-3 px-4 text-right">Actions</th>
                            </tr>
                          </thead>
                          <tbody>
                            {Object.entries(riskProfiles).map(([key, val]) => (
                              <tr key={key} className="border-b border-slate-850 hover:bg-slate-800/40 text-sm">
                                <td className="py-3.5 px-4 font-mono text-indigo-400">{key}</td>
                                <td className="py-3.5 px-4 text-white font-semibold">{val.score}/100</td>
                                <td className="py-3.5 px-4">
                                  <span className={`px-2 py-0.5 rounded-full text-xs font-semibold ${
                                    val.riskClass === "High" ? "bg-red-500/10 text-red-400" : val.riskClass === "Low" ? "bg-emerald-500/10 text-emerald-400" : "bg-yellow-500/10 text-yellow-400"
                                  }`}>
                                    {val.riskClass}
                                  </span>
                                </td>
                                <td className="py-3.5 px-4 text-slate-400 text-xs">{val.lastAssessmentDate}</td>
                                <td className="py-3.5 px-4 text-right">
                                  <button onClick={() => deleteRiskProfile(key)} className="text-red-400 hover:text-red-300 p-1.5 hover:bg-red-500/10 rounded transition">
                                    <Trash2 className="w-4 h-4" />
                                  </button>
                                </td>
                              </tr>
                            ))}
                          </tbody>
                        </table>
                      </div>
                    )}
                  </div>
                )}
              </>
            )}
          </div>

          {/* Form Side panel */}
          <div className="bg-slate-850/40 border border-slate-800 rounded-2xl p-6">
            <h3 className="text-lg font-semibold text-white mb-5 flex items-center gap-2">
              <Plus className="w-5 h-5 text-indigo-400" /> Add Test Record
            </h3>

            {/* Form auth */}
            {activeTab === "auth" && (
              <form onSubmit={handleAddAuth} className="space-y-4">
                <div>
                  <label className="block text-xs font-semibold text-slate-400 mb-1.5">User Email</label>
                  <input required type="email" placeholder="saigiresh666@gmail.com" value={authForm.email} onChange={e=>setAuthForm({...authForm, email: e.target.value})} className="w-full bg-slate-900 border border-slate-800 text-white rounded-lg px-3 py-2 text-sm focus:border-indigo-500 outline-none" />
                </div>
                <div>
                  <label className="block text-xs font-semibold text-slate-400 mb-1.5">Full Name</label>
                  <input required type="text" placeholder="Sai Giresh" value={authForm.fullName} onChange={e=>setAuthForm({...authForm, fullName: e.target.value})} className="w-full bg-slate-900 border border-slate-800 text-white rounded-lg px-3 py-2 text-sm focus:border-indigo-500 outline-none" />
                </div>
                <div>
                  <label className="block text-xs font-semibold text-slate-400 mb-1.5">Password Hash</label>
                  <input required type="text" value={authForm.passwordHash} onChange={e=>setAuthForm({...authForm, passwordHash: e.target.value})} className="w-full bg-slate-900 border border-slate-800 text-white rounded-lg px-3 py-2 text-sm focus:border-indigo-500 outline-none" />
                </div>
                <button type="submit" className="w-full py-2 bg-indigo-600 hover:bg-indigo-550 text-white rounded-lg font-semibold text-sm transition mt-3">Add to auth_users</button>
              </form>
            )}

            {/* Form users */}
            {activeTab === "users" && (
              <form onSubmit={handleAddUser} className="space-y-4">
                <div>
                  <label className="block text-xs font-semibold text-slate-400 mb-1.5">User Email</label>
                  <input required type="email" placeholder="saigiresh666@gmail.com" value={userForm.email} onChange={e=>setUserForm({...userForm, email: e.target.value})} className="w-full bg-slate-900 border border-slate-800 text-white rounded-lg px-3 py-2 text-sm focus:border-indigo-500 outline-none" />
                </div>
                <div>
                  <label className="block text-xs font-semibold text-slate-400 mb-1.5">Full Name</label>
                  <input required type="text" placeholder="Sai Giresh" value={userForm.fullName} onChange={e=>setUserForm({...userForm, fullName: e.target.value})} className="w-full bg-slate-900 border border-slate-800 text-white rounded-lg px-3 py-2 text-sm focus:border-indigo-500 outline-none" />
                </div>
                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <label className="block text-xs font-semibold text-slate-400 mb-1.5">Age</label>
                    <input required type="number" value={userForm.age} onChange={e=>setUserForm({...userForm, age: e.target.value})} className="w-full bg-slate-900 border border-slate-800 text-white rounded-lg px-3 py-2 text-sm focus:border-indigo-500 outline-none" />
                  </div>
                  <div>
                    <label className="block text-xs font-semibold text-slate-400 mb-1.5">Occupation</label>
                    <input required type="text" value={userForm.occupation} onChange={e=>setUserForm({...userForm, occupation: e.target.value})} className="w-full bg-slate-900 border border-slate-800 text-white rounded-lg px-3 py-2 text-sm focus:border-indigo-500 outline-none" />
                  </div>
                </div>
                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <label className="block text-xs font-semibold text-slate-400 mb-1.5">Monthly Inc (₹)</label>
                    <input required type="number" value={userForm.monthlyIncome} onChange={e=>setUserForm({...userForm, monthlyIncome: e.target.value})} className="w-full bg-slate-900 border border-slate-800 text-white rounded-lg px-3 py-2 text-sm focus:border-indigo-500 outline-none" />
                  </div>
                  <div>
                    <label className="block text-xs font-semibold text-slate-400 mb-1.5">Monthly Exp (₹)</label>
                    <input required type="number" value={userForm.monthlyExpenses} onChange={e=>setUserForm({...userForm, monthlyExpenses: e.target.value})} className="w-full bg-slate-900 border border-slate-800 text-white rounded-lg px-3 py-2 text-sm focus:border-indigo-500 outline-none" />
                  </div>
                </div>
                <button type="submit" className="w-full py-2 bg-indigo-600 hover:bg-indigo-550 text-white rounded-lg font-semibold text-sm transition mt-3">Add to users</button>
              </form>
            )}

            {/* Form transactions */}
            {activeTab === "transactions" && (
              <form onSubmit={handleAddTx} className="space-y-4">
                <div>
                  <label className="block text-xs font-semibold text-slate-400 mb-1.5">User Email</label>
                  <input required type="email" placeholder="saigiresh666@gmail.com" value={txForm.email} onChange={e=>setTxForm({...txForm, email: e.target.value})} className="w-full bg-slate-900 border border-slate-800 text-white rounded-lg px-3 py-2 text-sm focus:border-indigo-500 outline-none" />
                </div>
                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <label className="block text-xs font-semibold text-slate-400 mb-1.5">Amount (₹)</label>
                    <input required type="number" value={txForm.amount} onChange={e=>setTxForm({...txForm, amount: e.target.value})} className="w-full bg-slate-900 border border-slate-800 text-white rounded-lg px-3 py-2 text-sm focus:border-indigo-500 outline-none" />
                  </div>
                  <div>
                    <label className="block text-xs font-semibold text-slate-400 mb-1.5">Type</label>
                    <select value={txForm.type} onChange={e=>setTxForm({...txForm, type: e.target.value})} className="w-full bg-slate-900 border border-slate-800 text-white rounded-lg px-3 py-2.5 text-sm focus:border-indigo-500 outline-none">
                      <option value="Expense">Expense</option>
                      <option value="Income">Income</option>
                    </select>
                  </div>
                </div>
                <div>
                  <label className="block text-xs font-semibold text-slate-400 mb-1.5">Category</label>
                  <select value={txForm.category} onChange={e=>setTxForm({...txForm, category: e.target.value})} className="w-full bg-slate-900 border border-slate-800 text-white rounded-lg px-3 py-2.5 text-sm focus:border-indigo-500 outline-none">
                    <option value="Food">Food</option>
                    <option value="Rent">Rent</option>
                    <option value="Shopping">Shopping</option>
                    <option value="Travel">Travel</option>
                    <option value="Entertainment">Entertainment</option>
                    <option value="Others">Others</option>
                  </select>
                </div>
                <div>
                  <label className="block text-xs font-semibold text-slate-400 mb-1.5">Note</label>
                  <input type="text" placeholder="Snacks" value={txForm.note} onChange={e=>setTxForm({...txForm, note: e.target.value})} className="w-full bg-slate-900 border border-slate-800 text-white rounded-lg px-3 py-2 text-sm focus:border-indigo-500 outline-none" />
                </div>
                <button type="submit" className="w-full py-2 bg-indigo-600 hover:bg-indigo-550 text-white rounded-lg font-semibold text-sm transition mt-3">Add Transaction</button>
              </form>
            )}

            {/* Form goals */}
            {activeTab === "goals" && (
              <form onSubmit={handleAddGoal} className="space-y-4">
                <div>
                  <label className="block text-xs font-semibold text-slate-400 mb-1.5">User Email</label>
                  <input required type="email" placeholder="saigiresh666@gmail.com" value={goalForm.email} onChange={e=>setGoalForm({...goalForm, email: e.target.value})} className="w-full bg-slate-900 border border-slate-800 text-white rounded-lg px-3 py-2 text-sm focus:border-indigo-500 outline-none" />
                </div>
                <div>
                  <label className="block text-xs font-semibold text-slate-400 mb-1.5">Goal Name</label>
                  <input required type="text" placeholder="Emergency Fund" value={goalForm.goalName} onChange={e=>setGoalForm({...goalForm, goalName: e.target.value})} className="w-full bg-slate-900 border border-slate-800 text-white rounded-lg px-3 py-2 text-sm focus:border-indigo-500 outline-none" />
                </div>
                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <label className="block text-xs font-semibold text-slate-400 mb-1.5">Target (₹)</label>
                    <input required type="number" value={goalForm.targetAmount} onChange={e=>setGoalForm({...goalForm, targetAmount: e.target.value})} className="w-full bg-slate-900 border border-slate-800 text-white rounded-lg px-3 py-2 text-sm focus:border-indigo-500 outline-none" />
                  </div>
                  <div>
                    <label className="block text-xs font-semibold text-slate-400 mb-1.5">Saved (₹)</label>
                    <input required type="number" value={goalForm.currentSavedAmount} onChange={e=>setGoalForm({...goalForm, currentSavedAmount: e.target.value})} className="w-full bg-slate-900 border border-slate-800 text-white rounded-lg px-3 py-2 text-sm focus:border-indigo-500 outline-none" />
                  </div>
                </div>
                <div>
                  <label className="block text-xs font-semibold text-slate-400 mb-1.5">Priority</label>
                  <select value={goalForm.priority} onChange={e=>setGoalForm({...goalForm, priority: e.target.value})} className="w-full bg-slate-900 border border-slate-800 text-white rounded-lg px-3 py-2.5 text-sm focus:border-indigo-500 outline-none">
                    <option value="High">High</option>
                    <option value="Medium">Medium</option>
                    <option value="Low">Low</option>
                  </select>
                </div>
                <button type="submit" className="w-full py-2 bg-indigo-600 hover:bg-indigo-550 text-white rounded-lg font-semibold text-sm transition mt-3">Add Goal</button>
              </form>
            )}

            {/* Form risk */}
            {activeTab === "risk" && (
              <form onSubmit={handleAddRisk} className="space-y-4">
                <div>
                  <label className="block text-xs font-semibold text-slate-400 mb-1.5">User Email</label>
                  <input required type="email" placeholder="saigiresh666@gmail.com" value={riskForm.email} onChange={e=>setRiskForm({...riskForm, email: e.target.value})} className="w-full bg-slate-900 border border-slate-800 text-white rounded-lg px-3 py-2 text-sm focus:border-indigo-500 outline-none" />
                </div>
                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <label className="block text-xs font-semibold text-slate-400 mb-1.5">Score (0-100)</label>
                    <input required type="number" min="0" max="100" value={riskForm.score} onChange={e=>setRiskForm({...riskForm, score: e.target.value})} className="w-full bg-slate-900 border border-slate-800 text-white rounded-lg px-3 py-2 text-sm focus:border-indigo-500 outline-none" />
                  </div>
                  <div>
                    <label className="block text-xs font-semibold text-slate-400 mb-1.5">Risk Class</label>
                    <select value={riskForm.riskClass} onChange={e=>setRiskForm({...riskForm, riskClass: e.target.value})} className="w-full bg-slate-900 border border-slate-800 text-white rounded-lg px-3 py-2.5 text-sm focus:border-indigo-500 outline-none">
                      <option value="Low">Low</option>
                      <option value="Medium">Medium</option>
                      <option value="High">High</option>
                    </select>
                  </div>
                </div>
                <button type="submit" className="w-full py-2 bg-indigo-600 hover:bg-indigo-550 text-white rounded-lg font-semibold text-sm transition mt-3">Add Risk Profile</button>
              </form>
            )}
          </div>
        </div>
      </div>
    </div>
  )
}
