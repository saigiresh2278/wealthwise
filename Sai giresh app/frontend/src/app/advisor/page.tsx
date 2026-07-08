"use client"

import { useEffect, useState } from "react"
import { useRouter } from "next/navigation"
import { useAuth } from "@/context/AuthContext"
import { getTransactions, getGoals, getRiskProfile } from "@/lib/store"
import { generateAdvice } from "@/lib/ai/advisorEngine"
import { Bot, AlertTriangle, Lightbulb, PiggyBank, TrendingUp, BookOpen, BarChart4, Shield, Sparkles } from "lucide-react"
import Link from "next/link"

export default function AdvisorPage() {
  const router = useRouter()
  const { user, profile, isLoggedIn } = useAuth()
  const [advice, setAdvice] = useState<any>(null)

  useEffect(() => {
    if (!isLoggedIn) { router.replace("/login"); return }
    if (user && profile) {
      const txns = getTransactions(user.email)
      const gs = getGoals(user.email)
      const risk = getRiskProfile(user.email)
      setAdvice(generateAdvice(profile, txns, gs, risk))
    }
  }, [isLoggedIn, user, profile, router])

  if (!profile) {
    return (
      <div className="max-w-4xl mx-auto text-center py-20">
        <Bot size={48} className="text-gray-600 mx-auto mb-3" />
        <h2 className="text-xl text-white font-semibold mb-2">Complete Your Profile First</h2>
        <p className="text-gray-400 mb-4">Set up your financial profile to get AI-powered advice</p>
        <Link href="/onboarding" className="gradient-btn inline-flex items-center gap-2 px-5 py-2.5 rounded-xl text-white">Complete Profile</Link>
      </div>
    )
  }

  return (
    <div className="max-w-6xl mx-auto space-y-6">
      <div className="flex items-center gap-3">
        <div className="w-10 h-10 rounded-xl bg-purple-500/10 flex items-center justify-center">
          <Sparkles size={20} className="text-purple-400" />
        </div>
        <div>
          <h1 className="text-2xl font-bold text-white">AI Financial Advisor</h1>
          <p className="text-gray-400 text-sm">Personalized recommendations powered by intelligence</p>
        </div>
      </div>

      {advice && (
        <>
          <div className="glass-card p-5">
            <div className="flex items-start gap-3">
              <Bot size={20} className="text-[#4f8cff] mt-0.5 shrink-0" />
              <p className="text-gray-300 leading-relaxed">{advice.overview}</p>
            </div>
          </div>

          {advice.alerts.length > 0 && (
            <div className="glass-card p-5 border-l-2 border-yellow-500">
              <h2 className="text-white font-semibold mb-3 flex items-center gap-2">
                <AlertTriangle size={18} className="text-yellow-400" /> Alerts
              </h2>
              <div className="space-y-2">
                {advice.alerts.map((a: string, i: number) => (
                  <div key={i} className="flex items-start gap-2 text-sm text-gray-300">
                    <span className="text-yellow-400 mt-0.5">•</span>
                    {a}
                  </div>
                ))}
              </div>
            </div>
          )}

          <div className="glass-card p-5">
            <h2 className="text-white font-semibold mb-3 flex items-center gap-2">
              <Lightbulb size={18} className="text-[#4f8cff]" /> Recommendations
            </h2>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
              {advice.recommendations.map((r: string, i: number) => (
                <div key={i} className="bg-white/5 rounded-xl p-3 flex items-start gap-2 text-sm text-gray-300">
                  <BarChart4 size={16} className="text-[#4f8cff] mt-0.5 shrink-0" /> {r}
                </div>
              ))}
            </div>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div className="glass-card p-5">
              <h2 className="text-white font-semibold mb-3 flex items-center gap-2">
                <PiggyBank size={18} className="text-cyan-400" /> Savings Ideas
              </h2>
              <div className="space-y-2">
                {advice.savingsIdeas.map((s: string, i: number) => (
                  <div key={i} className="flex items-start gap-2 text-sm text-gray-300">
                    <span className="text-cyan-400 mt-0.5">•</span> {s}
                  </div>
                ))}
              </div>
            </div>
            <div className="glass-card p-5">
              <h2 className="text-white font-semibold mb-3 flex items-center gap-2">
                <TrendingUp size={18} className="text-green-400" /> Income Ideas
              </h2>
              <div className="space-y-2">
                {advice.incomeIdeas.map((s: string, i: number) => (
                  <div key={i} className="flex items-start gap-2 text-sm text-gray-300">
                    <span className="text-green-400 mt-0.5">•</span> {s}
                  </div>
                ))}
              </div>
            </div>
          </div>

          <div className="glass-card p-5">
            <h2 className="text-white font-semibold mb-3 flex items-center gap-2">
              <BookOpen size={18} className="text-purple-400" /> Learning Path
            </h2>
            <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 gap-3">
              {advice.learningPath.map((l: string, i: number) => (
                <Link key={i} href="/learning"
                  className="bg-white/5 rounded-xl p-3 text-sm text-gray-300 hover:bg-white/10 transition-colors flex items-center gap-2">
                  <Shield size={14} className="text-purple-400" /> {l}
                </Link>
              ))}
            </div>
          </div>
        </>
      )}

      {!advice && (
        <div className="glass-card p-12 text-center">
          <Bot size={48} className="text-gray-600 mx-auto mb-3" />
          <p className="text-gray-400">Add transactions and goals to receive personalized advice</p>
        </div>
      )}
    </div>
  )
}
