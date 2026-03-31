'use client';

import React, { useState, useEffect, useCallback, useRef, use } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { useRouter } from 'next/navigation';
import Link from 'next/link';
import { fetchApi } from '@/lib/api';
import { useAuthStore } from '@/store/authStore';
import { CaretLeft, Lightning, Clock, ArrowRight, CheckCircle, WarningCircle, Timer, Trophy, XCircle, CreditCard, Hourglass } from '@phosphor-icons/react';
import AiBidAssistant from '@/app/components/AiBidAssistant';

interface AuctionData {
  auctionId: number;
  itemId: number;
  sellerUsername: string;
  startingPrice: number;
  currentHighestBid: number;
  highestBidderUsername: string | null;
  status: string;
  result: string | null;
  remainingSeconds: number;
}

interface Item {
  id: number;
  title: string;
  description: string;
  startingPrice: number;
  status: string;
  condition: string;
  keywords: string;
  shippingDays: number;
  shippingCost: number;
  expeditedShippingCost: number;
  ownerUsername?: string;
  auction?: AuctionData | null;
}

interface Bid {
  bidId: number;
  bidderUsername: string;
  amount: number;
  timestamp: string;
}

interface PageProps {
  params: Promise<{ id: string }>;
}

function formatCountdown(totalSeconds: number): string {
  if (totalSeconds <= 0) return 'Ended';
  const h = Math.floor(totalSeconds / 3600);
  const m = Math.floor((totalSeconds % 3600) / 60);
  const s = totalSeconds % 60;
  if (h > 0) return `${h}h ${m}m ${s}s`;
  if (m > 0) return `${m}m ${s}s`;
  return `${s}s`;
}

