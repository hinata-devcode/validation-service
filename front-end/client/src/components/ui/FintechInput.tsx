import React from "react";
import { cn } from "@/lib/utils";

interface FintechInputProps extends React.InputHTMLAttributes<HTMLInputElement> {
  label: string;
  error?: string;
}

export const FintechInput = React.forwardRef<HTMLInputElement, FintechInputProps>(
  ({ label, error, className, ...props }, ref) => {
    return (
      <div className="space-y-2">
        <label className="block text-sm font-semibold text-foreground/80">
          {label}
        </label>
        <div className="relative">
          <input
            ref={ref}
            className={cn(
              "w-full px-4 py-3 rounded-xl bg-background border border-border/60",
              "text-foreground placeholder:text-muted-foreground",
              "focus:outline-none focus:border-primary focus:ring-4 focus:ring-primary/10",
              "transition-all duration-200 shadow-sm",
              error ? "border-destructive focus:border-destructive focus:ring-destructive/10" : "",
              className
            )}
            {...props}
          />
        </div>
        {error && (
          <p className="text-sm font-medium text-destructive animate-in slide-in-from-top-1">
            {error}
          </p>
        )}
      </div>
    );
  }
);
FintechInput.displayName = "FintechInput";
