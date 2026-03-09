"use client";
import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { api } from "@/lib/api";
import { Order, PageResponse } from "@/lib/types";
import { Package, ChevronLeft, ChevronRight } from "lucide-react";
import Link from "next/link";
import toast from "react-hot-toast";

const STATUS_STYLES: Record<string, string> = {
  PLACED: "bg-blue-100 text-blue-700",
  CONFIRMED: "bg-yellow-100 text-yellow-700",
  SHIPPED: "bg-orange-100 text-orange-700",
  DELIVERED: "bg-green-100 text-green-700",
  CANCELLED: "bg-red-100 text-red-700",
};

export default function OrdersPage() {
  const [orders, setOrders] = useState<Order[]>([]);
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(1);
  const router = useRouter();

  useEffect(() => {
    api.get("/api/orders/my", { params: { page, size: 10 } })
      .then((res) => {
        const data: PageResponse<Order> = res.data.data;
        setOrders(data.content);
        setTotalPages(data.totalPages);
      })
      .catch(() => router.push("/login"))
      .finally(() => setLoading(false));
  }, [page, router]);

  if (loading) return (
    <div className="max-w-4xl mx-auto px-4 py-10 animate-pulse space-y-4">
      {[1, 2, 3].map(i => <div key={i} className="h-32 bg-gray-200 rounded-2xl" />)}
    </div>
  );

  if (orders.length === 0) return (
    <div className="max-w-4xl mx-auto px-4 py-20 text-center">
      <Package className="w-20 h-20 text-gray-200 mx-auto mb-4" />
      <h2 className="text-xl font-semibold text-gray-600 mb-2">No orders yet</h2>
      <p className="text-gray-400 mb-6 text-sm">Your order history will appear here</p>
      <Link href="/" className="bg-indigo-600 text-white px-6 py-3 rounded-xl font-medium hover:bg-indigo-700 transition inline-block">
        Start Shopping
      </Link>
    </div>
  );

  return (
    <div className="max-w-4xl mx-auto px-4 py-10">
      <h1 className="text-2xl font-bold text-gray-900 mb-8">My Orders</h1>
      <div className="space-y-4">
        {orders.map((order) => (
          <div key={order.id} className="bg-white rounded-2xl shadow-sm border border-gray-100 p-6">
            <div className="flex items-center justify-between mb-4">
              <div>
                <span className="text-xs text-gray-400 font-medium">ORDER #{order.id}</span>
                <p className="text-xs text-gray-400 mt-0.5">
                  {new Date(order.createdAt).toLocaleDateString("en-IN", { day: "numeric", month: "long", year: "numeric" })}
                </p>
              </div>
              <div className="text-right">
                <span className={`text-xs font-semibold px-3 py-1 rounded-full ${STATUS_STYLES[order.status] || "bg-gray-100 text-gray-600"}`}>
                  {order.status}
                </span>
                <p className="text-lg font-bold text-indigo-600 mt-1">₹{order.totalAmount.toLocaleString("en-IN")}</p>
              </div>
            </div>

            <div className="border-t border-gray-50 pt-4 space-y-2">
              {order.items.map((item) => (
                <div key={item.id} className="flex justify-between text-sm text-gray-600">
                  <span>{item.product.name} × {item.quantity}</span>
                  <span className="font-medium">₹{(item.priceAtPurchase * item.quantity).toLocaleString("en-IN")}</span>
                </div>
              ))}
            </div>
          </div>
        ))}
      </div>

      {/* Pagination */}
      {totalPages > 1 && (
        <div className="flex items-center justify-center gap-4 mt-8">
          <button onClick={() => setPage(p => Math.max(0, p - 1))} disabled={page === 0}
            className="p-2 rounded-xl border border-gray-200 hover:bg-gray-50 disabled:opacity-40 transition">
            <ChevronLeft className="w-5 h-5" />
          </button>
          <span className="text-sm text-gray-600 font-medium">Page {page + 1} of {totalPages}</span>
          <button onClick={() => setPage(p => Math.min(totalPages - 1, p + 1))} disabled={page >= totalPages - 1}
            className="p-2 rounded-xl border border-gray-200 hover:bg-gray-50 disabled:opacity-40 transition">
            <ChevronRight className="w-5 h-5" />
          </button>
        </div>
      )}
    </div>
  );
}
