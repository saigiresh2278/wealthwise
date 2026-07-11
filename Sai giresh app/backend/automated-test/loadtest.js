/**
 * WealthWise AI — Baseline / Load Test
 * 100 virtual users · 60 seconds per endpoint
 * Results are written to an Excel sheet in real-time as each endpoint finishes
 */

const autocannon = require("autocannon");
const { generateExcelReport } = require("./generateReport");

const BASE_URL      = process.env.API_URL || "http://localhost:5000/api";
const CONNECTIONS   = parseInt(process.env.CONNECTIONS || "100");
const DURATION      = parseInt(process.env.DURATION    || "60");

// ─── Endpoints under test ────────────────────────────────────────────────────
const ENDPOINTS = [
  // Health
  { id: "HEALTH-001", group: "Health",        method: "GET",  path: "/",                                                          label: "GET / (Health Check)"            },
  // Auth
  { id: "AUTH-001",   group: "Auth",          method: "GET",  path: `/auth/profile/${encodeURIComponent("saigiresh666@gmail.com")}`, label: "GET /auth/profile (user 1)"      },
  { id: "AUTH-002",   group: "Auth",          method: "GET",  path: `/auth/profile/${encodeURIComponent("harsha123@gmail.com")}`,    label: "GET /auth/profile (user 2)"      },
  { id: "AUTH-003",   group: "Auth",          method: "GET",  path: `/auth/risk-profile/${encodeURIComponent("saigiresh666@gmail.com")}`, label: "GET /auth/risk-profile"  },
  { id: "AUTH-004",   group: "Auth",          method: "POST", path: `/auth/login`,
    body: JSON.stringify({ email: "saigiresh666@gmail.com", passwordHash: "test_hash" }), label: "POST /auth/login"               },
  { id: "AUTH-005",   group: "Auth",          method: "POST", path: `/auth/register`,
    body: JSON.stringify({ email: `loadtest_${Date.now()}@test.com`, fullName: "Load Tester", passwordHash: "hash_123" }), label: "POST /auth/register" },
  // Transactions
  { id: "TXN-001",    group: "Transactions",  method: "GET",  path: `/transactions/${encodeURIComponent("saigiresh666@gmail.com")}`,  label: "GET /transactions (user 1)"     },
  { id: "TXN-002",    group: "Transactions",  method: "GET",  path: `/transactions/${encodeURIComponent("harsha123@gmail.com")}`,     label: "GET /transactions (user 2)"     },
  { id: "TXN-003",    group: "Transactions",  method: "POST", path: `/transactions/${encodeURIComponent("saigiresh666@gmail.com")}`,
    body: JSON.stringify({ id: `lt_${Date.now()}`, amount: 250, category: "Food", type: "Expense", note: "Load test", date: new Date().toISOString().split("T")[0] }),
    label: "POST /transactions (add)"                                                                                                                                          },
  // Goals
  { id: "GOAL-001",   group: "Goals",         method: "GET",  path: `/goals/${encodeURIComponent("saigiresh666@gmail.com")}`,         label: "GET /goals (user 1)"            },
  { id: "GOAL-002",   group: "Goals",         method: "GET",  path: `/goals/${encodeURIComponent("harsha123@gmail.com")}`,            label: "GET /goals (user 2)"            },
  { id: "GOAL-003",   group: "Goals",         method: "POST", path: `/goals/${encodeURIComponent("saigiresh666@gmail.com")}`,
    body: JSON.stringify({ id: `gl_${Date.now()}`, goalName: "Load Test Goal", targetAmount: 10000, currentSavedAmount: 0, priority: "Medium", targetDate: "2027-01-01" }),
    label: "POST /goals (add)"                                                                                                                                                 },
  // Advisor
  { id: "ADV-001",    group: "Advisor",       method: "GET",  path: `/advisor/recommendations/${encodeURIComponent("saigiresh666@gmail.com")}`, label: "GET /advisor/recommendations (user 1)" },
  { id: "ADV-002",    group: "Advisor",       method: "GET",  path: `/advisor/recommendations/${encodeURIComponent("harsha123@gmail.com")}`,    label: "GET /advisor/recommendations (user 2)" },
];

