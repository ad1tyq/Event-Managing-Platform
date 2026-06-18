'use client';

import React, { useState, useEffect } from 'react';
import { useRouter, useParams } from 'next/navigation';
import { Shield, ArrowLeft, User, Key, Mail, Building, MapPin, Briefcase, GraduationCap, Clock, CheckCircle, XCircle, FileIcon } from 'lucide-react';
import { Button } from '@/components/ui/Button';
import { fetchTeamDetails } from '@/lib/api';

export default function TeamDetailsPage() {
  const router = useRouter();
  const params = useParams();
  const teamId = params.id as string;

  const [teamData, setTeamData] = useState<any>(null);
  const [submissions, setSubmissions] = useState<any[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [errorMsg, setErrorMsg] = useState('');

  const getPayloadData = (payload: any) => {
    if (!payload) return {};
    if (typeof payload === 'string') {
      try { return JSON.parse(payload); } catch (e) { return {}; }
    }
    return payload;
  };

  useEffect(() => {
    loadData();
  }, [teamId]);

  const loadData = async () => {
    const token = localStorage.getItem('admin_token');
    if (!token) {
      router.push('/');
      return;
    }

    setIsLoading(true);
    try {
      const data = await fetchTeamDetails(teamId, token);
      setTeamData(data.team);
      
      // Sort submissions by time descending (latest first)
      const sortedSubs = (data.submissions || []).sort((a: any, b: any) => {
        return new Date(b.submittedAt).getTime() - new Date(a.submittedAt).getTime();
      });
      setSubmissions(sortedSubs);
    } catch (err: any) {
      console.error(err);
      setErrorMsg(err.message || 'Failed to fetch team details');
    } finally {
      setIsLoading(false);
    }
  };

  if (isLoading) {
    return (
      <main className="flex min-h-screen items-center justify-center bg-zinc-950">
        <div className="text-zinc-400 font-mono flex items-center">
          <Shield className="h-5 w-5 mr-2 animate-pulse text-terminal" />
          Loading Team Profile...
        </div>
      </main>
    );
  }

  if (errorMsg || !teamData) {
    return (
      <main className="flex min-h-screen flex-col items-center justify-center bg-zinc-950 p-6">
        <div className="text-red-500 font-mono mb-4">Error: {errorMsg || 'Team not found'}</div>
        <Button onClick={() => router.push('/admin')}>Return to Admin Dashboard</Button>
      </main>
    );
  }

  // Parse JSON member details
  let parsedMember: any = {};
  try {
    if (teamData.memberDetails) {
      if (typeof teamData.memberDetails === 'string') {
        parsedMember = JSON.parse(teamData.memberDetails);
      } else {
        parsedMember = teamData.memberDetails;
      }
    }
  } catch (e) {
    console.error('Failed to parse member details', e);
  }

  return (
    <main className="flex min-h-screen flex-col items-center p-6 sm:p-24 bg-zinc-950">
      
      {/* Header */}
      <div className="w-full max-w-5xl flex justify-between items-center mb-8">
        <div className="flex items-center space-x-4">
          <Button variant="outline" onClick={() => router.push('/admin')} className="border-zinc-800 text-zinc-400 hover:text-zinc-200">
            <ArrowLeft className="h-4 w-4 mr-2" /> Back
          </Button>
          <h1 className="text-3xl font-mono tracking-tight text-zinc-100 flex items-center">
            {teamData.teamName}
          </h1>
          <span className="bg-terminal/10 text-terminal border border-terminal/20 px-3 py-1 rounded-full text-xs font-mono font-bold">
            {teamData.totalScore} PTS TOTAL
          </span>
        </div>
      </div>

      <div className="w-full max-w-5xl grid grid-cols-1 lg:grid-cols-3 gap-8">
        
        {/* Left Column: Team Profile */}
        <div className="lg:col-span-1 space-y-6">
          
          <div className="rounded-lg border border-zinc-800 bg-zinc-900/50 p-6 shadow-2xl">
            <h2 className="text-xl font-mono text-zinc-100 mb-6 flex items-center">
              <User className="h-5 w-5 mr-2 text-zinc-400" />
              Credentials
            </h2>
            <div className="space-y-4">
              <div className="bg-zinc-950 p-3 rounded border border-zinc-800">
                <span className="text-xs text-zinc-500 block mb-1">Unstop Team ID</span>
                <span className="font-mono text-zinc-200">{teamData.unstopTeamId}</span>
              </div>
              <div className="bg-zinc-950 p-3 rounded border border-zinc-800">
                <span className="text-xs text-zinc-500 block mb-1">Passcode</span>
                <span className="font-mono text-zinc-200 flex items-center">
                  <Key className="h-3 w-3 mr-2 text-zinc-500" />
                  {teamData.teamPasscode}
                </span>
              </div>
              <div className="bg-zinc-950 p-3 rounded border border-zinc-800">
                <span className="text-xs text-zinc-500 block mb-1">Registered At</span>
                <span className="font-mono text-zinc-400 text-sm">
                  {new Date(teamData.registeredAt).toLocaleString()}
                </span>
              </div>
            </div>
          </div>

          <div className="rounded-lg border border-zinc-800 bg-zinc-900/50 p-6 shadow-2xl">
            <h2 className="text-xl font-mono text-zinc-100 mb-6 flex items-center">
              <Building className="h-5 w-5 mr-2 text-zinc-400" />
              Member Demographics
            </h2>
            {Object.keys(parsedMember).length > 0 ? (
              <div className="space-y-4">
                <div className="flex items-center text-sm text-zinc-300">
                  <User className="h-4 w-4 mr-3 text-zinc-500" />
                  {parsedMember.first_name} {parsedMember.last_name} ({parsedMember.gender})
                </div>
                <div className="flex items-center text-sm text-zinc-300">
                  <Mail className="h-4 w-4 mr-3 text-zinc-500" />
                  {parsedMember.email}
                </div>
                <div className="flex items-center text-sm text-zinc-300">
                  <Building className="h-4 w-4 mr-3 text-zinc-500" />
                  {parsedMember.institute_name}
                </div>
                <div className="flex items-center text-sm text-zinc-300">
                  <GraduationCap className="h-4 w-4 mr-3 text-zinc-500" />
                  {parsedMember.course} - {parsedMember.course_specialization}
                </div>
                <div className="flex items-center text-sm text-zinc-300">
                  <Briefcase className="h-4 w-4 mr-3 text-zinc-500" />
                  Class of {parsedMember.graduating_year}
                </div>
                <div className="flex items-center text-sm text-zinc-300">
                  <MapPin className="h-4 w-4 mr-3 text-zinc-500" />
                  {parsedMember.location}
                </div>
              </div>
            ) : (
              <p className="text-sm italic text-zinc-500">No member details found.</p>
            )}
          </div>

        </div>

        {/* Right Column: Submissions Timeline */}
        <div className="lg:col-span-2 space-y-6">
          <div className="rounded-lg border border-zinc-800 bg-zinc-900/50 p-6 shadow-2xl">
            <h2 className="text-xl font-mono text-zinc-100 mb-6 flex items-center">
              <FileIcon className="h-5 w-5 mr-2 text-terminal" />
              Submission History
            </h2>
            
            {submissions.length === 0 ? (
              <div className="p-8 text-center rounded-lg border border-dashed border-zinc-800 bg-zinc-900/20 text-zinc-500 italic">
                This team has not submitted any tasks yet.
              </div>
            ) : (
              <div className="space-y-4 relative border-l border-zinc-800 ml-4 pl-6 pb-4">
                {submissions.map((sub, idx) => (
                  <div key={idx} className="relative">
                    {/* Timeline Dot */}
                    <div className={`absolute -left-[31px] top-1 h-3 w-3 rounded-full border-2 border-zinc-950 ${
                      sub.status === 'APPROVED' ? 'bg-green-500' :
                      sub.status === 'REJECTED' ? 'bg-red-500' :
                      'bg-yellow-500'
                    }`} />
                    
                    <div className="bg-zinc-950 border border-zinc-800 rounded-lg p-5">
                      <div className="flex justify-between items-start mb-4">
                        <div>
                          <div className="flex items-center space-x-3 mb-1">
                            <span className="font-mono font-bold text-zinc-200 text-lg">{sub.taskId}</span>
                            <span className="text-xs text-zinc-500 bg-zinc-900 px-2 py-1 rounded">Round {sub.roundNumber}</span>
                          </div>
                          <div className="flex items-center text-xs text-zinc-500 font-mono">
                            <Clock className="h-3 w-3 mr-1" />
                            {new Date(sub.submittedAt).toLocaleString()}
                          </div>
                        </div>
                        <div className="flex flex-col items-end">
                          <span className={`text-xs font-mono font-bold px-2 py-1 rounded border mb-2 ${
                            sub.status === 'APPROVED' ? 'text-green-400 bg-green-400/10 border-green-400/20' :
                            sub.status === 'REJECTED' ? 'text-red-400 bg-red-400/10 border-red-400/20' :
                            'text-yellow-400 bg-yellow-400/10 border-yellow-400/20'
                          }`}>
                            {sub.status}
                          </span>
                          {sub.averageScore !== null && sub.averageScore !== undefined && (
                            <span className="text-sm font-mono text-zinc-300">
                              Score: <span className={sub.status === 'APPROVED' ? 'text-green-400' : 'text-zinc-100'}>{sub.averageScore}</span>
                            </span>
                          )}
                        </div>
                      </div>

                      {/* Payload (e.g. GitHub URL) */}
                      {sub.payload && (
                        <div className="mb-4">
                          <span className="text-xs text-zinc-500 block mb-1">Submission Payload</span>
                          <div className="bg-zinc-900 p-2 rounded text-sm font-mono text-zinc-300 break-all">
                            {getPayloadData(sub.payload).githubUrl || (typeof sub.payload === 'string' ? sub.payload : JSON.stringify(sub.payload))}
                          </div>
                        </div>
                      )}

                      {/* Rejection / Feedback */}
                      {sub.rejectionReason && (
                        <div className={`mt-4 p-3 rounded text-sm ${
                          sub.status === 'APPROVED' 
                            ? 'bg-zinc-900 border border-zinc-800 text-zinc-300' 
                            : 'bg-red-500/10 border border-red-500/20 text-red-200'
                        }`}>
                          <span className="text-xs uppercase tracking-wider font-semibold opacity-50 block mb-1">Judge/Admin Feedback</span>
                          {sub.rejectionReason}
                        </div>
                      )}
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>

      </div>
    </main>
  );
}
