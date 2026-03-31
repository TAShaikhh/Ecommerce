'use client';

import React, { useState } from 'react';
import { motion } from 'framer-motion';
import { useRouter } from 'next/navigation';
import { fetchApi } from '@/lib/api';
import { Package, ArrowRight, WarningCircle } from '@phosphor-icons/react';

export default function SellItem() {
  const router = useRouter();
  
  const [formData, setFormData] = useState({
    title: '',
    description: '',
    condition: 'NEW',
    keywords: '',
    shippingCost: '',
    expeditedShippingCost: '',
    shippingDays: '',
    startingPrice: '',
    auctionDurationSeconds: '60',
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement>) => {
    setFormData(prev => ({ ...prev, [e.target.name]: e.target.value }));
  };

  const handleCreate = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError('');

    try {
      const res = await fetchApi('/items', {
        method: 'POST',
        body: JSON.stringify({
          ...formData,
          shippingCost: Number(formData.shippingCost),
          expeditedShippingCost: Number(formData.expeditedShippingCost),
          shippingDays: Number(formData.shippingDays),
          startingPrice: Number(formData.startingPrice),
          auctionDurationSeconds: Number(formData.auctionDurationSeconds),
        })
      });
      router.push(`/catalogue/${res.itemId}`);
    } catch (err: any) {
      setError(err.message || 'Failed to list asset.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-[80vh] pb-24 px-6 lg:px-12 max-w-4xl mx-auto">
      <motion.div
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.5 }}
      >
        <div className="flex items-center gap-4 mb-6">
          <div className="w-12 h-12 bg-white rounded-xl shadow-sm border border-zinc-200 flex items-center justify-center text-zinc-950">
            <Package weight="fill" className="w-6 h-6" />
          </div>
          <div>
            <h1 className="text-3xl font-medium tracking-tight text-zinc-950">List an Asset</h1>
            <p className="text-sm font-medium text-zinc-500">Inject an item into the deterministic bidding infrastructure.</p>
          </div>
        </div>

        <form onSubmit={handleCreate} className="space-y-12">
          {/* Main Details */}
          <div className="bg-white border border-zinc-200 rounded-[2rem] p-8 lg:p-10 shadow-sm">
            <h2 className="text-xs font-semibold uppercase tracking-widest text-zinc-500 mb-6">ASSET DETAILS</h2>
            <div className="space-y-6">
              <div className="flex flex-col gap-2">
                <label className="text-xs font-semibold uppercase tracking-widest text-zinc-500">Asset Title</label>
                <input
                  type="text"
                  name="title"
                  value={formData.title}
                  onChange={handleChange}
                  className="w-full bg-zinc-50 border border-zinc-200 rounded-xl px-4 py-3 text-sm focus:outline-none focus:ring-2 focus:ring-zinc-950/10 transition-all font-medium"
                  placeholder="E.g. RTX 4090 Advanced Edition"
                  required
                />
              </div>

              <div className="flex flex-col gap-2">
                <label className="text-xs font-semibold uppercase tracking-widest text-zinc-500">Description</label>
                <textarea
                  name="description"
                  value={formData.description}
                  onChange={handleChange}
                  rows={4}
                  className="w-full bg-zinc-50 border border-zinc-200 rounded-xl px-4 py-3 text-sm focus:outline-none focus:ring-2 focus:ring-zinc-950/10 transition-all font-medium resize-none"
                  placeholder="Provide explicit specifications..."
                  required
                />
              </div>

              <div className="grid grid-cols-2 gap-6">
                <div className="flex flex-col gap-2">
                  <label className="text-xs font-semibold uppercase tracking-widest text-zinc-500">Condition</label>
                  <select
                    name="condition"
                    value={formData.condition}
                    onChange={handleChange}
                    className="w-full bg-zinc-50 border border-zinc-200 rounded-xl px-4 py-3 text-sm focus:outline-none focus:ring-2 focus:ring-zinc-950/10 transition-all font-medium appearance-none"
                    required
                  >
                    <option value="NEW">NEW</option>
                    <option value="EXCELLENT">EXCELLENT</option>
                    <option value="GOOD">GOOD</option>
                    <option value="USED">USED</option>
                  </select>
                </div>
                <div className="flex flex-col gap-2">
                  <label className="text-xs font-semibold uppercase tracking-widest text-zinc-500">Keywords (Space separated)</label>
                  <input
                    type="text"
                    name="keywords"
                    value={formData.keywords}
                    onChange={handleChange}
                    className="w-full bg-zinc-50 border border-zinc-200 rounded-xl px-4 py-3 text-sm focus:outline-none focus:ring-2 focus:ring-zinc-950/10 transition-all font-medium"
                    placeholder="hardware gpu silicon"
                    required
                  />
                </div>
              </div>
            </div>
          </div>

          {/* Logistics & Price */}
          <div className="bg-white border border-zinc-200 rounded-[2rem] p-8 lg:p-10 shadow-sm">
            <h2 className="text-xs font-semibold uppercase tracking-widest text-zinc-500 mb-6">ECONOMICS & LOGISTICS</h2>
            <div className="grid grid-cols-2 gap-6 mb-6">
              <div className="flex flex-col gap-2 relative">
                <label className="text-xs font-semibold uppercase tracking-widest text-zinc-500">Starting Price</label>
                <span className="absolute left-4 top-[38px] text-zinc-400 font-mono select-none">$</span>
                <input
                  type="number"
                  step="0.01"
                  min="1"
                  name="startingPrice"
                  value={formData.startingPrice}
                  onChange={handleChange}
                  className="w-full bg-zinc-50 border border-zinc-200 rounded-xl py-3 pl-8 pr-4 font-mono text-sm focus:outline-none focus:ring-2 focus:ring-zinc-950/10 transition-all"
                  placeholder="500.00"
                  required
                />
              </div>
              <div className="flex flex-col gap-2 relative">
                <label className="text-xs font-semibold uppercase tracking-widest text-zinc-500">Auction Duration</label>
                <input
                  type="number"
                  min="10"
                  name="auctionDurationSeconds"
                  value={formData.auctionDurationSeconds}
                  onChange={handleChange}
                  className="w-full bg-zinc-50 border border-zinc-200 rounded-xl py-3 px-4 font-mono text-sm focus:outline-none focus:ring-2 focus:ring-zinc-950/10 transition-all pr-12"
                  placeholder="60"
                  required
                />
                <span className="absolute right-4 top-[38px] text-zinc-400 text-xs font-semibold tracking-widest">SEC</span>
              </div>
            </div>

            <div className="grid grid-cols-3 gap-6">
               <div className="flex flex-col gap-2 relative">
                <label className="text-xs font-semibold uppercase tracking-widest text-zinc-500">Base Ship</label>
                <span className="absolute left-4 top-[38px] text-zinc-400 font-mono select-none">$</span>
                <input
                  type="number"
                  step="0.01"
                  min="0"
                  name="shippingCost"
                  value={formData.shippingCost}
                  onChange={handleChange}
                  className="w-full bg-zinc-50 border border-zinc-200 rounded-xl py-3 pl-8 pr-4 text-sm font-mono focus:outline-none focus:ring-2 focus:ring-zinc-950/10 transition-all"
                  placeholder="15.00"
                  required
                />
              </div>
              <div className="flex flex-col gap-2 relative">
                <label className="text-xs font-semibold uppercase tracking-widest text-zinc-500">Expedited Ship</label>
                <span className="absolute left-4 top-[38px] text-zinc-400 font-mono select-none">$</span>
                <input
                  type="number"
                  step="0.01"
                  min="0"
                  name="expeditedShippingCost"
                  value={formData.expeditedShippingCost}
                  onChange={handleChange}
                  className="w-full bg-zinc-50 border border-zinc-200 rounded-xl py-3 pl-8 pr-4 text-sm font-mono focus:outline-none focus:ring-2 focus:ring-zinc-950/10 transition-all"
                  placeholder="25.00"
                  required
                />
              </div>
              <div className="flex flex-col gap-2 relative">
                <label className="text-xs font-semibold uppercase tracking-widest text-zinc-500">Ship Days</label>
                <input
                  type="number"
                  min="1"
                  name="shippingDays"
                  value={formData.shippingDays}
                  onChange={handleChange}
                  className="w-full bg-zinc-50 border border-zinc-200 rounded-xl py-3 pl-4 pr-12 text-sm font-mono focus:outline-none focus:ring-2 focus:ring-zinc-950/10 transition-all"
                  placeholder="5"
                  required
                />
                <span className="absolute right-3 top-[38px] text-zinc-400 text-xs font-semibold tracking-widest">DAYS</span>
              </div>
            </div>
          </div>

          <div className="flex items-center justify-end gap-6 pt-6">
             {error && (
                <div className="flex items-center gap-1.5 text-red-500 text-sm font-medium mr-auto">
                   <WarningCircle weight="fill" className="w-5 h-5" />
                   {error}
                </div>
             )}
             <button
               type="button"
               onClick={() => router.back()}
               className="text-sm font-medium text-zinc-500 hover:text-zinc-950 transition-colors"
             >
               Cancel
             </button>
             <button
              type="submit"
              disabled={loading}
              className="group relative flex items-center justify-center gap-2 bg-zinc-950 text-white rounded-full px-8 py-3.5 font-medium transition-all active:scale-[0.98] disabled:opacity-50 overflow-hidden"
            >
              <span className="relative z-10">{loading ? 'Injecting...' : 'List in Catalogue'}</span>
              {!loading && <ArrowRight weight="bold" className="w-4 h-4 relative z-10 group-hover:translate-x-1 transition-transform" />}
              <motion.div
                className="absolute inset-0 bg-zinc-800 scale-x-0 group-hover:scale-x-100 origin-left transition-transform duration-500 ease-[cubic-bezier(0.16,1,0.3,1)]"
              />
            </button>
          </div>
        </form>
      </motion.div>
    </div>
  );
}
