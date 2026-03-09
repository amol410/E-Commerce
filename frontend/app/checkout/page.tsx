"use client";
import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { api } from "@/lib/api";
import { Cart } from "@/lib/types";
import { useCart } from "@/context/CartContext";
import { CheckCircle, ShoppingBag } from "lucide-react";
import Link from "next/link";
import toast from "react-hot-toast";

export default function CheckoutPage() {
  const [cart, setCart] = useState<Cart | null>(null);
  const [loading, setLoading] = useState(true);
  const [placing, setPlacing] = useState(false);
  const [ordered, setOrdered] = useState(false);
  const router = useRouter();
  const { setCartCount } = useCart();

  useEffect(() => {
    api.get("/api/cart")
      .then((res) => setCart(res.data.data))
      .catch(() => router.push("/login"))
      .finally(() => setLoading(false));
  }, [router]);

  const placeOrder = async () => {
    setPlacing(true);
    try {
      await api.post("/api/orders/place");
      setCartCount(0);
      setOrdered(true);
      toast.success("Order placed successfully!");
    } catch (err: any) {
      toast.error(err.response?.data?.message || "Failed to place order");
    } finally {
      setPlacing(false);
    }
  };

  if (loading) return <div className="max-w-2xl mx-auto px-4 py-20 animate-pulse"><div className="h-64 bg-gray-200 rounded-2xl" /></div>;

  if (ordered) return (
    <div className="max-w-2xl mx-auto px-4 py-24 text-center">
      <div className="bg-white rounded-3xl shadow-sm border border-gray-100 p-12">
        <CheckCircle className="w-20 h-20 text-green-500 mx-auto mb-4" />
        <h2 className="text-2xl font-bold text-gray-900 mb-2">Order Placed!</h2>
        <p className="text-gray-500 mb-8">Your order has been placed successfully. We'll confirm it shortly.</p>
        <div className="flex gap-3 justify-center">
          <Link href="/orders" className="bg-indigo-600 text-white px-6 py-3 rounded-xl font-medium hover:bg-indigo-700 transition">
            View Orders
          </Link>
          <Link href="/" className="border border-gray-200 text-gray-700 px-6 py-3 rounded-xl font-medium hover:bg-gray-50 transition">
            Continue Shopping
          </Link>
        </div>
      </div>
    </div>
  );

  if (!cart || cart.items.length === 0) return (
    <div className="max-w-2xl mx-auto px-4 py-20 text-center">
      <ShoppingBag className="w-16 h-16 text-gray-200 mx-auto mb-4" />
      <p className="text-gray-500 mb-4">Your cart is empty</p>
      <Link href="/" className="text-indigo-600 hover:underline text-sm">Browse products</Link>
    </div>
  );

  const total = cart.items.reduce((sum, i) => sum + i.product.price * i.quantity, 0);

  return (
    <div className="max-w-2xl mx-auto px-4 py-10">
      <h1 className="text-2xl font-bold text-gray-900 mb-8">Checkout</h1>
      <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-6 mb-6">
        <h2 className="font-semibold text-gray-900 mb-4">Order Summary</h2>
        <div className="space-y-3">
          {cart.items.map((item) => (
            <div key={item.id} className="flex justify-between text-sm text-gray-600">
              <span>{item.product.name} × {item.quantity}</span>
              <span className="font-medium text-gray-900">₹{(item.product.price * item.quantity).toLocaleString("en-IN")}</span>
            </div>
          ))}
        </div>
        <div className="border-t border-gray-100 pt-4 mt-4 flex justify-between font-bold text-gray-900">
          <span>Total</span>
          <span className="text-indigo-600 text-xl">₹{total.toLocaleString("en-IN")}</span>
        </div>
      </div>

      <button
        onClick={placeOrder}
        disabled={placing}
        className="w-full bg-indigo-600 hover:bg-indigo-700 text-white font-semibold py-4 rounded-xl transition disabled:opacity-60 text-lg"
      >
        {placing ? "Placing Order..." : `Place Order — ₹${total.toLocaleString("en-IN")}`}
      </button>
      <p className="text-center text-xs text-gray-400 mt-3">By placing the order, you agree to our terms of service</p>
    </div>
  );
}
