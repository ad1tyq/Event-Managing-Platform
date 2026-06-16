import React, { InputHTMLAttributes } from 'react';

export interface InputProps extends InputHTMLAttributes<HTMLInputElement> {
  label?: string;
  error?: string;
}

export const Input = React.forwardRef<HTMLInputElement, InputProps>(
  ({ className = '', type, label, error, ...props }, ref) => {
    return (
      <div className="flex w-full flex-col gap-1.5">
        {label && (
          <label className="text-sm font-sans text-zinc-400">
            {label}
          </label>
        )}
        <input
          type={type}
          className={`flex h-10 w-full rounded-md border border-zinc-800 bg-zinc-950/50 px-3 py-2 text-sm font-mono text-zinc-100 placeholder:text-zinc-600 focus-visible:outline-none focus-visible:ring-1 focus-visible:ring-terminal focus-visible:border-terminal disabled:cursor-not-allowed disabled:opacity-50 transition-colors ${className}`}
          ref={ref}
          {...props}
        />
        {error && <span className="text-xs text-red-500 font-mono mt-1">{error}</span>}
      </div>
    );
  }
);

Input.displayName = 'Input';
