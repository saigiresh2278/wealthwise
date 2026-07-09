import { Router, Request, Response } from "express";
import { db } from "../config/firebase";

const router = Router();

const encodeEmail = (email: string): string => {
  return email.trim().toLowerCase().replace(/\./g, ",");
};

// Heuristic advice engine (replicated from frontend)
function generateRuleBasedAdvice(
  profile: any,
  transactions: any[],
  goals: any[],
  riskProfile: any
) {
  if (!profile) return null;

  const recentTxns = transactions.filter(t => {
    const d = new Date(t.date);
    const m = new Date();
    m.setMonth(m.getMonth() - 3);
    return d >= m;
  });
  const totalIncome = recentTxns.filter(t => t.type === "Income").reduce((s, t) => s + Number(t.amount || 0), 0);
  const totalExpenses = recentTxns.filter(t => t.type === "Expense").reduce((s, t) => s + Number(t.amount || 0), 0);
  const savingsRate = totalIncome > 0 ? ((totalIncome - totalExpenses) / totalIncome) * 100 : 0;

  const alerts: string[] = [];
  if (profile.monthlyExpenses > profile.monthlyIncome * 0.7) {
    alerts.push("Your expenses exceed 70% of income. Consider reducing discretionary spending.");
  }
  if (goals.some(g => {
    const t = new Date(g.targetDate);
    const now = new Date();
    const monthsLeft = (t.getFullYear() - now.getFullYear()) * 12 + (t.getMonth() - now.getMonth());
    return monthsLeft <= 3 && Number(g.currentSavedAmount || 0) < Number(g.targetAmount || 0);
  })) {
    alerts.push("You have goals approaching their deadline. Review progress immediately.");
  }
  if (savingsRate < 10 && totalIncome > 0) {
    alerts.push("Your savings rate is below 10%. Aim for at least 20%.");
  }
  if (!riskProfile) {
    alerts.push("Take the risk assessment quiz for personalized investment advice.");
  }

  const recommendations: string[] = [];
  if (riskProfile) {
    if (riskProfile.riskClass === "Low") {
      recommendations.push("Consider fixed deposits, debt funds, and government bonds.");
      recommendations.push("Build a diversified portfolio with 70% debt and 30% equity.");
    } else if (riskProfile.riskClass === "Medium") {
      recommendations.push("Consider balanced mutual funds with 50-50 debt-equity allocation.");
      recommendations.push("Explore index funds and blue-chip stocks for steady growth.");
    } else {
      recommendations.push("Consider aggressive growth funds, small-cap funds, and direct equity.");
      recommendations.push("International diversification can reduce overall portfolio risk.");
    }
  }
  recommendations.push("Maintain an emergency fund covering 3-6 months of expenses.");
  recommendations.push("Review and rebalance your portfolio every 6 months.");

  const savingsIdeas = [
    "Use the 50/30/20 rule: 50% needs, 30% wants, 20% savings.",
    "Automate transfers to a dedicated savings account on payday.",
    "Cancel unused subscriptions and memberships.",
    "Cook at home more often to reduce food expenses.",
  ];

  const incomeIdeas = [
    "Explore freelancing or consulting in your area of expertise.",
    "Invest in skill development for career growth.",
    "Consider passive income through dividend-paying stocks.",
    "Start a side hustle aligned with your interests.",
  ];

  const learningPath = [
    "Basics of Personal Finance",
    "Understanding Mutual Funds & SIPs",
    "Stock Market Fundamentals",
    "Tax Planning & Optimization",
    "Retirement Planning",
    "Estate Planning",
  ];

  return {
    overview: `Based on your profile, you have a savings rate of ${savingsRate.toFixed(1)}% with ${profile.monthlyIncome > 0 ? "a monthly income of ₹" + Number(profile.monthlyIncome).toLocaleString() : "no recorded income"}. ${goals.length > 0 ? "You are tracking " + goals.length + " financial goal(s)." : "You haven't set any financial goals yet."} ${riskProfile ? "Your risk tolerance is " + riskProfile.riskClass + "." : "Complete the risk assessment for personalized recommendations."}`,
    alerts,
    recommendations,
    savingsIdeas,
    incomeIdeas,
    learningPath,
  };
}

