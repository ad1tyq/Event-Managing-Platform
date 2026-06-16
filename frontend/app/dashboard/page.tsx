import React from 'react';
import Link from 'next/link';
import { Terminal } from 'lucide-react';
import { Button } from '@/components/ui/Button';

export default function DashboardPage() {
  return (
    <main className="flex min-h-screen flex-col items-center justify-center p-6 sm:p-24 bg-zinc-950">
      <div className="w-full max-w-2xl space-y-8 rounded-lg border border-zinc-800 bg-zinc-900/50 p-12 shadow-2xl backdrop-blur-sm text-center">
        <div className="flex justify-center mb-6">
          <div className="flex h-16 w-16 items-center justify-center rounded-full bg-zinc-900 border border-zinc-800">
            <Terminal className="h-8 w-8 text-terminal" />
          </div>
        </div>
        
        <h1 className="text-3xl font-mono tracking-tight text-zinc-100">
          Session Active
        </h1>
        
        <p className="font-sans text-zinc-400">
          Welcome to the Unlock&apos;D Participant Dashboard. This is a placeholder for the actual dashboard content.
        </p>

        <div className="pt-8">
          <Link href="/">
            <Button variant="outline">
              Terminate Session
            </Button>
          </Link>
        </div>
      </div>
    </main>
  );
}
