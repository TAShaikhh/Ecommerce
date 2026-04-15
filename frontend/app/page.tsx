'use client';

import React from 'react';
import { motion } from 'framer-motion';
import Link from 'next/link';
import { ArrowUpRight, Gavel, ShieldCheck, Lightning, ChartLineUp } from '@phosphor-icons/react';

const stagger = {
  hidden: { opacity: 0 },
  visible: {
    opacity: 1,
    transition: { staggerChildren: 0.1, delayChildren: 0.1 },
  },
};

const fadeUp = {
  hidden: { opacity: 0, y: 24 },
  visible: { opacity: 1, y: 0, transition: { type: 'spring' as const, stiffness: 80, damping: 20 } },
};

const features = [
  { icon: Gavel, title: 'Forward Auctions', desc: 'Bid incrementally. Highest bid wins when the clock runs out.' },
  { icon: ShieldCheck, title: 'Session Security', desc: 'Token-based auth with server-side session validation.' },
  { icon: Lightning, title: 'Real-Time', desc: 'Live countdown, instant bid updates, polling infrastructure.' },
  { icon: ChartLineUp, title: 'Full Lifecycle', desc: 'List → Bid → Win → Pay → Receipt. End-to-end in one system.' },
];

export default function Home() {
  return (
    <div className="min-h-screen flex flex-col">
      {/* ─── HERO ─── */}
      <section className="flex-1 pt-40 pb-24 px-6 lg:px-12 grid grid-cols-1 lg:grid-cols-2 gap-12 lg:gap-24 items-center max-w-[1400px] mx-auto w-full">
        <motion.div
          initial="hidden"
          animate="visible"
          variants={stagger}
          className="flex flex-col items-start"
        >
          <motion.div
            variants={fadeUp}
            className="px-4 py-1.5 rounded-full border border-zinc-200 text-xs font-semibold tracking-widest uppercase text-zinc-500 mb-8"
          >
            PrimeBid Enterprise
          </motion.div>
          
          <motion.h1
            variants={fadeUp}
            className="text-5xl md:text-7xl font-medium tracking-tighter leading-[1.05] text-zinc-950 mb-8 max-w-[700px]"
          >
            The intelligent
            <br className="hidden md:block" /> format for
            <br className="hidden md:block" /> modern auctions.
          </motion.h1>

          <motion.p
            variants={fadeUp}
            className="text-lg text-zinc-500 mb-12 max-w-md leading-relaxed"
          >
            Discover high-value assets. Participate in deterministic bidding environments powered by the PrimeBid gateway infrastructure.
          </motion.p>

          <motion.div variants={fadeUp} className="flex items-center gap-4">
            <Link
              href="/catalogue"
              className="group relative flex items-center justify-center gap-3 bg-zinc-950 text-white px-8 py-4 rounded-full overflow-hidden transition-all active:scale-95"
            >
              <span className="relative z-10 font-medium">Enter Catalogue</span>
              <ArrowUpRight weight="bold" className="w-4 h-4 relative z-10 group-hover:translate-x-0.5 group-hover:-translate-y-0.5 transition-transform" />
              <div
                className="absolute inset-0 bg-zinc-800 translate-y-full group-hover:translate-y-0 transition-transform duration-500 ease-[cubic-bezier(0.16,1,0.3,1)]"
              />
            </Link>
            <Link
              href="/signup"
              className="text-sm font-medium text-zinc-500 hover:text-zinc-950 transition-colors px-4 py-4"
            >
              Create account →
            </Link>
          </motion.div>
        </motion.div>

        {/* ─── Hero Card ─── */}
        <motion.div
          initial={{ opacity: 0, x: 30, rotateY: 8 }}
          animate={{ opacity: 1, x: 0, rotateY: 0 }}
          transition={{ type: 'spring', stiffness: 50, damping: 20, delay: 0.3 }}
          className="w-full relative hidden lg:block"
        >
          <div className="relative h-[600px] rounded-[3rem] overflow-hidden bg-zinc-100 p-8 lg:p-10">
            <div className="absolute inset-0 bg-gradient-to-tr from-zinc-200/80 to-zinc-50/50" />
            
            <div className="relative z-10 w-full h-full liquid-glass rounded-3xl p-8 shadow-2xl shadow-zinc-900/5 ring-1 ring-zinc-900/5 flex flex-col justify-between">
              <div className="flex justify-between items-start">
                <div className="flex items-center gap-3">
                  <div className="w-10 h-10 rounded-full bg-zinc-950 flex items-center justify-center">
                    <span className="text-xs text-white font-bold">PB</span>
                  </div>
                  <div>
                    <div className="text-xs text-zinc-400 font-medium">Asset #4201</div>
                    <div className="text-sm font-medium text-zinc-950">Vintage Chronograph</div>
                  </div>
                </div>
                <div className="px-3 py-1 rounded-full bg-green-100/70 text-green-700 text-xs font-semibold tracking-wide flex items-center gap-1.5">
                  <motion.div
                    animate={{ scale: [1, 1.5, 1], opacity: [1, 0.5, 1] }}
                    transition={{ duration: 2, repeat: Infinity, ease: "easeInOut" }}
                    className="w-1.5 h-1.5 rounded-full bg-green-500"
                  />
                  LIVE
                </div>
              </div>
              
              {/* Simulated bid entries */}
              <div className="flex-1 flex flex-col justify-center space-y-3 py-8">
                {[
                  { user: 'collector_x', amount: '$14,250', time: '2s ago', active: true },
                  { user: 'dealer_prime', amount: '$14,100', time: '18s ago', active: false },
                  { user: 'asset_mgr', amount: '$13,800', time: '45s ago', active: false },
                ].map((bid, i) => (
                  <motion.div
                    key={i}
                    initial={{ opacity: 0, x: -10 }}
                    animate={{ opacity: 1, x: 0 }}
                    transition={{ delay: 0.6 + i * 0.15 }}
                    className={`flex items-center justify-between px-4 py-3 rounded-xl ${i === 0 ? 'bg-zinc-100/80' : ''}`}
                  >
                    <div className="flex items-center gap-3">
                      <div className={`w-7 h-7 rounded-full flex items-center justify-center text-[10px] font-semibold ${i === 0 ? 'bg-zinc-950 text-white' : 'bg-zinc-200 text-zinc-500'}`}>
                        {bid.user.substring(0, 2).toUpperCase()}
                      </div>
                      <div>
                        <div className="text-sm font-medium text-zinc-950">{bid.user}</div>
                        <div className="text-[10px] text-zinc-400">{bid.time}</div>
                      </div>
                    </div>
                    <span className="font-mono text-sm tracking-tighter text-zinc-950 font-semibold">{bid.amount}</span>
                  </motion.div>
                ))}
              </div>
              
              <div className="flex justify-between items-end border-t border-zinc-200/50 pt-6">
                <div>
                  <div className="text-[10px] text-zinc-400 uppercase tracking-widest font-semibold mb-1">Highest Bid</div>
                  <div className="text-3xl font-mono tracking-tighter text-zinc-950">$14,250.00</div>
                </div>
                <div className="flex items-center gap-2 text-xs text-zinc-400 font-medium">
                  <Lightning weight="fill" className="w-3.5 h-3.5 text-amber-400" />
                  3m 42s remaining
                </div>
              </div>
            </div>
          </div>
        </motion.div>
      </section>

      {/* ─── FEATURES GRID ─── */}
      <section className="border-t border-zinc-200/60 bg-white/50">
        <div className="max-w-[1400px] mx-auto px-6 lg:px-12 py-20">
          <motion.div
            initial="hidden"
            whileInView="visible"
            viewport={{ once: true, margin: '-100px' }}
            variants={stagger}
            className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-8"
          >
            {features.map((f, i) => (
              <motion.div key={i} variants={fadeUp} className="group p-6 rounded-2xl hover:bg-zinc-50 transition-colors">
                <div className="w-10 h-10 rounded-xl bg-zinc-100 flex items-center justify-center mb-4 group-hover:bg-zinc-950 group-hover:text-white transition-all duration-300 text-zinc-500">
                  <f.icon weight="fill" className="w-5 h-5" />
                </div>
                <h3 className="text-base font-medium tracking-tight text-zinc-950 mb-2">{f.title}</h3>
                <p className="text-sm text-zinc-500 leading-relaxed">{f.desc}</p>
              </motion.div>
            ))}
          </motion.div>
        </div>
      </section>
    </div>
  );
}
