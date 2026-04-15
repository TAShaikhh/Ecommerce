'use client';

import React, { useState, useEffect } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import Link from 'next/link';
import { usePathname } from 'next/navigation';
import { useAuthStore } from '@/store/authStore';
import { Storefront, Plus, User, SignOut, Gear, List, X } from '@phosphor-icons/react';

export default function NavBar() {
  const pathname = usePathname();
  const { user, token, logout, hydrate } = useAuthStore();
  const [scrolled, setScrolled] = useState(false);
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);
  const isAuthPage = pathname === '/login' || pathname === '/signup';

  useEffect(() => {
    if (!isAuthPage) {
      hydrate();
    }
  }, [hydrate, isAuthPage]);

  // Close mobile menu on route change
  useEffect(() => {
    setMobileMenuOpen(false);
  }, [pathname]);

  useEffect(() => {
    const handleScroll = () => {
      setScrolled(window.scrollY > 20);
    };
    if (isAuthPage) {
      return;
    }
    window.addEventListener('scroll', handleScroll);
    return () => window.removeEventListener('scroll', handleScroll);
  }, [isAuthPage]);

  const navLinks = [
    { label: 'Catalogue', path: '/catalogue', icon: Storefront },
    ...(token
      ? [
          { label: 'Sell Item', path: '/dashboard/sell', icon: Plus },
          { label: 'Settings', path: '/dashboard/settings', icon: Gear },
        ]
      : []),
  ];

  if (isAuthPage) {
    return null;
  }

  return (
    <motion.header
      initial={{ y: -100 }}
      animate={{ y: 0 }}
      transition={{ type: 'spring', stiffness: 100, damping: 20 }}
      className={`fixed top-4 left-1/2 -translate-x-1/2 z-50 w-[95%] max-w-5xl rounded-3xl md:rounded-full transition-all duration-500 ease-[cubic-bezier(0.16,1,0.3,1)] overflow-hidden ${
        scrolled || mobileMenuOpen ? 'liquid-glass' : 'bg-transparent'
      }`}
    >
      <div className="flex items-center justify-between px-4 md:px-6 py-3">
        {/* LOGO */}
        <Link href="/" className="flex items-center h-full">
          <img src="/PrimeBidLogo.webp" alt="PrimeBid Logo" className="h-8 md:h-11 w-auto object-contain hover:scale-105 transition-transform" />
        </Link>

        {/* DESKTOP NAV */}
        <nav className="hidden md:flex items-center gap-8">
          {navLinks.map((link) => {
            const isActive = pathname === link.path || pathname.startsWith(link.path + '/');
            const Icon = link.icon;
            return (
              <Link
                key={link.path}
                href={link.path}
                className="relative group flex items-center gap-2 text-sm font-medium text-zinc-600 hover:text-zinc-950 transition-colors"
              >
                <Icon weight={isActive ? "fill" : "regular"} className="w-4 h-4" />
                {link.label}
                {isActive && (
                  <motion.div
                    layoutId="navbar-indicator"
                    className="absolute -bottom-1.5 left-0 right-0 h-px bg-zinc-950"
                    transition={{ type: 'spring', stiffness: 300, damping: 30 }}
                  />
                )}
              </Link>
            );
          })}
        </nav>

        {/* AUTH BLOCK */}
        <div className="flex items-center gap-2 md:gap-4">
          <AnimatePresence mode="wait">
            {token ? (
              <motion.div
                key="user-menu"
                initial={{ opacity: 0, x: 10 }}
                animate={{ opacity: 1, x: 0 }}
                exit={{ opacity: 0, x: -10 }}
                className="hidden sm:flex items-center gap-2 md:gap-4"
              >
                <div className="flex items-center gap-2 px-3 py-1.5 rounded-full bg-zinc-100 text-xs md:text-sm font-medium">
                  <User className="w-4 h-4" />
                  <span className="truncate max-w-[80px] md:max-w-none">{user?.firstName || user?.username}</span>
                </div>
                <button
                  onClick={logout}
                  className="p-1.5 text-zinc-500 hover:text-red-500 hover:bg-red-50 rounded-full transition-colors"
                  aria-label="Log out"
                >
                  <SignOut className="w-4 h-4 md:w-5 md:h-5" />
                </button>
              </motion.div>
            ) : (
              <motion.div
                key="login-btn"
                initial={{ opacity: 0, x: 10 }}
                animate={{ opacity: 1, x: 0 }}
                exit={{ opacity: 0, x: -10 }}
                className="hidden sm:flex items-center gap-2 md:gap-3"
              >
                <Link
                  href="/login"
                  className="text-xs md:text-sm font-medium text-zinc-600 hover:text-zinc-950 transition-colors"
                >
                  Sign in
                </Link>
                <Link
                  href="/signup"
                  className="px-3 md:px-4 py-1.5 md:py-2 text-xs md:text-sm font-medium text-white bg-zinc-950 rounded-full hover:bg-zinc-800 transition-colors active:scale-95 whitespace-nowrap"
                >
                  Create account
                </Link>
              </motion.div>
            )}
          </AnimatePresence>

          {/* MOBILE MENU TOGGLE */}
          <button
            onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
            className="md:hidden p-2 text-zinc-900 bg-zinc-100/50 hover:bg-zinc-200 rounded-full transition-colors"
            aria-label="Toggle Menu"
          >
            {mobileMenuOpen ? <X className="w-5 h-5" /> : <List className="w-5 h-5" />}
          </button>
        </div>
      </div>

      {/* MOBILE DROPDOWN TRAY */}
      <AnimatePresence>
        {mobileMenuOpen && (
          <motion.div
            initial={{ height: 0, opacity: 0 }}
            animate={{ height: 'auto', opacity: 1 }}
            exit={{ height: 0, opacity: 0 }}
            transition={{ duration: 0.3, ease: 'easeInOut' }}
            className="md:hidden border-t border-zinc-200/50 bg-white/80 backdrop-blur-md"
          >
            <nav className="flex flex-col gap-2 p-4">
              {navLinks.map((link) => {
                const isActive = pathname === link.path || pathname.startsWith(link.path + '/');
                const Icon = link.icon;
                return (
                  <Link
                    key={link.path}
                    href={link.path}
                    className={`flex items-center gap-3 px-4 py-3 rounded-xl text-sm font-semibold transition-colors ${
                      isActive ? 'bg-zinc-100 text-zinc-950' : 'text-zinc-600 hover:bg-zinc-50 hover:text-zinc-950'
                    }`}
                  >
                    <Icon weight={isActive ? "fill" : "regular"} className="w-5 h-5" />
                    {link.label}
                  </Link>
                );
              })}

              <hr className="my-2 border-zinc-200" />

              {/* Mobile Auth Actions */}
              {token ? (
                <div className="flex items-center justify-between px-4 py-2">
                  <div className="flex items-center gap-3 text-sm font-medium text-zinc-950">
                    <div className="w-8 h-8 rounded-full bg-zinc-100 flex items-center justify-center">
                      <User className="w-4 h-4 text-zinc-500" />
                    </div>
                    {user?.firstName || user?.username}
                  </div>
                  <button
                    onClick={() => {
                      logout();
                      setMobileMenuOpen(false);
                    }}
                    className="flex items-center gap-2 text-sm font-semibold text-red-600 bg-red-50 hover:bg-red-100 px-4 py-2 rounded-xl transition-colors"
                  >
                    <SignOut className="w-4 h-4" />
                    Sign out
                  </button>
                </div>
              ) : (
                <div className="flex flex-col gap-2">
                  <Link
                    href="/login"
                    className="flex items-center justify-center w-full py-3 text-sm font-semibold text-zinc-700 bg-zinc-100 hover:bg-zinc-200 rounded-xl transition-colors"
                  >
                    Sign in
                  </Link>
                  <Link
                    href="/signup"
                    className="flex items-center justify-center w-full py-3 text-sm font-semibold text-white bg-zinc-950 hover:bg-zinc-800 rounded-xl transition-colors"
                  >
                    Create account
                  </Link>
                </div>
              )}
            </nav>
          </motion.div>
        )}
      </AnimatePresence>
    </motion.header>
  );
}
