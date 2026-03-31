'use client';

import React, { useState } from 'react';
import { motion } from 'framer-motion';
import { useAuthStore } from '@/store/authStore';
import { fetchApi } from '@/lib/api';
import { Gear, LockKey, CheckCircle, WarningCircle } from '@phosphor-icons/react';

export default function Settings() {
  const { user } = useAuthStore();
  const [currentPassword, setCurrentPassword] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const [status, setStatus] = useState<{ type: 'success' | 'error' | null; message: string }>({
    type: null,
    message: '',
  });

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!user?.username) return;

    setLoading(true);
    setStatus({ type: null, message: '' });

    try {
      await fetchApi('/auth/reset-password', {
        method: 'POST',
        body: JSON.stringify({
          username: user.username,
          currentPassword: currentPassword,
          newPassword: newPassword,
        }),
      });

      setStatus({ type: 'success', message: 'Password successfully updated.' });
      setCurrentPassword('');
      setNewPassword('');
    } catch (err: any) {
      setStatus({ type: 'error', message: err.message || 'Failed to update password.' });
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-[80vh] px-6 lg:px-12 max-w-3xl mx-auto flex flex-col justify-center">
      <motion.div
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.5 }}
      >
        <div className="flex items-center gap-4 mb-6">
          <div className="w-12 h-12 bg-white rounded-xl shadow-sm border border-zinc-200 flex items-center justify-center text-zinc-950">
            <Gear weight="fill" className="w-6 h-6" />
          </div>
          <div>
            <h1 className="text-3xl font-medium tracking-tight text-zinc-950">Security Settings</h1>
            <p className="text-sm font-medium text-zinc-500">Manage your identity credentials.</p>
          </div>
        </div>

        <div className="bg-white border border-zinc-200 rounded-[2rem] p-8 lg:p-10 shadow-sm relative overflow-hidden">
          <h2 className="text-xs font-semibold uppercase tracking-widest text-zinc-500 mb-6 flex items-center gap-2">
            <LockKey className="w-4 h-4" /> Reset Password
          </h2>

          <form onSubmit={handleSubmit} className="space-y-6">
            <div className="flex flex-col gap-2">
              <label className="text-xs font-semibold uppercase tracking-widest text-zinc-500">Current Password</label>
              <input
                type="password"
                value={currentPassword}
                onChange={(e) => setCurrentPassword(e.target.value)}
                className="w-full bg-zinc-50 border border-zinc-200 rounded-xl px-4 py-3 text-sm focus:outline-none focus:ring-2 focus:ring-zinc-950/10 transition-all font-medium"
                placeholder="Enter current password"
                required
              />
            </div>
            <div className="flex flex-col gap-2">
              <label className="text-xs font-semibold uppercase tracking-widest text-zinc-500">New Password</label>
              <input
                type="password"
                value={newPassword}
                onChange={(e) => setNewPassword(e.target.value)}
                className="w-full bg-zinc-50 border border-zinc-200 rounded-xl px-4 py-3 text-sm focus:outline-none focus:ring-2 focus:ring-zinc-950/10 transition-all font-medium"
                placeholder="Enter new strong password"
                minLength={6}
                required
              />
            </div>

            {status.type && (
              <motion.div
                initial={{ opacity: 0, height: 0 }}
                animate={{ opacity: 1, height: 'auto' }}
                className={`flex items-center gap-2 text-sm font-medium px-4 py-3 rounded-xl ${
                  status.type === 'success' 
                    ? 'bg-green-50 text-green-600 border border-green-100' 
                    : 'bg-red-50 text-red-500 border border-red-100'
                }`}
              >
                {status.type === 'success' ? (
                  <CheckCircle weight="fill" className="w-5 h-5 flex-shrink-0" />
                ) : (
                  <WarningCircle weight="fill" className="w-5 h-5 flex-shrink-0" />
                )}
                {status.message}
              </motion.div>
            )}

            <div className="pt-4 border-t border-zinc-100 flex items-center justify-end">
              <button
                type="submit"
                disabled={loading || !newPassword || !currentPassword}
                className="group relative bg-zinc-950 text-white rounded-full px-8 py-3 font-medium transition-all active:scale-[0.98] hover:bg-zinc-800 disabled:opacity-50"
              >
                {loading ? 'Updating...' : 'Update Password'}
              </button>
            </div>
          </form>
        </div>
      </motion.div>
    </div>
  );
}
