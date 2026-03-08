import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { signupSchema } from "@shared/schema";
import { z } from "zod";
import { useSignup } from "@/hooks/use-auth";
import { FintechInput } from "@/components/ui/FintechInput";
import { FintechButton } from "@/components/ui/FintechButton";
import { Link } from "wouter";
import { ShieldCheck } from "lucide-react";

export default function Signup() {
  const signup = useSignup();
  
  const form = useForm<z.infer<typeof signupSchema>>({
    resolver: zodResolver(signupSchema),
    defaultValues: {
      username: "",
      password: "",
    },
  });

  const onSubmit = (data: z.infer<typeof signupSchema>) => {
    signup.mutate(data);
  };

  return (
    <div className="min-h-screen flex flex-col justify-center py-12 sm:px-6 lg:px-8 bg-slate-50 relative overflow-hidden">
      {/* Decorative background elements */}
      <div className="absolute top-[-10%] right-[-10%] w-[40%] h-[40%] rounded-full bg-primary/5 blur-3xl" />
      <div className="absolute bottom-[-10%] left-[-10%] w-[40%] h-[40%] rounded-full bg-blue-500/5 blur-3xl" />

      <div className="sm:mx-auto sm:w-full sm:max-w-md relative z-10">
        <div className="flex justify-center mb-6">
          <div className="w-12 h-12 rounded-xl bg-primary shadow-lg shadow-primary/25 flex items-center justify-center transform rotate-6">
            <ShieldCheck className="w-7 h-7 text-white" />
          </div>
        </div>
        <h2 className="text-center text-3xl font-display font-extrabold text-foreground tracking-tight">
          Create an account
        </h2>
        <p className="mt-2 text-center text-sm text-muted-foreground">
          Start verifying bank accounts instantly
        </p>
      </div>

      <div className="mt-8 sm:mx-auto sm:w-full sm:max-w-md relative z-10">
        <div className="glass-card py-8 px-4 shadow sm:rounded-2xl sm:px-10">
          <form className="space-y-6" onSubmit={form.handleSubmit(onSubmit)}>
            <FintechInput
              label="Username"
              type="text"
              placeholder="Choose a username"
              {...form.register("username")}
              error={form.formState.errors.username?.message}
            />

            <FintechInput
              label="Password"
              type="password"
              placeholder="Create a strong password"
              {...form.register("password")}
              error={form.formState.errors.password?.message}
            />

            <FintechButton 
              type="submit" 
              className="w-full" 
              isLoading={signup.isPending}
            >
              Create account
            </FintechButton>
          </form>

          <div className="mt-6 text-center">
            <p className="text-sm text-muted-foreground">
              Already have an account?{" "}
              <Link href="/login" className="font-semibold text-primary hover:text-primary/80 transition-colors">
                Sign in
              </Link>
            </p>
          </div>
        </div>
      </div>
    </div>
  );
}
