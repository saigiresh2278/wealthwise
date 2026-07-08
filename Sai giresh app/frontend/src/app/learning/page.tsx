"use client"

import { useState } from "react"
import { useRouter } from "next/navigation"
import { useAuth } from "@/context/AuthContext"
import { BookOpen, ChevronDown, ChevronUp, Bookmark, TrendingUp, Shield, PieChart, BarChart3, Target, DollarSign, LineChart, Briefcase, Home, GraduationCap } from "lucide-react"

const TOPICS = [
  {
    title: "Personal Finance Basics",
    icon: BookOpen,
    color: "text-blue-400",
    content: `Personal finance is about managing your money effectively. The key principles include:
• Budgeting: Track income and expenses using 50/30/20 rule
• Emergency Fund: Save 3-6 months of expenses
• Debt Management: Prioritize high-interest debt repayment
• Insurance: Protect yourself and your assets
• Investing: Grow your wealth over time

Start with a simple budget, build an emergency fund, and gradually expand your financial knowledge.`
  },
  {
    title: "Understanding Mutual Funds & SIPs",
    icon: TrendingUp,
    color: "text-green-400",
    content: `Mutual Funds pool money from multiple investors to invest in stocks, bonds, and other assets.

Key Concepts:
• NAV (Net Asset Value): Price per unit of a mutual fund
• SIP (Systematic Investment Plan): Invest fixed amounts regularly
• Lump Sum: Invest a large amount at once
• Expense Ratio: Annual fee charged by the fund

Benefits of SIP:
• Rupee cost averaging
• Power of compounding
• Disciplined investing habit
• Low minimum investment starting at ₹500`
  },
  {
    title: "Stock Market Fundamentals",
    icon: LineChart,
    color: "text-purple-400",
    content: `The stock market allows buying/selling shares of publicly listed companies.

Important Concepts:
• Stocks: Ownership in a company
• IPO: First time a company sells shares to public
• Dividend: Share of company profits paid to shareholders
• Market Cap: Total value of a company's shares
• PE Ratio: Price-to-Earnings ratio for valuation

Investment Strategies:
• Value Investing: Find undervalued stocks
• Growth Investing: Invest in high-growth companies
• Dividend Investing: Focus on regular income
• Index Investing: Track market indices like Nifty 50`
  },
  {
    title: "Tax Planning & Optimization",
    icon: DollarSign,
    color: "text-yellow-400",
    content: `Smart tax planning can save you significant money each year.

Tax Saving Options (India):
• Section 80C: Investments up to ₹1.5L (PPF, ELSS, NSC, etc.)
• Section 80D: Health insurance premiums
• Section 24(b): Home loan interest (up to ₹2L)
• HRA: House Rent Allowance exemption
• NPS: Additional ₹50,000 deduction

Tips:
• Start tax planning early in the financial year
• Diversify your 80C investments
• Don't invest solely for tax benefits - consider returns too`
  },
  {
    title: "Retirement Planning",
    icon: Shield,
    color: "text-red-400",
    content: `Retirement planning ensures financial independence in your golden years.

Key Steps:
1. Calculate your retirement corpus needs
2. Start early to leverage compounding
3. Use retirement-specific instruments (NPS, PPF, EPF)
4. Diversify across asset classes
5. Review and adjust regularly

The 4% Rule: You can safely withdraw 4% of your retirement corpus annually. Aim for a corpus that's 25x your annual expenses.`
  },
  {
    title: "Emergency Fund Planning",
    icon: Shield,
    color: "text-cyan-400",
    content: `An emergency fund is your financial safety net for unexpected events.

Guidelines:
• Amount: 3-6 months of essential expenses
• Where to keep: Savings account, liquid funds, or FD
• When to use: Job loss, medical emergency, major repair
• Replenish: Restore the fund after using it

Building Strategy:
• Start with a small goal (1 month expenses)
• Automate monthly transfers
• Use windfalls (bonuses, tax refunds)
• Keep it separate from your regular account`
  },
  {
    title: "Credit Score & Debt Management",
    icon: Briefcase,
    color: "text-orange-400",
    content: `Your credit score affects loan approvals and interest rates.

Understanding Credit Score:
• Range: 300-900 (750+ is excellent)
• Factors: Payment history (35%), Credit utilization (30%), Length (15%), Mix (10%), New credit (10%)

Debt Management Strategies:
• Avalanche method: Pay highest interest first
• Snowball method: Pay smallest balance first
• Debt consolidation: Combine multiple debts
• Balance transfer: Move to lower interest card

Tips: Pay bills on time, keep credit utilization below 30%, check credit report annually.`
  },
  {
    title: "Real Estate & Home Buying",
    icon: Home,
    color: "text-pink-400",
    content: `Buying a home is one of the biggest financial decisions you'll make.

Key Considerations:
• Location, location, location
• Budget: EMI should not exceed 40% of income
• Down payment: 20% of property value ideally
• Hidden costs: Registration, stamp duty, maintenance
• Home loan: Compare interest rates across banks

Rent vs Buy:
• Buy if staying 5+ years
• Rent if you need flexibility
• Consider opportunity cost of down payment`
  },
  {
    title: "Investment Strategies for Beginners",
    icon: PieChart,
    color: "text-indigo-400",
    content: `Start your investment journey with these beginner-friendly strategies.

Getting Started:
1. Index Funds/ETFs: Low-cost diversified exposure
2. Large-cap Mutual Funds: Invest in established companies
3. PPF/EPF: Risk-free with tax benefits
4. Gold ETFs: Hedge against inflation

Asset Allocation by Age:
• 20s: 80% equity, 20% debt
• 30s: 70% equity, 30% debt
• 40s: 60% equity, 40% debt
• 50s+: 40% equity, 60% debt

Rule of 100: Subtract your age from 100 for equity percentage.`
  },
  {
    title: "Financial Goal Setting",
    icon: Target,
    color: "text-teal-400",
    content: `Setting clear financial goals is the foundation of financial success.

SMART Goals Framework:
• Specific: "Save ₹5L for a car" not "Save money"
• Measurable: Track progress with numbers
• Achievable: Realistic given your income
• Relevant: Aligns with your life plans
• Time-bound: Set a deadline

Goal Categories:
• Short-term (1-3 years): Vacation, emergency fund
• Medium-term (3-7 years): Car, down payment
• Long-term (7+ years): Retirement, child's education

Prioritize goals and allocate savings accordingly.`
  },
]

