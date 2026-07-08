"use client"

import { useState, useEffect } from "react"
import { useRouter } from "next/navigation"
import { useAuth } from "@/context/AuthContext"
import { getRiskProfile, saveRiskProfile, generateId } from "@/lib/store"
import { RISK_QUESTIONS, calculateRiskScore, RiskAnswer } from "@/lib/ai/riskAnalyzer"
import { Shield, ShieldAlert, ShieldCheck, ArrowLeft, RotateCcw } from "lucide-react"

export default function RiskAnalyzerPage() {
  const router = useRouter()
  const { user, isLoggedIn } = useAuth()
  const [answers, setAnswers] = useState<RiskAnswer[]>([])
  const [step, setStep] = useState(0)
  const [result, setResult] = useState<{ score: number; riskClass: "Low" | "Medium" | "High" } | null>(null)
  const [savedProfile, setSavedProfile] = useState<any>(null)

  useEffect(() => {
    if (!isLoggedIn) { router.replace("/login"); return }
    if (user) setSavedProfile(getRiskProfile(user.email))
  }, [isLoggedIn, user, router])

  const handleAnswer = (value: number) => {
    const newAnswers = [...answers]
    const existing = newAnswers.findIndex(a => a.questionIndex === step)
    if (existing >= 0) {
      newAnswers[existing] = { questionIndex: step, answer: value }
    } else {
      newAnswers.push({ questionIndex: step, answer: value })
    }
    setAnswers(newAnswers)

    if (step < RISK_QUESTIONS.length - 1) {
      setStep(step + 1)
    } else {
      const res = calculateRiskScore(newAnswers)
      setResult(res)
      if (user) {
        saveRiskProfile({ email: user.email, score: res.score, riskClass: res.riskClass, lastAssessmentDate: new Date().toISOString() })
      }
    }
  }

  const goBack = () => { if (step > 0) setStep(step - 1) }

  const reset = () => {
    setAnswers([])
    setStep(0)
    setResult(null)
  }

  const riskColors = { Low: "text-green-400 border-green-500/30 bg-green-500/10", Medium: "text-yellow-400 border-yellow-500/30 bg-yellow-500/10", High: "text-red-400 border-red-500/30 bg-red-500/10" }
  const riskIcons = { Low: ShieldCheck, Medium: ShieldAlert, High: ShieldAlert }
  const riskDescriptions = {
    Low: "You prefer safe, stable investments with guaranteed returns. Focus on fixed deposits, debt funds, and government bonds.",
    Medium: "You have a balanced approach. Consider a mix of debt and equity investments like balanced mutual funds.",
    High: "You're comfortable with market fluctuations for higher returns. Explore equity funds, stocks, and growth investments.",
  }

  if (result) {
    const Icon = riskIcons[result.riskClass]
    return (
      <div className="max-w-2xl mx-auto space-y-6">
        <h1 className="text-2xl font-bold text-white">Risk Assessment Results</h1>
        <div className={`glass-card p-8 text-center ${riskColors[result.riskClass]}`}>
          <Icon size={64} className="mx-auto mb-4" />
          <h2 className="text-2xl font-bold mb-1">{result.riskClass} Risk Tolerance</h2>
          <p className="text-5xl font-bold my-4">{result.score}/100</p>
          <p className="text-sm opacity-80">{riskDescriptions[result.riskClass]}</p>
        </div>
        <div className="flex gap-3">
          <button onClick={reset} className="gradient-btn flex items-center gap-2 px-5 py-2.5 rounded-xl text-white">
            <RotateCcw size={16} /> Retake Quiz
          </button>
          <button onClick={() => router.push("/advisor")} className="bg-white/5 text-white px-5 py-2.5 rounded-xl hover:bg-white/10 transition-colors">
            View AI Advice
          </button>
        </div>
      </div>
    )
  }

  return (
    <div className="max-w-2xl mx-auto space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-white">Risk Tolerance Quiz</h1>
        <p className="text-gray-400 text-sm">Answer 5 questions to determine your risk profile</p>
      </div>

      {savedProfile && !result && (
        <div className="glass-card p-4 flex items-center justify-between">
          <div>
            <p className="text-sm text-gray-400">Previous assessment:</p>
            <p className="text-white font-medium">{savedProfile.riskClass} risk ({savedProfile.score}/100)</p>
          </div>
          <button onClick={reset} className="text-[#4f8cff] text-sm hover:underline">Retake</button>
        </div>
      )}

      <div className="glass-card p-6">
        <div className="flex justify-between text-sm text-gray-500 mb-4">
          <span>Question {step + 1} of {RISK_QUESTIONS.length}</span>
          <span>{Math.round(((step + 1) / RISK_QUESTIONS.length) * 100)}%</span>
        </div>
        <div className="w-full h-1 bg-white/5 rounded-full mb-6">
          <div className="h-full bg-[#4f8cff] rounded-full transition-all" style={{ width: `${((step + 1) / RISK_QUESTIONS.length) * 100}%` }} />
        </div>

        <h2 className="text-lg text-white font-medium mb-4">{RISK_QUESTIONS[step].question}</h2>

        <div className="space-y-2">
          {RISK_QUESTIONS[step].options.map((opt, idx) => {
            const selected = answers.find(a => a.questionIndex === step)?.answer === idx
            return (
              <button key={idx} onClick={() => handleAnswer(idx)}
                className={`w-full text-left p-3 rounded-xl text-sm transition-all
                  ${selected ? "bg-[#4f8cff]/20 border border-[#4f8cff]/40 text-white" : "bg-white/5 border border-transparent text-gray-300 hover:bg-white/10"}`}>
                {opt}
              </button>
            )
          })}
        </div>

        {step > 0 && (
          <button onClick={goBack} className="mt-4 flex items-center gap-1 text-sm text-gray-400 hover:text-white transition-colors">
            <ArrowLeft size={14} /> Previous
          </button>
        )}
      </div>
    </div>
  )
}
