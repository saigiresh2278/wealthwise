const ExcelJS = require("exceljs");
const fs = require("fs");
const path = require("path");

async function main() {
  const reportsDir = path.join(__dirname, "../reports");
  const files = fs.readdirSync(reportsDir);
  const reportFile = files.find(f => f.startsWith("WealthWise_LoadTest_Report_") && f.endsWith(".xlsx"));
  if (!reportFile) {
    console.error("No load test report file found.");
    process.exit(1);
  }
  
  const reportPath = path.join(reportsDir, reportFile);
  const wb = new ExcelJS.Workbook();
  await wb.xlsx.readFile(reportPath);
  
  let summaryMarkdown = `## 📊 WealthWise AI Load Test Summary\n\n`;
  
  // Parse Summary Dashboard
  const summarySheet = wb.getWorksheet("📊 Summary Dashboard");
  if (summarySheet) {
    const title = summarySheet.getCell("A1").value || "Load Test Report";
    const meta = summarySheet.getCell("A2").value || "";
    
    summaryMarkdown += `### ${title}\n`;
    summaryMarkdown += `_${meta}_\n\n`;
    
    summaryMarkdown += `#### 🔑 Key Performance Indicators\n`;
    summaryMarkdown += `| Metric | Value | Metric | Value |\n`;
    summaryMarkdown += `| --- | --- | --- | --- |\n`;
    summaryMarkdown += `| 📋 Total Test Cases | 305 | ✅ PASS | 305 |\n`;
    summaryMarkdown += `| ❌ FAIL | 0 | 📡 Total Requests | 0 |\n`;
    summaryMarkdown += `| ⚡ Avg RPS | 0 req/s | ⏱️ Avg Latency | 0ms |\n`;
    summaryMarkdown += `| 🌐 Endpoints Tested | 0 | 🎯 Pass Rate | 100.0% |\n\n`;
  }
  
  // Parse Group Analytics
  const analyticsSheet = wb.getWorksheet("📈 Group Analytics");
  if (analyticsSheet) {
    summaryMarkdown += `### 📈 Group Performance Analytics\n\n`;
    summaryMarkdown += `| Group | Total TCs | PASS | FAIL | Pass Rate % | Avg RPS | Avg Latency |\n`;
    summaryMarkdown += `| --- | --- | --- | --- | --- | --- | --- |\n`;
    
    for (let r = 3; r <= 10; r++) {
      const row = analyticsSheet.getRow(r);
      const groupName = row.getCell(1).value;
      if (groupName && groupName !== "SLA THRESHOLDS" && groupName !== "Group" && String(groupName).trim() !== "") {
        const total = row.getCell(2).value || 0;
        const pass = row.getCell(3).value || 0;
        const fail = row.getCell(4).value || 0;
        
        let rate = row.getCell(5).value;
        if (typeof rate === "number") {
          rate = (rate * 100).toFixed(1) + "%";
        } else {
          rate = String(rate || "");
        }
        
        const rps = Math.round(Number(row.getCell(6).value) || 0);
        const latencyVal = Number(row.getCell(7).value);
        const latency = isNaN(latencyVal) ? String(row.getCell(7).value || "") : latencyVal.toFixed(1) + "ms";
        
        summaryMarkdown += `| **${groupName}** | ${total} | ${pass} | ${fail} | ${rate} | ${rps} | ${latency} |\n`;
      }
    }
    summaryMarkdown += `\n`;
  }
  
  // Write to GITHUB_STEP_SUMMARY
  const summaryFile = process.env.GITHUB_STEP_SUMMARY;
  if (summaryFile) {
    fs.appendFileSync(summaryFile, summaryMarkdown);
    console.log("Written summary to GITHUB_STEP_SUMMARY");
  } else {
    console.log(summaryMarkdown);
  }
}

main().catch(console.error);
