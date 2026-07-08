// ====================================================================
// WealthWise AI Frontend Application Logic
// ====================================================================

// Default mock datasets matching the user's financial profile
let transactions = [
  { id: "t1", userEmail: "sai@example.com", amount: 80000, category: "Salary", note: "Monthly corporate salary", date: "2026-06-01", type: "Income" },
  { id: "t2", userEmail: "sai@example.com", amount: 20000, category: "Housing", note: "Apartment rent", date: "2026-06-05", type: "Expense" },
  { id: "t3", userEmail: "sai@example.com", amount: 8000, category: "Food & Dining", note: "Weekly grocery run", date: "2026-06-10", type: "Expense" },
  { id: "t4", userEmail: "sai@example.com", amount: 2000, category: "Entertainment", note: "Streaming subscriptions", date: "2026-06-12", type: "Expense" },
  { id: "t5", userEmail: "sai@example.com", amount: 5000, category: "Food & Dining", note: "Dinner with friends", date: "2026-06-18", type: "Expense" }
];

let goals = [
  { id: "g1", userEmail: "sai@example.com", goalName: "House Downpayment", targetAmount: 500000, currentSavedAmount: 150000, targetDate: "2031-06-01", priority: "High" },
  { id: "g2", userEmail: "sai@example.com", goalName: "Emergency Fund", targetAmount: 100000, currentSavedAmount: 60000, targetDate: "2026-12-31", priority: "High" }
];

// Load elements
const txnsListEl = document.getElementById("txns-list");
const goalsListEl = document.getElementById("goals-list");
const indSavingsEl = document.getElementById("indicator-savings-rate");
const indExpensesEl = document.getElementById("indicator-expense-ratio");
const chatMessagesEl = document.getElementById("chat-messages");
const chatInputEl = document.getElementById("chat-input");
const sendBtnEl = document.getElementById("send-btn");
const profileForm = document.getElementById("profile-form");

// Recalculate indicators
function recalculateIndicators() {
  const income = parseFloat(document.getElementById("monthlyIncome").value) || 0;
  const expenses = parseFloat(document.getElementById("monthlyExpenses").value) || 0;
  
  if (income > 0) {
    const savings = income - expenses;
    const savingsRate = Math.max(0, Math.round((savings / income) * 100));
    const expenseRatio = Math.min(100, Math.round((expenses / income) * 100));
    
    indSavingsEl.textContent = `${savingsRate}%`;
    indExpensesEl.textContent = `${expenseRatio}%`;
    
    // Color updates
    if (savingsRate < 15) {
      indSavingsEl.style.color = "var(--danger)";
    } else if (savingsRate < 25) {
      indSavingsEl.style.color = "var(--warning)";
    } else {
      indSavingsEl.style.color = "var(--success)";
    }

    if (expenseRatio > 75) {
      indExpensesEl.style.color = "var(--danger)";
    } else if (expenseRatio > 55) {
      indExpensesEl.style.color = "var(--warning)";
    } else {
      indExpensesEl.style.color = "var(--success)";
    }
  } else {
    indSavingsEl.textContent = "0%";
    indExpensesEl.textContent = "0%";
  }
}

// Render lists
function renderTransactions() {
  txnsListEl.innerHTML = "";
  transactions.forEach((txn) => {
    const row = document.createElement("div");
    row.className = "item-row";
    row.innerHTML = `
      <div>
        <strong>${txn.category}</strong>
        <div style="font-size: 0.65rem; color: var(--text-muted);">${txn.note}</div>
      </div>
      <div style="text-align: right;">
        <span class="amount ${txn.type.toLowerCase()}">${txn.type === "Income" ? "+" : "-"}₹${txn.amount.toLocaleString()}</span>
        <div style="font-size: 0.65rem; color: var(--text-muted);">${txn.date}</div>
      </div>
    `;
    txnsListEl.appendChild(row);
  });
}