export default function LearningHubPage() {
  const [expanded, setExpanded] = useState<number | null>(null)

  return (
    <div className="max-w-4xl mx-auto space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-white">Learning Hub</h1>
        <p className="text-gray-400 text-sm">Expand your financial knowledge</p>
      </div>

      <div className="space-y-3">
        {TOPICS.map((topic, i) => {
          const isOpen = expanded === i
          const Icon = topic.icon
          return (
            <div key={i} className="glass-card overflow-hidden transition-all">
              <button
                onClick={() => setExpanded(isOpen ? null : i)}
                className="w-full p-4 flex items-center justify-between text-left"
              >
                <div className="flex items-center gap-3">
                  <div className={`w-10 h-10 rounded-xl flex items-center justify-center ${topic.color.replace("text", "bg")}/10`}>
                    <Icon size={20} className={topic.color} />
                  </div>
                  <span className="text-white font-medium">{topic.title}</span>
                </div>
                {isOpen ? <ChevronUp size={18} className="text-gray-400" /> : <ChevronDown size={18} className="text-gray-400" />}
              </button>
              {isOpen && (
                <div className="px-4 pb-4 pt-0">
                  <div className="border-t border-white/5 pt-4">
                    <p className="text-gray-300 text-sm whitespace-pre-line leading-relaxed">{topic.content}</p>
                  </div>
                </div>
              )}
            </div>
          )
        })}
      </div>
    </div>
  )
}
