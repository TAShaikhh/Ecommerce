'use client';

import React, { useState, useEffect, use } from 'react';
import { motion } from 'framer-motion';
import { fetchApi } from '@/lib/api';
import Link from 'next/link';
import { CheckCircle, DownloadSimple, CreditCard, Package, Truck } from '@phosphor-icons/react';

interface PageProps {
  params: Promise<{ id: string }>;
}

export default function ReceiptPage({ params }: PageProps) {
  const { id } = use(params);
  const [receipt, setReceipt] = useState<any>(null);
  const [error, setError] = useState('');

  useEffect(() => {
    fetchApi(`/receipt/${id}`)
      .then(setReceipt)
      .catch((e) => setError(e.message || 'Failed to load receipt'));
  }, [id]);

  if (error) {
    return (
      <div className="min-h-[80vh] flex flex-col items-center justify-center p-6">
        <div className="text-red-500 text-sm font-medium bg-red-50 px-6 py-4 rounded-2xl border border-red-100">
          {error}
        </div>
        <Link href="/catalogue" className="mt-6 text-sm font-medium text-zinc-500 hover:text-zinc-950">
          Return to Catalogue
        </Link>
      </div>
    );
  }

  if (!receipt) {
    return (
      <div className="min-h-[80vh] flex items-center justify-center">
        <div className="w-12 h-12 bg-zinc-200 rounded-full animate-pulse" />
      </div>
    );
  }

  return (
    <div className="min-h-[80vh] flex flex-col items-center justify-center p-6">
      <motion.div
        initial={{ opacity: 0, scale: 0.9, y: 20 }}
        animate={{ opacity: 1, scale: 1, y: 0 }}
        transition={{ type: 'spring', stiffness: 100, damping: 20 }}
        className="w-full max-w-md bg-white border border-zinc-200 rounded-[2.5rem] p-10 shadow-2xl relative overflow-hidden"
      >
        {/* Success strip */}
        <div className="absolute top-0 inset-x-0 h-1 bg-gradient-to-r from-emerald-400 to-green-500" />

        <div className="flex flex-col items-center justify-center text-center mb-10">
          <motion.div
            initial={{ scale: 0 }}
            animate={{ scale: 1 }}
            transition={{ type: 'spring', stiffness: 200, damping: 15, delay: 0.2 }}
            className="w-20 h-20 bg-green-50 rounded-full flex items-center justify-center mb-6"
          >
            <CheckCircle weight="fill" className="w-10 h-10 text-green-500" />
          </motion.div>
          <h1 className="text-3xl font-medium tracking-tight text-zinc-950 mb-2">Payment Confirmed</h1>
          <p className="text-sm font-mono tracking-tighter text-zinc-500">Transaction #{receipt.transactionId}</p>
          {receipt.status && (
            <div className="mt-3 px-3 py-1 bg-emerald-50 text-emerald-700 text-xs font-semibold rounded-full tracking-widest uppercase">
              {receipt.status}
            </div>
          )}
        </div>

        {/* Item title */}
        {receipt.itemTitle && (
          <div className="mb-6 text-center">
            <div className="flex items-center justify-center gap-2 text-zinc-500 mb-1">
              <Package className="w-4 h-4" />
              <span className="text-[10px] font-semibold uppercase tracking-widest">Item Purchased</span>
            </div>
            <h2 className="text-xl font-medium tracking-tight text-zinc-950">{receipt.itemTitle}</h2>
          </div>
        )}

        <div className="space-y-4 mb-8">
           <div className="flex justify-between items-center py-2 border-b border-zinc-100">
             <span className="text-zinc-500 text-sm">Winning Bid</span>
             <span className="font-mono tracking-tighter text-zinc-950 font-medium">${receipt.winningPrice?.toFixed(2) ?? '0.00'}</span>
           </div>
           <div className="flex justify-between items-center py-2 border-b border-zinc-100">
             <span className="text-zinc-500 text-sm">Base Shipping</span>
             <span className="font-mono tracking-tighter text-zinc-950 font-medium">${receipt.shippingCost?.toFixed(2) ?? '0.00'}</span>
           </div>
           {receipt.expedited && (
             <div className="flex justify-between items-center py-2 border-b border-zinc-100">
               <span className="text-zinc-500 text-sm">Expedited Shipping</span>
               <span className="font-mono tracking-tighter text-emerald-600 font-medium">+${receipt.expeditedShippingCost?.toFixed(2) ?? '0.00'}</span>
             </div>
           )}
           <div className="flex justify-between items-center py-2 border-b border-zinc-100">
             <span className="text-zinc-950 text-sm font-semibold">Total Paid</span>
             <span className="font-mono tracking-tighter text-zinc-950 font-bold text-lg">${receipt.totalPaid?.toFixed(2) ?? '0.00'}</span>
           </div>
        </div>

        {/* Card & Shipping info */}
        <div className="space-y-3 mb-8 p-4 bg-zinc-50 rounded-2xl border border-zinc-100">
          {receipt.cardName && (
            <div className="flex items-center gap-3 text-sm">
              <CreditCard className="w-4 h-4 text-zinc-400" />
              <span className="text-zinc-600">
                {receipt.cardName}
                {receipt.cardLastFour && <span className="font-mono text-zinc-400 ml-2">•••• {receipt.cardLastFour}</span>}
              </span>
            </div>
          )}
          {receipt.shippingMessage && (
            <div className="flex items-center gap-3 text-sm">
              <Truck className="w-4 h-4 text-emerald-500" />
              <span className="text-emerald-600 font-medium">{receipt.shippingMessage}</span>
            </div>
          )}
        </div>

        <div className="flex flex-col gap-4">
          <button className="w-full py-4 bg-zinc-50 text-zinc-950 rounded-xl font-medium border border-zinc-200 flex items-center justify-center gap-2 hover:bg-zinc-100 transition-colors">
            <DownloadSimple weight="bold" /> Download PDF
          </button>
          <Link href="/catalogue" className="text-center text-sm font-medium text-zinc-500 hover:text-zinc-950 transition-colors">
            Return to Catalogue
          </Link>
        </div>
      </motion.div>
    </div>
  );
}
