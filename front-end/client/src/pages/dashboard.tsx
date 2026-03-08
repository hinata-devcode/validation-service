import { useState, useEffect } from "react";
import { Navbar } from "@/components/layout/Navbar";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { v4 as uuidv4 } from "uuid";
import { bankAccountValidationSchema, vpaValidationSchema } from "@shared/schema";
import { useValidateBankAccount, useValidateVpa } from "@/hooks/use-validation";
import { FintechInput } from "@/components/ui/FintechInput";
import { FintechButton } from "@/components/ui/FintechButton";
import { ValidationPoller } from "@/components/validation/ValidationPoller";
import { useValidationHistory } from "@/hooks/use-validation-history";
import { apiClient } from "@/lib/axios";
import { buildUrl, api } from "@shared/routes";
import { Building2, Smartphone, Clock, CheckCircle2, XCircle, AlertCircle } from "lucide-react";
import { cn } from "@/lib/utils";

type Tab = "bank" | "vpa";

export default function Dashboard() {
  const [activeTab, setActiveTab] = useState<Tab>("bank");
  const [idempotencyKey, setIdempotencyKey] = useState<string>("");
  const [activeRequestId, setActiveRequestId] = useState<string | null>(null);
  const [checkingStatus, setCheckingStatus] = useState(false);
  const { history, addValidation, updateValidationStatus } = useValidationHistory();

  // Initialize key on mount
  useEffect(() => {
    setIdempotencyKey(uuidv4());
  }, []);

  const regenerateKey = () => setIdempotencyKey(uuidv4());

  const handleStartPolling = (requestId: string, type: "bank" | "vpa") => {
    setActiveRequestId(requestId);
    addValidation(requestId, type, "PROCESSING");
  };

  const handleReset = () => {
    setActiveRequestId(null);
    regenerateKey();
    bankForm.reset();
    vpaForm.reset();
  };

  const handleCheckStatus = async (validationRequestId: string) => {
    setCheckingStatus(true);
    try {
      const url = buildUrl(api.validation.status.path, { validationRequestId });
      const res = await apiClient.get(url);
      const data = res.data;
      updateValidationStatus(validationRequestId, data.executionStatus);
      setActiveRequestId(validationRequestId);
    } catch (error) {
      console.error("Failed to check status", error);
    } finally {
      setCheckingStatus(false);
    }
  };

  const validateBank = useValidateBankAccount(handleStartPolling);
  const validateVpa = useValidateVpa(handleStartPolling);

  const bankForm = useForm<z.infer<typeof bankAccountValidationSchema>>({
    resolver: zodResolver(bankAccountValidationSchema),
    defaultValues: {
      bank_account: { account_number: "", ifsc: "" },
      user_details: { name: "", email: "", phone: "" },
    }
  });

  const vpaForm = useForm<z.infer<typeof vpaValidationSchema>>({
    resolver: zodResolver(vpaValidationSchema),
    defaultValues: {
      vpa: { vpa_address: "" },
      user_details: { name: "", email: "", phone: "" },
    }
  });

  const onBankSubmit = (data: z.infer<typeof bankAccountValidationSchema>) => {
    validateBank.mutate(
      { data, idempotencyKey }, 
      { onSettled: () => regenerateKey() }
    );
  };

  const onVpaSubmit = (data: z.infer<typeof vpaValidationSchema>) => {
    validateVpa.mutate(
      { data, idempotencyKey },
      { onSettled: () => regenerateKey() }
    );
  };

  const getStatusColor = (status: string) => {
    if (status === "COMPLETED") return "text-green-600";
    if (status === "PROVIDER_CALL_TIMEOUT") return "text-amber-600";
    if (status === "FAILED" || status === "ERROR") return "text-red-600";
    return "text-blue-600";
  };

  const getStatusIcon = (status: string) => {
    if (status === "COMPLETED") return <CheckCircle2 className="w-4 h-4" />;
    if (status === "PROVIDER_CALL_TIMEOUT") return <AlertCircle className="w-4 h-4" />;
    if (status === "FAILED" || status === "ERROR") return <XCircle className="w-4 h-4" />;
    return <Clock className="w-4 h-4" />;
  };

  const getTypeLabel = (type: string) => {
    return type === "bank" ? "Bank Account" : "UPI / VPA";
  };

  const formatTime = (timestamp: number) => {
    const date = new Date(timestamp);
    const now = new Date();
    const diffMs = now.getTime() - date.getTime();
    const diffMins = Math.floor(diffMs / 60000);
    const diffHours = Math.floor(diffMs / 3600000);
    const diffDays = Math.floor(diffMs / 86400000);

    if (diffMins < 1) return "Just now";
    if (diffMins < 60) return `${diffMins}m ago`;
    if (diffHours < 24) return `${diffHours}h ago`;
    if (diffDays < 7) return `${diffDays}d ago`;
    return date.toLocaleDateString();
  };

  return (
    <div className="min-h-screen bg-slate-50">
      <Navbar />
      
      <main className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
        <div className="mb-10">
          <h1 className="text-4xl font-display font-extrabold text-foreground">Validation Dashboard</h1>
          <p className="mt-2 text-lg text-muted-foreground">
            Instantly verify bank accounts and UPI IDs with confidence.
          </p>
        </div>

        {activeRequestId ? (
          <ValidationPoller requestId={activeRequestId} onReset={handleReset} />
        ) : (
          <div className="fintech-shadow bg-card rounded-2xl border border-border overflow-hidden">
            {/* Tabs Header */}
            <div className="flex border-b border-border bg-slate-50/50">
              <button
                onClick={() => setActiveTab("bank")}
                className={cn(
                  "flex-1 flex items-center justify-center py-4 px-6 text-sm font-semibold transition-colors",
                  activeTab === "bank" 
                    ? "text-primary border-b-2 border-primary bg-white" 
                    : "text-muted-foreground hover:text-foreground hover:bg-slate-100"
                )}
              >
                <Building2 className="w-5 h-5 mr-2" />
                Bank Account
              </button>
              <button
                onClick={() => setActiveTab("vpa")}
                className={cn(
                  "flex-1 flex items-center justify-center py-4 px-6 text-sm font-semibold transition-colors",
                  activeTab === "vpa" 
                    ? "text-primary border-b-2 border-primary bg-white" 
                    : "text-muted-foreground hover:text-foreground hover:bg-slate-100"
                )}
              >
                <Smartphone className="w-5 h-5 mr-2" />
                UPI / VPA
              </button>
            </div>

            {/* Forms Content */}
            <div className="p-6 md:p-8">
              {activeTab === "bank" && (
                <form onSubmit={bankForm.handleSubmit(onBankSubmit)} className="space-y-8 animate-in fade-in slide-in-from-right-4 duration-300">
                  <div className="space-y-6">
                    <h3 className="text-lg font-bold border-b pb-2">Bank Details</h3>
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                      <FintechInput
                        label="Account Number"
                        placeholder="e.g. 1234567890"
                        {...bankForm.register("bank_account.account_number")}
                        error={bankForm.formState.errors.bank_account?.account_number?.message}
                      />
                      <FintechInput
                        label="IFSC Code"
                        placeholder="e.g. HDFC0001234"
                        {...bankForm.register("bank_account.ifsc")}
                        error={bankForm.formState.errors.bank_account?.ifsc?.message}
                      />
                    </div>
                  </div>

                  <div className="space-y-6">
                    <h3 className="text-lg font-bold border-b pb-2">User Details</h3>
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                      <FintechInput
                        label="Full Name"
                        placeholder="John Doe"
                        {...bankForm.register("user_details.name")}
                        error={bankForm.formState.errors.user_details?.name?.message}
                      />
                      <FintechInput
                        label="Email"
                        type="email"
                        placeholder="john@example.com"
                        {...bankForm.register("user_details.email")}
                        error={bankForm.formState.errors.user_details?.email?.message}
                      />
                      <FintechInput
                        label="Phone Number"
                        placeholder="9876543210"
                        {...bankForm.register("user_details.phone")}
                        error={bankForm.formState.errors.user_details?.phone?.message}
                      />
                    </div>
                  </div>

                  <div className="pt-4 flex justify-end">
                    <FintechButton type="submit" isLoading={validateBank.isPending}>
                      Verify Bank Account
                    </FintechButton>
                  </div>
                </form>
              )}

              {activeTab === "vpa" && (
                <form onSubmit={vpaForm.handleSubmit(onVpaSubmit)} className="space-y-8 animate-in fade-in slide-in-from-left-4 duration-300">
                  <div className="space-y-6">
                    <h3 className="text-lg font-bold border-b pb-2">UPI Details</h3>
                    <div className="grid grid-cols-1 gap-6">
                      <FintechInput
                        label="VPA Address"
                        placeholder="e.g. name@okhdfcbank"
                        {...vpaForm.register("vpa.vpa_address")}
                        error={vpaForm.formState.errors.vpa?.vpa_address?.message}
                      />
                    </div>
                  </div>

                  <div className="space-y-6">
                    <h3 className="text-lg font-bold border-b pb-2">User Details</h3>
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                      <FintechInput
                        label="Full Name"
                        placeholder="John Doe"
                        {...vpaForm.register("user_details.name")}
                        error={vpaForm.formState.errors.user_details?.name?.message}
                      />
                      <FintechInput
                        label="Email"
                        type="email"
                        placeholder="john@example.com"
                        {...vpaForm.register("user_details.email")}
                        error={vpaForm.formState.errors.user_details?.email?.message}
                      />
                      <FintechInput
                        label="Phone Number"
                        placeholder="9876543210"
                        {...vpaForm.register("user_details.phone")}
                        error={vpaForm.formState.errors.user_details?.phone?.message}
                      />
                    </div>
                  </div>

                  <div className="pt-4 flex justify-end">
                    <FintechButton type="submit" isLoading={validateVpa.isPending}>
                      Verify VPA Address
                    </FintechButton>
                  </div>
                </form>
              )}
            </div>
          </div>
        )}
      </main>

      {/* Recent Validations Section */}
      {history.length > 0 && !activeRequestId && (
        <section className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
          <h2 className="text-2xl font-bold text-foreground mb-6">Recent Validations</h2>
          <div className="fintech-shadow rounded-2xl bg-card border border-border overflow-hidden">
            <div className="overflow-x-auto">
              <table className="w-full">
                <thead className="bg-slate-50/50 border-b border-border">
                  <tr>
                    <th className="px-6 py-3 text-left text-xs font-semibold text-muted-foreground uppercase tracking-wider">Type</th>
                    <th className="px-6 py-3 text-left text-xs font-semibold text-muted-foreground uppercase tracking-wider">Status</th>
                    <th className="px-6 py-3 text-left text-xs font-semibold text-muted-foreground uppercase tracking-wider">Time</th>
                    <th className="px-6 py-3 text-right text-xs font-semibold text-muted-foreground uppercase tracking-wider">Action</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-border">
                  {history.map((item) => (
                    <tr key={item.id} className="hover:bg-slate-50/50 transition-colors">
                      <td className="px-6 py-4">
                        <span className="text-sm font-medium text-foreground">
                          {getTypeLabel(item.type)}
                        </span>
                      </td>
                      <td className="px-6 py-4">
                        <div className={`flex items-center space-x-2 ${getStatusColor(item.lastKnownStatus)}`}>
                          {getStatusIcon(item.lastKnownStatus)}
                          <span className="text-sm font-medium">{item.lastKnownStatus}</span>
                        </div>
                      </td>
                      <td className="px-6 py-4 text-sm text-muted-foreground">
                        {formatTime(item.timestamp)}
                      </td>
                      <td className="px-6 py-4 text-right">
                        {item.lastKnownStatus === "PROCESSING" || item.lastKnownStatus === "PROVIDER_CALL_TIMEOUT" ? (
                          <button
                            onClick={() => handleCheckStatus(item.id)}
                            disabled={checkingStatus}
                            className="inline-flex items-center px-3 py-1.5 text-sm font-medium rounded-lg bg-primary text-primary-foreground hover:opacity-90 disabled:opacity-50 transition-opacity"
                          >
                            {checkingStatus ? "Checking..." : "Check Status"}
                          </button>
                        ) : (
                          <button
                            onClick={() => setActiveRequestId(item.id)}
                            className="inline-flex items-center px-3 py-1.5 text-sm font-medium rounded-lg border border-border text-foreground hover:bg-slate-100 transition-colors"
                          >
                            View Details
                          </button>
                        )}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        </section>
      )}
    </div>
  );
}
