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

export async function submitProject(githubUrl: string, description: string, roundNumber: number, taskId: string, token: string): Promise<{ error?: string, id?: string }> {
  const response = await fetch(`${API_BASE_URL}/submit`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`
    },
    body: JSON.stringify({ githubUrl, description, roundNumber, taskId }),
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
    }
  });
  if (!response.ok) throw new Error('Failed to fetch status');
  return await response.json();
}
