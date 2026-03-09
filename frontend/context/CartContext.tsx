"use client";
import { createContext, useContext, useState, ReactNode } from "react";

interface CartContextType {
  cartCount: number;
  setCartCount: (n: number) => void;
  incrementCart: () => void;
  decrementCart: () => void;
}

const CartContext = createContext<CartContextType | null>(null);

export function CartProvider({ children }: { children: ReactNode }) {
  const [cartCount, setCartCount] = useState(0);

  const incrementCart = () => setCartCount((c) => c + 1);
  const decrementCart = () => setCartCount((c) => Math.max(0, c - 1));

  return (
    <CartContext.Provider value={{ cartCount, setCartCount, incrementCart, decrementCart }}>
      {children}
    </CartContext.Provider>
  );
}

export function useCart() {
  const ctx = useContext(CartContext);
  if (!ctx) throw new Error("useCart must be used within CartProvider");
  return ctx;
}
