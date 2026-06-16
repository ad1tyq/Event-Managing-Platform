'use client';

import React, { useState, useRef } from 'react';
import { UploadCloud, File as FileIcon, X, CheckCircle, AlertCircle } from 'lucide-react';
import { Button } from '@/components/ui/Button';
import { Toast, useToast } from '@/components/ui/Toast';
import { uploadCsv } from '@/lib/api';
import { useRouter } from 'next/navigation';

export default function AdminPage() {
  const router = useRouter();
  const { toast, showToast, hideToast } = useToast();
  const [isDragging, setIsDragging] = useState(false);
  const [file, setFile] = useState<File | null>(null);
  const [isLoading, setIsLoading] = useState(false);
  const [result, setResult] = useState<{ message: string; type: 'success' | 'error' } | null>(null);
  const fileInputRef = useRef<HTMLInputElement>(null);

  const handleDragOver = (e: React.DragEvent) => {
    e.preventDefault();
    setIsDragging(true);
  };

  const handleDragLeave = (e: React.DragEvent) => {
    e.preventDefault();
    setIsDragging(false);
  };

  const handleDrop = (e: React.DragEvent) => {
    e.preventDefault();
    setIsDragging(false);
    
    if (e.dataTransfer.files && e.dataTransfer.files.length > 0) {
      const droppedFile = e.dataTransfer.files[0];
      validateAndSetFile(droppedFile);
    }
  };

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (e.target.files && e.target.files.length > 0) {
      validateAndSetFile(e.target.files[0]);
    }
  };

  const validateAndSetFile = (selectedFile: File) => {
    setResult(null);
    if (selectedFile.type === 'text/csv' || selectedFile.name.endsWith('.csv')) {
      setFile(selectedFile);
    } else {
      showToast('Please upload a valid .csv file', 'error');
    }
  };

  const handleUpload = async () => {
    if (!file) return;

    setIsLoading(true);
    setResult(null);

    try {
      const response = await uploadCsv(file);
      
      if (response.startsWith('Import Successful')) {
        setResult({ message: response, type: 'success' });
        showToast(response, 'success');
        setFile(null);
      } else {
        setResult({ message: response, type: 'error' });
        showToast('Upload Failed', 'error');
      }
    } catch (err: any) {
      console.error(err);
      setResult({ message: 'Failed to connect to server.', type: 'error' });
      showToast('Connection Error', 'error');
    } finally {
      setIsLoading(false);
    }
  };

  const handleLogout = () => {
    localStorage.removeItem('admin_token');
    router.push('/');
  };

  return (
    <main className="flex min-h-screen flex-col items-center p-6 sm:p-24 bg-zinc-950">
      {/* Header bar with Logout */}
      <div className="w-full max-w-2xl flex justify-end mb-8">
        <Button variant="outline" onClick={handleLogout}>
          Terminate Session
        </Button>
      </div>

      {toast && (
        <Toast
          message={toast.message}
          type={toast.type}
          onClose={hideToast}
        />
      )}

      <div className="w-full max-w-2xl space-y-8">
        <div className="flex flex-col items-center space-y-2 text-center">
          <h1 className="text-3xl font-mono tracking-tight text-zinc-100">
            Admin Ingestion Dashboard
          </h1>
          <p className="text-sm font-sans text-zinc-500">
            Upload participant data CSV for database population.
          </p>
        </div>

        <div className="rounded-lg border border-zinc-800 bg-zinc-900/50 p-8 shadow-2xl backdrop-blur-sm">
          {!file ? (
            <div
              className={`flex flex-col items-center justify-center rounded-lg border-2 border-dashed p-12 transition-colors ${
                isDragging
                  ? 'border-terminal bg-terminal/5'
                  : 'border-zinc-800 bg-zinc-950 hover:border-zinc-700 hover:bg-zinc-900'
              }`}
              onDragOver={handleDragOver}
              onDragLeave={handleDragLeave}
              onDrop={handleDrop}
            >
              <UploadCloud className="mb-4 h-10 w-10 text-zinc-500" />
              <p className="mb-2 text-sm font-sans text-zinc-400">
                Drag and drop your CSV file here, or
              </p>
              <Button
                variant="outline"
                onClick={() => fileInputRef.current?.click()}
              >
                Browse Files
              </Button>
              <input
                type="file"
                ref={fileInputRef}
                onChange={handleFileChange}
                accept=".csv"
                className="hidden"
              />
              <p className="mt-4 text-xs font-mono text-zinc-600">
                Supports: .csv
              </p>
            </div>
          ) : (
            <div className="flex flex-col space-y-6">
              <div className="flex items-center justify-between rounded-md border border-zinc-800 bg-zinc-950 p-4">
                <div className="flex items-center space-x-4">
                  <div className="flex h-10 w-10 items-center justify-center rounded bg-zinc-900">
                    <FileIcon className="h-5 w-5 text-terminal" />
                  </div>
                  <div>
                    <p className="text-sm font-mono text-zinc-200">{file.name}</p>
                    <p className="text-xs font-sans text-zinc-500">
                      {(file.size / 1024).toFixed(2)} KB
                    </p>
                  </div>
                </div>
                <button
                  onClick={() => setFile(null)}
                  disabled={isLoading}
                  className="rounded-md p-2 text-zinc-500 hover:bg-zinc-900 hover:text-zinc-300 disabled:opacity-50"
                >
                  <X className="h-4 w-4" />
                </button>
              </div>

              <Button
                onClick={handleUpload}
                isLoading={isLoading}
                className="w-full"
              >
                Execute Import
              </Button>
            </div>
          )}

          {result && (
            <div className={`mt-6 flex items-start space-x-3 rounded-md p-4 text-sm font-mono ${
              result.type === 'success' 
                ? 'bg-terminal/10 text-terminal border border-terminal/20' 
                : 'bg-red-500/10 text-red-500 border border-red-500/20'
            }`}>
              {result.type === 'success' ? (
                <CheckCircle className="h-5 w-5 shrink-0" />
              ) : (
                <AlertCircle className="h-5 w-5 shrink-0" />
              )}
              <div>
                <p className="font-semibold">System Response:</p>
                <p className="mt-1 opacity-90">{result.message}</p>
              </div>
            </div>
          )}
        </div>
      </div>
    </main>
  );
}
