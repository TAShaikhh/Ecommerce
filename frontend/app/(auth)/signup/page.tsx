'use client';

import React, { useState, useEffect } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import Link from 'next/link';
import { useRouter } from 'next/navigation';
import { fetchApi } from '@/lib/api';
import { useAuthStore } from '@/store/authStore';
import { ArrowRight, WarningCircle } from '@phosphor-icons/react';

export default function Signup() {
  const router = useRouter();
  const { token, hydrated } = useAuthStore();
  
  const [formData, setFormData] = useState({
    username: '',
    password: '',
    firstName: '',
    lastName: '',
    address: '',
  });
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState('');

  // Redirect if already logged in
  useEffect(() => {
    if (hydrated && token) {
      router.replace('/catalogue');
    }
  }, [hydrated, token, router]);

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setFormData(prev => ({ ...prev, [e.target.name]: e.target.value }));
  };

  const handleSignup = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsLoading(true);
    setError('');

    try {
      await fetchApi('/auth/signup', {
        method: 'POST',
        body: JSON.stringify(formData),
      });
      router.push('/login?registered=true');
    } catch (err: any) {
      setError(err.message || 'Failed to create account');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="min-h-screen grid grid-cols-1 md:grid-cols-2 relative">
      <div className="absolute inset-x-4 top-4 z-20 flex justify-center md:inset-x-auto md:right-6 md:left-auto md:justify-end">
        <div className="flex w-full max-w-sm items-center justify-center gap-2 rounded-full border border-zinc-200 bg-white/95 px-3 py-2 shadow-sm backdrop-blur md:w-auto md:max-w-none">
          <Link
            href="/login"
            className="rounded-full px-4 py-2 text-sm font-medium text-zinc-600 transition-colors hover:text-zinc-950"
          >
            Sign in
          </Link>
          <Link
            href="/signup"
            className="rounded-full px-4 py-2 text-sm font-medium text-zinc-950 bg-zinc-100"
          >
            Create account
          </Link>
        </div>
      </div>

      {/* Left: Form */}
      <div className="flex items-center justify-center px-6 pb-8 pt-28 md:min-h-screen md:p-12 bg-[#fdfdfc] min-h-screen order-2 md:order-1">
        <motion.div
          initial={{ opacity: 0, x: -20 }}
          animate={{ opacity: 1, x: 0 }}
          transition={{ duration: 0.6, delay: 0.1, ease: [0.16, 1, 0.3, 1] }}
          className="w-full max-w-md"
        >
          <Link href="/" className="hidden md:inline-flex items-center gap-2 mb-12">
            <div className="w-10 h-10 bg-zinc-950 rounded-lg flex items-center justify-center">
              <span className="text-white font-bold tracking-widest text-sm">PB</span>
            </div>
          </Link>

          {/* Mobile logo */}
          <Link href="/" className="md:hidden inline-flex items-center gap-2 mb-12">
            <div className="w-8 h-8 bg-zinc-950 rounded-md flex items-center justify-center">
              <span className="text-white font-bold tracking-widest text-xs">PB</span>
            </div>
          </Link>

          <div className="mb-10">
            <h1 className="text-3xl font-medium tracking-tight text-zinc-950 mb-2">Create Identity</h1>
            <p className="text-zinc-500 text-sm">Fill in your details to register as an operator.</p>
          </div>

          <form onSubmit={handleSignup} className="space-y-5">
            <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
              <div className="flex flex-col gap-2">
                <label className="text-xs font-semibold uppercase tracking-widest text-zinc-500">First Name</label>
                <input
                  type="text"
                  name="firstName"
                  value={formData.firstName}
                  onChange={handleChange}
                  className="w-full bg-zinc-50 border border-zinc-200 rounded-xl px-4 py-3.5 text-sm focus:outline-none focus:ring-2 focus:ring-zinc-950/10 focus:border-zinc-300 transition-all font-medium placeholder:font-normal placeholder:text-zinc-400"
                  placeholder="Jane"
                  required
                  autoFocus
                />
              </div>
              <div className="flex flex-col gap-2">
                <label className="text-xs font-semibold uppercase tracking-widest text-zinc-500">Last Name</label>
                <input
                  type="text"
                  name="lastName"
                  value={formData.lastName}
                  onChange={handleChange}
                  className="w-full bg-zinc-50 border border-zinc-200 rounded-xl px-4 py-3.5 text-sm focus:outline-none focus:ring-2 focus:ring-zinc-950/10 focus:border-zinc-300 transition-all font-medium placeholder:font-normal placeholder:text-zinc-400"
                  placeholder="Doe"
                  required
                />
              </div>
            </div>

            <div className="flex flex-col gap-2">
              <label className="text-xs font-semibold uppercase tracking-widest text-zinc-500">Address</label>
              <input
                type="text"
                name="address"
                value={formData.address}
                onChange={handleChange}
                className="w-full bg-zinc-50 border border-zinc-200 rounded-xl px-4 py-3.5 text-sm focus:outline-none focus:ring-2 focus:ring-zinc-950/10 focus:border-zinc-300 transition-all font-medium placeholder:font-normal placeholder:text-zinc-400"
                placeholder="123 Market Street, Suite 4"
                required
              />
            </div>

            <div className="flex flex-col gap-2">
              <label className="text-xs font-semibold uppercase tracking-widest text-zinc-500">Username</label>
              <input
                type="text"
                name="username"
                value={formData.username}
                onChange={handleChange}
                className="w-full bg-zinc-50 border border-zinc-200 rounded-xl px-4 py-3.5 text-sm focus:outline-none focus:ring-2 focus:ring-zinc-950/10 focus:border-zinc-300 transition-all font-medium placeholder:font-normal placeholder:text-zinc-400"
                placeholder="unique_identifier"
                required
              />
            </div>

            <div className="flex flex-col gap-2">
              <label className="text-xs font-semibold uppercase tracking-widest text-zinc-500">Password</label>
              <input
                type="password"
                name="password"
                value={formData.password}
                onChange={handleChange}
                className="w-full bg-zinc-50 border border-zinc-200 rounded-xl px-4 py-3.5 text-sm focus:outline-none focus:ring-2 focus:ring-zinc-950/10 focus:border-zinc-300 transition-all font-medium placeholder:font-normal placeholder:text-zinc-400"
                placeholder="••••••••"
                minLength={6}
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
              disabled={isLoading || !formData.username || !formData.password || !formData.firstName || !formData.lastName || !formData.address}
              className="group relative w-full flex items-center justify-center gap-2 bg-zinc-950 text-white rounded-full py-4 font-medium transition-all active:scale-[0.98] mt-2 disabled:opacity-50 disabled:active:scale-100 overflow-hidden"
            >
              <span className="relative z-10">{isLoading ? 'Creating...' : 'Create Account'}</span>
              {!isLoading && <ArrowRight weight="bold" className="w-4 h-4 relative z-10 group-hover:translate-x-1 transition-transform" />}
              <div
                className="absolute inset-0 bg-zinc-800 scale-x-0 group-hover:scale-x-100 origin-left transition-transform duration-500 ease-[cubic-bezier(0.16,1,0.3,1)]"
              />
            </button>
          </form>

          <p className="mt-8 text-center text-sm text-zinc-500">
            Already registered?{' '}
            <Link href="/login" className="text-zinc-950 font-medium hover:underline underline-offset-4 decoration-zinc-300">
              Sign in
            </Link>
          </p>
        </motion.div>
      </div>

      {/* Right: Branding Panel */}
      <div className="hidden md:flex flex-col justify-between p-12 bg-zinc-950 relative overflow-hidden min-h-screen order-1 md:order-2">
        <div className="absolute inset-0 bg-gradient-to-bl from-zinc-800 via-zinc-900 to-zinc-950" />
        <div className="absolute bottom-0 right-0 w-[500px] h-[500px] bg-zinc-600 rounded-full blur-[120px] opacity-15" />
        <div className="absolute top-1/4 left-0 w-[300px] h-[300px] bg-zinc-700 rounded-full blur-[80px] opacity-10" />
        
        <div className="relative z-10" />

        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.8, ease: [0.16, 1, 0.3, 1] }}
          className="relative z-10"
        >
          <h2 className="text-4xl text-white font-medium tracking-tight mb-4 max-w-sm leading-[1.15]">
            Begin operating on the exchange.
          </h2>
          <p className="text-zinc-400 text-sm max-w-sm leading-relaxed">
            Register to participate as a buyer or seller in our deterministic item catalog.
          </p>
        </motion.div>
      </div>
    </div>
  );
}
