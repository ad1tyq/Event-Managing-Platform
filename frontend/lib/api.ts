const API_BASE_URL = 'http://localhost:8080/api';

export async function loginTeam(teamName: string, teamPasscode: string): Promise<{ token?: string, error?: string }> {
  const response = await fetch(`${API_BASE_URL}/login`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({ teamName, teamPasscode }),
  });

  if (!response.ok && response.status !== 401 && response.status !== 400) {
    throw new Error('Network response was not ok');
  }

  // The backend now returns JSON {"token": "..."} or {"error": "..."}
  return await response.json();
}

export async function submitProject(githubUrl: string, description: string, token: string): Promise<{ error?: string, id?: string }> {
  const response = await fetch(`${API_BASE_URL}/submit`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`
    },
    body: JSON.stringify({ githubUrl, description }),
  });

  if (!response.ok && response.status !== 400) {
     throw new Error('Network response was not ok');
  }
  
  return await response.json();
}

export async function loginAdmin(username: string, passwordHash: string): Promise<{ token?: string, message?: string, error?: string }> {
  const response = await fetch(`${API_BASE_URL}/user/login`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({ username, passwordHash }),
  });

  if (!response.ok && response.status !== 401) {
    throw new Error('Network response was not ok');
  }

  // The backend now returns JSON
  return await response.json();
}

export async function uploadCsv(file: File): Promise<string> {
  const formData = new FormData();
  // Based on user requirement, the key MUST be exactly "file", despite backend mismatch.
  formData.append('file', file);

  const response = await fetch(`${API_BASE_URL}/user/import`, {
    method: 'POST',
    body: formData,
  });

  if (!response.ok) {
    throw new Error('Network response was not ok');
  }

  // The backend returns a plain string
  return await response.text();
}

export async function fetchTeamStatus(token: string): Promise<{
  allowedTaskId: string;
  allowedRound: number;
  pending: boolean;
  gmeetLink?: string;
  queuePosition?: number;
  leaderboardPublished?: boolean;
}> {
  const response = await fetch(`${API_BASE_URL}/status`, {
    headers: {
      'Authorization': `Bearer ${token}`
    },
    cache: 'no-store'
  });
  if (!response.ok) throw new Error('Failed to fetch status');
  return await response.json();
}

export async function fetchSubmissionsByStatus(status: string, token: string): Promise<any[]> {
  const response = await fetch(`${API_BASE_URL}/admin/submissions?status=${status}`, {
    headers: {
      'Authorization': `Bearer ${token}`
    },
    cache: 'no-store'
  });
  if (!response.ok) throw new Error('Failed to fetch submissions');
  return await response.json();
}

export async function fetchEvent(eventId: number, token: string): Promise<any> {
  const response = await fetch(`${API_BASE_URL}/admin/events/${eventId}`, {
    headers: {
      'Authorization': `Bearer ${token}`
    },
    cache: 'no-store'
  });
  if (!response.ok) throw new Error('Failed to fetch event');
  return await response.json();
}

export async function submitEvaluation(submissionId: number, scoreBreakdown: Record<string, number>, feedback: string, token: string): Promise<any> {
  const response = await fetch(`${API_BASE_URL}/evaluate`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`
    },
    body: JSON.stringify({ submissionId, scoreBreakdown, feedback }),
  });
  if (!response.ok) {
    const errorBody = await response.json().catch(() => ({}));
    throw new Error(errorBody.error || 'Failed to submit evaluation');
  }
  return await response.json();
}

export async function fetchMyEvaluations(token: string): Promise<any[]> {
  const response = await fetch(`${API_BASE_URL}/evaluations/me`, {
    headers: { 'Authorization': `Bearer ${token}` },
    cache: 'no-store'
  });
  if (!response.ok) throw new Error('Failed to fetch my evaluations');
  return await response.json();
}

