import express, { Express, Request, Response } from "express";
import cors from "cors";
import dotenv from "dotenv";
import authRoutes from "./routes/auth";
import transactionRoutes from "./routes/transactions";
import goalRoutes from "./routes/goals";
import advisorRoutes from "./routes/advisor";
import "./config/firebase";

dotenv.config();

const app: Express = express();
const PORT = process.env.PORT || 5000;

app.use(cors({
  origin: "*",
  methods: ["GET", "POST", "PUT", "DELETE"],
  allowedHeaders: ["Content-Type", "Authorization"]
}));
app.use(express.json());

app.use((req, res, next) => {
  console.log(`[${new Date().toISOString()}] ${req.method} ${req.url}`);
  next();
});

app.use("/api/auth", authRoutes);
app.use("/api/transactions", transactionRoutes);
app.use("/api/goals", goalRoutes);
app.use("/api/advisor", advisorRoutes);

app.get("/", (req: Request, res: Response) => {
  res.status(200).json({
    status: "healthy",
    message: "WealthWise AI Backend API is running.",
    timestamp: new Date().toISOString(),
    firebaseInitialized: true
  });
});

app.use((err: any, req: Request, res: Response, next: any) => {
  console.error(err.stack);
  res.status(500).json({ error: "Internal Server Error", message: err.message });
});

app.listen(PORT, () => {
  console.log(`⚡️[server]: Backend server is running at http://localhost:${PORT}`);
});

export default app;