// Optional Gemini AI integration helper
async function generateGeminiAIAdvice(apiKey: string, profile: any, transactions: any[], goals: any[], risk: any): Promise<string | null> {
  try {
    const prompt = `
      You are WealthWise AI, a professional financial planning advisor. Analyze the following user financial data and provide a concise, high-impact summary (max 150 words) with actionable insights.
      
      User Profile:
      - Monthly Income: ₹${profile.monthlyIncome}
      - Monthly Expenses: ₹${profile.monthlyExpenses}
      - Occupation: ${profile.occupation}
      - Primary Goal: ${profile.mainFinancialGoal}
      - Risk Class: ${risk ? risk.riskClass : "Not assessed"} (Score: ${risk ? risk.score : "N/A"})
      
      Goals being tracked:
      ${goals.map(g => `- ${g.goalName}: Target ₹${g.targetAmount}, Saved ₹${g.currentSavedAmount}, Target Date: ${g.targetDate}`).join("\n")}
      
      Recent Transactions (last few records):
      ${transactions.slice(-5).map(t => `- [${t.type}] ${t.category}: ₹${t.amount} (${t.note})`).join("\n")}
      
      Provide structural feedback on:
      1. Budgeting and savings opportunity
      2. Goal progress
      3. Actionable investment tips suited to their risk tolerance.
    `;

    const response = await fetch(
      `https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=${apiKey}`,
      {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          contents: [{ parts: [{ text: prompt }] }]
        })
      }
    );

    if (!response.ok) {
      console.error("Gemini API call failed with status", response.status);
      return null;
    }

    const data = (await response.json()) as any;
    const generatedText = data.candidates?.[0]?.content?.parts?.[0]?.text;
    return generatedText || null;
  } catch (error) {
    console.error("Error generating Gemini advice:", error);
    return null;
  }
}

/**
 * @route GET /api/advisor/recommendations/:email
 * @desc Get automated + optional AI advisor recommendations
 */
router.get("/recommendations/:email", async (req: Request, res: Response): Promise<void> => {
  const { email } = req.params;

  try {
    const emailKey = encodeEmail(email);

    // Fetch user profile
    const profileSnap = await db.ref("users").child(emailKey).get();
    if (!profileSnap.exists()) {
      res.status(404).json({ error: "User profile not found. Please complete onboarding first." });
      return;
    }
    const profile = profileSnap.val();

    // Fetch transactions
    const txSnap = await db.ref("transactions").child(emailKey).get();
    const transactions: any[] = [];
    if (txSnap.exists()) {
      const val = txSnap.val();
      Object.keys(val).forEach(key => val[key] && transactions.push(val[key]));
    }

    // Fetch goals
    const goalsSnap = await db.ref("goals").child(emailKey).get();
    const goals: any[] = [];
    if (goalsSnap.exists()) {
      const val = goalsSnap.val();
      Object.keys(val).forEach(key => val[key] && goals.push(val[key]));
    }

    // Fetch risk profile
    const riskSnap = await db.ref("risk_profiles").child(emailKey).get();
    const riskProfile = riskSnap.exists() ? riskSnap.val() : null;

    // Generate rule-based recommendations
    const advice: any = generateRuleBasedAdvice(profile, transactions, goals, riskProfile);

    // Try generating Gemini AI recommendation if API key is present
    const geminiApiKey = process.env.GEMINI_API_KEY;
    if (geminiApiKey && advice) {
      const aiOverview = await generateGeminiAIAdvice(geminiApiKey, profile, transactions, goals, riskProfile);
      if (aiOverview) {
        advice.aiOverview = aiOverview;
      }
    }

    res.status(200).json(advice);
  } catch (error: any) {
    console.error("Advisor Engine Error:", error);
    res.status(500).json({ error: error.message });
  }
});

export default router;
