"use client"

import { useEffect } from "react"
import { useRouter } from "next/navigation"
import { useAuth } from "@/context/AuthContext"
import { TrendingUp, Loader2 } from "lucide-react"

export default function Home() {
  const router = useRouter()
  const { isLoggedIn, user } = useAuth()

  useEffect(() => {
    if (isLoggedIn && user) {
      router.replace("/dashboard")
    }
  }, [isLoggedIn, user, router])

  return (
    <div className="min-h-screen flex items-center justify-center bg-[#0a0e1a]">
      <div className="flex flex-col items-center gap-4">
        <div className="w-16 h-16 rounded-2xl bg-gradient-to-br from-[#4f8cff] to-[#6366f1] flex items-center justify-center">
          <TrendingUp size={32} className="text-white" />
        </div>
        <div className="flex items-center gap-2 text-gray-400">
          <Loader2 size={18} className="animate-spin" />
          <span>Loading WealthWise AI...</span>
        </div>
      </div>
    </div>
  )
}
