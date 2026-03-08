import React from "react";
import { cn } from "@/lib/utils";
import { Loader2 } from "lucide-react";

interface FintechButtonProps extends React.ButtonHTMLAttributes<HTMLButtonElement> {
  isLoading?: boolean;
  variant?: "primary" | "secondary" | "outline";
}

export const FintechButton = React.forwardRef<HTMLButtonElement, FintechButtonProps>(
  ({ children, isLoading, variant = "primary", className, ...props }, ref) => {
    const baseStyles = "relative flex items-center justify-center px-6 py-3 rounded-xl font-semibold transition-all duration-300 ease-out overflow-hidden group";
    
    const variants = {
      primary: "bg-primary text-primary-foreground shadow-[0_4px_14px_0_hsl(var(--primary)/0.39)] hover:shadow-[0_6px_20px_rgba(79,70,229,0.23)] hover:-translate-y-0.5 active:translate-y-0",
      secondary: "bg-secondary text-secondary-foreground hover:bg-secondary/80",
      outline: "border-2 border-border bg-transparent hover:border-primary hover:text-primary"
    };

    return (
      <button
        ref={ref}
        disabled={isLoading || props.disabled}
        className={cn(
          baseStyles,
          variants[variant],
          (isLoading || props.disabled) && "opacity-70 cursor-not-allowed transform-none hover:transform-none hover:shadow-none",
          className
        )}
        {...props}
      >
        {/* Shine effect on primary button */}
        {variant === 'primary' && !props.disabled && !isLoading && (
          <div className="absolute inset-0 -translate-x-full bg-gradient-to-r from-transparent via-white/20 to-transparent group-hover:animate-[shimmer_1.5s_infinite]" />
        )}
        
        {isLoading ? (
          <Loader2 className="w-5 h-5 mr-2 animate-spin" />
        ) : null}
        <span className="relative z-10">{children}</span>
      </button>
    );
  }
);
FintechButton.displayName = "FintechButton";
