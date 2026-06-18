import React, { useState, useEffect } from 'react';
import { Button } from '@/components/ui/Button';
import { Input } from '@/components/ui/Input';
import { submitProject, fetchTeamStatus } from '@/lib/api';
import { useToast } from '@/components/ui/Toast';

export function SubmitForm() {
  const { showToast } = useToast();
  const [isLoading, setIsLoading] = useState(false);
  
  const [status, setStatus] = useState<{
    allowedTaskId: string;
    allowedRound: number;
    isPending: boolean;
  } | null>(null);
  
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
    if (status.isPending) {
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
        setStatus(prev => prev ? { ...prev, isPending: true } : prev);
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

  if (status.allowedTaskId === 'COMPLETED') {
    return (
      <div className="text-center p-8 border border-green-500/20 bg-green-500/10 rounded-lg">
        <h2 className="text-2xl font-bold text-green-400 mb-2">Event Completed!</h2>
        <p className="text-zinc-300">You have successfully submitted Round 3. Thank you for participating!</p>
      </div>
    );
  }

  if (status.allowedTaskId === 'WAITING_ROOM') {
    return (
      <div className="text-center p-8 border border-blue-500/20 bg-blue-500/10 rounded-lg">
        <h2 className="text-2xl font-bold text-blue-400 mb-2">Waiting Room</h2>
        <p className="text-zinc-300">Congratulations on finishing the round early! Hang tight, the next round has not started yet.</p>
      </div>
    );
  }

  return (
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

      {status.isPending && (
        <div className="text-sm text-yellow-500 bg-yellow-500/10 border border-yellow-500/20 p-3 rounded">
          You cannot resubmit because your submission is under review right now.
        </div>
      )}

      <Button type="submit" className="w-full" isLoading={isLoading} disabled={status.isPending}>
        Submit Project
      </Button>
    </form>
  );
}
