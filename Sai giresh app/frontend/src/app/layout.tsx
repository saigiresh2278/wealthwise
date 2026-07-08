import type { Metadata } from "next"
import { AuthProvider } from "@/context/AuthContext"
import Layout from "@/components/Layout"
import "./globals.css"

export const metadata: Metadata = {
  title: "WealthWise AI - Smart Financial Advisor",
  description: "Democratizing Wealth Management Through AI",
}

export default function RootLayout({ children }: { children: React.ReactNode }) {
  return (
    <html lang="en" className="dark">
      <body className="antialiased">
        <AuthProvider>
          <Layout>{children}</Layout>
        </AuthProvider>
      </body>
    </html>
  )
}
