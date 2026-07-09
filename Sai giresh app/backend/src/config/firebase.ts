import * as admin from "firebase-admin";
import * as dotenv from "dotenv";

dotenv.config();

const databaseURL = process.env.FIREBASE_DATABASE_URL || "https://studentexpensetracking-default-rtdb.firebaseio.com";
const projectId = process.env.FIREBASE_PROJECT_ID || "studentexpensetracking";

const initializeFirebaseAdmin = (): admin.app.App => {
  // Option 1: Initialize using a local service account key file
  const serviceAccountPath = process.env.GOOGLE_APPLICATION_CREDENTIALS;
  if (serviceAccountPath) {
    try {
      console.log(`Attempting initialization via Service Account from path: ${serviceAccountPath}`);
      return admin.initializeApp({
        credential: admin.credential.cert(serviceAccountPath),
        databaseURL: databaseURL
      });
    } catch (certError: any) {
      console.error("Failed to initialize using Service Account certificate:", certError.message);
    }
  }

  // Option 2: Fallback using inline SERVICE_ACCOUNT_KEY env var
  const serviceAccountKeyJson = process.env.SERVICE_ACCOUNT_KEY;
  if (serviceAccountKeyJson) {
    try {
      console.log("Attempting initialization via inline SERVICE_ACCOUNT_KEY JSON string...");
      const serviceAccount = JSON.parse(serviceAccountKeyJson);
      return admin.initializeApp({
        credential: admin.credential.cert(serviceAccount),
        databaseURL: databaseURL
      });
    } catch (parseError: any) {
      console.error("Failed to parse SERVICE_ACCOUNT_KEY env variable:", parseError.message);
    }
  }

  // Option 3: Fallback using Application Default Credentials (ADC) or Project ID config
  try {
    console.log("Attempting initialization via Application Default Credentials (ADC)...");
    return admin.initializeApp({
      credential: admin.credential.applicationDefault(),
      databaseURL: databaseURL
    });
  } catch (adcError: any) {
    console.warn("ADC initialization failed. Initializing with Project ID fallback (read/write may require authentication settings):", adcError.message);
    return admin.initializeApp({
      projectId: projectId,
      databaseURL: databaseURL
    });
  }
};

const app = initializeFirebaseAdmin();
export const db = admin.database();
