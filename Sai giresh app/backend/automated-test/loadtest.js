/**
 * WealthWise AI - Baseline/Load Test Suite
 * 100 virtual users · 60 seconds · Measures RPS, latency, errors
 */

const autocannon = require("autocannon");
const { generateExcelReport } = require("./generateReport");

const BASE_URL = "http://localhost:5000/api";
const DURATION_SECONDS = 60;
const CONNECTIONS = 100; // 100 virtual users

// ─── Test user pool ───────────────────────────────────────────────────────────
const TEST_USERS = [
  { email: "saigiresh666@gmail.com", emailKey: "saigiresh666@gmail,com" },
  { email: "harsha123@gmail.com",    emailKey: "harsha123@gmail,com"    },
  { email: "student.test@gmail.com", emailKey: "student,test@gmail,com" },
];
const randomUser = () => TEST_USERS[Math.floor(Math.random() * TEST_USERS.length)];

// ─── Endpoint definitions (all endpoints under test) ─────────────────────────
const ENDPOINTS = [
  // Auth endpoints
  { id: "AUTH-001", group: "Auth", method: "GET",  path: `/auth/profile/${encodeURIComponent("saigiresh666@gmail.com")}`, label: "GET /auth/profile (user 1)" },
  { id: "AUTH-002", group: "Auth", method: "GET",  path: `/auth/profile/${encodeURIComponent("harsha123@gmail.com")}`,    label: "GET /auth/profile (user 2)" },
  { id: "AUTH-003", group: "Auth", method: "GET",  path: `/auth/profile/${encodeURIComponent("student.test@gmail.com")}`, label: "GET /auth/profile (user 3)" },
  { id: "AUTH-004", group: "Auth", method: "POST", path: `/auth/login`, body: JSON.stringify({ email: "saigiresh666@gmail.com", passwordHash: "hash_test" }), label: "POST /auth/login (user 1)" },
  { id: "AUTH-005", group: "Auth", method: "POST", path: `/auth/login`, body: JSON.stringify({ email: "harsha123@gmail.com", passwordHash: "hash_test" }),    label: "POST /auth/login (user 2)" },
  { id: "AUTH-006", group: "Auth", method: "POST", path: `/auth/register`, body: JSON.stringify({ email: "loadtest@example.com", fullName: "Load Tester", passwordHash: "hash_load" }), label: "POST /auth/register" },
  { id: "AUTH-007", group: "Auth", method: "GET",  path: `/auth/risk-profile/${encodeURIComponent("saigiresh666@gmail.com")}`, label: "GET /auth/risk-profile (user 1)" },
  { id: "AUTH-008", group: "Auth", method: "GET",  path: `/auth/risk-profile/${encodeURIComponent("harsha123@gmail.com")}`,    label: "GET /auth/risk-profile (user 2)" },

  // Transaction endpoints
  { id: "TXN-001",  group: "Transactions", method: "GET",  path: `/transactions/${encodeURIComponent("saigiresh666@gmail.com")}`, label: "GET /transactions (user 1)" },
  { id: "TXN-002",  group: "Transactions", method: "GET",  path: `/transactions/${encodeURIComponent("harsha123@gmail.com")}`,    label: "GET /transactions (user 2)" },
  { id: "TXN-003",  group: "Transactions", method: "GET",  path: `/transactions/${encodeURIComponent("student.test@gmail.com")}`, label: "GET /transactions (user 3)" },
  { id: "TXN-004",  group: "Transactions", method: "POST", path: `/transactions/${encodeURIComponent("saigiresh666@gmail.com")}`,
    body: JSON.stringify({ id: Date.now(), amount: 150, category: "Food", type: "Expense", note: "Load test", date: "2026-07-09" }),
    label: "POST /transactions single (user 1)" },
  { id: "TXN-005",  group: "Transactions", method: "POST", path: `/transactions/${encodeURIComponent("harsha123@gmail.com")}`,
    body: JSON.stringify({ id: Date.now(), amount: 500, category: "Travel", type: "Income", note: "Load test", date: "2026-07-09" }),
    label: "POST /transactions single (user 2)" },

  // Goal endpoints
  { id: "GOAL-001", group: "Goals", method: "GET",  path: `/goals/${encodeURIComponent("saigiresh666@gmail.com")}`, label: "GET /goals (user 1)" },
  { id: "GOAL-002", group: "Goals", method: "GET",  path: `/goals/${encodeURIComponent("harsha123@gmail.com")}`,    label: "GET /goals (user 2)" },
  { id: "GOAL-003", group: "Goals", method: "GET",  path: `/goals/${encodeURIComponent("student.test@gmail.com")}`, label: "GET /goals (user 3)" },
  { id: "GOAL-004", group: "Goals", method: "POST", path: `/goals/${encodeURIComponent("saigiresh666@gmail.com")}`,
    body: JSON.stringify({ id: Date.now(), goalName: "Load Test Goal", targetAmount: 50000, currentSavedAmount: 1000, priority: "Medium", targetDate: "2027-01-01" }),
    label: "POST /goals single (user 1)" },

  // Advisor endpoints
  { id: "ADV-001",  group: "Advisor", method: "GET",  path: `/advisor/recommendations/${encodeURIComponent("saigiresh666@gmail.com")}`, label: "GET /advisor/recommendations (user 1)" },
  { id: "ADV-002",  group: "Advisor", method: "GET",  path: `/advisor/recommendations/${encodeURIComponent("harsha123@gmail.com")}`,    label: "GET /advisor/recommendations (user 2)" },
];

