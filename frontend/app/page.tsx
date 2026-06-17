'use client';

import React, { useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { Terminal, Users, Shield } from 'lucide-react';
import { Input } from '@/components/ui/Input';
import { Button } from '@/components/ui/Button';
import { Toast, useToast } from '@/components/ui/Toast';
import { loginTeam, loginAdmin } from '@/lib/api';

export default function LoginPage() {
  const router = useRouter();
  const { toast, showToast, hideToast } = useToast();
  const [isLoading, setIsLoading] = useState(false);
  const [loginType, setLoginType] = useState<'team' | 'staff'>('team');
  
  const [formData, setFormData] = useState({
    identifier: '',
    secret: '',
  });
  const [errorMsg, setErrorMsg] = useState('');

  const [cursorVisible, setCursorVisible] = useState(true);

  // Blinking cursor effect
  useEffect(() => {
    const interval = setInterval(() => {
      setCursorVisible((v) => !v);
    }, 530);
    return () => clearInterval(interval);
  }, []);

  // Clear errors and form data when switching types
  const handleTypeSwitch = (type: 'team' | 'staff') => {
    setLoginType(type);
    setErrorMsg('');
    setFormData({ identifier: '', secret: '' });
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setErrorMsg('');
    
    if (!formData.identifier.trim() || !formData.secret.trim()) {
      setErrorMsg('Both fields are required.');
      return;
    }

    setIsLoading(true);

    try {
      if (loginType === 'team') {
        const response = await loginTeam(formData.identifier, formData.secret);
        if (response.token) {
          localStorage.setItem('team_token', response.token);
          showToast('Team Login Successful', 'success');
          setTimeout(() => {
            router.push('/dashboard');
          }, 1500);
        } else if (response.error) {
          setErrorMsg(response.error);
          showToast('Login Failed', 'error');
        } else {
          setErrorMsg('Invalid Credentials');
          showToast('Login Failed', 'error');
        }
      } else {
        const response = await loginAdmin(formData.identifier, formData.secret);
        if (response.token) {
          localStorage.setItem('admin_token', response.token);
          showToast('Staff Login Successful', 'success');
          setTimeout(() => {
            router.push('/admin');
          }, 1500);
        } else if (response.error) {
          setErrorMsg(response.error);
          showToast('Login Failed', 'error');
        } else {
          setErrorMsg('Invalid Credentials');
          showToast('Login Failed', 'error');
        }
      }
    } catch (err) {
      console.error(err);
      setErrorMsg('Failed to connect to server.');
      showToast('Connection Error', 'error');
    } finally {
      setIsLoading(false);
    }
  };

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
  };

  return (
    <main className="flex min-h-screen flex-col items-center justify-center p-6 sm:p-24">
      {toast && (
        <Toast
          message={toast.message}
          type={toast.type}
          onClose={hideToast}
        />
      )}
      
      <div className="w-full max-w-md space-y-8">
        <div className="flex flex-col items-center space-y-2 text-center">
          <div className="flex h-12 w-12 items-center justify-center rounded-full bg-zinc-900 border border-zinc-800 mb-4">
            <Terminal className="h-6 w-6 text-terminal" />
          </div>
          <h1 className="text-3xl font-mono tracking-tight text-zinc-100 flex items-center">
            Unlock&apos;D
            <span
              className={`ml-1 inline-block h-6 w-3 bg-terminal transition-opacity ${
                cursorVisible ? 'opacity-100' : 'opacity-0'
              }`}
            />
          </h1>
          <p className="text-sm font-sans text-zinc-500">
            Progressive product-building hackathon platform
          </p>
        </div>

        <div className="rounded-lg border border-zinc-800 bg-zinc-900/50 p-8 shadow-2xl backdrop-blur-sm">
          {/* Segmented Control */}
          <div className="flex p-1 mb-8 space-x-1 bg-zinc-950 rounded-lg border border-zinc-800">
            <button
              onClick={() => handleTypeSwitch('team')}
              className={`flex-1 flex items-center justify-center py-2 text-sm font-medium rounded-md transition-all ${
                loginType === 'team'
                  ? 'bg-zinc-800 text-zinc-100 shadow'
                  : 'text-zinc-400 hover:text-zinc-200 hover:bg-zinc-800/50'
              }`}
            >
              <Users className="w-4 h-4 mr-2" />
              Team
            </button>
            <button
              onClick={() => handleTypeSwitch('staff')}
              className={`flex-1 flex items-center justify-center py-2 text-sm font-medium rounded-md transition-all ${
                loginType === 'staff'
                  ? 'bg-zinc-800 text-zinc-100 shadow'
                  : 'text-zinc-400 hover:text-zinc-200 hover:bg-zinc-800/50'
              }`}
            >
              <Shield className="w-4 h-4 mr-2" />
              Staff
            </button>
          </div>

          <form onSubmit={handleSubmit} className="space-y-6">
            <div className="space-y-4">
              <Input
                label={loginType === 'team' ? 'Team Name' : 'Username'}
                name="identifier"
                type="text"
                placeholder={loginType === 'team' ? 'e.g. byte_me' : 'admin_user'}
                value={formData.identifier}
                onChange={handleChange}
                disabled={isLoading}
                required
              />
              
              <Input
                label={loginType === 'team' ? 'Passcode' : 'Password'}
                name="secret"
                type="password"
                placeholder="••••••••"
                value={formData.secret}
                onChange={handleChange}
                disabled={isLoading}
                required
              />
            </div>

            {errorMsg && (
              <div className="text-sm font-mono text-red-500 bg-red-500/10 border border-red-500/20 rounded p-3">
                &gt; Error: {errorMsg}
              </div>
            )}

            <Button
              type="submit"
              className="w-full"
              isLoading={isLoading}
            >
              Initialize Session
            </Button>
          </form>
        </div>
        
        <div className="text-center">
          <p className="text-xs font-mono text-zinc-600">
            System status: <span className="text-terminal">Online</span>
          </p>
        </div>
      </div>
    </main>
  );
}