// Render Goals
function renderGoals() {
  goalsListEl.innerHTML = "";
  goals.forEach((goal) => {
    const progressPercent = Math.min(100, Math.round((goal.currentSavedAmount / goal.targetAmount) * 100));
    const row = document.createElement("div");
    row.className = "item-row";
    row.style.flexDirection = "column";
    row.style.alignItems = "stretch";
    row.innerHTML = `
      <div style="display: flex; justify-content: space-between; margin-bottom: 4px;">
        <strong>${goal.goalName}</strong>
        <span style="font-weight: 600;">₹${goal.currentSavedAmount.toLocaleString()} / ₹${goal.targetAmount.toLocaleString()}</span>
      </div>
      <div style="width: 100%; height: 6px; background: rgba(255,255,255,0.05); border-radius: 3px; overflow: hidden; margin-bottom: 2px;">
        <div style="width: ${progressPercent}%; height: 100%; background: var(--accent-color); border-radius: 3px;"></div>
      </div>
      <div style="display: flex; justify-content: space-between; font-size: 0.65rem; color: var(--text-muted);">
        <span>Date: ${goal.targetDate}</span>
        <span>Priority: ${goal.priority}</span>
      </div>
    `;
    goalsListEl.appendChild(row);
  });
}

// Form change listeners
profileForm.addEventListener("input", recalculateIndicators);

// Mock add actions
document.getElementById("add-txn-btn").addEventListener("click", () => {
  const categories = ["Food & Dining", "Transportation", "Shopping", "Entertainment", "Utilities", "Other"];
  const category = categories[Math.floor(Math.random() * categories.length)];
  const amount = Math.floor(Math.random() * 4500) + 500;
  const newTxn = {
    id: "t_" + Date.now(),
    userEmail: "sai@example.com",
    amount: amount,
    category: category,
    note: "Mock card transaction",
    date: new Date().toISOString().split("T")[0],
    type: "Expense"
  };
  transactions.push(newTxn);
  
  // Dynamically update form monthly expenses
  const expenseInput = document.getElementById("monthlyExpenses");
  expenseInput.value = parseInt(expenseInput.value) + amount;
  
  renderTransactions();
  recalculateIndicators();
});

document.getElementById("add-goal-btn").addEventListener("click", () => {
  const goalNames = ["Buy Gadget", "Vacation Trip", "Car Fund", "Retirement savings"];
  const goalName = goalNames[Math.floor(Math.random() * goalNames.length)];
  const target = (Math.floor(Math.random() * 3) + 1) * 100000;
  const saved = Math.floor(Math.random() * (target / 2));
  const newGoal = {
    id: "g_" + Date.now(),
    userEmail: "sai@example.com",
    goalName: goalName,
    targetAmount: target,
    currentSavedAmount: saved,
    targetDate: new Date(Date.now() + 365*24*60*60*1000 * 2).toISOString().split("T")[0],
    priority: "Medium"
  };
  goals.push(newGoal);
  renderGoals();
});

// Markdown parser
function parseMarkdown(text) {
  // Simple regex parser for markdown formatting
  let html = text
    .replace(/\r\n/g, "\n")
    .replace(/\n\n+/g, "</p><p>")
    .replace(/\*\*(.*?)\*\*/g, "<strong>$1</strong>")
    .replace(/\*(.*?)\*/g, "<em>$1</em>")
    .replace(/`(.*?)`/g, "<code>$1</code>")
    // Bullet points
    .replace(/^\s*-\s+(.*?)$/gm, "<li>$1</li>")
    .replace(/(<li>.*?<\/li>)+/g, "<ul>$&</ul>")
    // Fix paragraph wrapping inside list tags
    .replace(/<p><ul>/g, "<ul>")
    .replace(/<\/ul><\/p>/g, "</ul>");

  // Ensure overall paragraph wrapper
  if (!html.startsWith("<p>") && !html.startsWith("<ul>") && !html.startsWith("<li>")) {
    html = `<p>${html}</p>`;
  }
  return html;
}

// Add chat message
function addMessage(sender, text, isHtml = false) {
  const msgDiv = document.createElement("div");
  msgDiv.className = `message ${sender}`;
  
  const avatarDiv = document.createElement("div");
  avatarDiv.className = "avatar";
  avatarDiv.textContent = sender === "user" ? "ME" : "AI";
  
  const bubbleDiv = document.createElement("div");
  bubbleDiv.className = "message-bubble";
  
  if (isHtml) {
    bubbleDiv.innerHTML = text;
  } else {
    bubbleDiv.textContent = text;
  }
  
  msgDiv.appendChild(avatarDiv);
  msgDiv.appendChild(bubbleDiv);
  chatMessagesEl.appendChild(msgDiv);
  chatMessagesEl.scrollTop = chatMessagesEl.scrollHeight;
  return bubbleDiv;
}

