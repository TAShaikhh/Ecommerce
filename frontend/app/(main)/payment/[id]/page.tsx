'use client';

import React, { useState, useEffect, use } from 'react';
import { motion } from 'framer-motion';
import { useRouter } from 'next/navigation';
import { fetchApi } from '@/lib/api';
import { CreditCard, ShieldCheck, Lightning, Receipt, MapPin, WarningCircle } from '@phosphor-icons/react';

interface PageProps {
  params: Promise<{ id: string }>;
}

export default function PaymentPage({ params }: PageProps) {
  const { id } = use(params);
  const router = useRouter();
  
  const [formData, setFormData] = useState({
    cardName: '',
    cardNumber: '',
    expiryDate: '',
    securityCode: '',
  });
  const [expedited, setExpedited] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [invoice, setInvoice] = useState<any>(null);
  const [invoiceLoading, setInvoiceLoading] = useState(true);

  useEffect(() => {
    setInvoiceLoading(true);
    fetchApi(`/payment-page/${id}`)
      .then(setInvoice)
      .catch((e) => setError(e.message || 'Cannot load payment details.'))
      .finally(() => setInvoiceLoading(false));
  }, [id]);

  const handlePay = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError('');

    try {
      const res = await fetchApi('/pay', {
        method: 'POST',
        body: JSON.stringify({
          itemId: Number(id),
          expedited,
          ...formData
        })
      });
      // the backend returns paymentId
      router.push(`/receipt/${res.paymentId}`);
    } catch (err: any) {
      setError(err.message || 'Payment failed');
    } finally {
      setLoading(false);
    }
  };

  if (invoiceLoading) {
    return (
      <div className="min-h-[80vh] flex flex-col items-center justify-center p-6">
        <div className="w-12 h-12 bg-zinc-200 rounded-full animate-pulse mb-4" />
        <p className="text-sm text-zinc-500">Loading payment details...</p>
      </div>
    );
  }

  return (
    <div className="min-h-[80vh] flex flex-col items-center justify-center p-6">
      <motion.div
        initial={{ opacity: 0, y: 20, scale: 0.95 }}
        animate={{ opacity: 1, y: 0, scale: 1 }}
        transition={{ type: 'spring', stiffness: 100, damping: 20 }}
        className="w-full max-w-lg bg-white border border-zinc-200 rounded-[2.5rem] p-10 shadow-2xl shadow-zinc-950/5 relative overflow-hidden"
      >
        <div className="absolute top-0 inset-x-0 h-1 bg-gradient-to-r from-emerald-400 to-teal-500" />
        
        <div className="flex items-center gap-4 mb-10">
          <div className="w-12 h-12 bg-zinc-50 rounded-full flex items-center justify-center text-zinc-950 border border-zinc-100">
            <CreditCard weight="fill" className="w-6 h-6" />
          </div>
          <div>
            <h1 className="text-2xl font-medium tracking-tight text-zinc-950">Secure Checkout</h1>
            <p className="text-sm font-medium text-zinc-500 flex items-center gap-1"><ShieldCheck className="w-4 h-4 text-emerald-500" /> End-to-end encrypted</p>
          </div>
        </div>

        {invoice && (
          <div className="mb-8 p-6 bg-zinc-50 rounded-2xl border border-zinc-200">
            <h3 className="text-sm font-semibold tracking-wider text-zinc-500 uppercase flex items-center gap-2 mb-4"><Receipt className="w-4 h-4" /> Order Summary</h3>
            <div className="space-y-3 font-medium text-sm">
              <div className="flex justify-between text-zinc-600">
                <span>{invoice.itemTitle} (Winning Bid)</span>
                <span className="font-mono">${invoice.winningBid?.toFixed(2)}</span>
              </div>
              <div className="flex justify-between text-zinc-600">
                <span>Base Shipping ({invoice.shippingDays} days)</span>
                <span className="font-mono">${invoice.shippingCost?.toFixed(2)}</span>
              </div>
              {expedited && (
                <div className="flex justify-between text-emerald-600">
                  <span>Expedited Shipping</span>
                  <span className="font-mono">+${invoice.expeditedShippingCost?.toFixed(2)}</span>
                </div>
              )}
              <div className="pt-3 border-t border-zinc-200 flex justify-between text-zinc-950 text-base font-semibold">
                 <span>Total</span>
                 <span className="font-mono">${(expedited ? invoice.totalWithExpeditedShipping : invoice.totalWithStandardShipping)?.toFixed(2)}</span>
              </div>
            </div>

            {/* Shipping address from backend */}
            {invoice.shippingAddress && (
              <div className="mt-4 pt-4 border-t border-zinc-200 flex items-start gap-2 text-sm text-zinc-600">
                <MapPin className="w-4 h-4 mt-0.5 text-zinc-400 flex-shrink-0" />
                <div>
                  <div className="text-[10px] font-semibold uppercase tracking-widest text-zinc-400 mb-1">Ship To</div>
                  <div className="font-medium text-zinc-950">{invoice.shippingAddress}</div>
                </div>
              </div>
            )}
          </div>
        )}

        <form onSubmit={handlePay} className="space-y-6">
          <div className="flex flex-col gap-2">
            <label className="text-xs font-semibold uppercase tracking-widest text-zinc-500">Name on Card</label>
            <input
              type="text"
              value={formData.cardName}
              onChange={(e) => setFormData({ ...formData, cardName: e.target.value })}
              className="w-full bg-zinc-50 border border-zinc-200 rounded-xl px-4 py-3 text-sm focus:outline-none focus:ring-2 focus:ring-zinc-950/10 transition-all font-medium"
              placeholder="JANE DOE"
              required
            />
          </div>

          <div className="flex flex-col gap-2">
            <label className="text-xs font-semibold uppercase tracking-widest text-zinc-500">Card Number</label>
            <input
              type="text"
              value={formData.cardNumber}
              onChange={(e) => setFormData({ ...formData, cardNumber: e.target.value })}
              className="w-full bg-zinc-50 border border-zinc-200 rounded-xl px-4 py-3 font-mono tracking-widest text-sm focus:outline-none focus:ring-2 focus:ring-zinc-950/10 transition-all"
              placeholder="0000 0000 0000 0000"
              maxLength={16}
              required
            />
          </div>

          <div className="grid grid-cols-2 gap-4">
            <div className="flex flex-col gap-2">
              <label className="text-xs font-semibold uppercase tracking-widest text-zinc-500">Expiry (MM/YY)</label>
              <input
                type="text"
                value={formData.expiryDate}
                onChange={(e) => setFormData({ ...formData, expiryDate: e.target.value })}
                className="w-full bg-zinc-50 border border-zinc-200 rounded-xl px-4 py-3 font-mono text-sm focus:outline-none focus:ring-2 focus:ring-zinc-950/10 transition-all"
                placeholder="12/28"
                maxLength={5}
                required
              />
            </div>
            <div className="flex flex-col gap-2">
              <label className="text-xs font-semibold uppercase tracking-widest text-zinc-500">CVC</label>
              <input
                type="password"
                value={formData.securityCode}
                onChange={(e) => setFormData({ ...formData, securityCode: e.target.value })}
                className="w-full bg-zinc-50 border border-zinc-200 rounded-xl px-4 py-3 font-mono text-sm focus:outline-none focus:ring-2 focus:ring-zinc-950/10 transition-all"
                placeholder="•••"
                maxLength={4}
                required
              />
            </div>
          </div>

          <div className="pt-4 border-t border-zinc-100 flex items-center justify-between">
            <div className="flex items-center gap-3">
              <button
                type="button"
                onClick={() => setExpedited(!expedited)}
                className={`w-12 h-6 rounded-full transition-colors relative flex items-center ${expedited ? 'bg-zinc-950' : 'bg-zinc-200'}`}
              >
                <div className={`w-4 h-4 rounded-full bg-white transition-transform ${expedited ? 'translate-x-7' : 'translate-x-1'}`} />
              </button>
              <span className="text-sm font-medium text-zinc-950 flex items-center gap-1">
                Expedited Shipping <Lightning weight="fill" className="text-amber-400" />
              </span>
            </div>
          </div>

          {error && (
            <motion.div
              initial={{ opacity: 0, height: 0 }}
              animate={{ opacity: 1, height: 'auto' }}
              className="flex items-start gap-2 text-red-500 bg-red-50 p-3 rounded-lg text-sm font-medium"
            >
              <WarningCircle weight="fill" className="w-5 h-5 shrink-0 mt-0.5" />
              <p>{error}</p>
            </motion.div>
          )}

          <button
            type="submit"
            disabled={loading || !invoice}
            className="w-full py-4 bg-zinc-950 text-white rounded-xl font-medium tracking-wide transition-all active:scale-[0.98] disabled:opacity-50 mt-6"
          >
            {loading ? 'Processing...' : `Pay $${invoice ? (expedited ? invoice.totalWithExpeditedShipping : invoice.totalWithStandardShipping)?.toFixed(2) : '0.00'}`}
          </button>
        </form>
      </motion.div>
    </div>
  );
}
