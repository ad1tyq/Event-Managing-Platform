'use client';

import React, { useState, useRef, useEffect } from 'react';
import { UploadCloud, File as FileIcon, X, CheckCircle, AlertCircle, RefreshCw, Zap, Trophy, ArrowRight, Shield } from 'lucide-react';
import { Button } from '@/components/ui/Button';
import { Toast, useToast } from '@/components/ui/Toast';
import { uploadCsv, fetchSubmissionsByStatus, fetchEvent, finalizeSubmission, updateGlobalRound, fetchLeaderboard } from '@/lib/api';
import { useRouter } from 'next/navigation';

export default function AdminPage() {
  const router = useRouter();
  const { toast, showToast, hideToast } = useToast();

  // Importer State
  const [isDragging, setIsDragging] = useState(false);
  const [file, setFile] = useState<File | null>(null);
  const [isImporting, setIsImporting] = useState(false);
  const [importResult, setImportResult] = useState<{ message: string; type: 'success' | 'error' } | null>(null);
  const fileInputRef = useRef<HTMLInputElement>(null);

  // Math Brain State (Finalize)
  const [gradedSubs, setGradedSubs] = useState<any[]>([]);
  const [isFinalizing, setIsFinalizing] = useState<Record<number, boolean>>({});

  const getPayloadData = (payload: any) => {
    if (!payload) return {};
    if (typeof payload === 'string') {
      try { return JSON.parse(payload); } catch (e) { return {}; }
    }
    return payload;
  };

  // Leaderboard State
  const [leaderboard, setLeaderboard] = useState<{ id: string; teamName: string; totalScore: number }[]>([]);

  // Sync Switch State
  const [eventData, setEventData] = useState<any | null>(null);
  const [isSyncing, setIsSyncing] = useState(false);

  useEffect(() => {
    loadDashboardData();
  }, []);

  const loadDashboardData = async () => {
    const token = localStorage.getItem('admin_token');
    if (!token) {
      router.push('/');
      return;
    }
    try {
      const [subs, ev, lBoard] = await Promise.all([
        fetchSubmissionsByStatus('GRADED', token).catch(() => []),
        fetchEvent(1, token).catch(() => null),
        fetchLeaderboard(token).catch(() => [])
      ]);
      setGradedSubs(subs);
      setEventData(ev);
      setLeaderboard(lBoard);
    } catch (err) {
      console.error(err);
    }
  };

  const handleDragOver = (e: React.DragEvent) => {
    e.preventDefault();
    setIsDragging(true);
  };

  const handleDragLeave = (e: React.DragEvent) => {
    e.preventDefault();
    setIsDragging(false);
  };

  const handleDrop = (e: React.DragEvent) => {
    e.preventDefault();
    setIsDragging(false);

    if (e.dataTransfer.files && e.dataTransfer.files.length > 0) {
      const droppedFile = e.dataTransfer.files[0];
      validateAndSetFile(droppedFile);
    }
  };

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (e.target.files && e.target.files.length > 0) {
      validateAndSetFile(e.target.files[0]);
    }
  };

  const validateAndSetFile = (selectedFile: File) => {
    setImportResult(null);
    if (selectedFile.type === 'text/csv' || selectedFile.name.endsWith('.csv')) {
      setFile(selectedFile);
    } else {
      showToast('Please upload a valid .csv file', 'error');
    }
  };

  const handleUpload = async () => {
    if (!file) return;

    setIsImporting(true);
    setImportResult(null);

    try {
      const response = await uploadCsv(file);

      if (response.startsWith('Import Successful')) {
        setImportResult({ message: response, type: 'success' });
        showToast(response, 'success');
        setFile(null);
      } else {
        setImportResult({ message: response, type: 'error' });
        showToast('Upload Failed', 'error');
      }
    } catch (err: any) {
      console.error(err);
      setImportResult({ message: 'Failed to connect to server.', type: 'error' });
      showToast('Connection Error', 'error');
    } finally {
      setIsImporting(false);
    }
  };

  const handleFinalize = async (submissionId: number) => {
    setIsFinalizing(prev => ({ ...prev, [submissionId]: true }));
    try {
      const token = localStorage.getItem('admin_token')!;
      await finalizeSubmission(submissionId, token);
      showToast('Submission finalized successfully!', 'success');
      await loadDashboardData();
    } catch (err: any) {
      console.error(err);
      showToast(err.message || 'Failed to finalize', 'error');
    } finally {
      setIsFinalizing(prev => ({ ...prev, [submissionId]: false }));
    }
  };

  const handleAdvanceRound = async () => {
    if (!eventData) return;
    setIsSyncing(true);
    try {
      const token = localStorage.getItem('admin_token')!;
      const newRound = eventData.currentGlobalRound + 1;
      await updateGlobalRound(eventData.id, newRound, token);
      showToast(`Global Ceiling raised to Round ${newRound}!`, 'success');
      await loadDashboardData();
    } catch (err: any) {
      console.error(err);
      showToast(err.message || 'Failed to update round', 'error');
    } finally {
      setIsSyncing(false);
    }
  };

  const handleResetRound = async () => {
    if (!eventData) return;
    setIsSyncing(true);
    try {
      const token = localStorage.getItem('admin_token')!;
      await updateGlobalRound(eventData.id, 1, token);
      showToast(`Global Ceiling reset to Round 1!`, 'success');
      await loadDashboardData();
    } catch (err: any) {
      console.error(err);
      showToast(err.message || 'Failed to reset round', 'error');
    } finally {
      setIsSyncing(false);
    }
  };

  const handleLogout = () => {
    localStorage.removeItem('admin_token');
    router.push('/');
  };

  return (
    <main className="flex min-h-screen flex-col items-center p-6 sm:p-24 bg-zinc-950">
      <div className="w-full max-w-5xl flex justify-between items-center mb-8">
        <h1 className="text-3xl font-mono tracking-tight text-zinc-100 flex items-center">
          <Shield className="h-6 w-6 mr-3 text-terminal" />
          Admin Dashboard
        </h1>
        <div className="flex space-x-4">
          <Button variant="outline" onClick={() => router.push('/judging')} className="border-terminal/50 text-terminal hover:bg-terminal/10">
            Judging Portal <ArrowRight className="h-4 w-4 ml-2" />
          </Button>
          <Button variant="outline" onClick={handleLogout}>
            Terminate Session
          </Button>
        </div>
      </div>

      {toast && (
        <Toast
          message={toast.message}
          type={toast.type}
          onClose={hideToast}
        />
      )}

      <div className="w-full max-w-5xl grid grid-cols-1 lg:grid-cols-2 gap-8">

        {/* Left Column: CSV Importer & Sync Switch */}
        <div className="space-y-8">
          {/* SYNC SWITCH */}
          <div className="rounded-lg border border-zinc-800 bg-zinc-900/50 p-6 shadow-2xl">
            <h2 className="text-xl font-mono text-zinc-100 mb-4 flex items-center">
              <Zap className="h-5 w-5 mr-2 text-yellow-500" />
              The Synchronization Switch
            </h2>
            <p className="text-sm text-zinc-400 mb-6">
              Control the Global Ceiling. Teams finishing early are placed in the Waiting Room until you advance the round.
            </p>
            <div className="flex items-center justify-between p-4 bg-zinc-950 border border-zinc-800 rounded-md">
              <div>
                <span className="text-xs text-zinc-500 block">Current Global Round</span>
                <span className="text-3xl font-mono font-bold text-terminal">
                  {eventData?.currentGlobalRound || '...'}
                </span>
              </div>
              <div className="flex space-x-2">
                <Button onClick={handleResetRound} isLoading={isSyncing} variant="outline" className="border-red-500/50 text-red-500 hover:bg-red-500/10">
                  <RefreshCw className="h-4 w-4 mr-2" />
                  Reset
                </Button>
                <Button onClick={handleAdvanceRound} isLoading={isSyncing} className="bg-yellow-500/10 text-yellow-500 border border-yellow-500/50 hover:bg-yellow-500/20">
                  <ArrowRight className="h-4 w-4 mr-2" />
                  Start Next Round
                </Button>
              </div>
            </div>
          </div>

          {/* CSV IMPORTER */}
          <div className="rounded-lg border border-zinc-800 bg-zinc-900/50 p-6 shadow-2xl">
            <h2 className="text-xl font-mono text-zinc-100 mb-4 flex items-center">
              <UploadCloud className="h-5 w-5 mr-2 text-zinc-400" />
              CSV Ingestion
            </h2>
            <p className="text-sm text-zinc-400 mb-6">
              Upload Unstop participant data to generate passcodes and populate the database.
            </p>
            {!file ? (
              <div
                className={`flex flex-col items-center justify-center rounded-lg border-2 border-dashed p-12 transition-colors ${isDragging
                  ? 'border-terminal bg-terminal/5'
                  : 'border-zinc-800 bg-zinc-950 hover:border-zinc-700 hover:bg-zinc-900'
                  }`}
                onDragOver={handleDragOver}
                onDragLeave={handleDragLeave}
                onDrop={handleDrop}
              >
                <UploadCloud className="mb-4 h-10 w-10 text-zinc-500" />
                <p className="mb-2 text-sm font-sans text-zinc-400">
                  Drag and drop your CSV file here, or
                </p>
                <Button
                  variant="outline"
                  onClick={() => fileInputRef.current?.click()}
                >
                  Browse Files
                </Button>
                <input
                  type="file"
                  ref={fileInputRef}
                  onChange={handleFileChange}
                  accept=".csv"
                  className="hidden"
                />
                <p className="mt-4 text-xs font-mono text-zinc-600">
                  Supports: .csv
                </p>
              </div>
            ) : (
              <div className="flex flex-col space-y-6">
                <div className="flex items-center justify-between rounded-md border border-zinc-800 bg-zinc-950 p-4">
                  <div className="flex items-center space-x-4">
                    <div className="flex h-10 w-10 items-center justify-center rounded bg-zinc-900">
                      <FileIcon className="h-5 w-5 text-terminal" />
                    </div>
                    <div>
                      <p className="text-sm font-mono text-zinc-200">{file.name}</p>
                      <p className="text-xs font-sans text-zinc-500">
                        {(file.size / 1024).toFixed(2)} KB
                      </p>
                    </div>
                  </div>
                  <button
                    onClick={() => setFile(null)}
                    disabled={isImporting}
                    className="rounded-md p-2 text-zinc-500 hover:bg-zinc-900 hover:text-zinc-300 disabled:opacity-50"
                  >
                    <X className="h-4 w-4" />
                  </button>
                </div>

                <Button
                  onClick={handleUpload}
                  isLoading={isImporting}
                  className="w-full"
                >
                  Execute Import
                </Button>
              </div>
            )}

            {importResult && (
              <div className={`mt-6 flex items-start space-x-3 rounded-md p-4 text-sm font-mono ${importResult.type === 'success'
                ? 'bg-terminal/10 text-terminal border border-terminal/20'
                : 'bg-red-500/10 text-red-500 border border-red-500/20'
                }`}>
                {importResult.type === 'success' ? (
                  <CheckCircle className="h-5 w-5 shrink-0" />
                ) : (
                  <AlertCircle className="h-5 w-5 shrink-0" />
                )}
                <div>
                  <p className="font-semibold">System Response:</p>
                  <p className="mt-1 opacity-90">{importResult.message}</p>
                </div>
              </div>
            )}
          </div>
        </div>

        {/* Right Column: Finalize Module */}
        <div className="space-y-4">
          <h2 className="text-xl font-mono text-zinc-100 flex items-center">
            <CheckCircle className="h-5 w-5 mr-2 text-green-500" />
            The Math Brain (Finalize)
          </h2>
          <p className="text-sm text-zinc-400 mb-6">
            Submissions that have been fully evaluated by judges. Finalize to automatically calculate pass/fail states based on the event config threshold.
          </p>

          <div className="space-y-3">
            {gradedSubs.length === 0 ? (
              <div className="p-8 text-center rounded-lg border border-dashed border-zinc-800 bg-zinc-900/20 text-zinc-500 italic">
                No evaluated submissions ready for finalization.
              </div>
            ) : (
              gradedSubs.map(sub => (
                <div key={sub.id} className="flex flex-col sm:flex-row sm:items-center justify-between p-4 rounded-lg border border-zinc-800 bg-zinc-900/50">
                  <div className="mb-4 sm:mb-0">
                    <div className="flex items-center space-x-2 mb-1">
                      <span className="font-mono text-xs px-2 py-1 bg-zinc-800 rounded text-zinc-300">
                        {sub.taskId}
                      </span>
                      <span className="text-xs text-zinc-300 font-bold">{sub.teamName}</span>
                      <span className="text-xs text-zinc-500">Round {sub.roundNumber}</span>
                    </div>
                    <p className="text-sm font-medium text-zinc-300 line-clamp-1">{getPayloadData(sub.payload).githubUrl}</p>
                  </div>
                  <Button
                    onClick={() => handleFinalize(sub.id)}
                    isLoading={isFinalizing[sub.id]}
                    className="bg-green-500/10 text-green-500 border border-green-500/50 hover:bg-green-500/20 whitespace-nowrap"
                  >
                    Execute Finalization
                  </Button>
                </div>
              ))
            )}
          </div>

          {/* LEADERBOARD MODULE */}
          <div className="mt-8 rounded-lg border border-zinc-800 bg-zinc-900/50 p-6 shadow-2xl">
            <h2 className="text-xl font-mono text-zinc-100 mb-4 flex items-center">
              <Trophy className="h-5 w-5 mr-2 text-yellow-500" />
              Global Leaderboard
            </h2>
            <div className="space-y-2 max-h-[300px] overflow-y-auto pr-2">
              {leaderboard.length === 0 ? (
                <p className="text-zinc-500 italic text-sm">No scores recorded yet.</p>
              ) : (
                leaderboard.map((team, idx) => (
                  <div
                    key={idx}
                    onClick={() => router.push(`/admin/teams/${team.id}`)}
                    className="flex justify-between items-center p-3 rounded bg-zinc-950 border border-zinc-800 cursor-pointer hover:border-terminal/50 transition-colors"
                  >
                    <div className="flex items-center space-x-3">
                      <span className="font-mono text-zinc-500 w-4">{idx + 1}.</span>
                      <span className="font-medium text-zinc-200">{team.teamName}</span>
                    </div>
                    <span className="font-mono text-terminal font-bold">{team.totalScore} pts</span>
                  </div>
                ))
              )}
            </div>
          </div>
        </div>

      </div>
    </main>
  );
}