// ─── Run one endpoint ─────────────────────────────────────────────────────────
function runEndpoint(ep) {
  return new Promise((resolve) => {
    const opts = {
      url:         BASE_URL + ep.path,
      method:      ep.method,
      headers:     { "Content-Type": "application/json" },
      connections: CONNECTIONS,
      duration:    DURATION,
      title:       ep.label,
      ...(ep.body && { body: ep.body }),
    };

    autocannon(opts, (err, result) => {
      const ts = new Date().toISOString();
      if (err || !result) {
        console.error(`[${ts}] ❌ ${ep.id} ERROR: ${err?.message}`);
        resolve({ ...ep, error: err?.message, status: "ERROR",
          rps: 0, avgLatencyMs: 0, minLatencyMs: 0, maxLatencyMs: 0,
          p50: 0, p75: 0, p90: 0, p99: 0,
          totalRequests: 0, errors: 1, non2xx: 0, throughput: 0,
          timestamp: ts });
        return;
      }

      const totalReqs  = result.requests.total;
      const errors     = result.errors + result.timeouts;
      const errorRate  = totalReqs > 0 ? (errors / totalReqs) * 100 : 100;
      const rps        = Math.round(result.requests.average);
      const avgLat     = Number((result.latency.mean || 0).toFixed(2));

      // SLA: avg latency < 1000ms, error rate < 5%, RPS >= 5
      const slaLat   = avgLat < 1000;
      const slaErr   = errorRate < 5;
      const slaRps   = rps >= 5;
      const status   = slaLat && slaErr && slaRps ? "PASS" : "FAIL";

      const r = {
        id:           ep.id,
        group:        ep.group,
        method:       ep.method,
        label:        ep.label,
        url:          BASE_URL + ep.path,
        connections:  CONNECTIONS,
        durationSec:  DURATION,
        totalRequests: totalReqs,
        rps,
        rpsMin:       Math.round(result.requests.min || 0),
        rpsMax:       Math.round(result.requests.max || 0),
        avgLatencyMs: avgLat,
        minLatencyMs: result.latency.min || 0,
        maxLatencyMs: result.latency.max || 0,
        p50:          result.latency.p50 || 0,
        p75:          result.latency.p75 || 0,
        p90:          result.latency.p90 || 0,
        p99:          result.latency.p99 || 0,
        throughput:   Math.round(result.throughput.average || 0),
        errors,
        non2xx:       result.non2xxResponses || 0,
        errorRatePct: errorRate.toFixed(2),
        slaLatency:   slaLat ? "✓ PASS" : "✗ FAIL",
        slaErrors:    slaErr ? "✓ PASS" : "✗ FAIL",
        slaRps:       slaRps ? "✓ PASS" : "✗ FAIL",
        status,
        timestamp:    ts,
      };

      const icon = status === "PASS" ? "✅" : "❌";
      console.log(`[${ts}] ${icon} ${r.id} | RPS: ${r.rps} | Avg: ${r.avgLatencyMs}ms | Min: ${r.minLatencyMs}ms | Max: ${r.maxLatencyMs}ms | Errors: ${r.errors} | ${r.status}`);
      resolve(r);
    });
  });
}

// ─── Main ─────────────────────────────────────────────────────────────────────
async function main() {
  console.log("╔══════════════════════════════════════════════════════════════╗");
  console.log("║       WealthWise AI — Baseline / Load Test Suite            ║");
  console.log(`║  Virtual Users : ${String(CONNECTIONS).padEnd(3)}  |  Duration: ${DURATION}s per endpoint    ║`);
  console.log(`║  Target        : ${BASE_URL.padEnd(40)} ║`);
  console.log(`║  Endpoints     : ${ENDPOINTS.length} total                                   ║`);
  console.log("╚══════════════════════════════════════════════════════════════╝\n");

  const results = [];

  for (const ep of ENDPOINTS) {
    process.stdout.write(`\n⏳ [${ep.id}] ${ep.method} ${ep.path} — running ${DURATION}s with ${CONNECTIONS} users...\n`);
    const r = await runEndpoint(ep);
    results.push(r);
    // Generate/update Excel after every single endpoint (real-time update)
    await generateExcelReport(results, {
      virtualUsers:     CONNECTIONS,
      durationSec:      DURATION,
      baseUrl:          BASE_URL,
      totalEndpoints:   ENDPOINTS.length,
      completedSoFar:   results.length,
      inProgress:       true,
    });
    console.log(`   📊 Excel updated (${results.length}/${ENDPOINTS.length} endpoints done)`);
  }

  // Final summary
  const passed  = results.filter(r => r.status === "PASS").length;
  const failed  = results.filter(r => r.status === "FAIL").length;
  const errored = results.filter(r => r.status === "ERROR").length;
  const totalReqs = results.reduce((s, r) => s + (r.totalRequests || 0), 0);
  const avgRps    = Math.round(results.reduce((s, r) => s + (r.rps || 0), 0) / results.length);
  const avgLat    = (results.reduce((s, r) => s + Number(r.avgLatencyMs || 0), 0) / results.length).toFixed(1);

  console.log("\n╔══════════════════════════════════════════════════════════════╗");
  console.log("║                   FINAL SUMMARY                             ║");
  console.log("╠══════════════════════════════════════════════════════════════╣");
  console.log(`║  Endpoints Tested  : ${String(results.length).padEnd(38)} ║`);
  console.log(`║  ✅ PASS           : ${String(passed).padEnd(38)} ║`);
  console.log(`║  ❌ FAIL           : ${String(failed).padEnd(38)} ║`);
  console.log(`║  ⚠️  ERROR          : ${String(errored).padEnd(37)} ║`);
  console.log(`║  Total Requests    : ${String(totalReqs.toLocaleString()).padEnd(38)} ║`);
  console.log(`║  Avg RPS           : ${String(avgRps + " req/s").padEnd(38)} ║`);
  console.log(`║  Avg Latency       : ${String(avgLat + "ms").padEnd(38)} ║`);
  console.log("╚══════════════════════════════════════════════════════════════╝");

  // Final Excel with complete summary
  await generateExcelReport(results, {
    virtualUsers: CONNECTIONS, durationSec: DURATION,
    baseUrl: BASE_URL, totalEndpoints: ENDPOINTS.length,
    completedSoFar: results.length, inProgress: false,
    passed, failed, errored, totalRequests: totalReqs, avgRps, avgLatency: avgLat,
  });

  console.log("\n✅ Final Excel report written to: reports/WealthWise_LoadTest_Report.xlsx");
  process.exit(failed + errored > 0 ? 1 : 0);
}

main().catch(err => { console.error(err); process.exit(1); });