export async function finalizeSubmission(submissionId: number, token: string): Promise<any> {
  const response = await fetch(`${API_BASE_URL}/admin/submissions/${submissionId}/finalize`, {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${token}`
    }
  });
  if (!response.ok) {
    const errorBody = await response.json().catch(() => ({}));
    throw new Error(errorBody.error || 'Failed to finalize submission');
  }
  return await response.json();
}

export async function updateGlobalRound(eventId: number, newRound: number, token: string): Promise<any> {
  const response = await fetch(`${API_BASE_URL}/admin/events/${eventId}/round/${newRound}`, {
    method: 'PUT',
    headers: {
      'Authorization': `Bearer ${token}`
    }
  });
  if (!response.ok) {
    const errorBody = await response.json().catch(() => ({}));
    throw new Error(errorBody.error || 'Failed to update global round');
  }
  return await response.json();
}

export async function fetchLeaderboard(token: string): Promise<{ id: string; teamName: string; totalScore: number }[]> {
  const response = await fetch(`${API_BASE_URL}/admin/leaderboard`, {
    headers: {
      'Authorization': `Bearer ${token}`
    },
    cache: 'no-store'
  });
  if (!response.ok) throw new Error('Failed to fetch leaderboard');
  return await response.json();
}

export async function fetchTeamDetails(teamId: string, token: string): Promise<{ team: any; submissions: any[] }> {
  const response = await fetch(`${API_BASE_URL}/admin/teams/${teamId}`, {
    headers: {
      'Authorization': `Bearer ${token}`
    },
    cache: 'no-store'
  });
  if (!response.ok) {
    const err = await response.json().catch(() => ({}));
    throw new Error(err.error || 'Failed to fetch team details');
  }
  return await response.json();
}

export async function updateMeetingLink(eventId: number, meetingLink: string, token: string): Promise<any> {
  const response = await fetch(`${API_BASE_URL}/admin/events/${eventId}/meeting-link`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json', 'Authorization': `Bearer ${token}` },
    body: JSON.stringify({ meetingLink })
  });
  if (!response.ok) throw new Error('Failed to update meeting link');
  return await response.json();
}

export async function setActiveMeetingTeam(eventId: number, teamId: string, token: string): Promise<any> {
  const response = await fetch(`${API_BASE_URL}/admin/events/${eventId}/active-team/${teamId || 'none'}`, {
    method: 'PUT',
    headers: { 'Authorization': `Bearer ${token}` }
  });
  if (!response.ok) throw new Error('Failed to set active meeting team');
  return await response.json();
}

// ==========================================
// DEMO CALL API (Round 3)
// ==========================================

export async function fetchDemoCallsQueue(token: string): Promise<any[]> {
  const response = await fetch(`${API_BASE_URL}/admin/demo-calls/queue`, {
    headers: { 'Authorization': `Bearer ${token}` },
    cache: 'no-store'
  });
  if (!response.ok) throw new Error('Failed to fetch demo calls queue');
  return await response.json();
}

export async function inviteToCallDemo(demoCallId: number, meetingLink: string, token: string): Promise<any> {
  const response = await fetch(`${API_BASE_URL}/admin/demo-calls/${demoCallId}/call`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json', 'Authorization': `Bearer ${token}` },
    body: JSON.stringify({ meetingLink })
  });
  if (!response.ok) throw new Error('Failed to invite team to call');
  return await response.json();
}

export async function toggleLeaderboard(eventId: number, isPublished: boolean, token: string): Promise<any> {
  const response = await fetch(`${API_BASE_URL}/admin/events/${eventId}/leaderboard-toggle`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json', 'Authorization': `Bearer ${token}` },
    body: JSON.stringify({ isPublished })
  });
  if (!response.ok) throw new Error('Failed to toggle leaderboard');
  return await response.json();
}

export async function fetchParticipantLeaderboard(token: string): Promise<any[]> {
  const response = await fetch(`${API_BASE_URL}/leaderboard`, {
    headers: { 'Authorization': `Bearer ${token}` },
    cache: 'no-store'
  });
  if (!response.ok) {
    const err = await response.json().catch(()=>({}));
    throw new Error(err.error || 'Failed to fetch participant leaderboard');
  }
  return await response.json();
}

// ==========================================
// MENTORSHIP API (The Uber Engine)
// ==========================================

// --- Mentor Actions (Requires admin_token) ---

export async function getMentorStatus(token: string): Promise<any> {
  const response = await fetch(`${API_BASE_URL}/mentors/me/status`, {
    headers: { 'Authorization': `Bearer ${token}` }
  });
  if (!response.ok) throw new Error('Failed to fetch mentor status');
  return await response.json();
}

export async function updateMentorStatus(token: string, payload: { isActive?: boolean; currentStatus?: string; skills?: string }): Promise<any> {
  const response = await fetch(`${API_BASE_URL}/mentors/me/status`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json', 'Authorization': `Bearer ${token}` },
    body: JSON.stringify(payload)
  });
  if (!response.ok) throw new Error('Failed to update mentor status');
  return await response.json();
}

export async function getMentorSessions(token: string): Promise<any[]> {
  const response = await fetch(`${API_BASE_URL}/mentors/sessions`, {
    headers: { 'Authorization': `Bearer ${token}` }
  });
  if (!response.ok) throw new Error('Failed to fetch mentor sessions');
  return await response.json();
}

export async function acceptMentorSession(token: string, id: number, meetingLink: string): Promise<any> {
  const response = await fetch(`${API_BASE_URL}/mentors/sessions/${id}/accept`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json', 'Authorization': `Bearer ${token}` },
    body: JSON.stringify({ meetingLink })
  });
  if (!response.ok) {
    const err = await response.json().catch(()=>({}));
    throw new Error(err.error || 'Failed to accept session');
  }
  return await response.json();
}

export async function resolveMentorSession(token: string, id: number): Promise<any> {
  const response = await fetch(`${API_BASE_URL}/mentors/sessions/${id}/resolve`, {
    method: 'POST',
    headers: { 'Authorization': `Bearer ${token}` }
  });
  if (!response.ok) throw new Error('Failed to resolve session');
  return await response.json();
}

// --- Team Actions (Requires team_token) ---

export async function getAvailableMentors(token: string): Promise<any[]> {
  const response = await fetch(`${API_BASE_URL}/mentors/available`, {
    headers: { 'Authorization': `Bearer ${token}` }
  });
  if (!response.ok) throw new Error('Failed to fetch available mentors');
  return await response.json();
}

export async function getMyMentorRequest(token: string): Promise<any> {
  const response = await fetch(`${API_BASE_URL}/mentors/sessions/my-request`, {
    headers: { 'Authorization': `Bearer ${token}` }
  });
  if (!response.ok) throw new Error('Failed to fetch my mentor request');
  const text = await response.text();
  return text ? JSON.parse(text) : null;
}

export async function requestMentor(token: string, mentorId: number, issueDescription: string): Promise<any> {
  const response = await fetch(`${API_BASE_URL}/mentors/sessions/request`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json', 'Authorization': `Bearer ${token}` },
    body: JSON.stringify({ mentorId, issueDescription })
  });
  if (!response.ok) {
    const err = await response.json().catch(()=>({}));
    throw new Error(err.error || 'Failed to request mentor');
  }
  return await response.json();
}

export async function withdrawMentorRequest(token: string, id: number): Promise<any> {
  const response = await fetch(`${API_BASE_URL}/mentors/sessions/${id}/withdraw`, {
    method: 'DELETE',
    headers: { 'Authorization': `Bearer ${token}` }
  });
  if (!response.ok) {
    const err = await response.json().catch(()=>({}));
    throw new Error(err.error || 'Failed to withdraw request');
  }
  return await response.json();
}
