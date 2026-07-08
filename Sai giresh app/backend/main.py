import os
import json
import logging
from typing import Dict, Any, List, Optional
from fastapi import FastAPI, Request, HTTPException
from fastapi.responses import StreamingResponse
from fastapi.staticfiles import StaticFiles
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel
from dotenv import load_dotenv

# Load environment variables
load_dotenv()

# Configure logging
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger("wealthwise.main")

# Import the agent chat session runner
from agent import run_chat_session

app = FastAPI(title="WealthWise AI API Backend")

# Enable CORS for local testing on different ports if needed
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Pydantic models for incoming chat request
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
    """
    Exposes the google-antigravity agent streaming responses via SSE (Server-Sent Events).
    Receives prompt along with user's financial profile, transactions, goals, and risk profile.
    """
    # Extract data to dictionary format for the agent tools
    profile_dict = request.profile.dict() if request.profile else {}
    transactions_list = [t.dict() for t in request.transactions] if request.transactions else []
    goals_list = [g.dict() for g in request.goals] if request.goals else []
    risk_profile_dict = request.risk_profile.dict() if request.risk_profile else {}

    logger.info(f"Received chat request from {profile_dict.get('email', 'unknown_user')}")

    async def event_generator():
        try:
            async for token in run_chat_session(
                message=request.message,
                profile=profile_dict,
                transactions=transactions_list,
                goals=goals_list,
                risk_profile=risk_profile_dict
            ):
                # Yield SSE chunk
                yield f"data: {json.dumps({'token': token})}\n\n"
        except Exception as e:
            logger.error(f"Error in event_generator: {e}")
            yield f"data: {json.dumps({'error': str(e)})}\n\n"

    return StreamingResponse(event_generator(), media_type="text/event-stream")

# Serve the static files for the SPA frontend
# Ensure static directory exists
static_dir = os.path.join(os.path.dirname(__file__), "static")
os.makedirs(static_dir, exist_ok=True)

# Mount the static directory
app.mount("/", StaticFiles(directory=static_dir, html=True), name="static")

if __name__ == "__main__":
    import uvicorn
    # Load API key check on startup
    api_key = os.getenv("GEMINI_API_KEY")
    if not api_key:
        logger.warning("WARNING: GEMINI_API_KEY environment variable is not set. The agent may fail to run.")
    else:
        logger.info("GEMINI_API_KEY found in environment.")

    logger.info("Starting FastAPI server at http://localhost:8000")
    uvicorn.run("main:app", host="0.0.0.0", port=8000, reload=True)
