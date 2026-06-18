'use client';

import React, { useState, useEffect } from 'react';
import { getMentorStatus, updateMentorStatus, getMentorSessions, acceptMentorSession, resolveMentorSession } from '@/lib/api';
import { Button } from '@/components/ui/Button';
import { useToast } from '@/components/ui/Toast';
import { useRouter } from 'next/navigation';
import { Power, UserCheck, PlayCircle, CheckCircle, ArrowLeft } from 'lucide-react';

export default function MentorBoardPage() {
  const router = useRouter();
  const { showToast } = useToast();
  
  const [profile, setProfile] = useState<any>(null);
  const [sessions, setSessions] = useState<any[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  
  const [acceptingId, setAcceptingId] = useState<number | null>(null);
  const [meetingLink, setMeetingLink] = useState('');

  useEffect(() => {
    fetchData();
    const interval = setInterval(fetchData, 5000);
    return () => clearInterval(interval);
  }, []);

  const fetchData = async () => {
    const token = localStorage.getItem('admin_token');
    if (!token) {
      router.push('/');
      return;
    }
    try {
      const [p, s] = await Promise.all([
        getMentorStatus(token),
        getMentorSessions(token).catch(() => [])
      ]);
      setProfile(p);
      setSessions(s);
    } catch (err) {
      console.error(err);
    } finally {
      setIsLoading(false);
    }
  };

  const handleToggleDuty = async () => {
    try {
      const token = localStorage.getItem('admin_token')!;
      await updateMentorStatus(token, { isActive: !profile.isActive, currentStatus: !profile.isActive ? 'AVAILABLE' : 'OFFLINE' });
      showToast(!profile.isActive ? 'You are now ON DUTY' : 'You are now OFF DUTY', 'success');
      await fetchData();
    } catch (err: any) {
      showToast(err.message, 'error');
    }
  };

  const handleUpdateSkills = async (skills: string) => {
    try {
      const token = localStorage.getItem('admin_token')!;
      await updateMentorStatus(token, { skills });
      showToast('Skills updated', 'success');
      await fetchData();
    } catch (err: any) {
      showToast(err.message, 'error');
    }
  };

  const handleAccept = async (id: number) => {
    if (!meetingLink.trim()) {
      showToast('Please provide a meeting link', 'error');
      return;
    }
    try {
      const token = localStorage.getItem('admin_token')!;
      await acceptMentorSession(token, id, meetingLink);
      showToast('Session accepted. Your status is now BUSY.', 'success');
      setAcceptingId(null);
      setMeetingLink('');
      await fetchData();
    } catch (err: any) {
      showToast(err.message, 'error');
    }
  };

  const handleResolve = async (id: number) => {
    try {
      const token = localStorage.getItem('admin_token')!;
      await resolveMentorSession(token, id);
      showToast('Session resolved. You are back to AVAILABLE.', 'success');
      await fetchData();
    } catch (err: any) {
      showToast(err.message, 'error');
    }
  };

  if (isLoading) return <div className="min-h-screen bg-zinc-950 p-8 text-zinc-500">Loading Command Center...</div>;

  const activeSession = sessions.find(s => s.status === 'ACTIVE');
  const pendingSessions = sessions.filter(s => s.status === 'REQUESTED');

  return (
    <main className="min-h-screen bg-zinc-950 p-6 sm:p-24 flex flex-col items-center">
      <div className="w-full max-w-4xl flex justify-between items-center mb-8">
        <h1 className="text-3xl font-mono text-zinc-100 flex items-center">
          <UserCheck className="h-8 w-8 mr-3 text-terminal" />
          Mentor Command Center
        </h1>
        <Button variant="outline" onClick={() => router.push('/judging')} className="border-zinc-800 text-zinc-400">
          <ArrowLeft className="h-4 w-4 mr-2" /> Back to Judging
        </Button>
      </div>

      {/* CLOCK IN SWITCH */}
      <div className="w-full max-w-4xl rounded-lg border border-zinc-800 bg-zinc-900/50 p-8 shadow-2xl mb-8 flex flex-col sm:flex-row items-center justify-between">
        <div>
          <h2 className="text-xl font-mono text-zinc-100 mb-2">Duty Status</h2>
          <p className="text-sm text-zinc-400 max-w-md">Toggle to let teams know you are available. You will be automatically marked as BUSY when you accept a request.</p>
          <div className="mt-4 flex items-center space-x-2">
            <span className="text-sm text-zinc-500">Current State:</span>
            <span className={`font-mono font-bold px-2 py-1 rounded text-xs ${profile?.currentStatus === 'AVAILABLE' ? 'bg-green-500/20 text-green-500' : profile?.currentStatus === 'BUSY' ? 'bg-red-500/20 text-red-500' : 'bg-zinc-800 text-zinc-500'}`}>
              {profile?.currentStatus}
            </span>
          </div>
        </div>
        <div className="mt-6 sm:mt-0 flex flex-col items-end space-y-4">
          <Button 
            onClick={handleToggleDuty} 
            className={`px-8 py-6 text-lg font-bold shadow-2xl ${profile?.isActive ? 'bg-red-600 hover:bg-red-500 text-white' : 'bg-green-600 hover:bg-green-500 text-white'}`}
          >
            <Power className="mr-2 h-6 w-6" />
            {profile?.isActive ? 'Clock Out (Go Offline)' : 'Clock In (Go On Duty)'}
          </Button>
        </div>
      </div>

      {profile?.isActive && (
        <div className="w-full max-w-4xl grid grid-cols-1 md:grid-cols-2 gap-8">
          
          {/* INCOMING QUEUE */}
          <div className="rounded-lg border border-zinc-800 bg-zinc-900/50 p-6 shadow-xl flex flex-col">
            <h3 className="text-lg font-mono text-zinc-100 mb-4 flex items-center">
              <PlayCircle className="h-5 w-5 mr-2 text-yellow-500" />
              Incoming Hails ({pendingSessions.length})
            </h3>
            <div className="space-y-4 overflow-y-auto flex-1">
              {pendingSessions.length === 0 ? (
                <div className="p-8 text-center text-zinc-500 border border-dashed border-zinc-800 rounded-lg">
                  No active requests.
                </div>
              ) : (
                pendingSessions.map(s => (
                  <div key={s.id} className="p-4 border border-zinc-800 bg-zinc-950 rounded-lg">
                    <div className="flex justify-between items-center mb-2">
                      <span className="font-medium text-zinc-200">{s.teamName}</span>
                      <span className="text-xs text-zinc-500">{Math.round((Date.now() - new Date(s.requestedAt).getTime()) / 60000)}m ago</span>
                    </div>
                    <p className="text-sm text-zinc-400 mb-4 bg-zinc-900 p-2 rounded">{s.issueDescription}</p>
                    
                    {acceptingId === s.id ? (
                      <div className="space-y-2">
                        <input 
                          type="url" 
                          placeholder="Paste GMeet Link here..." 
                          className="w-full bg-zinc-900 border border-terminal/50 rounded p-2 text-sm text-zinc-200 focus:outline-none"
                          value={meetingLink}
                          onChange={e => setMeetingLink(e.target.value)}
                        />
                        <div className="flex space-x-2">
                          <Button onClick={() => handleAccept(s.id)} className="flex-1 bg-terminal hover:bg-terminal/80 text-black text-xs">Confirm Accept</Button>
                          <Button onClick={() => setAcceptingId(null)} variant="outline" className="text-xs">Cancel</Button>
                        </div>
                      </div>
                    ) : (
                      <Button 
                        onClick={() => setAcceptingId(s.id)} 
                        disabled={!!activeSession}
                        className="w-full bg-blue-600 hover:bg-blue-500 text-white"
                      >
                        Accept Request
                      </Button>
                    )}
                  </div>
                ))
              )}
            </div>
          </div>

          {/* ACTIVE SESSION */}
          <div className="rounded-lg border border-zinc-800 bg-zinc-900/50 p-6 shadow-xl flex flex-col">
            <h3 className="text-lg font-mono text-zinc-100 mb-4 flex items-center">
              <CheckCircle className="h-5 w-5 mr-2 text-green-500" />
              Active Session
            </h3>
            {activeSession ? (
              <div className="p-6 border border-green-500/30 bg-green-500/5 rounded-lg text-center flex-1 flex flex-col justify-center">
                <h4 className="text-xl font-mono text-zinc-100 mb-2">Helping {activeSession.teamName}</h4>
                <p className="text-sm text-zinc-400 mb-6 bg-zinc-950 p-3 rounded-lg text-left">{activeSession.issueDescription}</p>
                <Button 
                  onClick={() => handleResolve(activeSession.id)}
                  className="bg-green-600 hover:bg-green-500 text-white py-6 text-lg w-full shadow-[0_0_15px_rgba(34,197,94,0.3)]"
                >
                  Resolve Ticket & Go Available
                </Button>
              </div>
            ) : (
              <div className="p-8 text-center text-zinc-500 border border-dashed border-zinc-800 rounded-lg flex-1 flex items-center justify-center">
                You are currently AVAILABLE.<br/>Accept a request from the queue to start.
              </div>
            )}
            
            <div className="mt-8 border-t border-zinc-800 pt-4">
               <label className="text-sm text-zinc-400 block mb-2">Update your listed skills (visible to teams)</label>
               <div className="flex space-x-2">
                 <input 
                   type="text" 
                   className="flex-1 bg-zinc-950 border border-zinc-800 rounded p-2 text-sm text-zinc-200 focus:border-terminal outline-none"
                   placeholder="React, Spring, Python..."
                   defaultValue={profile?.skills || ''}
                   onBlur={e => handleUpdateSkills(e.target.value)}
                 />
               </div>
            </div>
          </div>

        </div>
      )}

    </main>
  );
}
