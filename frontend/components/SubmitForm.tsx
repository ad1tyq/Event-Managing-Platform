import React, { useState, useEffect } from 'react';
import { Button } from '@/components/ui/Button';
import { Input } from '@/components/ui/Input';
import { submitProject, fetchTeamStatus, fetchParticipantLeaderboard } from '@/lib/api';
import { useToast } from '@/components/ui/Toast';
import { Trophy } from 'lucide-react';
import { MentorQueue } from '@/components/MentorQueue';

export function SubmitForm() {
  const { showToast } = useToast();
  const [isLoading, setIsLoading] = useState(false);
  
  const [status, setStatus] = useState<{
    allowedTaskId: string;
    allowedRound: number;
    pending: boolean;
    gmeetLink?: string;
    queuePosition?: number;
    leaderboardPublished?: boolean;
  } | null>(null);

  const [leaderboardData, setLeaderboardData] = useState<{ id: string; teamName: string; totalScore: number }[]>([]);
  
  const [formData, setFormData] = useState({
    githubUrl: '',
    description: '',
  });

  useEffect(() => {
    async function loadStatus() {
      const token = localStorage.getItem('team_token');
      if (!token) return;
      try {
        const teamStatus = await fetchTeamStatus(token);
        setStatus(teamStatus);
        
        if (teamStatus.leaderboardPublished) {
           const lBoard = await fetchParticipantLeaderboard(token);
           setLeaderboardData(lBoard);
        }
      } catch(err) {
        console.error(err);
      }
    }
    loadStatus();
  }, []);

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ 
      ...prev, 
      [name]: value 
    }));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!status) return;
    if (status.pending) {
      showToast('You cannot resubmit because your submission is under review right now.', 'error');
      return;
    }
    
    setIsLoading(true);

    try {
      const token = localStorage.getItem('team_token');
      if (!token) {
        showToast('Authentication error. Please log in again.', 'error');
        return;
      }

      const response = await submitProject(
        formData.githubUrl,
        formData.description,
        token
      );

      if (response.error) {
        showToast(response.error, 'error');
      } else {
        showToast('Submission successful!', 'success');
        // Update local status so they are instantly locked
        setStatus(prev => prev ? { ...prev, pending: true } : prev);
      }
    } catch (err) {
      showToast('Failed to submit. Please try again.', 'error');
      console.error(err);
    } finally {
      setIsLoading(false);
    }
  };

  if (!status) {
    return <div className="text-zinc-400">Loading progress...</div>;
  }

  const renderLeaderboard = () => {
    if (!status?.leaderboardPublished) return null;
    return (
      <div className="mt-8 rounded-lg border border-zinc-800 bg-zinc-900/50 p-6 shadow-2xl text-left">
        <h2 className="text-xl font-mono text-zinc-100 mb-4 flex items-center">
           <Trophy className="h-5 w-5 mr-2 text-yellow-500" />
           Global Leaderboard
        </h2>
        <div className="space-y-2 max-h-[300px] overflow-y-auto pr-2">
          {leaderboardData.length === 0 ? (
            <p className="text-zinc-500 italic text-sm">No scores recorded yet.</p>
          ) : (
            leaderboardData.map((team, idx) => (
              <div
                key={idx}
                className="flex justify-between items-center p-3 rounded bg-zinc-950 border border-zinc-800"
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
    );
  };

  if (status.allowedTaskId === 'COMPLETED') {
    return (
      <div className="space-y-8">
        <div className="text-center p-8 border border-green-500/20 bg-green-500/10 rounded-lg">
          <h2 className="text-2xl font-bold text-green-400 mb-2">Event Completed!</h2>
          <p className="text-zinc-300">You have successfully finished the event. Thank you for participating!</p>
        </div>
        {renderLeaderboard()}
      </div>
    );
  }

  if (status.allowedTaskId === 'WAITING_ROOM') {
    return (
      <div className="space-y-8">
        <div className="text-center p-8 border border-blue-500/20 bg-blue-500/10 rounded-lg">
          <h2 className="text-2xl font-bold text-blue-400 mb-2">Waiting Room</h2>
          <p className="text-zinc-300">Congratulations on finishing the round early! Hang tight, the next round has not started yet.</p>
        </div>
        {renderLeaderboard()}
      </div>
    );
  }

  if (status.allowedTaskId === 'MEETING_WAITING_ROOM') {
    return (
      <div className="space-y-8 text-center">
        <div className="p-8 border border-zinc-800 bg-zinc-900/50 rounded-lg shadow-2xl">
          <h2 className="text-2xl font-mono text-zinc-100 mb-2">Round 3 Waiting Room</h2>
          <p className="text-zinc-400 mb-6">You are currently in the queue. Please wait for the admins to invite you to the live call.</p>
          <div className="inline-block px-6 py-3 border border-terminal/30 bg-terminal/10 rounded-md">
            <span className="text-sm text-zinc-400 block mb-1">Queue Position</span>
            <span className="text-4xl font-mono font-bold text-terminal">{status.queuePosition || 1}</span>
          </div>
        </div>
        {renderLeaderboard()}
      </div>
    );
  }

  if (status.allowedTaskId === 'JOIN_MEETING') {
    return (
      <div className="space-y-8 text-center">
        <div className="p-12 border border-green-500/20 bg-green-500/5 rounded-lg shadow-2xl">
          <h2 className="text-3xl font-mono font-bold text-green-400 mb-4 animate-pulse">It's Your Turn!</h2>
          <p className="text-zinc-300 mb-8">The judges are ready for you. Click the button below to join the live GMeet call.</p>
          <a href={status.gmeetLink || '#'} target="_blank" rel="noreferrer">
            <Button className="w-full sm:w-auto text-lg px-8 py-6 bg-green-600 hover:bg-green-500 text-white shadow-[0_0_15px_rgba(34,197,94,0.5)]">
              Join Live Call Now
            </Button>
          </a>
        </div>
        {renderLeaderboard()}
      </div>
    );
  }

  return (
    <div className="space-y-8">
      <form onSubmit={handleSubmit} className="space-y-6 text-left">
      <div className="space-y-4">
        <div>
          <label className="mb-2 block text-sm font-mono text-zinc-400">
            Current Task
          </label>
          <div className="w-full rounded-md border border-zinc-800 bg-zinc-950 px-3 py-2 text-sm text-zinc-500 font-mono">
            {status.allowedTaskId}
          </div>
        </div>

        <Input
          label="GitHub Repository URL"
          name="githubUrl"
          type="url"
          placeholder="https://github.com/team/repo"
          value={formData.githubUrl}
          onChange={handleChange}
          required
        />

        <div className="space-y-1">
          <label className="text-sm font-medium text-zinc-300">Project Description</label>
          <textarea
            name="description"
            className="flex min-h-[120px] w-full rounded-md border border-zinc-800 bg-zinc-950 px-3 py-2 text-sm text-zinc-100 placeholder:text-zinc-500 focus:outline-none focus:ring-2 focus:ring-terminal/50 disabled:cursor-not-allowed disabled:opacity-50"
            placeholder={status.allowedRound === 1 ? "Tell us about the feature you just built..." : "Summarize your final polish and demo for this round..."}
            value={formData.description}
            onChange={handleChange}
            required
          />
        </div>
      </div>

      {status.pending && (
        <div className="text-sm text-yellow-500 bg-yellow-500/10 border border-yellow-500/20 p-3 rounded">
          You cannot resubmit because your submission is under review right now.
        </div>
      )}

      <Button type="submit" className="w-full" isLoading={isLoading} disabled={status.pending}>
        Submit Project
      </Button>
    </form>
    
    <div className="pt-8 mt-8 border-t border-zinc-800">
       <MentorQueue />
    </div>

    {renderLeaderboard()}
  </div>
  );
}
