export interface RiskAnswer {
  questionIndex: number
  answer: number
}

export const RISK_QUESTIONS = [
  {
    question: "How would you react to a sudden 20% drop in your investments?",
    options: ["Sell everything immediately", "Sell some investments", "Hold and wait", "Buy more at lower price"],
  },
  {
    question: "What is your investment horizon?",
    options: ["Less than 1 year", "1-3 years", "3-7 years", "More than 7 years"],
  },
  {
    question: "Which statement best describes your investment knowledge?",
    options: ["No knowledge", "Basic knowledge", "Moderate knowledge", "Advanced knowledge"],
  },
  {
    question: "What percentage of your monthly income can you comfortably invest?",
    options: ["0-5%", "5-15%", "15-30%", "More than 30%"],
  },
  {
    question: "Your ideal investment return expectation per year is:",
    options: ["5-7% (Fixed deposits)", "8-10% (Debt funds)", "10-12% (Balanced funds)", "12-15%+ (Equity funds)"],
  },
]

export function calculateRiskScore(answers: RiskAnswer[]): { score: number; riskClass: "Low" | "Medium" | "High" } {
  if (answers.length === 0) return { score: 0, riskClass: "Low" }

  const total = answers.reduce((sum, a) => sum + (a.answer + 1), 0)
  const maxScore = RISK_QUESTIONS.length * 4
  const score = Math.round((total / maxScore) * 100)

  let riskClass: "Low" | "Medium" | "High"
  if (score < 40) riskClass = "Low"
  else if (score < 70) riskClass = "Medium"
  else riskClass = "High"

  return { score, riskClass }
}
