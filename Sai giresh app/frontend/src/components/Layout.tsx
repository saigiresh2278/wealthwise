"use client"

import { useState, useEffect } from "react"
import Link from "next/link"
import { usePathname, useRouter } from "next/navigation"
import { useAuth } from "@/context/AuthContext"
import {
  LayoutDashboard, ArrowLeftRight, Target, Bot, BarChart3, BookOpen,
  User, Settings, LogOut, Menu, X, TrendingUp, Moon, Sun, Loader2
} from "lucide-react"

const NAV_ITEMS = [
  { href: "/dashboard", label: "Dashboard", icon: LayoutDashboard },
  { href: "/transactions", label: "Transactions", icon: ArrowLeftRight },
  { href: "/goals", label: "Goals", icon: Target },
  { href: "/advisor", label: "AI Advisor", icon: Bot },
  { href: "/reports", label: "Reports", icon: BarChart3 },
  { href: "/learning", label: "Learn", icon: BookOpen },
  { href: "/profile", label: "Profile", icon: User },
  { href: "/settings", label: "Settings", icon: Settings },
]

export default function Layout({ children }: { children: React.ReactNode }) {
  const [sidebarOpen, setSidebarOpen] = useState(false)
  const pathname = usePathname()
  const router = useRouter()
  const { user, isLoggedIn, loading, logout, darkMode, toggleDarkMode } = useAuth()

  useEffect(() => {
    if (loading) return

    const authPaths = ["/login", "/signup"]
    const isAuthPath = authPaths.includes(pathname)

    if (!isLoggedIn && !isAuthPath) {
      router.replace("/login")
    } else if (isLoggedIn && (isAuthPath || pathname === "/")) {
      router.replace("/dashboard")
    }
  }, [loading, isLoggedIn, pathname, router])

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-[#0a0e1a]">
        <div className="flex flex-col items-center gap-4">
          <div className="w-16 h-16 rounded-2xl bg-gradient-to-br from-[#4f8cff] to-[#6366f1] flex items-center justify-center">
            <TrendingUp size={32} className="text-white animate-pulse" />
          </div>
          <div className="flex items-center gap-2 text-gray-400">
            <Loader2 size={18} className="animate-spin text-[#4f8cff]" />
            <span>Loading WealthWise AI...</span>
          </div>
        </div>
      </div>
    )
  }

  if (!isLoggedIn) {
    return <>{children}</>
  }

  const handleLogout = () => {
    logout()
    router.push("/login")
  }

  return (
    <div className="min-h-screen flex">
      {/* Mobile overlay */}
      {sidebarOpen && (
        <div className="fixed inset-0 bg-black/50 z-40 lg:hidden" onClick={() => setSidebarOpen(false)} />
      )}

      {/* Sidebar */}
      <aside className={`
        fixed lg:static inset-y-0 left-0 z-50 w-64 bg-[#0d1225] border-r border-[#1e2844]
        transform transition-transform duration-200
        ${sidebarOpen ? "translate-x-0" : "-translate-x-full lg:translate-x-0"}
      `}>
        <div className="p-5 border-b border-[#1e2844] flex items-center justify-between">
          <Link href="/dashboard" className="flex items-center gap-2">
            <div className="w-8 h-8 rounded-lg bg-gradient-to-br from-[#4f8cff] to-[#6366f1] flex items-center justify-center">
              <TrendingUp size={18} className="text-white" />
            </div>
            <span className="font-bold text-lg text-white">WealthWise</span>
          </Link>
          <button className="lg:hidden text-gray-400" onClick={() => setSidebarOpen(false)}>
            <X size={20} />
          </button>
        </div>

        <nav className="p-3 space-y-1">
          {NAV_ITEMS.map(item => {
            const isActive = pathname === item.href || pathname.startsWith(item.href + "/")
            const Icon = item.icon
            return (
              <Link
                key={item.href}
                href={item.href}
                onClick={() => setSidebarOpen(false)}
                className={`
                  flex items-center gap-3 px-4 py-2.5 rounded-xl text-sm transition-all
                  ${isActive
                    ? "bg-gradient-to-r from-[#4f8cff]/20 to-[#6366f1]/10 text-[#4f8cff] border border-[#4f8cff]/20"
                    : "text-gray-400 hover:text-white hover:bg-white/5"
                  }
                `}
              >
                <Icon size={18} />
                {item.label}
              </Link>
            )
          })}
        </nav>

        <div className="absolute bottom-0 left-0 right-0 p-4 border-t border-[#1e2844]">
          <div className="flex items-center justify-between mb-3">
            <button onClick={toggleDarkMode} className="p-2 rounded-lg hover:bg-white/5 text-gray-400 transition-colors">
              {darkMode ? <Sun size={18} /> : <Moon size={18} />}
            </button>
          </div>
          <div className="flex items-center gap-3 px-3 py-2 rounded-xl bg-white/5">
            <div className="w-8 h-8 rounded-full bg-[#4f8cff] flex items-center justify-center text-sm font-bold text-white">
              {user?.fullName?.charAt(0) || "U"}
            </div>
            <div className="flex-1 min-w-0">
              <p className="text-sm text-white truncate">{user?.fullName}</p>
              <p className="text-xs text-gray-500 truncate">{user?.email}</p>
            </div>
            <button onClick={handleLogout} className="p-1.5 rounded-lg hover:bg-red-500/10 text-gray-400 hover:text-red-400 transition-colors">
              <LogOut size={16} />
            </button>
          </div>
        </div>
      </aside>

      {/* Main */}
      <div className="flex-1 flex flex-col min-h-screen">
        {/* Mobile header */}
        <header className="lg:hidden flex items-center justify-between p-4 border-b border-[#1e2844] bg-[#0d1225]">
          <button onClick={() => setSidebarOpen(true)} className="text-gray-400">
            <Menu size={24} />
          </button>
          <div className="flex items-center gap-2">
            <div className="w-7 h-7 rounded-lg bg-gradient-to-br from-[#4f8cff] to-[#6366f1] flex items-center justify-center">
              <TrendingUp size={14} className="text-white" />
            </div>
            <span className="font-bold text-white">WealthWise</span>
          </div>
          <button onClick={toggleDarkMode} className="text-gray-400">
            {darkMode ? <Sun size={20} /> : <Moon size={20} />}
          </button>
        </header>

        <main className="flex-1 p-4 lg:p-6 overflow-auto">
          {children}
        </main>
      </div>
    </div>
  )
}
