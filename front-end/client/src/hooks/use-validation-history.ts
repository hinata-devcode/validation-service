import { useState, useEffect } from "react";

export interface ValidationHistoryItem {
  id: string;
  timestamp: number;
  type: "bank" | "vpa";
  lastKnownStatus: string;
}

const STORAGE_KEY = "validationHistory";
const MAX_ITEMS = 5;

export function useValidationHistory() {
  const [history, setHistory] = useState<ValidationHistoryItem[]>([]);
  const [isLoaded, setIsLoaded] = useState(false);

  // Load from localStorage on mount
  useEffect(() => {
    const stored = localStorage.getItem(STORAGE_KEY);
    if (stored) {
      try {
        setHistory(JSON.parse(stored));
      } catch (e) {
        console.error("Failed to parse validation history", e);
        setHistory([]);
      }
    }
    setIsLoaded(true);
  }, []);

  // Save to localStorage whenever history changes
  useEffect(() => {
    if (isLoaded) {
      localStorage.setItem(STORAGE_KEY, JSON.stringify(history));
    }
  }, [history, isLoaded]);

  const addValidation = (
    id: string,
    type: "bank" | "vpa",
    status: string = "PROCESSING"
  ) => {
    const newItem: ValidationHistoryItem = {
      id,
      timestamp: Date.now(),
      type,
      lastKnownStatus: status,
    };

    setHistory((prev) => [newItem, ...prev].slice(0, MAX_ITEMS));
  };

  const updateValidationStatus = (id: string, status: string) => {
    setHistory((prev) =>
      prev.map((item) =>
        item.id === id ? { ...item, lastKnownStatus: status } : item
      )
    );
  };

  const clearHistory = () => {
    setHistory([]);
    localStorage.removeItem(STORAGE_KEY);
  };

  return {
    history,
    addValidation,
    updateValidationStatus,
    clearHistory,
  };
}