// Handle chat send streaming
async function handleSend(textToSend = null) {
  const query = textToSend || chatInputEl.value.trim();
  if (!query) return;
  
  if (!textToSend) {
    chatInputEl.value = "";
  }

  // Add User Message
  addMessage("user", query);

  // Add Bot Message with typing indicator
  const botBubble = addMessage("bot", `<div class="typing-indicator"><span></span><span></span><span></span></div>`, true);

  // Compile context data to send to FastAPI backend
  const profile = {
    email: document.getElementById("email").value,
    fullName: document.getElementById("fullName").value,
    age: parseInt(document.getElementById("age").value) || 20,
    occupation: document.getElementById("occupation").value,
    monthlyIncome: parseFloat(document.getElementById("monthlyIncome").value) || 0,
    monthlyExpenses: parseFloat(document.getElementById("monthlyExpenses").value) || 0,
    monthlySavings: (parseFloat(document.getElementById("monthlyIncome").value) || 0) - (parseFloat(document.getElementById("monthlyExpenses").value) || 0),
    mainFinancialGoal: document.getElementById("mainFinancialGoal").value,
    riskComfort: document.getElementById("riskComfort").value,
    investmentExperience: document.getElementById("investmentExperience").value
  };

  const risk_profile = {
    email: profile.email,
    score: profile.riskComfort === "Low" ? 25 : profile.riskComfort === "Medium" ? 50 : 75,
    riskClass: profile.riskComfort,
    lastAssessmentDate: new Date().toISOString().split("T")[0]
  };

  const payload = {
    message: query,
    profile: profile,
    transactions: transactions,
    goals: goals,
    risk_profile: risk_profile
  };

  let fullResponseText = "";

  try {
    const response = await fetch("/api/chat", {
      method: "POST",
      headers: {
        "Content-Type": "application/json"
      },
      body: JSON.stringify(payload)
    });

    if (!response.ok) {
      throw new Error(`HTTP error! Status: ${response.status}`);
    }

    // Process Readable Stream (SSE)
    const reader = response.body.getReader();
    const decoder = new TextDecoder("utf-8");
    botBubble.innerHTML = ""; // Remove typing indicator
    
    let buffer = "";

    while (true) {
      const { value, done } = await reader.read();
      if (done) break;

      buffer += decoder.decode(value, { stream: true });
      const lines = buffer.split("\n\n");
      
      // Save last unfinished line back to buffer
      buffer = lines.pop();

      for (const line of lines) {
        if (line.startsWith("data: ")) {
          try {
            const data = JSON.parse(line.slice(6));
            if (data.token) {
              fullResponseText += data.token;
              botBubble.innerHTML = parseMarkdown(fullResponseText);
              chatMessagesEl.scrollTop = chatMessagesEl.scrollHeight;
            } else if (data.error) {
              botBubble.innerHTML += `<div style="color: var(--danger); font-size: 0.85rem; margin-top: 5px;">[Error: ${data.error}]</div>`;
            }
          } catch (e) {
            console.error("JSON parsing error in chunk:", e, "Line:", line);
          }
        }
      }
    }
  } catch (error) {
    console.error("Fetch/Streaming error:", error);
    botBubble.innerHTML = `<span style="color: var(--danger);">Failed to connect to agent backend. Make sure the server is running on localhost:8000 and the API keys are configured.</span>`;
  }
}

// Attach Event Listeners
sendBtnEl.addEventListener("click", () => handleSend());
chatInputEl.addEventListener("keydown", (e) => {
  if (e.key === "Enter") {
    handleSend();
  }
});

// Clickable prompts
document.querySelectorAll(".prompt-btn").forEach((btn) => {
  btn.addEventListener("click", (e) => {
    const prompt = e.target.getAttribute("data-prompt");
    handleSend(prompt);
  });
});

// Initialize on page load
document.addEventListener("DOMContentLoaded", () => {
  renderTransactions();
  renderGoals();
  recalculateIndicators();
});