export default function ItemDetail({ params }: PageProps) {
  const router = useRouter();
  const { id } = use(params);
  const { token, user } = useAuthStore();
  
  const [item, setItem] = useState<Item | null>(null);
  const [bids, setBids] = useState<Bid[]>([]);
  const [loading, setLoading] = useState(true);
  
  const [bidAmount, setBidAmount] = useState('');
  const [bidding, setBidding] = useState(false);
  const [bidError, setBidError] = useState('');
  const [bidSuccess, setBidSuccess] = useState(false);
  
  const [auctionResult, setAuctionResult] = useState<any>(null);
  const [countdown, setCountdown] = useState<number>(0);
  const [waitingForEnd, setWaitingForEnd] = useState(false);
  const initialLoadDone = useRef(false);

  const loadData = useCallback(async () => {
    try {
      const itemData = await fetchApi(`/catalogue/items/${id}`);
      setItem(itemData);

      const serverRemaining = itemData.auction?.remainingSeconds ?? 0;
      const backendStatus = itemData.auction?.status;

      // Update countdown from server
      if (serverRemaining > 0) {
        setCountdown(serverRemaining);
        setWaitingForEnd(false);
      } else if (backendStatus === 'ACTIVE' && serverRemaining <= 0) {
        // Auction expired but backend scheduler hasn't marked it ENDED yet
        setCountdown(0);
        setWaitingForEnd(true);
      } else {
        // Auction has ended (backend says ENDED)
        setCountdown(0);
        setWaitingForEnd(false);
      }

      // Load bid history
      try {
        const bidData = await fetchApi(`/bid/history/${id}`);
        const bidList = bidData.bids || [];
        setBids(Array.isArray(bidList) ? bidList : []);
      } catch(e) {
        // Bid history may fail if no auction exists yet
      }

      // Check auction result when auction has ended
      const isEnded = backendStatus === 'ENDED' || itemData.status === 'EXPIRED';
      if (isEnded && token) {
        try {
          const res = await fetchApi(`/auction-result/${id}`);
          setAuctionResult(res);
        } catch(e) {
          // May fail if session is invalid
        }
      }
    } catch (e) {
      console.error(e);
    } finally {
      if (!initialLoadDone.current) {
        initialLoadDone.current = true;
        setLoading(false);
      }
    }
  }, [id, token]);

  // Initial load + polling
  useEffect(() => {
    loadData();
    const interval = setInterval(loadData, 3000);
    return () => clearInterval(interval);
  }, [id, loadData]);

  // When waiting for backend to process auction end, poll faster (every 1s)
  useEffect(() => {
    if (!waitingForEnd) return;
    const fastPoll = setInterval(loadData, 1000);
    return () => clearInterval(fastPoll);
  }, [waitingForEnd, loadData]);

  // Client-side countdown timer
  useEffect(() => {
    if (countdown <= 0) return;
    const timer = setInterval(() => {
      setCountdown(prev => {
        if (prev <= 1) {
          clearInterval(timer);
          setWaitingForEnd(true);
          loadData();
          return 0;
        }
        return prev - 1;
      });
    }, 1000);
    return () => clearInterval(timer);
  }, [countdown > 0]);

  const handlePlaceBid = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!token) return router.push('/login');
    
    setBidding(true);
    setBidError('');
    setBidSuccess(false);

    try {
      // Must select item first before bidding per backend logic
      await fetchApi(`/catalogue/select/${id}`, { method: 'POST' });
      await fetchApi('/bid', {
        method: 'POST',
        body: JSON.stringify({ itemId: Number(id), amount: Number(bidAmount) })
      });
      setBidAmount('');
      setBidSuccess(true);
      setTimeout(() => setBidSuccess(false), 3000);
      loadData();
    } catch (err: any) {
      setBidError(err.message || 'Failed to place bid');
    } finally {
      setBidding(false);
    }
  };

  if (loading) {
    return (
      <div className="min-h-screen pt-24 px-6 flex items-center justify-center">
        <div className="w-12 h-12 bg-zinc-200 rounded-full animate-pulse" />
      </div>
    );
  }

  if (!item) {
    return (
      <div className="min-h-screen pt-24 flex flex-col items-center justify-center">
        <WarningCircle className="w-12 h-12 text-zinc-400 mb-4" />
        <h1 className="text-2xl font-medium tracking-tight">Item Not Found</h1>
      </div>
    );
  }

  // Compute the highest bid
  const auctionHighest = item.auction?.currentHighestBid ?? 0;
  const bidHistoryHighest = bids.length > 0 ? Math.max(...bids.map(b => b.amount)) : 0;
  const highestBid = Math.max(auctionHighest, bidHistoryHighest, item.auction?.startingPrice ?? item.startingPrice ?? 0);
  
  // State derivation
  const isAuctionActive = item.auction?.status === 'ACTIVE' && countdown > 0 && !waitingForEnd;
  const isAuctionEnded = item.auction?.status === 'ENDED' || item.status === 'EXPIRED';
  const isCountdownExpired = countdown <= 0 && (item.auction?.status === 'ACTIVE' || waitingForEnd);
  
  // Auction result fields
  const winnerUsername = auctionResult?.winner;
  const isWinner = auctionResult?.isWinner === true;
  const auctionResultType = auctionResult?.result; // "SOLD" or "UNSOLD"

  return (
    <div className="min-h-screen px-6 lg:px-12 pb-24 max-w-[1400px] mx-auto">
      <Link href="/catalogue" className="inline-flex items-center gap-2 text-sm font-medium text-zinc-500 hover:text-zinc-950 transition-colors mb-12 group">
        <CaretLeft weight="bold" className="w-4 h-4 group-hover:-translate-x-1 transition-transform" />
        Back to Directory
      </Link>

      <div className="grid grid-cols-1 lg:grid-cols-12 gap-12 lg:gap-24 items-start">
        {/* Left Side: Item Info */}
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.6, ease: [0.16, 1, 0.3, 1] }}
          className="lg:col-span-7 flex flex-col"
        >
          <div className="flex items-center gap-4 mb-6">
            <span className={`px-4 py-1.5 rounded-full text-xs font-semibold tracking-widest uppercase flex items-center gap-2 ${
              isAuctionActive 
                ? 'bg-zinc-950 text-white' 
                : isAuctionEnded && auctionResultType === 'SOLD'
                  ? 'bg-emerald-50 text-emerald-700'
                  : isCountdownExpired
                    ? 'bg-amber-50 text-amber-700'
                    : 'bg-zinc-100 text-zinc-500'
            }`}>
              {isAuctionActive && (
                <motion.div
                  animate={{ scale: [1, 1.5, 1], opacity: [1, 0.5, 1] }}
                  transition={{ duration: 2, repeat: Infinity, ease: "easeInOut" }}
                  className="w-2 h-2 rounded-full bg-green-500"
                />
              )}
              {isCountdownExpired && (
                <motion.div
                  animate={{ rotate: 360 }}
                  transition={{ duration: 2, repeat: Infinity, ease: "linear" }}
                  className="w-3 h-3"
                >
                  <Hourglass weight="fill" className="w-3 h-3" />
                </motion.div>
              )}
              {isAuctionActive ? 'LIVE' : isCountdownExpired ? 'ENDING' : isAuctionEnded ? (auctionResultType === 'SOLD' ? 'SOLD' : 'ENDED') : item.status}
            </span>
            <span className="text-sm font-medium text-zinc-500 flex items-center gap-1.5">
              <Timer weight="bold" className="w-4 h-4" /> 
              {isAuctionActive 
                ? formatCountdown(countdown)
                : isCountdownExpired
                  ? 'Finalizing...'
                  : isAuctionEnded
                    ? 'Auction Complete'
                    : `${item.auction?.remainingSeconds ?? 0}s Duration`}
            </span>
          </div>

          <h1 className="text-5xl lg:text-7xl font-medium tracking-tighter leading-[1.1] text-zinc-950 mb-8">
            {item.title}
          </h1>

          <div className="prose prose-zinc prose-p:text-lg prose-p:leading-relaxed text-zinc-600 mb-12 max-w-2xl">
            <p>{item.description}</p>
          </div>

          <div className="grid grid-cols-2 gap-x-12 gap-y-8 py-8 border-y border-zinc-200/50 mb-12">
            <div>
              <div className="text-xs font-semibold text-zinc-500 uppercase tracking-widest mb-1">Condition</div>
              <div className="text-zinc-950 font-medium">{item.condition}</div>
            </div>
            <div>
              <div className="text-xs font-semibold text-zinc-500 uppercase tracking-widest mb-1">Keywords</div>
              <div className="text-zinc-950 font-medium capitalize">{item.keywords?.split(' ').join(', ')}</div>
            </div>
            <div>
              <div className="text-xs font-semibold text-zinc-500 uppercase tracking-widest mb-1">Shipping Cost</div>
              <div className="text-zinc-950 font-mono tracking-tighter">${item.shippingCost?.toFixed(2)}</div>
            </div>
            <div>
              <div className="text-xs font-semibold text-zinc-500 uppercase tracking-widest mb-1">Expedited Available</div>
              <div className="text-zinc-950 font-mono tracking-tighter">+${item.expeditedShippingCost?.toFixed(2)}</div>
            </div>
            <div>
              <div className="text-xs font-semibold text-zinc-500 uppercase tracking-widest mb-1">Shipping Days</div>
              <div className="text-zinc-950 font-medium">{item.shippingDays} days</div>
            </div>
            {item.ownerUsername && (
              <div>
                <div className="text-xs font-semibold text-zinc-500 uppercase tracking-widest mb-1">Seller</div>
                <div className="text-zinc-950 font-medium">{item.ownerUsername}</div>
              </div>
            )}
          </div>
        </motion.div>

        {/* Right Side: Dynamic Bidding/Result Terminal */}
        <motion.div
          initial={{ opacity: 0, x: 20 }}
          animate={{ opacity: 1, x: 0 }}
          transition={{ duration: 0.6, delay: 0.2, ease: [0.16, 1, 0.3, 1] }}
          className="lg:col-span-5 w-full sticky top-32"
        >
          <div className="bg-[#f9fafb] border border-zinc-200 shadow-sm rounded-[2.5rem] p-8 lg:p-10 relative overflow-hidden">
            {/* Holographic foil bg effect for active auctions */}
            {isAuctionActive && (
              <div className="absolute inset-0 bg-gradient-to-tr from-blue-50/20 via-zinc-50/0 to-emerald-50/20 mix-blend-multiply opacity-50" />
            )}
            {/* Green bg for winners */}
            {isAuctionEnded && isWinner && (
              <div className="absolute inset-0 bg-gradient-to-tr from-emerald-50/30 via-zinc-50/0 to-green-50/30 mix-blend-multiply opacity-50" />
            )}

            <div className="relative z-10 flex flex-col h-full">
              {/* Price display */}
              <div className="mb-8">
                <div className="text-xs font-semibold text-zinc-500 uppercase tracking-widest mb-2">
                  {isAuctionActive ? 'Highest Bid' : isAuctionEnded ? 'Final Price' : isCountdownExpired ? 'Last Bid' : 'Starting Price'}
                </div>
                <div className="text-6xl font-mono tracking-tighter text-zinc-950">
                  ${(highestBid || 0).toFixed(2)}
                </div>
                {isAuctionActive && item.auction?.highestBidderUsername && (
                  <div className="mt-2 text-sm text-zinc-500 font-medium flex items-center gap-1.5">
                    Leading: <span className="text-zinc-950">{item.auction.highestBidderUsername}</span>
                  </div>
                )}
                {isAuctionEnded && winnerUsername && (
                  <div className="mt-2 text-sm text-zinc-500 font-medium flex items-center gap-1.5">
                    Winner: <span className="text-zinc-950 font-semibold">{winnerUsername}</span>
                  </div>
                )}
              </div>

              {/* ─── STATE 1: ACTIVE AUCTION (bidding form) ─── */}
              {isAuctionActive && (
                <>
                  {/* Countdown bar */}
                  <div className="mb-6 bg-white border border-zinc-200 rounded-2xl p-4 flex items-center justify-between">
                    <div className="flex items-center gap-2 text-sm font-semibold text-zinc-950">
                      <Timer weight="bold" className="w-5 h-5 text-amber-500" />
                      Time Remaining
                    </div>
                    <div className="font-mono text-lg tracking-tighter text-zinc-950 font-semibold">
                      {formatCountdown(countdown)}
                    </div>
                  </div>

                  <form onSubmit={handlePlaceBid} className="mb-10 w-full relative">
                    <div className="relative flex items-center bg-white border border-zinc-200 rounded-full overflow-hidden focus-within:ring-2 focus-within:ring-zinc-950/10 focus-within:border-zinc-300 transition-all">
                      <span className="absolute left-6 text-zinc-400 font-mono text-lg">$</span>
                      <input
                        type="number"
                        step="1"
                        min={Math.floor(highestBid) + 1}
                        value={bidAmount}
                        onChange={(e) => setBidAmount(e.target.value)}
                        placeholder={String(Math.floor(highestBid) + 1)}
                        className="w-full py-5 pl-12 pr-[140px] font-mono text-xl tracking-tighter bg-transparent focus:outline-none"
                        disabled={bidding}
                      />
                      <button
                        type="submit"
                        disabled={bidding || !bidAmount}
                        className="absolute right-2 px-6 py-3 bg-zinc-950 text-white rounded-full font-medium text-sm transition-all hover:bg-zinc-800 active:scale-95 disabled:opacity-50"
                      >
                        {bidding ? 'Placing...' : 'Place Bid'}
                      </button>
                    </div>
                    {!token && (
                      <div className="text-zinc-500 text-sm mt-3 px-4 font-medium">
                        <Link href="/login" className="text-zinc-950 underline underline-offset-4 decoration-zinc-300">Sign in</Link> to place a bid
                      </div>
                    )}
                    {bidError && (
                      <div className="text-red-500 text-sm mt-3 flex items-center gap-1.5 px-4 font-medium">
                        <WarningCircle weight="fill" /> {bidError}
                      </div>
                    )}
                    {bidSuccess && (
                      <motion.div 
                        initial={{ opacity: 0, y: 5 }}
                        animate={{ opacity: 1, y: 0 }}
                        className="text-emerald-600 text-sm mt-3 flex items-center gap-1.5 px-4 font-medium"
                      >
                        <CheckCircle weight="fill" /> Bid placed successfully!
                      </motion.div>
                    )}
                  </form>
                </>
              )}

              {/* ─── STATE 2: COUNTDOWN HIT ZERO, WAITING FOR BACKEND ─── */}
              {isCountdownExpired && (
                <motion.div
                  initial={{ opacity: 0, y: 10 }}
                  animate={{ opacity: 1, y: 0 }}
                  className="mb-10 bg-amber-50 border border-amber-200 rounded-[2rem] p-6 flex flex-col items-center justify-center text-center"
                >
                  <motion.div
                    animate={{ rotate: [0, 180, 360] }}
                    transition={{ duration: 2, repeat: Infinity, ease: "linear" }}
                    className="mb-4"
                  >
                    <Hourglass weight="fill" className="w-10 h-10 text-amber-500" />
                  </motion.div>
                  <h3 className="text-lg font-medium tracking-tight mb-2 text-amber-900">Auction Ending...</h3>
                  <p className="text-sm text-amber-700">The auction has expired. Waiting for the system to finalize the results.</p>
                </motion.div>
              )}

              {/* ─── STATE 3: AUCTION ENDED — RESULT LOADED ─── */}
              {isAuctionEnded && auctionResult && (
                <motion.div
                  initial={{ opacity: 0, scale: 0.95, y: 10 }}
                  animate={{ opacity: 1, scale: 1, y: 0 }}
                  transition={{ type: 'spring', stiffness: 100, damping: 20 }}
                  className="mb-10"
                >
                  {auctionResultType === 'SOLD' ? (
                    <div className="bg-white border border-zinc-200 rounded-[2rem] p-6 flex flex-col items-center justify-center text-center">
                      <Trophy weight="fill" className="w-12 h-12 text-amber-500 mb-4" />
                      <h3 className="text-xl font-medium tracking-tight mb-2">Auction Concluded</h3>
                      <p className="text-sm text-zinc-500 mb-2">
                        Winner: <span className="font-semibold text-zinc-950">{winnerUsername}</span>
                      </p>
                      <p className="text-sm text-zinc-500 mb-6">
                        Winning Bid: <span className="font-mono font-semibold text-zinc-950">${highestBid.toFixed(2)}</span>
                      </p>
                      
                      {isWinner ? (
                        <div className="w-full space-y-3">
                          <div className="bg-emerald-50 border border-emerald-200 rounded-2xl p-4 text-center">
                            <CheckCircle weight="fill" className="w-6 h-6 text-emerald-500 mx-auto mb-2" />
                            <p className="text-sm font-semibold text-emerald-800">🎉 Congratulations! You won this auction!</p>
                          </div>
                          <Link
                            href={`/payment/${id}`}
                            className="w-full py-4 rounded-full bg-zinc-950 text-white font-medium flex items-center justify-center gap-2 hover:bg-zinc-800 transition-colors active:scale-[0.98]"
                          >
                            <CreditCard weight="fill" className="w-5 h-5" />
                            Proceed to Payment <ArrowRight weight="bold" />
                          </Link>
                        </div>
                      ) : (
                        <div className="text-sm font-medium text-zinc-500 bg-zinc-50 px-6 py-3 rounded-full border border-zinc-100">
                          You did not win this auction.
                        </div>
                      )}
                    </div>
                  ) : (
                    <div className="bg-white border border-zinc-200 rounded-[2rem] p-6 flex flex-col items-center justify-center text-center">
                      <XCircle weight="fill" className="w-12 h-12 text-zinc-400 mb-4" />
                      <h3 className="text-xl font-medium tracking-tight mb-2">Auction Ended</h3>
                      <p className="text-sm text-zinc-500">This auction ended with no bids.</p>
                    </div>
                  )}
                </motion.div>
              )}

              {/* ─── STATE 4: AUCTION ENDED — WAITING FOR RESULT (logged in) ─── */}
              {isAuctionEnded && !auctionResult && token && (
                <div className="mb-10 bg-white border border-zinc-200 rounded-[2rem] p-6 text-center">
                  <div className="w-8 h-8 bg-zinc-200 rounded-full animate-pulse mx-auto mb-3" />
                  <p className="text-sm text-zinc-500">Loading auction result...</p>
                </div>
              )}

              {/* ─── STATE 5: AUCTION ENDED — NOT LOGGED IN ─── */}
              {isAuctionEnded && !auctionResult && !token && (
                <div className="mb-10 bg-white border border-zinc-200 rounded-[2rem] p-6 text-center">
                  <Trophy weight="fill" className="w-10 h-10 text-zinc-300 mx-auto mb-3" />
                  <p className="text-sm text-zinc-500 mb-3">This auction has ended.</p>
                  <Link href="/login" className="text-sm font-medium text-zinc-950 underline underline-offset-4 decoration-zinc-300">
                    Sign in to view results
                  </Link>
                </div>
              )}

              {/* ─── BID HISTORY (always visible) ─── */}
              <div>
                <div className="flex items-center justify-between mb-4 border-b border-zinc-200/50 pb-2 text-xs font-semibold uppercase tracking-widest text-zinc-400">
                  <span>Bid History</span>
                  <span className="flex items-center gap-1">
                    <Lightning /> 
                    {isAuctionActive ? 'Live' : `${bids.length} Total`}
                  </span>
                </div>
                
                <div className="space-y-1 max-h-[250px] overflow-y-auto pr-2 scrollbar-none">
                  {bids.length === 0 ? (
                    <div className="text-sm text-zinc-400 italic py-4">No bids placed yet.</div>
                  ) : (
                    <AnimatePresence>
                      {[...bids].sort((a,b) => b.amount - a.amount).map((bid, i) => (
                        <motion.div
                          key={bid.bidId || i}
                          layout
                          initial={{ opacity: 0, x: -10 }}
                          animate={{ opacity: 1, x: 0 }}
                          className={`flex items-center justify-between p-3 rounded-xl transition-colors ${
                            i === 0 ? 'bg-zinc-100/80' : 'hover:bg-zinc-100/50'
                          }`}
                        >
                          <div className="flex items-center gap-3">
                            <div className={`w-8 h-8 rounded-full flex items-center justify-center text-xs font-medium ${
                              i === 0 ? 'bg-zinc-950 text-white' : 'bg-zinc-200 text-zinc-500'
                            }`}>
                              {bid.bidderUsername.substring(0, 2).toUpperCase()}
                            </div>
                            <div className="flex flex-col">
                              <span className="text-sm font-medium text-zinc-950">{bid.bidderUsername}</span>
                              {i === 0 && <span className="text-[10px] font-semibold text-emerald-600 uppercase tracking-widest">Highest</span>}
                            </div>
                          </div>
                          <span className={`font-mono tracking-tighter ${i === 0 ? 'text-zinc-950 font-semibold' : 'text-zinc-600'}`}>${bid.amount.toFixed(2)}</span>
                        </motion.div>
                      ))}
                    </AnimatePresence>
                  )}
                </div>
              </div>

              {/* ─── AI BID ASSISTANT (Gen AI Chat) ─── */}
              {token && (
                <AiBidAssistant
                  itemId={Number(id)}
                  isActive={isAuctionActive}
                  currentHighestBid={highestBid}
                  token={token}
                />
              )}
            </div>
          </div>
        </motion.div>
      </div>
    </div>
  );
}
