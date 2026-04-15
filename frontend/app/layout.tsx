import type { Metadata } from "next";
import { Geist, Geist_Mono } from "next/font/google";
import "./globals.css";
import NavBar from "./components/NavBar";

const geistSans = Geist({
  variable: "--font-geist-sans",
  subsets: ["latin"],
});

const geistMono = Geist_Mono({
  variable: "--font-geist-mono",
  subsets: ["latin"],
});

export const metadata: Metadata = {
  title: "PrimeBid | Forward Auction System",
  description: "Next-generation forward auctioning infrastructure.",
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html
      lang="en"
      className={`${geistSans.variable} ${geistMono.variable} h-full antialiased`}
    >
      <body className="min-h-[100dvh] flex flex-col bg-[#fdfdfc] text-zinc-950 selection:bg-blue-500/30">
        <NavBar />
        {/* No padding/max-width here — each page group handles its own container */}
        <main className="flex-1 w-full">
          {children}
        </main>
      </body>
    </html>
  );
}
