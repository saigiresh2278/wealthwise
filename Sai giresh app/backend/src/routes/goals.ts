import { Router, Request, Response } from "express";
import { db } from "../config/firebase";

const router = Router();

const encodeEmail = (email: string): string => {
  return email.trim().toLowerCase().replace(/\./g, ",");
};

/**
 * @route GET /api/goals/:email
 * @desc Get all goals for a specific user
 */
router.get("/:email", async (req: Request, res: Response): Promise<void> => {
  const { email } = req.params;

  try {
    const emailKey = encodeEmail(email);
    const goalRef = db.ref("goals").child(emailKey);
    const snapshot = await goalRef.get();

    const goals: any[] = [];
    if (snapshot.exists()) {
      const val = snapshot.val();
      Object.keys(val).forEach(key => {
        if (val[key]) {
          goals.push(val[key]);
        }
      });
    }

    res.status(200).json(goals);
  } catch (error: any) {
    console.error("Get Goals Error:", error);
    res.status(500).json({ error: error.message });
  }
});

/**
 * @route POST /api/goals/:email
 * @desc Create/Sync financial goal(s) (supports single object or batch array)
 */
router.post("/:email", async (req: Request, res: Response): Promise<void> => {
  const { email } = req.params;
  const body = req.body;
  const emailKey = encodeEmail(email);

  if (Array.isArray(body)) {
    try {
      const dataMap: Record<string, any> = {};
      body.forEach((g: any) => {
        if (g.id) {
          dataMap[g.id.toString()] = {
            id: g.id,
            userEmail: email,
            goalName: g.goalName,
            targetAmount: Number(g.targetAmount || 0),
            currentSavedAmount: Number(g.currentSavedAmount || 0),
            targetDate: g.targetDate || new Date().toISOString().split("T")[0],
            priority: g.priority || "Medium"
          };
        }
      });
      await db.ref("goals").child(emailKey).set(dataMap);
      res.status(200).json({ message: "Goals batch synced successfully" });
    } catch (error: any) {
      console.error("Batch Save Goals Error:", error);
      res.status(500).json({ error: error.message });
    }
    return;
  }

  // Single goal path
  if (!body.id || !body.goalName || !body.targetAmount) {
    res.status(400).json({ error: "Missing goal fields (id, goalName, targetAmount)" });
    return;
  }

  try {
    const goalRef = db.ref("goals").child(emailKey).child(body.id.toString());
    const data = {
      id: body.id,
      userEmail: email,
      goalName: body.goalName,
      targetAmount: Number(body.targetAmount),
      currentSavedAmount: Number(body.currentSavedAmount || 0),
      targetDate: body.targetDate || new Date().toISOString().split("T")[0],
      priority: body.priority || "Medium"
    };

    await goalRef.set(data);
    res.status(201).json({ message: "Goal saved successfully", goal: data });
  } catch (error: any) {
    console.error("Save Goal Error:", error);
    res.status(500).json({ error: error.message });
  }
});

/**
 * @route PUT /api/goals/:email/:id
 * @desc Update a specific goal
 */
router.put("/:email/:id", async (req: Request, res: Response): Promise<void> => {
  const { email, id } = req.params;
  const updates = req.body;

  try {
    const emailKey = encodeEmail(email);
    const goalRef = db.ref("goals").child(emailKey).child(id);
    const snapshot = await goalRef.get();

    if (!snapshot.exists()) {
      res.status(404).json({ error: "Goal not found" });
      return;
    }

    await goalRef.update(updates);
    const updatedSnap = await goalRef.get();
    res.status(200).json({ message: "Goal updated successfully", goal: updatedSnap.val() });
  } catch (error: any) {
    console.error("Update Goal Error:", error);
    res.status(500).json({ error: error.message });
  }
});

/**
 * @route DELETE /api/goals/:email/:id
 * @desc Delete a specific goal
 */
router.delete("/:email/:id", async (req: Request, res: Response): Promise<void> => {
  const { email, id } = req.params;

  try {
    const emailKey = encodeEmail(email);
    const goalRef = db.ref("goals").child(emailKey).child(id);
    const snapshot = await goalRef.get();

    if (!snapshot.exists()) {
      res.status(404).json({ error: "Goal not found" });
      return;
    }

    await goalRef.remove();
    res.status(200).json({ message: "Goal deleted successfully" });
  } catch (error: any) {
    console.error("Delete Goal Error:", error);
    res.status(500).json({ error: error.message });
  }
});

export default router;
