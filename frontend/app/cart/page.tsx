"use client";
import { useEffect, useState } from "react";
import { api } from "@/lib/api";
import { Cart } from "@/lib/types";
import { useCart } from "@/context/CartContext";
import { Trash2, ShoppingBag, Plus, Minus, ArrowRight } from "lucide-react";
import Link from "next/link";
import Image from "next/image";
import toast from "react-hot-toast";

export default function CartPage() {
  const [cart, setCart] = useState<Cart | null>(null);
  const [loading, setLoading] = useState(true);
  const { setCartCount } = useCart();

  const fetchCart = async () => {
    try {
      const res = await api.get("/api/cart");
      const cartData: Cart = res.data.data;
      setCart(cartData);
      setCartCount(cartData.items.reduce((sum, i) => sum + i.quantity, 0));
    } catch {
      setCart(null);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { fetchCart(); }, []);

  const updateQty = async (itemId: number, qty: number) => {
    try {
      await api.put(`/api/cart/update/${itemId}?quantity=${qty}`);
      fetchCart();
    } catch (err: any) {
      toast.error(err.response?.data?.message || "Failed to update");
    }
  };

  const removeItem = async (itemId: number) => {
    try {
      await api.delete(`/api/cart/remove/${itemId}`);
      toast.success("Item removed");
      fetchCart();
    } catch {
      toast.error("Failed to remove item");
    }
  };

  const total = cart?.items.reduce((sum, i) => sum + i.product.price * i.quantity, 0) ?? 0;

  if (loading) return (
    <div className="max-w-4xl mx-auto px-4 py-10 animate-pulse space-y-4">
      {[1, 2, 3].map(i => <div key={i} className="h-24 bg-gray-200 rounded-2xl" />)}
    </div>
  );

  if (!cart || cart.items.length === 0) return (
    <div className="max-w-4xl mx-auto px-4 py-20 text-center">
      <ShoppingBag className="w-20 h-20 text-gray-200 mx-auto mb-4" />
      <h2 className="text-xl font-semibold text-gray-600 mb-2">Your cart is empty</h2>
      <p className="text-gray-400 mb-6 text-sm">Add products to get started</p>
      <Link href="/" className="bg-indigo-600 text-white px-6 py-3 rounded-xl font-medium hover:bg-indigo-700 transition inline-block">
        Browse Products
      </Link>
    </div>
  );

  return (
    <div className="max-w-4xl mx-auto px-4 py-10">
      <h1 className="text-2xl font-bold text-gray-900 mb-8">Shopping Cart ({cart.items.length} items)</h1>
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
        {/* Items */}
        <div className="lg:col-span-2 space-y-4">
          {cart.items.map((item) => (
            <div key={item.id} className="bg-white rounded-2xl shadow-sm border border-gray-100 p-4 flex gap-4 items-center">
              <div className="relative w-20 h-20 bg-gray-50 rounded-xl overflow-hidden flex-shrink-0">
                {item.product.imageUrl ? (
                  <Image src={item.product.imageUrl} alt={item.product.name} fill className="object-cover" />
                ) : (
                  <ShoppingBag className="w-8 h-8 text-gray-200 m-auto" />
                )}
              </div>
              <div className="flex-1 min-w-0">
                <h3 className="font-semibold text-gray-900 text-sm line-clamp-2">{item.product.name}</h3>
                <p className="text-indigo-600 font-bold mt-1">₹{item.product.price.toLocaleString("en-IN")}</p>
              </div>
              <div className="flex items-center gap-2">
                <button onClick={() => item.quantity > 1 ? updateQty(item.id, item.quantity - 1) : removeItem(item.id)}
                  className="p-1.5 rounded-lg border border-gray-200 hover:bg-gray-50 transition">
                  <Minus className="w-3 h-3" />
                </button>
                <span className="w-6 text-center text-sm font-semibold">{item.quantity}</span>
                <button onClick={() => updateQty(item.id, item.quantity + 1)}
                  disabled={item.quantity >= item.product.stock}
                  className="p-1.5 rounded-lg border border-gray-200 hover:bg-gray-50 transition disabled:opacity-40">
                  <Plus className="w-3 h-3" />
                </button>
              </div>
              <p className="font-bold text-gray-900 text-sm w-20 text-right">
                ₹{(item.product.price * item.quantity).toLocaleString("en-IN")}
              </p>
              <button onClick={() => removeItem(item.id)}
                className="p-2 text-red-400 hover:text-red-600 hover:bg-red-50 rounded-lg transition">
                <Trash2 className="w-4 h-4" />
              </button>
            </div>
          ))}
        </div>

        {/* Summary */}
        <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-6 h-fit">
          <h2 className="font-bold text-gray-900 text-lg mb-4">Order Summary</h2>
          <div className="space-y-2 text-sm text-gray-600 mb-4">
            {cart.items.map((i) => (
              <div key={i.id} className="flex justify-between">
                <span className="truncate max-w-[150px]">{i.product.name} × {i.quantity}</span>
                <span className="font-medium">₹{(i.product.price * i.quantity).toLocaleString("en-IN")}</span>
              </div>
            ))}
          </div>
          <div className="border-t border-gray-100 pt-4 mb-6">
            <div className="flex justify-between font-bold text-gray-900">
              <span>Total</span>
              <span className="text-indigo-600">₹{total.toLocaleString("en-IN")}</span>
            </div>
          </div>
          <Link href="/checkout"
            className="w-full bg-indigo-600 hover:bg-indigo-700 text-white font-semibold py-3 rounded-xl transition flex items-center justify-center gap-2">
            Checkout <ArrowRight className="w-4 h-4" />
          </Link>
        </div>
      </div>
    </div>
  );
}
