'use client';

import React, { useState, useEffect } from 'react';
import { getAvailableMentors, getMyMentorRequest, requestMentor, withdrawMentorRequest } from '@/lib/api';
import { Button } from '@/components/ui/Button';
import { useToast } from '@/components/ui/Toast';
import { HelpCircle, Clock, Video, X } from 'lucide-react';

export function MentorQueue() {
  const { showToast } = useToast();
  const [mentors, setMentors] = useState<any[]>([]);
  const [myRequest, setMyRequest] = useState<any>(null);
  const [isLoading, setIsLoading] = useState(true);
  
  const [selectedMentor, setSelectedMentor] = useState<any>(null);
  const [issueDescription, setIssueDescription] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);

  useEffect(() => {
    fetchData();
    const interval = setInterval(fetchData, 5000);
    return () => clearInterval(interval);
  }, []);

  const fetchData = async () => {
    const token = localStorage.getItem('team_token');
    if (!token) return;
    try {
      const [m, req] = await Promise.all([
        getAvailableMentors(token).catch(() => []),
        getMyMentorRequest(token).catch(() => null)
      ]);
      setMentors(m);
      setMyRequest(req);
    } catch (err) {
      console.error(err);
    } finally {
      setIsLoading(false);
    }
  };

  const handleRequest = async () => {
    if (!selectedMentor || !issueDescription.trim()) return;
    setIsSubmitting(true);
    try {
      const token = localStorage.getItem('team_token')!;
      await requestMentor(token, selectedMentor.userId, issueDescription);
      showToast('Mentor requested successfully!', 'success');
      setSelectedMentor(null);
      setIssueDescription('');
      await fetchData();
    } catch (err: any) {
      showToast(err.message, 'error');
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleWithdraw = async () => {
    if (!myRequest) return;
    setIsSubmitting(true);
    try {
      const token = localStorage.getItem('team_token')!;
      await withdrawMentorRequest(token, myRequest.id);
      showToast('Request withdrawn.', 'success');
      await fetchData();
    } catch (err: any) {
      showToast(err.message, 'error');
    } finally {
      setIsSubmitting(false);
    }
  };

  if (isLoading) return <div className="text-zinc-500 animate-pulse">Loading Live Radar...</div>;

  if (myRequest) {
    if (myRequest.status === 'REQUESTED') {
      return (
        <div className="p-6 border border-zinc-800 bg-zinc-900/50 rounded-lg text-center shadow-xl">
          <Clock className="h-10 w-10 text-yellow-500 mx-auto mb-4 animate-spin-slow" />
          <h3 className="text-xl font-mono text-zinc-100 mb-2">Waiting for {myRequest.mentorName}</h3>
          <p className="text-zinc-400 mb-6">Your request has been dispatched. The mentor will review your issue and provide a meeting link shortly.</p>
          <div className="bg-zinc-950 p-4 rounded text-left border border-zinc-800 mb-6">
            <span className="text-xs text-zinc-500 block mb-1">Issue Reported:</span>
            <p className="text-sm text-zinc-300">{myRequest.issueDescription}</p>
          </div>
          <Button onClick={handleWithdraw} isLoading={isSubmitting} variant="outline" className="border-red-500/50 text-red-500 hover:bg-red-500/10 w-full">
            Withdraw Request
          </Button>
        </div>
      );
    }

    if (myRequest.status === 'ACTIVE') {
      return (
        <div className="p-8 border border-green-500/30 bg-green-500/5 rounded-lg text-center shadow-2xl relative overflow-hidden">
          <div className="absolute top-0 left-0 w-full h-1 bg-gradient-to-r from-transparent via-green-500 to-transparent animate-pulse" />
          <Video className="h-12 w-12 text-green-500 mx-auto mb-4" />
          <h3 className="text-2xl font-mono text-zinc-100 mb-2">{myRequest.mentorName} Accepted!</h3>
          <p className="text-zinc-300 mb-8">The mentor is waiting for you in the call right now.</p>
          <a href={myRequest.meetingLink || '#'} target="_blank" rel="noreferrer">
            <Button className="bg-green-600 hover:bg-green-500 text-white w-full text-lg py-6 shadow-[0_0_15px_rgba(34,197,94,0.4)]">
              Join Live Meeting
            </Button>
          </a>
        </div>
      );
    }
  }

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between mb-4">
        <h3 className="text-lg font-mono text-zinc-100 flex items-center">
          <HelpCircle className="h-5 w-5 mr-2 text-terminal" />
          Live Mentor Radar
        </h3>
        <span className="text-xs bg-terminal/10 text-terminal px-2 py-1 rounded border border-terminal/20">
          {mentors.length} Mentors Online
        </span>
      </div>

      <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
        {mentors.map(m => (
          <div key={m.userId} className="p-4 border border-zinc-800 bg-zinc-900/50 rounded-lg flex flex-col justify-between">
            <div>
              <div className="flex items-center justify-between mb-2">
                <span className="font-mono font-medium text-zinc-200">{m.username}</span>
                <span className="flex items-center text-xs">
                  <span className={`h-2 w-2 rounded-full mr-1 ${m.currentStatus === 'AVAILABLE' ? 'bg-green-500' : 'bg-red-500'}`} />
                  {m.currentStatus}
                </span>
              </div>
              <p className="text-xs text-zinc-500 mb-4">{m.skills || 'General Support'}</p>
            </div>
            <Button 
              onClick={() => setSelectedMentor(m)} 
              disabled={m.currentStatus !== 'AVAILABLE'}
              variant="outline"
              className="w-full text-xs h-8 border-terminal/30 text-terminal hover:bg-terminal/10"
            >
              Hail Mentor
            </Button>
          </div>
        ))}
        {mentors.length === 0 && (
          <div className="col-span-1 sm:col-span-2 text-center p-6 border border-dashed border-zinc-800 rounded-lg text-zinc-500">
            No mentors are currently on duty.
          </div>
        )}
      </div>

      {selectedMentor && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 backdrop-blur-sm p-4">
          <div className="bg-zinc-950 border border-zinc-800 rounded-lg shadow-2xl w-full max-w-md p-6">
            <div className="flex justify-between items-center mb-6">
              <h3 className="text-xl font-mono text-zinc-100">Hail {selectedMentor.username}</h3>
              <button onClick={() => setSelectedMentor(null)} className="text-zinc-500 hover:text-zinc-300">
                <X className="h-5 w-5" />
              </button>
            </div>
            <div className="space-y-4">
              <div>
                <label className="block text-sm font-medium text-zinc-400 mb-1">Describe your roadblock</label>
                <textarea
                  className="w-full h-32 bg-zinc-900 border border-zinc-800 rounded-md p-3 text-sm text-zinc-200 focus:outline-none focus:border-terminal resize-none"
                  placeholder="e.g., We are getting a 500 CORS error on our FastAPI server..."
                  value={issueDescription}
                  onChange={e => setIssueDescription(e.target.value)}
                />
              </div>
              <div className="flex space-x-3">
                <Button onClick={() => setSelectedMentor(null)} variant="outline" className="flex-1">Cancel</Button>
                <Button onClick={handleRequest} isLoading={isSubmitting} className="flex-1">Send Hail</Button>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
