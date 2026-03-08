import type { Express } from "express";
import { createServer, type Server } from "http";

export async function registerRoutes(
  httpServer: Server,
  app: Express
): Promise<Server> {
  // The actual backend is an external Spring Boot service,
  // so we don't need to register local API routes here.
  // The frontend will communicate directly with the ngrok URL.
  return httpServer;
}
