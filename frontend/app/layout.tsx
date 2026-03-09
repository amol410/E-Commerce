import type { Metadata } from "next";
import { Inter } from "next/font/google";
import "./globals.css";
import { AuthProvider } from "@/context/AuthContext";
import { CartProvider } from "@/context/CartContext";
import Navbar from "@/components/Navbar";
import { Toaster } from "react-hot-toast";

const inter = Inter({ subsets: ["latin"] });

export const metadata: Metadata = {
  title: "ShopEase — E-Commerce",
  description: "Your one-stop online shopping destination",
  openGraph: {
    title: "ShopEase",
    description: "Your one-stop online shopping destination",
    type: "website",
  },
};

export default function RootLayout({ children }: { children: React.ReactNode }) {
  return (
    <html lang="en">
      <body className={`${inter.className} bg-gray-50 min-h-screen`}>
        <AuthProvider>
          <CartProvider>
            <Navbar />
            <main className="min-h-screen">{children}</main>
            <Toaster position="top-right" />
            <footer className="bg-gray-900 text-gray-400 text-center py-6 mt-16 text-sm">
              © 2025 ShopEase. Built with Spring Boot + Next.js
            </footer>
          </CartProvider>
        </AuthProvider>
      </body>
    </html>
  );
}
