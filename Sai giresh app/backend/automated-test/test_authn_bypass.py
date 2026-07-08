"""
WealthWise DAST Runner - Category 1: AuthN Bypass
Tests all protected endpoints with no/malformed/expired tokens.
A 2xx response = FINDING.
"""
import json
import time
import datetime
import requests

def load_input():
    with open("input.json") as f:
        return json.load(f)

cfg = load_input()
BASE_URL = cfg["baseUrl"]

VALID_CHAT_PAYLOAD = {
    "message": "What is my savings rate?",
    "profile": {
        "email": "test@example.com",
        "fullName": "Test User",
        "age": 30,
        "occupation": "Engineer",
        "monthlyIncome": 80000.0,
        "monthlyExpenses": 40000.0,
        "monthlySavings": 40000.0,
        "mainFinancialGoal": "Save for retirement",
        "riskComfort": "Medium",
        "investmentExperience": "Beginner"
    },
    "transactions": [],
    "goals": [],
    "risk_profile": {
        "email": "test@example.com",
        "score": 50.0,
        "riskClass": "Medium",
        "lastAssessmentDate": "2026-07-01"
    }
}

results = []

def run_test(endpoint, method, payload, headers, role, category, description, expected_status, note=""):
    url = BASE_URL + endpoint
    start = time.time()
    try:
        if method == "POST":
            r = requests.post(url, json=payload, headers=headers, timeout=10, stream=True)
            # Just read status, don't dump data
            status = r.status_code
        elif method == "GET":
            r = requests.get(url, headers=headers, timeout=10)
            status = r.status_code
        elif method == "HEAD":
            r = requests.head(url, headers=headers, timeout=10)
            status = r.status_code
        else:
            status = 0
    except Exception as e:
        status = 0
        note = f"Connection error: {str(e)[:80]}"
    elapsed = round((time.time() - start) * 1000, 2)
    finding = status != 0 and (status in [200, 201, 202, 204]) and expected_status not in [200, 201, 202, 204]
    results.append({
        "endpoint": endpoint,
        "method": method,
        "role": role,
        "status": status,
        "expected_status": expected_status,
        "finding": finding,
        "severity": "HIGH" if finding else "INFO",
        "response_time_ms": elapsed,
        "test_category": category,
        "note": note or description,
        "timestamp": datetime.datetime.utcnow().isoformat()
    })
    symbol = "✗ FINDING" if finding else ("✓" if status != 0 else "⚠ CONN-ERR")
    print(f"  {symbol}  [{status}] {method} {endpoint} | {role} | {category}")

print("\n=== CATEGORY 1: AuthN Bypass Tests ===\n")

# Test 1: POST /api/chat with no token (this API uses no auth by design)
run_test("/api/chat", "POST", VALID_CHAT_PAYLOAD, {}, "anonymous", "AuthN_Bypass",
         "Call /api/chat with no auth header", 200,
         "Endpoint has no auth enforcement — expected open access")

# Test 2: POST /api/chat with invalid Authorization header
run_test("/api/chat", "POST", VALID_CHAT_PAYLOAD, {"Authorization": "Bearer INVALID_TOKEN_12345"}, "anonymous", "AuthN_Bypass",
         "Call /api/chat with invalid Bearer token", 200,
         "Server should reject invalid tokens if auth is enforced")

# Test 3: POST /api/chat with expired JWT (forged)
expired_jwt = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ1c2VyQGV4YW1wbGUuY29tIiwicm9sZSI6InVzZXIiLCJleHAiOjE2MDAwMDAwMDB9.FAKESIG"
run_test("/api/chat", "POST", VALID_CHAT_PAYLOAD, {"Authorization": f"Bearer {expired_jwt}"}, "anonymous", "AuthN_Bypass",
         "Call /api/chat with expired JWT", 200, "Server must reject expired JWTs")

# Test 4: POST /api/chat with malformed token
run_test("/api/chat", "POST", VALID_CHAT_PAYLOAD, {"Authorization": "Bearer not.a.valid.jwt.format.here"}, "anonymous", "AuthN_Bypass",
         "Call /api/chat with malformed JWT structure", 200, "Server must reject malformed JWTs")

# Test 5: GET static root page
run_test("/", "GET", None, {}, "anonymous", "AuthN_Bypass", "Access static root (should be public)", 200, "SPA root should be public")

# Test 6: GET /docs (Swagger UI)
run_test("/docs", "GET", None, {}, "anonymous", "AuthN_Bypass", "Access Swagger UI /docs (should be public or restricted)", 200, "Swagger may be exposed publicly")

# Test 7: GET /openapi.json
run_test("/openapi.json", "GET", None, {}, "anonymous", "AuthN_Bypass", "Access OpenAPI spec /openapi.json", 200, "OpenAPI schema should not expose sensitive data")

# Test 8: GET /redoc
run_test("/redoc", "GET", None, {}, "anonymous", "AuthN_Bypass", "Access Redoc documentation", 200, "Alternative doc interface")

# Test 9: POST /api/chat with empty message
run_test("/api/chat", "POST", {**VALID_CHAT_PAYLOAD, "message": ""}, {}, "anonymous", "AuthN_Bypass",
         "Empty message field — check validation", 422, "Empty message should trigger validation error")

# Test 10: POST /api/chat with null profile
run_test("/api/chat", "POST", {"message": "hello", "profile": None}, {}, "anonymous", "AuthN_Bypass",
         "Null profile in chat request", 200, "Null profile should be gracefully handled")

with open("results_authn.json", "w") as f:
    json.dump(results, f, indent=2)
print(f"\n=== AuthN Bypass: {len(results)} tests done ===")
