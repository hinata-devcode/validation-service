import { z } from 'zod';
import {
  loginSchema,
  signupSchema,
  bankAccountValidationSchema,
  vpaValidationSchema,
  validationInitialResponseSchema,
  validationFinalResponseSchema
} from './schema';

export const errorSchemas = {
  internal: z.object({ message: z.string() })
};

export const api = {
  auth: {
    signup: {
      method: 'POST' as const,
      path: '/api/v1/auth/signup' as const,
      input: signupSchema,
      responses: {
        200: z.any(),
      },
    },
    login: {
      method: 'POST' as const,
      path: '/api/v1/auth/login' as const,
      input: loginSchema,
      responses: {
        200: z.any(),
      },
    },
    logout: {
      method: 'POST' as const,
      path: '/logout' as const,
      responses: {
        200: z.any(),
      }
    }
  },
  validation: {
    bankAccount: {
      method: 'POST' as const,
      path: '/api/v1/validation/bank-account' as const,
      input: bankAccountValidationSchema,
      responses: {
        202: validationInitialResponseSchema,
      },
    },
    vpa: {
      method: 'POST' as const,
      path: '/api/v1/validation/vpa' as const,
      input: vpaValidationSchema,
      responses: {
        202: validationInitialResponseSchema,
      },
    },
    status: {
      method: 'GET' as const,
      path: '/api/v1/validation/:validationRequestId' as const,
      responses: {
        200: validationFinalResponseSchema,
      },
    }
  }
};

export function buildUrl(path: string, params?: Record<string, string | number>): string {
  let url = path;
  if (params) {
    Object.entries(params).forEach(([key, value]) => {
      if (url.includes(`:${key}`)) {
        url = url.replace(`:${key}`, String(value));
      }
    });
  }
  return url;
}
