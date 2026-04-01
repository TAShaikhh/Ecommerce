'use client';

import React, { Suspense, useState, useEffect } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import Link from 'next/link';
import { useRouter, useSearchParams } from 'next/navigation';
import { fetchApi } from '@/lib/api';
import { useAuthStore } from '@/store/authStore';
import { ArrowRight, WarningCircle, CheckCircle } from '@phosphor-icons/react';

function LoginContent() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const { token, setAuth } = useAuthStore();
  
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState('');
  const justRegistered = searchParams.get('registered') === 'true';

  // Redirect if already logged in
  useEffect(() => {
    if (token) {
      router.replace('/catalogue');
    }
  }, [token, router]);

  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsLoading(true);
    setError('');

    try {
      const response = await fetchApi('/auth/login', {
        method: 'POST',
        body: JSON.stringify({ username, password }),
      });
      // Backend login response: { userId, username, token, _links }
      setAuth(response.token, {
        userId: response.userId,
        username: response.username,
      });
      router.push('/catalogue');
    } catch (err: any) {
      setError(err.message || 'Failed to login');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="min-h-screen grid grid-cols-1 md:grid-cols-2">
      {/* Left: Branding Panel */}
      <div className="hidden md:flex flex-col justify-between p-12 bg-zinc-950 relative overflow-hidden min-h-screen">
        <div className="absolute inset-0 bg-gradient-to-tr from-zinc-950 via-zinc-900 to-zinc-800" />
        <div className="absolute top-1/3 left-1/2 w-[600px] h-[600px] bg-zinc-700 rounded-full blur-[120px] -translate-x-1/2 -translate-y-1/2 opacity-20" />
        <div className="absolute bottom-0 right-0 w-[400px] h-[400px] bg-zinc-600 rounded-full blur-[100px] opacity-10" />
        
        <div className="relative z-10">
          <Link href="/" className="inline-flex items-center gap-2">
            <div className="w-10 h-10 bg-white rounded-lg flex items-center justify-center">
              <span className="text-zinc-950 font-bold tracking-widest text-sm">PB</span>
            </div>
            <span className="text-white font-semibold text-lg tracking-tight">PrimeBid</span>
          </Link>
        </div>

        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.8, ease: [0.16, 1, 0.3, 1] }}
          className="relative z-10"
        >
          <h2 className="text-4xl text-white font-medium tracking-tight mb-4 max-w-sm leading-[1.15]">
            Access the forward auction infrastructure.
          </h2>
          <p className="text-zinc-400 text-sm max-w-sm leading-relaxed">
            Strict, deterministic bidding environments powered by the PrimeBid cluster.
          </p>
        </motion.div>
      </div>

      {/* Right: Login Form */}
      <div className="flex items-center justify-center p-8 md:p-12 bg-[#fdfdfc] min-h-screen">
        <motion.div
          initial={{ opacity: 0, x: 20 }}
          animate={{ opacity: 1, x: 0 }}
          transition={{ duration: 0.6, delay: 0.1, ease: [0.16, 1, 0.3, 1] }}
          className="w-full max-w-md"
        >
          {/* Mobile logo */}
          <Link href="/" className="md:hidden inline-flex items-center gap-2 mb-12">
            <div className="w-8 h-8 bg-zinc-950 rounded-md flex items-center justify-center">
              <span className="text-white font-bold tracking-widest text-xs">PB</span>
            </div>
            <span className="font-semibold text-lg tracking-tight">PrimeBid</span>
          </Link>

          <div className="mb-10">
            <h1 className="text-3xl font-medium tracking-tight text-zinc-950 mb-2">Welcome back</h1>
            <p className="text-zinc-500 text-sm">Enter your credentials to connect</p>
          </div>

          {/* Success banner after signup */}
          <AnimatePresence>
            {justRegistered && (
              <motion.div
                initial={{ opacity: 0, height: 0 }}
                animate={{ opacity: 1, height: 'auto' }}
                exit={{ opacity: 0, height: 0 }}
                className="mb-6 flex items-center gap-2 text-emerald-700 bg-emerald-50 p-4 rounded-xl text-sm font-medium border border-emerald-100"
              >
                <CheckCircle weight="fill" className="w-5 h-5 flex-shrink-0" />
                Account created successfully. Sign in to continue.
              </motion.div>
            )}
          </AnimatePresence>

          <form onSubmit={handleLogin} className="space-y-5">
            <div className="flex flex-col gap-2">
              <label className="text-xs font-semibold uppercase tracking-widest text-zinc-500">Username</label>
              <input
                type="text"
                value={username}
                onChange={(e) => setUsername(e.target.value)}
                className="w-full bg-zinc-50 border border-zinc-200 rounded-xl px-4 py-3.5 text-sm focus:outline-none focus:ring-2 focus:ring-zinc-950/10 focus:border-zinc-300 transition-all font-medium placeholder:font-normal placeholder:text-zinc-400"
                placeholder="agent_smith"
                required
                autoFocus
              />
            </div>

            <div className="flex flex-col gap-2">
              <label className="text-xs font-semibold uppercase tracking-widest text-zinc-500">Password</label>
              <input
                type="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                className="w-full bg-zinc-50 border border-zinc-200 rounded-xl px-4 py-3.5 text-sm focus:outline-none focus:ring-2 focus:ring-zinc-950/10 focus:border-zinc-300 transition-all font-medium placeholder:font-normal placeholder:text-zinc-400"
                placeholder="••••••••"
                required
              />
            </div>

            <AnimatePresence>
              {error && (
                <motion.div
                  initial={{ opacity: 0, height: 0 }}
                  animate={{ opacity: 1, height: 'auto' }}
                  exit={{ opacity: 0, height: 0 }}
                  className="flex items-start gap-2 text-red-500 bg-red-50 p-3.5 rounded-xl text-sm border border-red-100"
                >
                  <WarningCircle weight="fill" className="w-5 h-5 shrink-0 mt-0.5" />
                  <p>{error}</p>
                </motion.div>
              )}
            </AnimatePresence>

            <button
              type="submit"
              disabled={isLoading || !username || !password}
              className="group relative w-full flex items-center justify-center gap-2 bg-zinc-950 text-white rounded-full py-4 font-medium transition-all active:scale-[0.98] disabled:opacity-50 disabled:active:scale-100 overflow-hidden mt-2"
            >
              <span className="relative z-10">{isLoading ? 'Authenticating...' : 'Sign In'}</span>
              {!isLoading && <ArrowRight weight="bold" className="w-4 h-4 relative z-10 group-hover:translate-x-1 transition-transform" />}
              <div
                className="absolute inset-0 bg-zinc-800 scale-x-0 group-hover:scale-x-100 origin-left transition-transform duration-500 ease-[cubic-bezier(0.16,1,0.3,1)]"
              />
            </button>
          </form>

          <p className="mt-8 text-center text-sm text-zinc-500">
            Don&apos;t have an account?{' '}
            <Link href="/signup" className="text-zinc-950 font-medium hover:underline underline-offset-4 decoration-zinc-300">
              Create one
            </Link>
          </p>
        </motion.div>
      </div>
    </div>
  );
}

export default function Login() {
  return (
    <Suspense fallback={<div className="min-h-screen bg-[#fdfdfc]" />}>
      <LoginContent />
    </Suspense>
  );
}
