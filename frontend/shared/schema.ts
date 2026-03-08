import { z } from "zod";

export const loginSchema = z.object({
  username: z.string().min(1, "Username is required"),
  password: z.string().min(1, "Password is required"),
});

export const signupSchema = loginSchema;

export const bankAccountValidationSchema = z.object({
  bank_account: z.object({
    account_number: z.string().min(1, "Account number is required"),
    ifsc: z.string().min(1, "IFSC is required"),
  }),
  user_details: z.object({
    name: z.string().min(1, "Name is required"),
    email: z.string().email("Invalid email"),
    phone: z.string().min(10, "Phone is required"),
  })
});

export const vpaValidationSchema = z.object({
  vpa: z.object({
    vpa_address: z.string().min(1, "VPA address is required"),
  }),
  user_details: z.object({
    name: z.string().min(1, "Name is required"),
    email: z.string().email("Invalid email"),
    phone: z.string().min(10, "Phone is required"),
  })
});

// Response types
export const validationInitialResponseSchema = z.object({
  executionStatus: z.string(), // "PROCESSING"
  validationRequestId: z.string(),
});

export const validationFinalResponseSchema = z.object({
  executionStatus: z.string(), // "COMPLETED", "FAILED", "PROVIDER_CALL_TIMEOUT"
  result: z.object({
    status: z.string().optional(), // "VALID" or "INVALID"
  }).optional(),
  confidenceLevel: z.string().optional(), // "LOW", "MEDIUM", "HIGH", "NOT_APPLICABLE"
  providerDetails: z.object({
    registeredName: z.string().optional(),
    nameMatchScore: z.number().optional(),
  }).nullable().optional(),
  bankDetailsJson: z.object({
    bank_name: z.string().optional(),
    account_type: z.string().optional(),
    account_number: z.string().optional(),
  }).nullable().optional(),
});
