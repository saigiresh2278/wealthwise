import express, { Express, Request, Response, NextFunction } from "express";
import cors from "cors";
import dotenv from "dotenv";
import compression from "compression";
import authRoutes from "./routes/auth";
import transactionRoutes from "./routes/transactions";
import goalRoutes from "./routes/goals";
import advisorRoutes from "./routes/advisor";
import "./config/firebase";

dotenv.config();

const app: Express = express();
const PORT = process.env.PORT || 5000;

// ─── In-memory cache ──────────────────────────────────────────────────────────
const cache = new Map<string, { data: any; expiry: number }>();
const CACHE_TTL_MS = 30_000; // 30 seconds

export function getCache(key: string): any | null {
  const entry = cache.get(key);
  if (!entry) return null;
  if (Date.now() > entry.expiry) { cache.delete(key); return null; }
  return entry.data;
}

export function setCache(key: string, data: any): void {
  cache.set(key, { data, expiry: Date.now() + CACHE_TTL_MS });
}

export function invalidateCache(pattern: string): void {
  for (const key of cache.keys()) {
    if (key.includes(pattern)) cache.delete(key);
  }
}

// ─── Middleware ───────────────────────────────────────────────────────────────
app.use(cors({
  origin: "*",
  methods: ["GET", "POST", "PUT", "DELETE", "OPTIONS"],
  allowedHeaders: ["Content-Type", "Authorization"]
}));

// Gzip compression — reduces payload size by 60-80% under load
app.use(compression());

// Parse JSON bodies up to 1mb
app.use(express.json({ limit: "1mb" }));

// HTTP keep-alive to reuse connections (critical for high concurrency)
app.use((req: Request, res: Response, next: NextFunction) => {
  res.setHeader("Connection", "keep-alive");
  res.setHeader("Keep-Alive", "timeout=30, max=1000");
  next();
});

// Request timeout — kill hanging requests after 10s
app.use((req: Request, res: Response, next: NextFunction) => {
  const timeout = setTimeout(() => {
    if (!res.headersSent) {
      res.status(503).json({ error: "Request timeout", message: "Server took too long to respond" });
    }
  }, 10_000);
  res.on("finish", () => clearTimeout(timeout));
  res.on("close",  () => clearTimeout(timeout));
  next();
});

// Minimal request logger (avoid console.log bottleneck under load)
let reqCount = 0;
app.use((req: Request, res: Response, next: NextFunction) => {
  reqCount++;
  if (reqCount % 100 === 0) {
    console.log(`[${new Date().toISOString()}] Processed ${reqCount} requests | Latest: ${req.method} ${req.url}`);
  }
  next();
});

// ─── Health endpoint (fast ping, no DB call) ──────────────────────────────────
app.get("/health", (_req: Request, res: Response) => {
  res.status(200).json({
    status: "healthy",
    timestamp: new Date().toISOString(),
    uptime: process.uptime().toFixed(2),
    cacheSize: cache.size,
    requestsServed: reqCount
  });
});

app.get("/", (_req: Request, res: Response) => {
  res.status(200).json({
    status: "healthy",
    message: "WealthWise AI Backend API is running.",
    timestamp: new Date().toISOString(),
    version: "2.0.0"
  });
});

// ─── Routes ──────────────────────────────────────────────────────────────────
app.use("/api/auth",         authRoutes);
app.use("/api/transactions", transactionRoutes);
app.use("/api/goals",        goalRoutes);
app.use("/api/advisor",      advisorRoutes);

// ─── 404 handler ─────────────────────────────────────────────────────────────
app.use((req: Request, res: Response) => {
  res.status(404).json({ error: "Route not found", path: req.url });
});

// ─── Global error handler ────────────────────────────────────────────────────
app.use((err: any, req: Request, res: Response, _next: NextFunction) => {
  console.error("Unhandled error:", err.message);
  if (!res.headersSent) {
    res.status(500).json({ error: "Internal Server Error", message: err.message });
  }
});

// ─── Start server ────────────────────────────────────────────────────────────
const server = app.listen(PORT, () => {
  console.log(`⚡️[server]: Backend server is running at http://localhost:${PORT}`);
  console.log(`✅ Cache TTL: ${CACHE_TTL_MS / 1000}s | Compression: ON | Keep-Alive: ON`);
});

// Increase keep-alive timeout on the HTTP server
server.keepAliveTimeout = 30_000;
server.headersTimeout   = 31_000;

export default app;
