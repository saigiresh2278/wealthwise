"use client"

import { useState } from "react"
import { useRouter } from "next/navigation"
import Link from "next/link"
import { useAuth } from "@/context/AuthContext"
import { TrendingUp, Mail, Lock, Eye, EyeOff } from "lucide-react"

export default function LoginPage() {
  const [email, setEmail] = useState("")
  const [password, setPassword] = useState("")
  const [showPwd, setShowPwd] = useState(false)
  const [error, setError] = useState("")
  const router = useRouter()
  const { login, loginWithGoogle, isLoggedIn } = useAuth()

  if (isLoggedIn) {
    router.replace("/dashboard")
    return null
  }

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault()
    setError("")
    if (!email || !password) { setError("Please fill all fields"); return }
    if (login(email, password)) {
      router.replace("/dashboard")
    } else {
      setError("Invalid email or password")
    }
  }

  const handleGoogleSignIn = async () => {
    setError("")
    const success = await loginWithGoogle()
    if (success) {
      router.replace("/dashboard")
    } else {
      setError("Failed to sign in with Google. Check console or credentials.")
    }
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-[#0a0e1a] p-4">
      <div className="w-full max-w-md">
        <div className="text-center mb-8">
          <div className="w-14 h-14 rounded-2xl bg-gradient-to-br from-[#4f8cff] to-[#6366f1] flex items-center justify-center mx-auto mb-4">
            <TrendingUp size={28} className="text-white" />
          </div>
          <h1 className="text-2xl font-bold text-white">Welcome Back</h1>
          <p className="text-gray-400 mt-1">Continue your financial journey</p>
        </div>

        <form onSubmit={handleSubmit} className="glass-card p-6 space-y-4">
          {error && (
            <div className="bg-red-500/10 border border-red-500/20 text-red-400 text-sm px-4 py-2.5 rounded-xl">
              {error}
            </div>
          )}

          <div>
            <label className="text-sm text-gray-400 mb-1.5 block">Email</label>
            <div className="relative">
              <Mail size={16} className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-500" />
              <input
                type="email" value={email} onChange={e => setEmail(e.target.value)}
                placeholder="your@email.com" className="w-full pl-10"
              />
            </div>
          </div>

          <div>
            <label className="text-sm text-gray-400 mb-1.5 block">Password</label>
            <div className="relative">
              <Lock size={16} className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-500" />
              <input
                type={showPwd ? "text" : "password"} value={password}
                onChange={e => setPassword(e.target.value)} placeholder="••••••••"
                className="w-full pl-10 pr-10"
              />
              <button type="button" onClick={() => setShowPwd(!showPwd)}
                className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-500">
                {showPwd ? <EyeOff size={16} /> : <Eye size={16} />}
              </button>
            </div>
          </div>

          <button type="submit" className="gradient-btn w-full py-2.5 rounded-xl text-white font-medium">
            Sign In
          </button>

          <div className="flex items-center my-4">
            <div className="flex-grow border-t border-white/10"></div>
            <span className="px-3 text-xs text-gray-500 uppercase">Or continue with</span>
            <div className="flex-grow border-t border-white/10"></div>
          </div>

          <button
            type="button"
            onClick={handleGoogleSignIn}
            className="w-full py-2.5 rounded-xl bg-white/5 border border-white/10 text-white font-medium hover:bg-white/10 transition flex items-center justify-center gap-2"
          >
            <svg className="w-4 h-4" viewBox="0 0 24 24">
              <path
                fill="#EA4335"
                d="M5.26620003,9.76453979 C6.19932007,6.93863986 8.85444018,4.90909091 12,4.90909091 C13.6909091,4.90909091 15.2181818,5.50909091 16.4181818,6.49090909 L19.9090909,3 C17.7818182,1.14545455 15.0545455,0 12,0 C7.29090909,0 3.25454545,2.72727273 1.28181818,6.68181818 L5.26620003,9.76453979 Z"
              />
              <path
                fill="#4285F4"
                d="M23.4900002,12.2727273 C23.4900002,11.4909091 23.418182,10.7363636 23.2909093,10 L12,10 L12,14.5454545 L18.4363638,14.5454545 C18.1636365,16.0181818 17.3272729,17.2727273 16.0818184,18.1090909 L19.9363637,21.0909091 C22.1909092,19.0090909 23.4900002,15.9272727 23.4900002,12.2727273 Z"
              />
              <path
                fill="#FBBC05"
                d="M1.28181818,6.68181818 C0.463636364,8.34545455 0,10.1909091 0,12.1181818 C0,14.0454545 0.463636364,15.8909091 1.28181818,17.5545455 L5.27726998,14.46886 C5.09918002,13.7259999 5,12.9330799 5,12.1181818 C5,11.3032837 5.09918002,10.5103637 5.27726998,9.76750357 L1.28181818,6.68181818 Z"
              />
              <path
                fill="#34A853"
                d="M12,19.2727273 C9.26363636,19.2727273 6.94545455,17.4454545 5.97272727,14.9090909 L1.98181818,17.9909091 C3.95454545,21.9454545 7.99090909,24.6363636 12,24.6363636 C15.0545455,24.6363636 17.7545455,23.6272727 19.9363637,21.0909091 L16.0818184,18.1090909 C15.0181818,18.8272727 13.6272727,19.2727273 12,19.2727273 Z"
              />
            </svg>
            Sign in with Google
          </button>

          <p className="text-center text-sm text-gray-400">
            Don&apos;t have an account?{" "}
            <Link href="/signup" className="text-[#4f8cff] hover:underline">Create one</Link>
          </p>
        </form>
      </div>
    </div>
  )
}
