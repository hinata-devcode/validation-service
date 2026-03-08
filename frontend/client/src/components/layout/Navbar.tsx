import { ShieldCheck, LogOut } from "lucide-react";
import { useLogout } from "@/hooks/use-auth";

export function Navbar() {
  const logout = useLogout();

  return (
    <nav className="sticky top-0 z-50 w-full bg-white/80 backdrop-blur-md border-b border-border">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 h-16 flex items-center justify-between">
        <div className="flex items-center space-x-2">
          <div className="w-8 h-8 rounded-lg bg-primary/10 flex items-center justify-center">
            <ShieldCheck className="w-5 h-5 text-primary" />
          </div>
          <span className="font-display font-bold text-xl tracking-tight text-foreground">
            Verify<span className="text-primary">X</span>
          </span>
        </div>
        
        <div className="flex items-center space-x-4">
          <button 
            onClick={() => logout.mutate()}
            disabled={logout.isPending}
            className="flex items-center px-3 py-2 text-sm font-medium text-muted-foreground hover:text-foreground transition-colors rounded-lg hover:bg-slate-100"
          >
            <LogOut className="w-4 h-4 mr-2" />
            Sign Out
          </button>
        </div>
      </div>
    </nav>
  );
}
