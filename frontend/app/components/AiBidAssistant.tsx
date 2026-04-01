'use client';

import React, { useState, useRef, useEffect } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { fetchApi } from '@/lib/api';
import { Robot, PaperPlaneTilt, Lightning, Strategy, Target, Clock, Stop, SpinnerGap, Sparkle, ChatCircle } from '@phosphor-icons/react';

interface ChatMessage {
  role: 'user' | 'ai';
  content: string;
  action?: string | null;
  suggestedStrategy?: string | null;
  suggestedMaxBid?: number | null;
  source?: string;
  timestamp: Date;
}

interface AutoBidStatus {
  sessionId: string;
  status: string;
  strategy: string;
  maxBid: number;
  bidsPlaced: number;
  currentBid: number;
  actionLog: { timestamp: string; action: string; amount: number; details: string }[];
}

interface AiBidAssistantProps {
  itemId: number;
  isActive: boolean; // auction is active
  currentHighestBid: number;
  token: string | null;
}

export default function AiBidAssistant({ itemId, isActive, currentHighestBid, token }: AiBidAssistantProps) {
  const [open, setOpen] = useState(false);
  const [messages, setMessages] = useState<ChatMessage[]>([]);
  const [input, setInput] = useState('');
  const [loading, setLoading] = useState(false);
  const [budget, setBudget] = useState('');

  // Auto-bid session state
  const [autoBidSession, setAutoBidSession] = useState<AutoBidStatus | null>(null);
  const [pollingActive, setPollingActive] = useState(false);

  const chatEndRef = useRef<HTMLDivElement>(null);
  const inputRef = useRef<HTMLInputElement>(null);

  // Scroll to bottom on new messages
  useEffect(() => {
    chatEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages]);

  // Auto-focus input when chat opens
  useEffect(() => {
    if (open) inputRef.current?.focus();
  }, [open]);

  // Poll auto-bid status when active
  useEffect(() => {
    if (!autoBidSession || autoBidSession.status !== 'ACTIVE') {
      setPollingActive(false);
      return;
    }
    setPollingActive(true);
    const interval = setInterval(async () => {
      try {
        const status = await fetchApi(`/auto-bid/status/${autoBidSession.sessionId}`);
        setAutoBidSession(status);
        if (status.status !== 'ACTIVE') {
          clearInterval(interval);
          setPollingActive(false);
          // Add a system message about the status change
          setMessages(prev => [...prev, {
            role: 'ai',
            content: status.status === 'WON' 
              ? '🎉 The auto-bidder won the auction! Congratulations!'
              : status.status === 'OUTBID_MAX'
                ? `⚠️ The auto-bidder has stopped — the required bid exceeds your maximum budget of $${status.maxBid}.`
                : `The auto-bidder has stopped. Status: ${status.status}`,
            timestamp: new Date(),
            source: 'SYSTEM'
          }]);
        }
      } catch (e) {
        // Silently fail
      }
    }, 3000);
    return () => clearInterval(interval);
  }, [autoBidSession?.sessionId, autoBidSession?.status]);

  // Add welcome message on first open
  useEffect(() => {
    if (open && messages.length === 0) {
      setMessages([{
        role: 'ai',
        content: `Hi! I'm PrimeBid AI 🤖 — your auction bidding assistant. I can help you:\n\n• Analyze this auction and recommend a bidding strategy\n• Start auto-bidding for you with a strategy of your choice\n• Answer questions about pricing and competition\n\nJust tell me what you'd like to do, or set a budget to get started!`,
        timestamp: new Date(),
        source: 'SYSTEM'
      }]);
    }
  }, [open]);

  const sendMessage = async () => {
    if (!input.trim() || loading || !token) return;

    const userMsg: ChatMessage = {
      role: 'user',
      content: input.trim(),
      timestamp: new Date(),
    };
    setMessages(prev => [...prev, userMsg]);
    setInput('');
    setLoading(true);

    try {
      const res = await fetchApi('/auto-bid/chat', {
        method: 'POST',
        body: JSON.stringify({
          itemId,
          message: userMsg.content,
          budget: budget ? Number(budget) : 0,
        }),
      });

      const aiMsg: ChatMessage = {
        role: 'ai',
        content: res.reply || 'I couldn\'t process that. Try asking about the auction or bidding strategy.',
        action: res.action || null,
        suggestedStrategy: res.suggestedStrategy || null,
        suggestedMaxBid: res.suggestedMaxBid || null,
        source: res.source || 'UNKNOWN',
        timestamp: new Date(),
      };
      setMessages(prev => [...prev, aiMsg]);
    } catch (err: any) {
      setMessages(prev => [...prev, {
        role: 'ai',
        content: `Sorry, I encountered an error: ${err.message || 'Unknown error'}. Please try again.`,
        timestamp: new Date(),
        source: 'ERROR',
      }]);
    } finally {
      setLoading(false);
    }
  };

  const handleStartAutoBid = async (strategy: string, maxBid: number) => {
    if (!token) return;
    setLoading(true);
    try {
      // Select item first
      await fetchApi(`/catalogue/select/${itemId}`, { method: 'POST' });
      const res = await fetchApi('/auto-bid/start', {
        method: 'POST',
        body: JSON.stringify({ itemId, maxBid, strategy }),
      });
      setAutoBidSession(res);
      setMessages(prev => [...prev, {
        role: 'ai',
        content: `✅ Auto-bidder activated!\n• Strategy: ${strategy}\n• Max Budget: $${maxBid}\n• Session: ${res.sessionId}\n\nI'm now monitoring the auction and will bid on your behalf. You can ask me to stop at any time.`,
        timestamp: new Date(),
        source: 'SYSTEM',
      }]);
    } catch (err: any) {
      setMessages(prev => [...prev, {
        role: 'ai',
        content: `❌ Could not start auto-bidder: ${err.message}`,
        timestamp: new Date(),
        source: 'ERROR',
      }]);
    } finally {
      setLoading(false);
    }
  };

  const handleStopAutoBid = async () => {
    if (!autoBidSession || !token) return;
    setLoading(true);
    try {
      const res = await fetchApi(`/auto-bid/stop/${autoBidSession.sessionId}`, { method: 'POST' });
      setAutoBidSession(prev => prev ? { ...prev, status: 'STOPPED' } : null);
      setMessages(prev => [...prev, {
        role: 'ai',
        content: `⛔ Auto-bidder stopped. ${res.message}`,
        timestamp: new Date(),
        source: 'SYSTEM',
      }]);
    } catch (err: any) {
      setMessages(prev => [...prev, {
        role: 'ai',
        content: `Failed to stop auto-bidder: ${err.message}`,
        timestamp: new Date(),
        source: 'ERROR',
      }]);
    } finally {
      setLoading(false);
    }
  };

  const quickPrompts = [
    { label: 'Analyze auction', msg: 'Analyze this auction and tell me the best strategy' },
    { label: 'Start bidding', msg: budget ? `Start auto-bidding for me with a max budget of $${budget}` : 'Start auto-bidding for me' },
    { label: 'How much?', msg: 'How much should I bid on this item?' },
  ];

  if (!token) return null;

  return (
    <div className="mt-6">
      {/* Toggle Button */}
      <motion.button
        onClick={() => setOpen(!open)}
        whileHover={{ scale: 1.02 }}
        whileTap={{ scale: 0.98 }}
        className={`w-full py-4 px-6 rounded-2xl font-medium text-sm flex items-center justify-center gap-3 transition-all border ${
          open
            ? 'bg-zinc-950 text-white border-zinc-950'
            : 'bg-gradient-to-r from-teal-50 to-emerald-50 border-teal-200/60 text-zinc-950 hover:border-teal-300'
        }`}
      >
        <ChatCircle weight="fill" className={`w-5 h-5 ${open ? 'text-teal-300' : 'text-teal-500'}`} />
        {open ? 'Close AI Assistant' : 'PrimeBid AI'}
        {!open && (
          <span className="text-[10px] font-bold uppercase tracking-widest px-2 py-0.5 rounded-full bg-teal-100 text-teal-600">
            Gen AI
          </span>
        )}
      </motion.button>

      {/* Chat Panel */}
      <AnimatePresence>
        {open && (
          <motion.div
            initial={{ opacity: 0, height: 0, y: -10 }}
            animate={{ opacity: 1, height: 'auto', y: 0 }}
            exit={{ opacity: 0, height: 0, y: -10 }}
            transition={{ type: 'spring', stiffness: 200, damping: 25 }}
            className="overflow-hidden"
          >
            <div className="mt-4 bg-white border border-zinc-200 rounded-[2rem] shadow-lg shadow-zinc-950/5 overflow-hidden">
              {/* Header */}
              <div className="px-6 py-4 border-b border-zinc-100 flex items-center justify-between bg-gradient-to-r from-teal-50/50 to-emerald-50/50">
                <div className="flex items-center gap-3">
                  <div className="w-8 h-8 bg-gradient-to-br from-teal-500 to-emerald-500 rounded-full flex items-center justify-center">
                    <ChatCircle weight="fill" className="w-4 h-4 text-white" />
                  </div>
                  <div>
                    <div className="text-sm font-semibold text-zinc-950">PrimeBid AI</div>
                    <div className="text-[10px] font-medium text-zinc-500 flex items-center gap-1">
                      <Sparkle weight="fill" className="w-3 h-3 text-teal-400" />
                      Powered by Gemini
                    </div>
                  </div>
                </div>
                {autoBidSession?.status === 'ACTIVE' && (
                  <div className="flex items-center gap-2">
                    <motion.div
                      animate={{ scale: [1, 1.3, 1], opacity: [1, 0.6, 1] }}
                      transition={{ duration: 1.5, repeat: Infinity }}
                      className="w-2 h-2 rounded-full bg-teal-500"
                    />
                    <span className="text-[10px] font-bold uppercase tracking-widest text-teal-600">Auto-Bidding</span>
                  </div>
                )}
              </div>

              {/* Budget Input */}
              <div className="px-6 py-3 border-b border-zinc-100 bg-zinc-50/50 flex items-center gap-3">
                <span className="text-[10px] font-bold uppercase tracking-widest text-zinc-400 whitespace-nowrap">Budget</span>
                <div className="relative flex-1 max-w-[200px]">
                  <span className="absolute left-3 top-1/2 -translate-y-1/2 text-zinc-400 text-sm font-mono">$</span>
                  <input
                    type="number"
                    value={budget}
                    onChange={(e) => setBudget(e.target.value)}
                    placeholder="Max budget"
                    className="w-full pl-7 pr-3 py-1.5 bg-white border border-zinc-200 rounded-lg text-sm font-mono focus:outline-none focus:ring-1 focus:ring-teal-300 focus:border-teal-300"
                  />
                </div>
                {budget && (
                  <span className="text-xs text-zinc-500">AI will stay within your budget</span>
                )}
              </div>

              {/* Auto-Bid Status Bar */}
              {autoBidSession && autoBidSession.status === 'ACTIVE' && (
                <div className="px-6 py-3 border-b border-zinc-100 bg-teal-50/50 flex items-center justify-between">
                  <div className="flex items-center gap-3 text-xs">
                    <span className="font-bold uppercase tracking-widest text-teal-700">
                      {autoBidSession.strategy}
                    </span>
                    <span className="text-teal-600">
                      {autoBidSession.bidsPlaced} bids placed
                    </span>
                    {autoBidSession.currentBid > 0 && (
                      <span className="font-mono font-semibold text-teal-700">
                        Last: ${autoBidSession.currentBid}
                      </span>
                    )}
                  </div>
                  <button
                    onClick={handleStopAutoBid}
                    disabled={loading}
                    className="text-xs font-semibold text-red-600 hover:text-red-700 flex items-center gap-1 disabled:opacity-50"
                  >
                    <Stop weight="fill" className="w-3 h-3" />
                    Stop
                  </button>
                </div>
              )}

              {/* Messages */}
              <div className="h-[300px] overflow-y-auto px-6 py-4 space-y-4 scrollbar-none">
                {messages.map((msg, i) => (
                  <motion.div
                    key={i}
                    initial={{ opacity: 0, y: 8 }}
                    animate={{ opacity: 1, y: 0 }}
                    transition={{ delay: 0.05 }}
                    className={`flex ${msg.role === 'user' ? 'justify-end' : 'justify-start'}`}
                  >
                    <div className={`max-w-[85%] ${msg.role === 'user' ? 'order-2' : 'order-1'}`}>
                      <div className={`px-4 py-3 rounded-2xl text-sm leading-relaxed whitespace-pre-line ${
                        msg.role === 'user'
                          ? 'bg-zinc-950 text-white rounded-tr-md'
                          : 'bg-zinc-100 text-zinc-800 rounded-tl-md'
                      }`}>
                        {msg.content}
                      </div>

                      {/* Action buttons from AI response */}
                      {msg.role === 'ai' && msg.action === 'START_AUTOBID' && msg.suggestedStrategy && isActive && (
                        <motion.div
                          initial={{ opacity: 0, y: 4 }}
                          animate={{ opacity: 1, y: 0 }}
                          className="mt-2 flex gap-2"
                        >
                          <button
                            onClick={() => handleStartAutoBid(
                              msg.suggestedStrategy!,
                              msg.suggestedMaxBid || Number(budget) || Math.floor(currentHighestBid * 1.5)
                            )}
                            disabled={loading || !!autoBidSession?.status?.match(/ACTIVE/)}
                            className="px-4 py-2 bg-teal-600 text-white rounded-xl text-xs font-semibold hover:bg-teal-700 transition-colors disabled:opacity-50 flex items-center gap-1.5"
                          >
                            <Lightning weight="fill" className="w-3 h-3" />
                            Activate {msg.suggestedStrategy}
                            {msg.suggestedMaxBid && ` ($${msg.suggestedMaxBid} max)`}
                          </button>
                        </motion.div>
                      )}

                      {msg.role === 'ai' && msg.action === 'STOP_AUTOBID' && autoBidSession?.status === 'ACTIVE' && (
                        <motion.div
                          initial={{ opacity: 0, y: 4 }}
                          animate={{ opacity: 1, y: 0 }}
                          className="mt-2"
                        >
                          <button
                            onClick={handleStopAutoBid}
                            disabled={loading}
                            className="px-4 py-2 bg-red-600 text-white rounded-xl text-xs font-semibold hover:bg-red-700 transition-colors disabled:opacity-50 flex items-center gap-1.5"
                          >
                            <Stop weight="fill" className="w-3 h-3" />
                            Stop Auto-Bidder
                          </button>
                        </motion.div>
                      )}

                      {/* Source badge */}
                      <div className="mt-1 flex items-center gap-2">
                        <span className="text-[10px] text-zinc-400">
                          {msg.timestamp.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                        </span>
                        {msg.role === 'ai' && msg.source && msg.source !== 'SYSTEM' && msg.source !== 'ERROR' && (
                          <span className={`text-[9px] font-bold uppercase tracking-widest px-1.5 py-0.5 rounded ${
                            msg.source.startsWith('GEMINI_AI') ? 'bg-teal-100 text-teal-600' : 'bg-zinc-100 text-zinc-500'
                          }`}>
                            {msg.source.startsWith('GEMINI_AI') ? 'AI' : 'Fallback'}
                          </span>
                        )}
                      </div>
                    </div>
                  </motion.div>
                ))}

                {loading && (
                  <motion.div
                    initial={{ opacity: 0 }}
                    animate={{ opacity: 1 }}
                    className="flex justify-start"
                  >
                    <div className="bg-zinc-100 px-4 py-3 rounded-2xl rounded-tl-md flex items-center gap-2">
                      <SpinnerGap weight="bold" className="w-4 h-4 text-teal-500 animate-spin" />
                      <span className="text-sm text-zinc-500">Thinking...</span>
                    </div>
                  </motion.div>
                )}

                <div ref={chatEndRef} />
              </div>

              {/* Quick Prompts */}
              {messages.length <= 1 && (
                <div className="px-6 pb-3 flex flex-wrap gap-2">
                  {quickPrompts.map((qp, i) => (
                    <button
                      key={i}
                      onClick={() => { setInput(qp.msg); }}
                      className="px-3 py-1.5 bg-zinc-100 text-zinc-600 rounded-full text-xs font-medium hover:bg-zinc-200 transition-colors"
                    >
                      {qp.label}
                    </button>
                  ))}
                </div>
              )}

              {/* Input */}
              <div className="px-4 py-3 border-t border-zinc-100 bg-zinc-50/50">
                <form
                  onSubmit={(e) => { e.preventDefault(); sendMessage(); }}
                  className="flex items-center gap-2"
                >
                  <input
                    ref={inputRef}
                    type="text"
                    value={input}
                    onChange={(e) => setInput(e.target.value)}
                    placeholder={isActive ? 'Ask about bidding, strategies, or say "start auto-bidding"...' : 'Auction has ended'}
                    disabled={loading || !isActive}
                    className="flex-1 px-4 py-3 bg-white border border-zinc-200 rounded-xl text-sm focus:outline-none focus:ring-1 focus:ring-teal-300 focus:border-teal-300 disabled:opacity-50"
                  />
                  <button
                    type="submit"
                    disabled={loading || !input.trim() || !isActive}
                    className="w-10 h-10 bg-zinc-950 text-white rounded-xl flex items-center justify-center hover:bg-zinc-800 transition-colors disabled:opacity-50 disabled:hover:bg-zinc-950 shrink-0"
                  >
                    <PaperPlaneTilt weight="fill" className="w-4 h-4" />
                  </button>
                </form>
              </div>
            </div>
          </motion.div>
        )}
      </AnimatePresence>
    </div>
  );
}
