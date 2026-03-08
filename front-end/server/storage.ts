// The backend is an external Spring Boot system, so we don't need local storage.
export interface IStorage {
  // Empty interface
}

export class MemStorage implements IStorage {
  constructor() {}
}

export const storage = new MemStorage();
