"""
WealthWise DAST — Hardcoded Secrets Scanner (Category 8)
Scans the codebase for committed secrets/credentials not covered by .gitignore.
"""
import os, re, json, datetime, pathlib

SECRETS_PATTERNS = {
    "API_KEY_GENERIC": re.compile(r'(?i)(api[_\-]?key|apikey)\s*[=:]\s*["\']?([A-Za-z0-9\-_]{20,})["\']?'),
    "BEARER_TOKEN": re.compile(r'(?i)bearer\s+([A-Za-z0-9\-_\.]{20,})'),
    "AWS_ACCESS_KEY": re.compile(r'AKIA[0-9A-Z]{16}'),
    "AWS_SECRET_KEY": re.compile(r'(?i)(aws[_\-]?secret|secret_access_key)\s*[=:]\s*["\']?([A-Za-z0-9/+]{40})["\']?'),
    "PRIVATE_KEY": re.compile(r'-----BEGIN (RSA |EC )?PRIVATE KEY-----'),
    "GEMINI_KEY": re.compile(r'(?i)(gemini[_\-]?api[_\-]?key)\s*[=:]\s*["\']?([A-Za-z0-9\-_]{20,})["\']?'),
    "PASSWORD_LITERAL": re.compile(r'(?i)(password|passwd|pwd)\s*[=:]\s*["\']([^"\']{6,})["\']'),
    "HARDCODED_SECRET": re.compile(r'(?i)(secret[_\-]?key|secret)\s*[=:]\s*["\']([^"\']{8,})["\']'),
    "GENERIC_TOKEN": re.compile(r'(?i)(token|auth_token)\s*[=:]\s*["\']([A-Za-z0-9\-_\.]{20,})["\']'),
    "FIREBASE_CONFIG": re.compile(r'(?i)apiKey\s*:\s*["\']([A-Za-z0-9\-_]{20,})["\']'),
}

# False positive exclusions (template variables, test placeholders)
FP_PATTERNS = [
    "your_gemini_api_key_here",
    "process.env.",
    "os.getenv(",
    "process.env",
    "YOUR_",
    "REPLACE_",
    "placeholder",
    "example",
    "xxx",
    "<YOUR",
    "${",
]

SCAN_DIR = os.path.abspath(os.path.join(os.path.dirname(__file__), ".."))
SKIP_DIRS = {".git", "__pycache__", "node_modules", ".next", ".venv", "venv", "automated-test"}
SCAN_EXTS = {".py", ".js", ".ts", ".tsx", ".json", ".yaml", ".yml", ".env", ".env.local",
             ".kt", ".java", ".xml", ".txt", ".sh", ".bat", ".mjs", ".properties"}

findings = []
scanned_files = 0
scanned_lines = 0

print(f"\n=== CATEGORY 8: Hardcoded Secrets Scanner ===")
print(f"  Root: {SCAN_DIR}\n")

for root, dirs, files in os.walk(SCAN_DIR):
    # Prune skip dirs
    dirs[:] = [d for d in dirs if d not in SKIP_DIRS]
    for fname in files:
        ext = pathlib.Path(fname).suffix.lower()
        if ext not in SCAN_EXTS:
            continue
        fpath = os.path.join(root, fname)
        try:
            with open(fpath, "r", encoding="utf-8", errors="ignore") as f:
                lines = f.readlines()
            scanned_files += 1
            scanned_lines += len(lines)
            for lineno, line in enumerate(lines, 1):
                # Skip comment lines (basic check)
                stripped = line.strip()
                if stripped.startswith("#") or stripped.startswith("//"):
                    continue
                for pattern_name, pattern in SECRETS_PATTERNS.items():
                    match = pattern.search(line)
                    if match:
                        matched_text = match.group(0)
                        # Check for false positives
                        is_fp = any(fp.lower() in matched_text.lower() or fp.lower() in line.lower() for fp in FP_PATTERNS)
                        relative_path = os.path.relpath(fpath, SCAN_DIR)
                        severity = "INFO" if is_fp else "CRITICAL"
                        finding_flag = not is_fp
                        # Never print the actual secret value
                        masked = f"{matched_text[:15]}***REDACTED***" if len(matched_text) > 15 else "***REDACTED***"
                        findings.append({
                            "endpoint": f"FILE:{relative_path}",
                            "method": "STATIC_SCAN",
                            "role": "scanner",
                            "status": "FOUND",
                            "expected_status": "NONE",
                            "finding": finding_flag,
                            "severity": severity,
                            "response_time_ms": 0,
                            "test_category": "Hardcoded_Secrets",
                            "note": f"Pattern={pattern_name} Line={lineno} Match={masked} FP={is_fp}",
                            "timestamp": datetime.datetime.utcnow().isoformat()
                        })
                        sym = "✗ FINDING" if finding_flag else "⚠ FP-excluded"
                        print(f"  {sym} [{pattern_name}] {relative_path}:{lineno} — {masked}")
        except Exception as e:
            pass

print(f"\n  Files scanned: {scanned_files} | Lines scanned: {scanned_lines}")
print(f"  Total matches: {len(findings)} | Confirmed findings: {sum(1 for x in findings if x['finding'])}")

with open("results_secrets.json", "w") as f:
    json.dump(findings, f, indent=2)
print(f"\n=== Secrets Scan: {len(findings)} results saved ===")
