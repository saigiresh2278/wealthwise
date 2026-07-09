"""
WealthWise Mock Backend Server
Standalone FastAPI server that mimics the real backend for DAST testing.
Does NOT require google-antigravity or GEMINI_API_KEY.
Runs on port 8000 and handles all real API routes.
"""
import json
import asyncio
import logging
from typing import Dict, Any, List, Optional
from fastapi import FastAPI, Request, HTTPException, status
from fastapi.responses import StreamingResponse, Response
from fastapi.staticfiles import StaticFiles
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel
import os

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger("wealthwise.mock")

app = FastAPI(title="WealthWise AI API Backend (Mock)")

# Same CORS config as real server — intentionally insecure for testing
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

class UserProfileModel(BaseModel):
    email: str
    fullName: str
    age: int
    occupation: str
    monthlyIncome: float
    monthlyExpenses: float
    monthlySavings: float
    mainFinancialGoal: str
    riskComfort: str
    investmentExperience: str

class TransactionModel(BaseModel):
    id: str
    userEmail: str
    amount: float
    category: str
    note: str
    date: str
    type: str

class GoalModel(BaseModel):
    id: str
    userEmail: str
    goalName: str
    targetAmount: float
    currentSavedAmount: float
    targetDate: str
    priority: str

class RiskProfileModel(BaseModel):
    email: str
    score: float
    riskClass: str
    lastAssessmentDate: str

class ChatRequest(BaseModel):
    message: str
    profile: Optional[UserProfileModel] = None
    transactions: Optional[List[TransactionModel]] = []
    goals: Optional[List[GoalModel]] = []
    risk_profile: Optional[RiskProfileModel] = None

@app.post("/api/chat")
async def chat_endpoint(request: ChatRequest):
    """Mock streaming response — identical signature to real backend."""
    profile = request.profile
    goals = request.goals or []

    income = profile.monthlyIncome if profile else 0
    expenses = profile.monthlyExpenses if profile else 0
    savings = income - expenses
    savings_rate = (savings / income * 100) if income > 0 else 0

    advice_parts = [
        f"### WealthWise AI Financial Analysis\n\n",
        f"Hello **{profile.fullName if profile else 'User'}**!\n\n",
        f"#### Income & Expenses\n",
        f"- **Monthly Income:** Rs.{income:,.2f}\n",
        f"- **Monthly Expenses:** Rs.{expenses:,.2f}\n",
        f"- **Savings Rate:** {savings_rate:.1f}%\n\n",
    ]

    if goals:
        advice_parts.append("#### Goal Progress\n")
        for g in goals:
            target = g.targetAmount
            saved = g.currentSavedAmount
            pct = (saved / target * 100) if target > 0 else 0
            advice_parts.append(f"- **{g.goalName}**: {pct:.1f}% complete\n")
        advice_parts.append("\n")

    advice_parts.append("#### Recommendations\n")
    if savings_rate < 20:
        advice_parts.append("- Increase your savings rate to at least 20%.\n")
    else:
        advice_parts.append("- Great savings habits! Keep it up.\n")

    async def event_generator():
        try:
            for part in advice_parts:
                for word in part.split(" "):
                    yield f"data: {json.dumps({'token': word + ' '})}\n\n"
                    await asyncio.sleep(0.005)
        except Exception as e:
            yield f"data: {json.dumps({'error': str(e)})}\n\n"

    return StreamingResponse(event_generator(), media_type="text/event-stream")

# Serve static files (if they exist)
static_dir = os.path.join(os.path.dirname(__file__), "static")
if os.path.exists(static_dir) and os.listdir(static_dir):
    app.mount("/", StaticFiles(directory=static_dir, html=True), name="static")

if __name__ == "__main__":
    import uvicorn
    uvicorn.run("mock_server:app", host="0.0.0.0", port=8000, reload=False)