// ─── Run a single endpoint load test ─────────────────────────────────────────
function runTest(endpoint) {
  return new Promise((resolve) => {
    const opts = {
      url: BASE_URL + endpoint.path,
      method: endpoint.method,
      headers: { "Content-Type": "application/json" },
      connections: CONNECTIONS,
      duration: DURATION_SECONDS,
      ...(endpoint.body && { body: endpoint.body }),
      title: endpoint.label,
    };

    autocannon(opts, (err, result) => {
      if (err) {
        console.error(`❌ Error on ${endpoint.id}: ${err.message}`);
        resolve({ ...endpoint, error: err.message });
        return;
      }

      const r = {
        id: endpoint.id,
        group: endpoint.group,
        method: endpoint.method,
        label: endpoint.label,
        path: BASE_URL + endpoint.path,
        connections: CONNECTIONS,
        durationSec: DURATION_SECONDS,
        totalRequests: result.requests.total,
        rps: Math.round(result.requests.average),
        rpsMin: Math.round(result.requests.min || 0),
        rpsMax: Math.round(result.requests.max || 0),
        avgLatencyMs: result.latency.mean.toFixed(2),
        minLatencyMs: result.latency.min,
        maxLatencyMs: result.latency.max,
        p50LatencyMs: result.latency.p50,
        p75LatencyMs: result.latency.p75,
        p90LatencyMs: result.latency.p90,
        p99LatencyMs: result.latency.p99,
        throughputBps: Math.round(result.throughput.average),
        errors: result.errors,
        timeouts: result.timeouts,
        non2xx: result.non2xxResponses,
        status: result.errors === 0 && result.non2xxResponses < result.requests.total * 0.05
          ? "PASS" : "FAIL",
      };

      console.log(`\n✅ ${r.id} - ${r.label}`);
      console.log(`   RPS: ${r.rps} req/s | Avg: ${r.avgLatencyMs}ms | Min: ${r.minLatencyMs}ms | Max: ${r.maxLatencyMs}ms | Errors: ${r.errors} | Status: ${r.status}`);
      resolve(r);
    });
  });
}

// ─── Main runner ─────────────────────────────────────────────────────────────
async function main() {
  console.log("╔══════════════════════════════════════════════════════════╗");
  console.log("║      WealthWise AI - Baseline / Load Test Suite         ║");
  console.log("╠══════════════════════════════════════════════════════════╣");
  console.log(`║  Virtual Users  : ${CONNECTIONS} concurrent connections          ║`);
  console.log(`║  Duration       : ${DURATION_SECONDS} seconds per endpoint              ║`);
  console.log(`║  Target         : ${BASE_URL}                ║`);
  console.log(`║  Endpoints      : ${ENDPOINTS.length} total                             ║`);
  console.log("╚══════════════════════════════════════════════════════════╝\n");

  const results = [];

  for (const endpoint of ENDPOINTS) {
    console.log(`\n🔄 Testing [${endpoint.id}] ${endpoint.method} ${endpoint.path}...`);
    const r = await runTest(endpoint);
    results.push(r);
  }

  console.log("\n\n══════════════════════════════════════════════════════════");
  console.log("  LOAD TEST SUMMARY");
  console.log("══════════════════════════════════════════════════════════");
  const passed = results.filter(r => r.status === "PASS").length;
  const failed = results.filter(r => r.status === "FAIL").length;
  const totalReqs = results.reduce((s, r) => s + (r.totalRequests || 0), 0);
  const avgRps = Math.round(results.reduce((s, r) => s + (r.rps || 0), 0) / results.length);
  const avgLat = (results.reduce((s, r) => s + Number(r.avgLatencyMs || 0), 0) / results.length).toFixed(2);
  console.log(`  Total Endpoints Tested : ${results.length}`);
  console.log(`  PASSED                 : ${passed}`);
  console.log(`  FAILED                 : ${failed}`);
  console.log(`  Total Requests Sent    : ${totalReqs.toLocaleString()}`);
  console.log(`  Average RPS            : ${avgRps} req/s`);
  console.log(`  Average Latency        : ${avgLat}ms`);
  console.log("══════════════════════════════════════════════════════════\n");

  // Generate Excel report
  await generateExcelReport(results, {
    virtualUsers: CONNECTIONS,
    durationSec: DURATION_SECONDS,
    baseUrl: BASE_URL,
    totalEndpoints: results.length,
    passed,
    failed,
    totalRequests: totalReqs,
    avgRps,
    avgLatency: avgLat,
  });
}

main().catch(console.error);
