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
  isPending: boolean;
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
