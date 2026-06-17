'use client';

import React from 'react';
import { Terminal, LogOut } from 'lucide-react';
import { Button } from '@/components/ui/Button';
import { SubmitForm } from '@/components/SubmitForm';
import { Toast, useToast } from '@/components/ui/Toast';
import { useRouter } from 'next/navigation';

export default function DashboardPage() {
  const { toast, hideToast } = useToast();
  const router = useRouter();

  const handleLogout = () => {
    localStorage.removeItem('team_token');
    router.push('/');
  };

  return (
    <main className="flex min-h-screen flex-col items-center p-6 sm:p-24 bg-zinc-950">
      {toast && (
        <Toast
          message={toast.message}
          type={toast.type}
          onClose={hideToast}
        />
      )}
      <div className="w-full max-w-2xl space-y-8 rounded-lg border border-zinc-800 bg-zinc-900/50 p-8 shadow-2xl backdrop-blur-sm">
        <div className="flex justify-between items-center mb-6">
          <div className="flex items-center space-x-4">
            <div className="flex h-12 w-12 items-center justify-center rounded-full bg-zinc-900 border border-zinc-800">
              <Terminal className="h-6 w-6 text-terminal" />
            </div>
            <h1 className="text-2xl font-mono tracking-tight text-zinc-100">
              Submission Portal
            </h1>
          </div>
          <Button variant="outline" className="text-xs px-3 h-8" onClick={handleLogout}>
            <LogOut className="h-4 w-4 mr-2" />
            Sign Out
          </Button>
        </div>
        
        <p className="font-sans text-zinc-400 mb-8">
          Submit your project details below. Ensure your GitHub repository is public and accessible by the judging panel.
        </p>

        <SubmitForm />

      </div>
    </main>
  );
}
