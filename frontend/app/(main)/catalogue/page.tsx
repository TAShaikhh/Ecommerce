'use client';

import React, { useState, useEffect, useCallback } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import Link from 'next/link';
import { fetchApi } from '@/lib/api';
import { MagnifyingGlass, Funnel, Clock, Package, Gavel, Timer } from '@phosphor-icons/react';

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
  shippingCost: number;
  expeditedShippingCost: number;
  shippingDays: number;
  auction?: AuctionData | null;
}

function formatCountdown(totalSeconds: number): string {
  if (totalSeconds <= 0) return '0s';
  const m = Math.floor(totalSeconds / 60);
  const s = totalSeconds % 60;
  if (m > 0) return `${m}m ${s}s`;
  return `${s}s`;
}

export default function Catalogue() {
  const [items, setItems] = useState<Item[]>([]);
  const [initialLoading, setInitialLoading] = useState(true);
  const [search, setSearch] = useState('');
  const [tick, setTick] = useState(0);
  const [lastFetchTime, setLastFetchTime] = useState(0);

  // Client-side countdown timer — tick every second to drive local countdown
  useEffect(() => {
    const interval = setInterval(() => setTick(t => t + 1), 1000);
    return () => clearInterval(interval);
  }, []);

  const loadItems = useCallback(async (keyword = '') => {
    try {
      const endpoint = keyword ? `/catalogue?keyword=${encodeURIComponent(keyword)}` : '/catalogue';
      const data = await fetchApi(endpoint);
      // Backend returns { items: [...], count: N, _links: {...} }
      const loadedItems: Item[] = data.items || [];
      setItems(loadedItems);
      setLastFetchTime(Date.now());
    } catch (e) {
      console.error(e);
      setItems([]);
    } finally {
      setInitialLoading(false);
    }
  }, []);

  useEffect(() => {
    const delayDebounceFn = setTimeout(() => {
      loadItems(search);
    }, 400);
    return () => clearTimeout(delayDebounceFn);
  }, [search, loadItems]);

  // Re-fetch every 5 seconds for live auction updates
  useEffect(() => {
    const interval = setInterval(() => loadItems(search), 5000);
    return () => clearInterval(interval);
  }, [search, loadItems]);

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault();
    loadItems(search);
  };

  const getAuctionStatusBadge = (item: Item) => {
    const auction = item.auction;
    if (!auction) return { label: item.status, className: 'bg-zinc-50 text-zinc-400' };
    
    if (auction.status === 'ACTIVE') {
      return { label: 'LIVE', className: 'bg-zinc-950 text-white' };
    }
    if (auction.status === 'ENDED' && auction.result === 'SOLD') {
      return { label: 'SOLD', className: 'bg-emerald-50 text-emerald-700' };
    }
    if (auction.status === 'ENDED' && auction.result === 'UNSOLD') {
      return { label: 'UNSOLD', className: 'bg-amber-50 text-amber-700' };
    }
    return { label: item.status, className: 'bg-zinc-50 text-zinc-400' };
  };

  const getDisplayPrice = (item: Item): { label: string; value: number } => {
    if (item.auction && item.auction.currentHighestBid > 0) {
      return { 
        label: item.auction.status === 'ACTIVE' ? 'Current Bid' : 'Final Price', 
        value: item.auction.currentHighestBid 
      };
    }
    return { label: 'Starting Price', value: item.auction?.startingPrice || item.startingPrice || 0 };
  };

  return (
    <div className="pb-24">
      {/* Header & Search */}
      <div className="mb-16 flex flex-col md:flex-row md:items-end justify-between gap-8">
        <motion.div
          initial={{ opacity: 0, y: 10 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.5 }}
        >
          <h1 className="text-4xl md:text-5xl font-medium tracking-tight text-zinc-950 mb-3">Catalogue</h1>
          <p className="text-zinc-500 max-w-sm">
            Browse the available deterministic assets currently circulating within the exchange.
          </p>
        </motion.div>

        <motion.form
          initial={{ opacity: 0, x: 20 }}
          animate={{ opacity: 1, x: 0 }}
          transition={{ duration: 0.5, delay: 0.1 }}
          onSubmit={handleSearch}
          className="flex items-center gap-2 w-full md:w-auto"
        >
          <div className="relative w-full md:w-[300px]">
            <MagnifyingGlass className="absolute left-4 top-1/2 -translate-y-1/2 w-5 h-5 text-zinc-400" />
            <input
              type="text"
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              placeholder="Search assets..."
              className="w-full bg-zinc-100 border-none rounded-full py-3.5 pl-12 pr-4 text-sm font-medium placeholder:text-zinc-400 focus:outline-none focus:ring-2 focus:ring-zinc-950/10 transition-shadow"
            />
          </div>
          <button
            type="button"
            className="p-3.5 bg-zinc-100 text-zinc-600 hover:text-zinc-950 rounded-full transition-colors active:scale-95"
            aria-label="Filter"
          >
            <Funnel className="w-5 h-5" />
          </button>
        </motion.form>
      </div>

      {/* Bento Grid */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        <AnimatePresence mode="popLayout">
          {initialLoading ? (
            Array.from({ length: 6 }).map((_, i) => (
              <motion.div
                key={`skeleton-${i}`}
                initial={{ opacity: 0, scale: 0.95 }}
                animate={{ opacity: 1, scale: 1 }}
                exit={{ opacity: 0, scale: 0.95 }}
                transition={{ duration: 0.4, delay: i * 0.05 }}
                className={`bg-zinc-50 border border-zinc-200 rounded-[2rem] p-8 aspect-square flex flex-col justify-between ${
                  i === 0 ? 'md:col-span-2 md:aspect-auto' : ''
                }`}
              >
                <div className="flex justify-between items-start">
                  <div className="w-12 h-12 bg-zinc-200 rounded-full animate-pulse" />
                  <div className="w-20 h-6 bg-zinc-200 rounded-full animate-pulse" />
                </div>
                <div className="space-y-4">
                  <div className="h-4 w-1/4 bg-zinc-200 rounded-full animate-pulse" />
                  <div className="h-8 w-3/4 bg-zinc-200 rounded-full animate-pulse" />
                </div>
              </motion.div>
            ))
          ) : items.length > 0 ? (
            items.map((item, i) => {
              const badge = getAuctionStatusBadge(item);
              const price = getDisplayPrice(item);
              const isActive = item.auction?.status === 'ACTIVE';
              const remaining = item.auction?.remainingSeconds ?? 0;
              // Compute client-side elapsed seconds since last data fetch
              const elapsed = lastFetchTime > 0 ? Math.floor((Date.now() - lastFetchTime) / 1000) : 0;
              const liveRemaining = Math.max(0, remaining - elapsed);

              return (
                <motion.div
                  key={item.id}
                  layoutId={`item-${item.id}`}
                  initial={{ opacity: 0, y: 20 }}
                  animate={{ opacity: 1, y: 0 }}
                  transition={{ type: 'spring', stiffness: 100, damping: 20, delay: i * 0.05 }}
                  className={`group relative bg-white border border-zinc-200 rounded-[2rem] p-8 flex flex-col justify-between transition-shadow hover:shadow-[0_20px_40px_-15px_rgba(0,0,0,0.05)] ${
                    i % 5 === 0 ? 'md:col-span-2 min-h-[400px]' : 'aspect-square min-h-[300px]'
                  }`}
                >
                  <Link href={`/catalogue/${item.id}`} className="absolute inset-0 z-10" aria-label={`View ${item.title}`} />
                  
                  <div className="flex justify-between items-start">
                    <div className="w-12 h-12 bg-zinc-50 rounded-full flex items-center justify-center border border-zinc-100 text-zinc-500 group-hover:scale-110 group-hover:bg-zinc-950 group-hover:text-white transition-all duration-500 ease-[cubic-bezier(0.16,1,0.3,1)]">
                      <Package className="w-5 h-5" />
                    </div>
                    <div className={`px-3 py-1 rounded-full text-xs font-medium tracking-wide flex items-center gap-1.5 ${badge.className}`}>
                      {isActive && (
                        <motion.div
                          animate={{ scale: [1, 1.5, 1], opacity: [1, 0.5, 1] }}
                          transition={{ duration: 2, repeat: Infinity, ease: "easeInOut" }}
                          className="w-1.5 h-1.5 rounded-full bg-green-500"
                        />
                      )}
                      {badge.label}
                    </div>
                  </div>

                  <div className="mt-12 relative z-20">
                    <div className="text-sm text-zinc-500 uppercase tracking-widest font-semibold mb-2 flex items-center gap-2">
                      {isActive ? (
                        <>
                          <Timer weight="bold" className="w-4 h-4" />
                          {formatCountdown(liveRemaining)}
                        </>
                      ) : (
                        <>
                          <Clock className="w-4 h-4" />
                          {item.auction?.status === 'ENDED' 
                            ? (item.auction.result === 'SOLD' ? 'Auction Won' : 'No Bids')
                            : 'Pending'
                          }
                        </>
                      )}
                    </div>
                    <h3 className="text-2xl md:text-3xl font-medium tracking-tight text-zinc-950 mb-4 line-clamp-2">
                      {item.title}
                    </h3>
                    
                    <div className="flex items-end justify-between border-t border-zinc-100 pt-6">
                      <div>
                        <span className="text-xs text-zinc-400 block mb-1">{price.label}</span>
                        <span className="text-xl font-mono tracking-tighter text-zinc-950">${price.value.toFixed(2)}</span>
                      </div>
                      {isActive && item.auction?.highestBidderUsername && (
                        <div className="flex items-center gap-1.5 text-xs text-zinc-500">
                          <Gavel className="w-3.5 h-3.5" />
                          <span className="font-medium">{item.auction.highestBidderUsername}</span>
                        </div>
                      )}
                    </div>
                  </div>
                </motion.div>
              );
            })
          ) : (
            <motion.div
              initial={{ opacity: 0 }}
              animate={{ opacity: 1 }}
              className="col-span-full py-24 flex flex-col items-center justify-center text-center"
            >
              <div className="w-16 h-16 bg-zinc-100 rounded-full flex items-center justify-center mb-6">
                <MagnifyingGlass className="w-6 h-6 text-zinc-400" />
              </div>
              <h3 className="text-xl font-medium tracking-tight text-zinc-950 mb-2">No items found</h3>
              <p className="text-zinc-500 max-w-sm">We couldn't find any assets matching your search criteria.</p>
            </motion.div>
          )}
        </AnimatePresence>
      </div>
    </div>
  );
}
