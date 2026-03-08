import { useMutation } from "@tanstack/react-query";
import { apiClient } from "@/lib/axios";
import { useToast } from "@/hooks/use-toast";
import { z } from "zod";
import { bankAccountValidationSchema, vpaValidationSchema } from "@shared/schema";
import { api } from "@shared/routes";

interface ValidationMutationContext {
  idempotencyKey: string;
}

export function useValidateBankAccount(onStartPolling: (id: string, type: "bank" | "vpa") => void) {
  const { toast } = useToast();

  return useMutation({
    mutationFn: async ({ data, idempotencyKey }: { data: z.infer<typeof bankAccountValidationSchema>, idempotencyKey: string }) => {
      const res = await apiClient.post(
        api.validation.bankAccount.path, 
        data,
        {
          headers: {
            "Idempotency-Key": idempotencyKey
          }
        }
      );
      return res.data;
    },
    onSuccess: (data) => {
      if (data.executionStatus === "PROCESSING" && data.validationRequestId) {
        onStartPolling(data.validationRequestId, "bank");
      }
    },
    onError: (error: any) => {
      const msg = error.response?.data?.message || error.response?.data;
      if (error.response?.status === 400 && (msg?.includes("IDEMPOTENCY_CONFLICT") || msg?.includes("Idempotency"))) {
        toast({
          variant: "destructive",
          title: "Duplicate Request",
          description: "This Idempotency-Key was already used. Please refresh to generate a new one.",
        });
      } else {
        toast({
          variant: "destructive",
          title: "Validation Failed",
          description: "Could not initiate validation request.",
        });
      }
    }
  });
}

export function useValidateVpa(onStartPolling: (id: string, type: "bank" | "vpa") => void) {
  const { toast } = useToast();

  return useMutation({
    mutationFn: async ({ data, idempotencyKey }: { data: z.infer<typeof vpaValidationSchema>, idempotencyKey: string }) => {
      const res = await apiClient.post(
        api.validation.vpa.path, 
        data,
        {
          headers: {
            "Idempotency-Key": idempotencyKey
          }
        }
      );
      return res.data;
    },
    onSuccess: (data) => {
      if (data.executionStatus === "PROCESSING" && data.validationRequestId) {
        onStartPolling(data.validationRequestId, "vpa");
      }
    },
    onError: (error: any) => {
      const msg = error.response?.data?.message || error.response?.data;
      if (error.response?.status === 400 && (msg?.includes("IDEMPOTENCY_CONFLICT") || msg?.includes("Idempotency"))) {
        toast({
          variant: "destructive",
          title: "Duplicate Request",
          description: "This Idempotency-Key was already used. Please refresh to generate a new one.",
        });
      } else {
        toast({
          variant: "destructive",
          title: "Validation Failed",
          description: "Could not initiate validation request.",
        });
      }
    }
  });
}
