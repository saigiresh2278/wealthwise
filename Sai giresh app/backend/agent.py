import os
import json
import logging
from typing import AsyncGenerator, Dict, Any, List
from google.antigravity import Agent, LocalAgentConfig

# Setup logger
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger("wealthwise.agent")

class FinancialTools:
    """
    Custom tools registered with the Google Antigravity Agent.
    These methods provide context about the user's financial profile,
    recent transactions, financial goals, and risk tolerance.
    """
    def __init__(
        self, 
        profile: Dict[str, Any], 
        transactions: List[Dict[str, Any]], 
        goals: List[Dict[str, Any]], 
        risk_profile: Dict[str, Any]
    ):
        self.profile = profile
        self.transactions = transactions
        self.goals = goals
        self.risk_profile = risk_profile

    def get_user_profile(self) -> Dict[str, Any]:
        """
        Retrieve the user's basic profile details (age, occupation, monthly income, monthly expenses, savings, main financial goal).
        Use this to understand their current income, expenses, and general background.
        """
        logger.info("Agent invoked tool: get_user_profile")
        return self.profile or {}

    def get_recent_transactions(self) -> List[Dict[str, Any]]:
        """
        Retrieve the list of recent transactions (income and expense details, amounts, categories, dates, notes).
        Use this to analyze spending habits or calculate exact numbers.
        """
        logger.info("Agent invoked tool: get_recent_transactions")
        return self.transactions or []

    def get_financial_goals(self) -> List[Dict[str, Any]]:
        """
        Retrieve the user's financial goals (goal name, target amount, current saved amount, target date, priority).
        Use this to check progress towards goals or analyze if they are achievable.
        """
        logger.info("Agent invoked tool: get_financial_goals")
        return self.goals or []

    def get_risk_profile(self) -> Dict[str, Any]:
        """
        Retrieve the user's risk tolerance profile (risk score, risk class: Low/Medium/High, last assessment date).
        Use this to tailor investment recommendations.
        """
        logger.info("Agent invoked tool: get_risk_profile")
        return self.risk_profile or {}


async def run_chat_session(
    message: str,
    profile: Dict[str, Any],
    transactions: List[Dict[str, Any]],
    goals: List[Dict[str, Any]],
    risk_profile: Dict[str, Any]
) -> AsyncGenerator[str, None]:
    """
    Instantiates a google-antigravity Agent with dynamic tools loaded with the user's context,
    runs the agentic chat loop, and yields text tokens in real time.
    Falls back to a local rules engine if GEMINI_API_KEY is not set.
    """
    api_key = os.getenv("GEMINI_API_KEY")
    if not api_key or api_key == "your_gemini_api_key_here":
        # Fallback to local rule-based simulation of the AI advisor
        logger.warning("GEMINI_API_KEY is not set. Falling back to local rules engine.")
        
        income = profile.get("monthlyIncome", 0)
        expenses = profile.get("monthlyExpenses", 0)
        savings = income - expenses
        savings_rate = (savings / income * 100) if income > 0 else 0
        expense_ratio = (expenses / income * 100) if income > 0 else 0
        
        advice = [
            f"### 📊 WealthWise AI Financial Analysis (Offline Local Mode)\n\n",
            f"Hello **{profile.get('fullName', 'User')}**, I am analyzing your finances using our local rules engine since `GEMINI_API_KEY` is not set.\n\n",
            f"#### 📈 Income & Expenses Overview\n",
            f"- **Monthly Income:** ₹{income:,.2f}\n",
            f"- **Monthly Expenses:** ₹{expenses:,.2f}\n",
            f"- **Savings Capacity:** ₹{savings:,.2f} per month\n",
            f"- **Savings Rate:** {savings_rate:.1f}% (Target: >20%)\n",
            f"- **Expense-to-Income Ratio:** {expense_ratio:.1f}% (Target: <50%)\n\n"
        ]
        
        if goals:
            advice.append("#### 🎯 Goal Progress Analysis\n")
            for g in goals:
                g_name = g.get("goalName", "Goal")
                target = g.get("targetAmount", 0)
                saved = g.get("currentSavedAmount", 0)
                pct = (saved / target * 100) if target > 0 else 0
                advice.append(f"- **{g_name}**: ₹{saved:,.0f} saved of ₹{target:,.0f} ({pct:.1f}% complete).\n")
            advice.append("\n")
            
        advice.append("#### 💡 Actionable Recommendations\n")
        if savings_rate < 20:
            advice.append("- ⚠️ **Low Savings Rate**: Your savings rate is below the standard 20%. Consider checking your transaction log and reducing discretionary costs (e.g. Food & Dining or Entertainment).\n")
        else:
            advice.append("- ✅ **Healthy Savings Habits**: Your savings rate is in a strong position! Automate your monthly savings to stay consistent.\n")
            
        if risk_profile:
            risk_class = risk_profile.get("riskClass", "Medium")
            advice.append(f"- 🛡️ **Investment Matching**: With your **{risk_class}** risk comfort, we suggest a balanced asset allocation (e.g. 50% index mutual funds, 50% stable government debt funds).\n")
            
        full_text = "".join(advice)
        
        # Simulate real-time streaming
        import asyncio
        for word in full_text.split(" "):
          yield word + " "
          await asyncio.sleep(0.02)
        return

    # Normal Agent Execution Flow
    # Create local tools instance loaded with current request context
    tools_instance = FinancialTools(profile, transactions, goals, risk_profile)
    
    # Define tool list for registration
    tools_list = [
        tools_instance.get_user_profile,
        tools_instance.get_recent_transactions,
        tools_instance.get_financial_goals,
        tools_instance.get_risk_profile
    ]

    # Setup the agent configuration
    config = LocalAgentConfig(
        system_instructions=(
            "You are WealthWise AI, an expert, empathetic, and premium financial advisor agent.\n"
            "Your goal is to provide highly personalized, data-backed financial advice, wealth growth strategies, "
            "and alert users to potential financial issues (such as high expense-to-income ratios or low savings rates).\n\n"
            "Use the provided tools to inspect the user's details. Do not guess user details if tools can provide them.\n"
            "Formulate detailed, actionable recommendations. Be encouraging, clear, and professional.\n"
            "Format your advice nicely using markdown (bullet points, bold text, etc.) so it reads well."
        ),
        tools=tools_list
    )

    try:
        # Start the Agent session
        async with Agent(config) as agent:
            # Perform the chat interaction
            response = await agent.chat(message)
            
            # Stream the tokens back
            async for token in response:
                yield token
    except Exception as e:
        logger.error(f"Error during agent session: {e}", exc_info=True)
        yield f"\n[Error during agent processing: {str(e)}]"
