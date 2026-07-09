import { initializeApp, getApps, getApp } from "firebase/app";
import { getDatabase } from "firebase/database";
import { getAuth } from "firebase/auth";

const firebaseConfig = {
  apiKey: "AIzaSyCX923vjrdJY_e5RH11Z9qcMcky8aBGcz4",
  authDomain: "studentexpensetracking.firebaseapp.com",
  databaseURL: "https://studentexpensetracking-default-rtdb.firebaseio.com",
  projectId: "studentexpensetracking",
  storageBucket: "studentexpensetracking.firebasestorage.app",
  messagingSenderId: "626076205858",
  appId: "1:626076205858:web:b1d7d07936a287ce3818e3"
};

// Initialize Firebase
const app = getApps().length === 0 ? initializeApp(firebaseConfig) : getApp();
const db = getDatabase(app);
const auth = getAuth(app);

export { app, db, auth };
