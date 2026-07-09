import { Router, Request, Response } from "express";
import { db } from "../config/firebase";

const router = Router();

const encodeEmail = (email: string): string => {
  return email.trim().toLowerCase().replace(/\./g, ",");
};

/**
 * @route GET /api/transactions/:email
 * @desc Get all transactions for a specific user
 */
router.get("/:email", async (req: Request, res: Response): Promise<void> => {
  const { email } = req.params;

  try {
    const emailKey = encodeEmail(email);
    const txRef = db.ref("transactions").child(emailKey);
    const snapshot = await txRef.get();

    const transactions: any[] = [];
    if (snapshot.exists()) {
      const val = snapshot.val();
      Object.keys(val).forEach(key => {
        if (val[key]) {
          transactions.push(val[key]);
        }
      });
    }

    res.status(200).json(transactions);
  } catch (error: any) {
    console.error("Get Transactions Error:", error);
    res.status(500).json({ error: error.message });
  }
});

/**
 * @route POST /api/transactions/:email
 * @desc Create/Sync transaction(s) (supports single object or batch array)
 */
router.post("/:email", async (req: Request, res: Response): Promise<void> => {
  const { email } = req.params;
  const body = req.body;
  const emailKey = encodeEmail(email);

  if (Array.isArray(body)) {
    try {
      const dataMap: Record<string, any> = {};
      body.forEach((t: any) => {
        if (t.id) {
          dataMap[t.id.toString()] = {
            id: t.id,
            userEmail: email,
            amount: Number(t.amount || 0),
            category: t.category || "Others",
            note: t.note || "",
            date: t.date || new Date().toISOString().split("T")[0],
            type: t.type || "Expense"
          };
        }
      });
      await db.ref("transactions").child(emailKey).set(dataMap);
      res.status(200).json({ message: "Transactions batch synced successfully" });
    } catch (error: any) {
      console.error("Batch Save Transactions Error:", error);
      res.status(500).json({ error: error.message });
    }
    return;
  }

  // Single transaction path
  if (!body.id || !body.amount || !body.category || !body.type) {
    res.status(400).json({ error: "Missing transaction fields (id, amount, category, type)" });
    return;
  }

  try {
    const txRef = db.ref("transactions").child(emailKey).child(body.id.toString());
    const data = {
      id: body.id,
      userEmail: email,
      amount: Number(body.amount),
      category: body.category,
      note: body.note || "",
      date: body.date || new Date().toISOString().split("T")[0],
      type: body.type
    };

    await txRef.set(data);
    res.status(201).json({ message: "Transaction saved successfully", transaction: data });
  } catch (error: any) {
    console.error("Save Transaction Error:", error);
    res.status(500).json({ error: error.message });
  }
});

/**
 * @route PUT /api/transactions/:email/:id
 * @desc Update a specific transaction
 */
router.put("/:email/:id", async (req: Request, res: Response): Promise<void> => {
  const { email, id } = req.params;
  const updates = req.body;

  try {
    const emailKey = encodeEmail(email);
    const txRef = db.ref("transactions").child(emailKey).child(id);
    const snapshot = await txRef.get();

    if (!snapshot.exists()) {
      res.status(404).json({ error: "Transaction not found" });
      return;
    }

    await txRef.update(updates);
    const updatedSnap = await txRef.get();
    res.status(200).json({ message: "Transaction updated successfully", transaction: updatedSnap.val() });
  } catch (error: any) {
    console.error("Update Transaction Error:", error);
    res.status(500).json({ error: error.message });
  }
});

/**
 * @route DELETE /api/transactions/:email/:id
 * @desc Delete a specific transaction
 */
router.delete("/:email/:id", async (req: Request, res: Response): Promise<void> => {
  const { email, id } = req.params;

  try {
    const emailKey = encodeEmail(email);
    const txRef = db.ref("transactions").child(emailKey).child(id);
    const snapshot = await txRef.get();

    if (!snapshot.exists()) {
      res.status(404).json({ error: "Transaction not found" });
      return;
    }

    await txRef.remove();
    res.status(200).json({ message: "Transaction deleted successfully" });
  } catch (error: any) {
    console.error("Delete Transaction Error:", error);
    res.status(500).json({ error: error.message });
  }
});

export default router;
