import { Router, Request, Response } from "express";
import { db } from "../config/firebase";

const router = Router();

const encodeEmail = (email: string): string => {
  return email.trim().toLowerCase().replace(/\./g, ",");
};

/**
 * @route POST /api/auth/register
 * @desc Sync or register a new user in firebase database
 */
router.post("/register", async (req: Request, res: Response): Promise<void> => {
  const { email, fullName, passwordHash } = req.body;

  if (!email || !fullName) {
    res.status(400).json({ error: "Missing required fields (email, fullName)" });
    return;
  }

  try {
    const emailKey = encodeEmail(email);
    const userRef = db.ref("users").child(emailKey);
    const authRef = db.ref("auth_users").child(emailKey);

    const userSnap = await userRef.get();
    const authSnap = await authRef.get();

    // Setup Auth credentials if not already stored
    if (!authSnap.exists()) {
      await authRef.set({
        email: email.trim().toLowerCase(),
        fullName: fullName.trim(),
        passwordHash: passwordHash || "demo_hash"
      });
    }

    // Setup base user profile if not already stored
    let profileData = {};
    if (!userSnap.exists()) {
      profileData = {
        email: email.trim().toLowerCase(),
        fullName: fullName.trim(),
        age: 25,
        occupation: "Student",
        monthlyIncome: 0,
        monthlyExpenses: 0,
        monthlySavings: 0,
        mainFinancialGoal: "Savings",
        riskComfort: "Medium",
        investmentExperience: "Beginner"
      };
      await userRef.set(profileData);
    } else {
      profileData = userSnap.val();
    }

    res.status(201).json({
      message: "Auth user synchronized successfully",
      profile: profileData
    });
  } catch (error: any) {
    console.error("Sync Auth Registration Error:", error);
    res.status(500).json({ error: error.message });
  }
});

/**
 * @route POST /api/auth/login
 * @desc Verify login credentials against the firebase users list
 */
router.post("/login", async (req: Request, res: Response): Promise<void> => {
  const { email, passwordHash } = req.body;

  if (!email || !passwordHash) {
    res.status(400).json({ error: "Missing login fields" });
    return;
  }

  try {
    const emailKey = encodeEmail(email);
    const authSnap = await db.ref("auth_users").child(emailKey).get();

    if (!authSnap.exists()) {
      res.status(404).json({ error: "User credentials not found" });
      return;
    }

    const credentials = authSnap.val();
    if (credentials.passwordHash !== passwordHash) {
      res.status(401).json({ error: "Invalid password credentials" });
      return;
    }

    // Retrieve user profile
    const userSnap = await db.ref("users").child(emailKey).get();
    res.status(200).json({
      message: "Login successful",
      profile: userSnap.exists() ? userSnap.val() : null
    });
  } catch (error: any) {
    console.error("Login Route Error:", error);
    res.status(500).json({ error: error.message });
  }
});

/**
 * @route GET /api/auth/profile/:email
 * @desc Get user onboarding profile details
 */
router.get("/profile/:email", async (req: Request, res: Response): Promise<void> => {
  const { email } = req.params;

  try {
    const emailKey = encodeEmail(email);
    const snapshot = await db.ref("users").child(emailKey).get();

    if (!snapshot.exists()) {
      res.status(404).json({ error: "User profile onboarding data not found" });
      return;
    }

    res.status(200).json(snapshot.val());
  } catch (error: any) {
    console.error("Get Profile Error:", error);
    res.status(500).json({ error: error.message });
  }
});

/**
 * @route POST /api/auth/profile/:email
 * @desc Create or update user onboarding profile
 */
router.post("/profile/:email", async (req: Request, res: Response): Promise<void> => {
  const { email } = req.params;
  const profile = req.body;

  try {
    const emailKey = encodeEmail(email);
    const userRef = db.ref("users").child(emailKey);

    const updatedProfile = {
      ...profile,
      email: email.trim().toLowerCase(),
      monthlySavings: Number(profile.monthlyIncome || 0) - Number(profile.monthlyExpenses || 0)
    };

    await userRef.set(updatedProfile);
    res.status(200).json({ message: "User profile saved successfully", profile: updatedProfile });
  } catch (error: any) {
    console.error("Update Profile Error:", error);
    res.status(500).json({ error: error.message });
  }
});

/**
 * @route DELETE /api/auth/profile/:email
 * @desc Reset and wipe user profile onboarding data
 */
router.delete("/profile/:email", async (req: Request, res: Response): Promise<void> => {
  const { email } = req.params;

  try {
    const emailKey = encodeEmail(email);
    await db.ref("users").child(emailKey).remove();
    res.status(200).json({ message: "User profile wiped successfully" });
  } catch (error: any) {
    console.error("Wipe Profile Error:", error);
    res.status(500).json({ error: error.message });
  }
});

/**
 * @route GET /api/auth/risk-profile/:email
 * @desc Get user risk assessment results
 */
router.get("/risk-profile/:email", async (req: Request, res: Response): Promise<void> => {
  const { email } = req.params;

  try {
    const emailKey = encodeEmail(email);
    const snapshot = await db.ref("risk_profiles").child(emailKey).get();

    if (!snapshot.exists()) {
      res.status(404).json({ error: "Risk profile not found" });
      return;
    }

    res.status(200).json(snapshot.val());
  } catch (error: any) {
    console.error("Get Risk Profile Error:", error);
    res.status(500).json({ error: error.message });
  }
});

/**
 * @route POST /api/auth/risk-profile/:email
 * @desc Save user risk assessment results
 */
router.post("/risk-profile/:email", async (req: Request, res: Response): Promise<void> => {
  const { email } = req.params;
  const risk = req.body;

  try {
    const emailKey = encodeEmail(email);
    const riskRef = db.ref("risk_profiles").child(emailKey);

    const updatedRisk = {
      ...risk,
      email: email.trim().toLowerCase(),
      lastAssessmentDate: new Date().toLocaleDateString()
    };

    await riskRef.set(updatedRisk);
    res.status(200).json({ message: "Risk profile saved successfully", riskProfile: updatedRisk });
  } catch (error: any) {
    console.error("Save Risk Profile Error:", error);
    res.status(500).json({ error: error.message });
  }
});

export default router;
