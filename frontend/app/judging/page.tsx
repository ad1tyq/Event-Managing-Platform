'use client';

import React, { useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { Gavel, LogOut, CheckCircle, ArrowLeft } from 'lucide-react';
import { Button } from '@/components/ui/Button';
import { Toast, useToast } from '@/components/ui/Toast';
import { fetchSubmissionsByStatus, fetchEvent, submitEvaluation } from '@/lib/api';

export default function JudgingPage() {
  const router = useRouter();
  const { toast, showToast, hideToast } = useToast();

  const getPayloadData = (payload: any) => {
    if (!payload) return {};
    if (typeof payload === 'string') {
      try { return JSON.parse(payload); } catch (e) { return {}; }
    }
    return payload;
  };

  const [submissions, setSubmissions] = useState<any[]>([]);
  const [selectedSub, setSelectedSub] = useState<any | null>(null);
  const [eventConfig, setEventConfig] = useState<any | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  // Grading State
  const [scoreBreakdown, setScoreBreakdown] = useState<Record<string, number>>({});
  const [feedback, setFeedback] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);

  useEffect(() => {
    loadData();
  }, []);

  const loadData = async () => {
    const token = localStorage.getItem('admin_token');
    if (!token) {
      router.push('/');
      return;
    }

    setIsLoading(true);
    try {
      const subs = await fetchSubmissionsByStatus('PENDING', token);
      setSubmissions(subs);

      // Assuming event ID 1 for this hackathon instance
      const event = await fetchEvent(1, token);
      if (typeof event.config === 'string') {
        setEventConfig(JSON.parse(event.config));
      } else {
        setEventConfig(event.config);
      }
    } catch (err: any) {
      console.error(err);
      showToast('Failed to load judging data.', 'error');
    } finally {
      setIsLoading(false);
    }
  };

  const handleLogout = () => {
    localStorage.removeItem('admin_token');
    router.push('/');
  };

  const handleSelectSubmission = (sub: any) => {
    setSelectedSub(sub);
    setFeedback('');

    // Find the rubric
    if (eventConfig && eventConfig.roadmap) {
      const taskDef = eventConfig.roadmap.find((t: any) => t.task_id === sub.taskId);
      if (taskDef && taskDef.rubric) {
        const initialScores: Record<string, number> = {};
        taskDef.rubric.forEach((cat: string) => {
          initialScores[cat] = 50; // Default to 50
        });
        setScoreBreakdown(initialScores);
      } else {
        setScoreBreakdown({ "Overall": 50 });
      }
    }
  };

  const handleScoreChange = (category: string, val: string) => {
    setScoreBreakdown(prev => ({
      ...prev,
      [category]: parseInt(val)
    }));
  };

  const handleSubmitGrade = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedSub) return;

    setIsSubmitting(true);
    try {
      const token = localStorage.getItem('admin_token')!;
      await submitEvaluation(selectedSub.id, scoreBreakdown, feedback, token);
      showToast('Evaluation submitted successfully!', 'success');
      setSelectedSub(null);
      await loadData(); // Reload list
    } catch (err: any) {
      console.error(err);
      showToast(err.message || 'Failed to submit evaluation', 'error');
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleInstantReject = async () => {
    if (!selectedSub) return;

    setIsSubmitting(true);
    try {
      const token = localStorage.getItem('admin_token')!;
      
      // Auto-populate all score categories to 0 for a guaranteed rejection upon finalization
      const zeroScores: Record<string, number> = {};
      Object.keys(scoreBreakdown).forEach(cat => {
        zeroScores[cat] = 0;
      });

      const rejectFeedback = feedback.trim() === '' ? 'Instant Rejection by Judge.' : feedback;

      await submitEvaluation(selectedSub.id, zeroScores, rejectFeedback, token);
      showToast('Evaluation submitted as REJECTED (0 points).', 'success');
      setSelectedSub(null);
      await loadData(); // Reload list
    } catch (err: any) {
      console.error(err);
      showToast(err.message || 'Failed to submit evaluation', 'error');
    } finally {
      setIsSubmitting(false);
    }
  };

  if (isLoading) {
    return (
      <main className="flex min-h-screen items-center justify-center bg-zinc-950">
        <div className="text-zinc-400 font-mono">Loading Judging Portal...</div>
      </main>
    );
  }

  return (
    <main className="flex min-h-screen flex-col p-6 sm:p-24 bg-zinc-950">
      {toast && <Toast message={toast.message} type={toast.type} onClose={hideToast} />}

      <div className="flex justify-between items-center mb-8">
        <div className="flex items-center space-x-4">
          <div className="flex h-12 w-12 items-center justify-center rounded-lg bg-zinc-900 border border-zinc-800">
            <Gavel className="h-6 w-6 text-terminal" />
          </div>
          <div>
            <h1 className="text-3xl font-mono tracking-tight text-zinc-100">Judging Portal</h1>
            <p className="text-sm text-zinc-500 font-sans">Evaluate pending team submissions dynamically.</p>
          </div>
        </div>
        <div className="flex space-x-4">
          <Button variant="outline" onClick={() => router.push('/judging/mentor-board')} className="border-blue-500/50 text-blue-500 hover:bg-blue-500/10">
            Mentor Command Center
          </Button>
          <Button variant="outline" onClick={() => router.push('/admin')} className="border-terminal/50 text-terminal hover:bg-terminal/10">
            <ArrowLeft className="h-4 w-4 mr-2" /> Admin Dashboard
          </Button>
          <Button variant="outline" onClick={handleLogout}>
            <LogOut className="h-4 w-4 mr-2" />
            Sign Out
          </Button>
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
        {/* Left Column: List of Submissions */}
        <div className="lg:col-span-1 rounded-lg border border-zinc-800 bg-zinc-900/50 p-6">
          <h2 className="text-xl font-mono text-zinc-200 mb-4">Pending Review ({submissions.length})</h2>
          <div className="space-y-3 max-h-[600px] overflow-y-auto pr-2">
            {submissions.length === 0 ? (
              <p className="text-zinc-500 italic">No submissions pending review.</p>
            ) : (
              submissions.map(sub => (
                <button
                  key={sub.id}
                  onClick={() => handleSelectSubmission(sub)}
                  className={`w-full text-left p-4 rounded-md border transition-all ${selectedSub?.id === sub.id ? 'border-terminal bg-terminal/10' : 'border-zinc-800 bg-zinc-950 hover:border-zinc-700'}`}
                >
                  <div className="flex justify-between items-center mb-1">
                    <div className="flex items-center space-x-2">
                      <span className="font-mono text-xs text-terminal">{sub.taskId}</span>
                      <span className="text-xs text-zinc-300 font-bold">{sub.teamName}</span>
                    </div>
                    <span className="text-xs text-zinc-500">Round {sub.roundNumber}</span>
                  </div>
                  <p className="text-sm font-medium text-zinc-300 line-clamp-1">{getPayloadData(sub.payload).githubUrl}</p>
                </button>
              ))
            )}
          </div>
        </div>

        {/* Right Column: Dynamic Rubric */}
        <div className="lg:col-span-2">
          {selectedSub ? (
            <div className="rounded-lg border border-zinc-800 bg-zinc-900/50 p-8 shadow-2xl">
              <div className="mb-6 pb-6 border-b border-zinc-800">
                <h2 className="text-2xl font-mono text-zinc-100 mb-2">Grading: {selectedSub.taskId}</h2>
                <div className="text-sm text-zinc-400 mb-4 flex items-center space-x-2">
                  <span>Team:</span>
                  <span className="font-bold text-zinc-200">{selectedSub.teamName}</span>
                </div>
                <a href={getPayloadData(selectedSub.payload).githubUrl} target="_blank" rel="noreferrer" className="text-terminal hover:underline text-sm mb-4 block">
                  {getPayloadData(selectedSub.payload).githubUrl}
                </a>
                <div className="bg-zinc-950 p-4 rounded-md border border-zinc-800 text-sm text-zinc-400 whitespace-pre-wrap">
                  {getPayloadData(selectedSub.payload).description || 'No description provided.'}
                </div>
              </div>

              <form onSubmit={handleSubmitGrade} className="space-y-8">
                <div className="space-y-6">
                  <h3 className="text-lg font-medium text-zinc-200">Dynamic Rubric</h3>
                  {Object.entries(scoreBreakdown).map(([category, score]) => (
                    <div key={category} className="space-y-2">
                      <div className="flex justify-between text-sm">
                        <span className="text-zinc-300 capitalize">{category}</span>
                        <span className="font-mono text-terminal">{score} / 100</span>
                      </div>
                      <input
                        type="range"
                        min="0"
                        max="100"
                        value={score}
                        onChange={(e) => handleScoreChange(category, e.target.value)}
                        className="w-full h-2 bg-zinc-800 rounded-lg appearance-none cursor-pointer accent-terminal"
                      />
                    </div>
                  ))}
                </div>

                <div className="space-y-2">
                  <label className="text-sm text-zinc-300">Feedback (Optional)</label>
                  <textarea
                    className="w-full bg-zinc-950 border border-zinc-800 rounded-md p-3 text-sm text-zinc-200 focus:outline-none focus:border-terminal min-h-[100px]"
                    placeholder="Provide constructive feedback for the team..."
                    value={feedback}
                    onChange={(e) => setFeedback(e.target.value)}
                  />
                </div>

                <div className="flex space-x-4">
                  <Button type="button" variant="outline" onClick={handleInstantReject} disabled={isSubmitting} className="w-1/3 border-red-500/50 text-red-500 hover:bg-red-500/10 hover:text-red-400">
                    Reject (0 pts)
                  </Button>
                  <Button type="submit" className="w-2/3" isLoading={isSubmitting}>
                    <CheckCircle className="w-4 h-4 mr-2" />
                    Submit Official Evaluation
                  </Button>
                </div>
              </form>
            </div>
          ) : (
            <div className="h-full flex flex-col items-center justify-center text-zinc-600 border border-dashed border-zinc-800 rounded-lg p-12 bg-zinc-900/20">
              <Gavel className="w-12 h-12 mb-4 opacity-20" />
              <p>Select a submission from the queue to begin grading.</p>
            </div>
          )}
        </div>
      </div>
    </main>
  );
}
