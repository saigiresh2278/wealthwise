"""
WealthWise DAST — Rate Limiting Test (Category 7)
Sends a bounded burst of ~30 requests to /api/chat to confirm a limit exists.
"""
import json, time, datetime, requests, concurrent.futures

with open("input.json") as f:
    cfg = json.load(f)
BASE_URL = cfg["baseUrl"]

PAYLOAD = {
    "message": "rate limit probe",
    "profile": {"email": "t@x.com", "fullName": "T", "age": 25, "occupation": "E",
                "monthlyIncome": 1000, "monthlyExpenses": 500, "monthlySavings": 500,
                "mainFinancialGoal": "S", "riskComfort": "Low", "investmentExperience": "Beginner"},
    "transactions": [], "goals": [],
    "risk_profile": {"email": "t@x.com", "score": 25, "riskClass": "Low", "lastAssessmentDate": "2026-01-01"}
}

results = []
statuses = []

print("\n=== CATEGORY 7: Rate Limiting (30 burst requests) ===\n")

def send_req(i):
    url = BASE_URL + "/api/chat"
    start = time.time()
    try:
        r = requests.post(url, json=PAYLOAD, headers={"Content-Type": "application/json"}, timeout=10, stream=True)
        return i, r.status_code, round((time.time()-start)*1000, 2)
    except Exception as e:
        return i, 0, round((time.time()-start)*1000, 2)

# Send 30 requests with limited concurrency (5)
with concurrent.futures.ThreadPoolExecutor(max_workers=5) as executor:
    futures = [executor.submit(send_req, i) for i in range(30)]
    for f in concurrent.futures.as_completed(futures):
        i, status, elapsed = f.result()
        statuses.append(status)
        results.append({
            "endpoint": "/api/chat",
            "method": "POST",
            "role": "anonymous",
            "status": status,
            "expected_status": 429,
            "finding": False,
            "severity": "INFO",
            "response_time_ms": elapsed,
            "test_category": "Rate_Limiting",
            "note": f"Burst request #{i+1}",
            "timestamp": datetime.datetime.utcnow().isoformat()
        })

# Analyze — if no 429 was seen, rate limiting is missing
has_limit = any(s == 429 for s in statuses)
print(f"  Status distribution: { {s: statuses.count(s) for s in set(statuses)} }")
if not has_limit:
    print("  ✗ FINDING: No 429 responses observed — Rate limiting NOT enforced!")
    for r in results:
        r["finding"] = True
        r["severity"] = "HIGH"
        r["note"] += " — No rate limit enforced"
else:
    print("  ✓ Rate limiting is active — 429 responses observed.")

with open("results_ratelimit.json", "w") as f:
    json.dump(results, f, indent=2)
print(f"\n=== Rate Limiting: {len(results)} tests done ===")
