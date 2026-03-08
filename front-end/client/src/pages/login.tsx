import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { loginSchema } from "@shared/schema";
import { z } from "zod";
import { useLogin } from "@/hooks/use-auth";
import { FintechInput } from "@/components/ui/FintechInput";
import { FintechButton } from "@/components/ui/FintechButton";
import { Link } from "wouter";
import { ShieldCheck } from "lucide-react";

export default function Login() {
  const login = useLogin();
  
  const form = useForm<z.infer<typeof loginSchema>>({
    resolver: zodResolver(loginSchema),
    defaultValues: {
      username: "",
      password: "",
    },
  });

  const onSubmit = (data: z.infer<typeof loginSchema>) => {
    login.mutate(data);
  };

  return (
    <div className="min-h-screen flex flex-col justify-center py-12 sm:px-6 lg:px-8 bg-slate-50 relative overflow-hidden">
      {/* Decorative background elements */}
      <div className="absolute top-[-10%] left-[-10%] w-[40%] h-[40%] rounded-full bg-primary/5 blur-3xl" />
      <div className="absolute bottom-[-10%] right-[-10%] w-[40%] h-[40%] rounded-full bg-blue-500/5 blur-3xl" />

      <div className="sm:mx-auto sm:w-full sm:max-w-md relative z-10">
        <div className="flex justify-center mb-6">
          <div className="w-12 h-12 rounded-xl bg-primary shadow-lg shadow-primary/25 flex items-center justify-center transform -rotate-6">
            <ShieldCheck className="w-7 h-7 text-white" />
          </div>
        </div>
        <h2 className="text-center text-3xl font-display font-extrabold text-foreground tracking-tight">
          Welcome back
        </h2>
        <p className="mt-2 text-center text-sm text-muted-foreground">
          Sign in to access your validation dashboard
        </p>
      </div>

      <div className="mt-8 sm:mx-auto sm:w-full sm:max-w-md relative z-10">
        <div className="glass-card py-8 px-4 shadow sm:rounded-2xl sm:px-10">
          <form className="space-y-6" onSubmit={form.handleSubmit(onSubmit)}>
            <FintechInput
              label="Username"
              type="text"
              placeholder="Enter your username"
              {...form.register("username")}
              error={form.formState.errors.username?.message}
            />

            <FintechInput
              label="Password"
              type="password"
              placeholder="••••••••"
              {...form.register("password")}
              error={form.formState.errors.password?.message}
            />

            <FintechButton 
              type="submit" 
              className="w-full" 
              isLoading={login.isPending}
            >
              Sign in securely
            </FintechButton>
          </form>

          <div className="mt-6 text-center">
            <p className="text-sm text-muted-foreground">
              Don't have an account?{" "}
              <Link href="/signup" className="font-semibold text-primary hover:text-primary/80 transition-colors">
                Sign up
              </Link>
            </p>
          </div>
        </div>
      </div>
    </div>
  );
}
