import { useMutation } from "@tanstack/react-query";
import { useLocation } from "wouter";
import { apiClient } from "@/lib/axios";
import { useToast } from "@/hooks/use-toast";
import { z } from "zod";
import { loginSchema, signupSchema } from "@shared/schema";

export function useLogin() {
  const [, setLocation] = useLocation();
  const { toast } = useToast();

  return useMutation({
    mutationFn: async (data: z.infer<typeof loginSchema>) => {
      const res = await apiClient.post("/api/v1/auth/login", data);
      return res.data;
    },
    onSuccess: () => {
      toast({
        title: "Welcome back",
        description: "Successfully logged into your account.",
      });
      setLocation("/dashboard");
    },
    onError: (error: any) => {
      toast({
        variant: "destructive",
        title: "Login failed",
        description: error.response?.data?.message || "Invalid credentials. Please try again.",
      });
    },
  });
}

export function useSignup() {
  const [, setLocation] = useLocation();
  const { toast } = useToast();

  return useMutation({
    mutationFn: async (data: z.infer<typeof signupSchema>) => {
      const res = await apiClient.post("/api/v1/auth/signup", data);
      return res.data;
    },
    onSuccess: () => {
      toast({
        title: "Account created",
        description: "Your account has been created.",
      });
      setLocation("/dashboard");
    },
    onError: (error: any) => {
      toast({
        variant: "destructive",
        title: "Signup failed",
        description: error.response?.data?.message || "Could not create account.",
      });
    },
  });
}

export function useLogout() {
  const [, setLocation] = useLocation();
  const { toast } = useToast();

  return useMutation({
    mutationFn: async () => {
      await apiClient.post("/logout");
    },
    onSuccess: () => {
      toast({
        title: "Logged out",
        description: "You have been securely logged out.",
      });
      setLocation("/login");
    },
    onError: () => {
      // Force redirect anyway
      setLocation("/login");
    }
  });
}
