"""
WealthWise DAST — Rate Limiting Tests (Live)
Sends 30 burst requests and checks if 429 is ever returned.
"""
import json, time, requests, sys
import concurrent.futures

sys.stdout.reconfigure(encoding='utf-8', errors='replace')

with open("input.json") as f:
    cfg = json.load(f)
BASE_URL = cfg["baseUrl"]

PAYLOAD = {
    "message": "rate limit probe",
    "profile": {
        "email": "t@x.com", "fullName": "T", "age": 25, "occupation": "E",
        "monthlyIncome": 1000.0, "monthlyExpenses": 500.0, "monthlySavings": 500.0,
        "mainFinancialGoal": "S", "riskComfort": "Low", "investmentExperience": "Beginner"
    },
    "transactions": [], "goals": [],
    "risk_profile": {"email": "t@x.com", "score": 25.0, "riskClass": "Low", "lastAssessmentDate": "2026-01-01"}
}

results = []
statuses = []

print("\n=== CATEGORY 7: Rate Limiting (30 burst requests) ===\n")

def send_req(i):
    url = BASE_URL + "/api/chat"
    start = time.time()
    try:
        r = requests.post(url, json=PAYLOAD, timeout=12, stream=True)
        status = r.status_code
        for chunk in r.iter_content(chunk_size=128):
            break
        return i, status, round((time.time()-start)*1000, 2)
    except Exception as e:
        return i, 0, round((time.time()-start)*1000, 2)

with concurrent.futures.ThreadPoolExecutor(max_workers=5) as executor:
    futures = {executor.submit(send_req, i): i for i in range(30)}
    for future in concurrent.futures.as_completed(futures):
        i, status, elapsed = future.result()
        statuses.append(status)
        print(f"  Request #{i+1:>2}: [{status}] {elapsed:.0f}ms")

# Analysis
has_limit = any(s == 429 for s in statuses)
dist = {s: statuses.count(s) for s in sorted(set(statuses))}
print(f"\n  Status distribution: {dist}")

# Assign pass/fail per test
# PASS = server responded (200 or 429). 
# We expect 429 but since this API has no rate limiting, 200 = expected (not a test failure per se)
# A test FAILS if status == 0 (connection refused/timeout)
for i, status in enumerate(statuses):
    passed = status != 0
    results.append({
        "endpoint": "/api/chat",
        "method": "POST",
        "role": "anonymous",
        "status_code": status,
        "test_status": "Pass" if passed else "Fail",
        "finding": "YES" if not has_limit else "NO",
        "severity": "HIGH" if not has_limit else "INFO",
        "category": "Rate_Limiting",
        "note": f"Burst #{i+1} — {'No rate limit (finding)' if not has_limit else 'Rate limited'}"
    })

if not has_limit:
    print("  FINDING: No 429 observed — Rate limiting NOT enforced!")
else:
    print("  Rate limiting active — 429 responses observed.")

with open("results_ratelimit.json", "w", encoding="utf-8") as f:
    json.dump(results, f, indent=2)
total = len(results)
passed_count = sum(1 for r in results if r["test_status"] == "Pass")
print(f"\n  Total: {total}  Pass: {passed_count}  Fail: {total - passed_count}")
print(f"  Saved: results_ratelimit.json")
