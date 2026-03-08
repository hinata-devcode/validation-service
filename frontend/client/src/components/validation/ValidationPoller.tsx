import React, { useEffect, useRef, useState } from "react";
import { apiClient } from "@/lib/axios";
import { buildUrl, api } from "@shared/routes";
import { Loader2, CheckCircle2, XCircle, AlertCircle, Building2, UserCircle, Hash } from "lucide-react";

interface ValidationPollerProps {
  requestId: string;
  onReset: () => void;
}

type PollStatus = "PROCESSING" | "COMPLETED" | "FAILED" | "PROVIDER_CALL_TIMEOUT" | "ERROR";

export function ValidationPoller({ requestId, onReset }: ValidationPollerProps) {
  const [status, setStatus] = useState<PollStatus>("PROCESSING");
  const [resultData, setResultData] = useState<any>(null);
  const providerCallTimeOutRef = useRef<any>(3000);

  useEffect(() => {
    let timeoutId: NodeJS.Timeout;
    let isMounted = true;

    const poll = async () => {
      if (!isMounted) return;

      try {
        const url = buildUrl(api.validation.status.path, { validationRequestId: requestId });
        const res = await apiClient.get(url);
        const data = res.data;

        if (data.executionStatus === "COMPLETED" || 
            data.executionStatus === "FAILED") {
          setStatus(data.executionStatus);
          setResultData(data);
        } else if (data.executionStatus === 'PROVIDER_CALL_TIMEOUT') {
          setStatus(data.executionStatus);
          const { current = {} } = providerCallTimeOutRef || {};
          const { value = 3000 } = current || {}
          timeoutId = setTimeout(poll, value * 2);
          providerCallTimeOutRef.current = {
            value: value * 2
          };
        }
        else {
          // Keep polling every 3 seconds
          timeoutId = setTimeout(poll, 3000);
        }
      } catch (error) {
        console.error("Polling error", error);
        setStatus("ERROR");
      }
    };

    poll();

    return () => {
      isMounted = false;
      clearTimeout(timeoutId);
    };
  }, [requestId]);

  if (status === "PROCESSING") {
    return (
      <div className="p-8 md:p-12 fintech-shadow rounded-2xl bg-card border border-border flex flex-col items-center justify-center space-y-6 text-center animate-in fade-in zoom-in duration-500">
        <div className="relative">
          <div className="absolute inset-0 rounded-full blur-xl bg-primary/20 animate-pulse" />
          <div className="w-16 h-16 rounded-full bg-primary/10 flex items-center justify-center relative z-10 border border-primary/20">
            <Loader2 className="w-8 h-8 text-primary animate-spin" />
          </div>
        </div>
        <div>
          <h3 className="text-xl font-bold text-foreground">Validating Details</h3>
          <p className="text-muted-foreground mt-2 max-w-sm mx-auto">
            Securely communicating with banking networks. This usually takes a few seconds...
          </p>
        </div>
      </div>
    );
  }

  if (status === "PROVIDER_CALL_TIMEOUT") {
    return (
      <div className="p-8 fintech-shadow rounded-2xl bg-amber-50/50 border border-amber-100 flex flex-col items-center justify-center space-y-6 text-center animate-in slide-in-from-bottom-4">
        <div className="w-16 h-16 rounded-full bg-amber-100 flex items-center justify-center">
          <Loader2 className="w-8 h-8 text-primary animate-spin" />
        </div>
        <div>
          <h3 className="text-xl font-bold text-amber-900">Processing Delayed</h3>
          <p className="text-amber-700/80 mt-2 max-w-sm mx-auto">
            The verification is taking longer than expected. Your request is safely queued and will be processed. You can check the status later.
          </p>
        </div>
        <div className="flex gap-3">
          <button disabled style={{cursor: 'not-allowed'}} onClick={onReset} className="text-sm font-semibold text-amber-700 hover:text-amber-800 underline underline-offset-4">
            Start new validation
          </button>
        </div>
      </div>
    );
  }

  if (status === "FAILED" || status === "ERROR") {
    return (
      <div className="p-8 fintech-shadow rounded-2xl bg-red-50/50 border border-red-100 flex flex-col items-center justify-center space-y-6 text-center animate-in slide-in-from-bottom-4">
        <div className="w-16 h-16 rounded-full bg-red-100 flex items-center justify-center">
          <AlertCircle className="w-8 h-8 text-destructive" />
        </div>
        <div>
          <h3 className="text-xl font-bold text-red-900">Validation Failed</h3>
          <p className="text-red-700/80 mt-2 max-w-sm mx-auto">
            Bank network is currently down. Please try again later.
          </p>
        </div>
        <button onClick={onReset} className="text-sm font-semibold text-red-700 hover:text-red-800 underline underline-offset-4">
          Try another validation
        </button>
      </div>
    );
  }

  // COMPLETED state - extract all data from result object
  const result = resultData?.result;
  const isValid = result?.status === "VALID";
  const confidenceLevel = result?.confidenceLevel || "N/A";
  const providerDetails = result?.providerDetails;
  const bankDetailsJson = result?.providerDetails?.bankDetailsJson;

  return (
    <div className="fintech-shadow rounded-2xl bg-card border border-border overflow-hidden animate-in slide-in-from-bottom-4">
      {/* Header */}
      <div className={`p-6 md:p-8 border-b bg-green-50/50 border-green-100 flex items-center justify-between`}>
        <div className="flex items-center space-x-4">
          <div className={`w-12 h-12 rounded-full flex items-center justify-center bg-green-100`}>
              <CheckCircle2 className="w-6 h-6 text-green-600" />
          </div>
          <div>
            <h3 className={`text-xl font-bold text-green-900}`}>
              {'Validation Successful'}
            </h3>
            <p className={`text-sm text-green-700/80`}>
              Confidence: <span className="font-semibold">{confidenceLevel}</span>
            </p>
          </div>
        </div>
        <button 
          onClick={onReset}
          className="px-4 py-2 rounded-lg text-sm font-semibold bg-white border shadow-sm hover:bg-gray-50 transition-colors"
        >
          New Validation
        </button>
      </div>

      {/* Results Grid */}
      <div className="p-6 md:p-8 space-y-6">
        {/* Primary Status Card */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          <div className="bg-slate-50 p-4 rounded-xl">
            <p className="text-xs text-muted-foreground font-semibold uppercase tracking-wider">Validation Status</p>
            <p className={`text-2xl font-bold mt-2 ${isValid ? 'text-green-600' : 'text-red-600'}`}>
              {result?.status || 'N/A'}
            </p>
          </div>
          <div className="bg-slate-50 p-4 rounded-xl">
            <p className="text-xs text-muted-foreground font-semibold uppercase tracking-wider">Confidence Level</p>
            <p className="text-2xl font-bold mt-2 text-slate-900">{confidenceLevel}</p>
          </div>
          <div className="bg-slate-50 p-4 rounded-xl">
            <p className="text-xs text-muted-foreground font-semibold uppercase tracking-wider">Verification Type</p>
            <p className="text-lg font-bold mt-2 text-slate-900">
              {bankDetailsJson ? "Bank Account" : "Identity"}
            </p>
          </div>
        </div>

        {/* Provider Details Section */}
        {providerDetails && (
          <div className="space-y-3">
            <h4 className="text-xs font-bold uppercase tracking-wider text-muted-foreground flex items-center">
              <UserCircle className="w-4 h-4 mr-2" /> Registered Entity Information
            </h4>
            <div className="bg-slate-50 p-5 rounded-xl space-y-4">
              <div>
                <p className="text-xs text-muted-foreground font-semibold mb-1">Registered Name</p>
                <p className="text-base font-semibold text-foreground">
                  {providerDetails.registeredName || '—'}
                </p>
              </div>
              {providerDetails.nameMatchScore !== undefined && (
                <div>
                  <div className="flex items-center justify-between mb-2">
                    <p className="text-xs text-muted-foreground font-semibold">Name Match Score</p>
                    <span className="text-sm font-bold text-foreground">{providerDetails.nameMatchScore}%</span>
                  </div>
                  <div className="w-full h-2.5 bg-slate-200 rounded-full overflow-hidden">
                    <div 
                      className={`h-full rounded-full transition-all ${
                        providerDetails.nameMatchScore > 80 
                          ? 'bg-green-500' 
                          : providerDetails.nameMatchScore > 50 
                          ? 'bg-yellow-500' 
                          : 'bg-red-500'
                      }`}
                      style={{ width: `${providerDetails.nameMatchScore}%` }}
                    />
                  </div>
                </div>
              )}
            </div>
          </div>
        )}

        {/* Bank Details Section (for VPA validations) */}
        {bankDetailsJson && (
          <div className="space-y-3">
            <h4 className="text-xs font-bold uppercase tracking-wider text-muted-foreground flex items-center">
              <Building2 className="w-4 h-4 mr-2" /> Bank Account Information
            </h4>
            <div className="bg-slate-50 p-5 rounded-xl space-y-4">
              <div>
                <p className="text-xs text-muted-foreground font-semibold mb-1">Bank Name</p>
                <p className="text-base font-semibold text-foreground">
                  {bankDetailsJson.bank_name || '—'}
                </p>
              </div>
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <p className="text-xs text-muted-foreground font-semibold mb-1">Account Type</p>
                  <p className="text-sm font-semibold text-foreground">
                    {bankDetailsJson.account_type || '—'}
                  </p>
                </div>
                <div>
                  <p className="text-xs text-muted-foreground font-semibold mb-1 flex items-center">
                    <Hash className="w-3 h-3 mr-1" /> Account Number
                  </p>
                  <p className="text-sm font-mono font-semibold text-foreground">
                    {bankDetailsJson.account_number 
                      ? `••••${bankDetailsJson.account_number.slice(-4)}` 
                      : '—'
                    }
                  </p>
                </div>
              </div>
            </div>
          </div>
        )}

        {/* Empty State for INVALID without provider details */}
        {!providerDetails && !isValid && (
          <div className="bg-slate-50 p-6 rounded-xl text-center">
            <p className="text-sm text-muted-foreground">
              No additional details available for this validation result.
            </p>
          </div>
        )}
      </div>
    </div>
  );
}
