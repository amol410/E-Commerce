"use client";
import { useState } from "react";
import { api } from "@/lib/api";
import { useAuth } from "@/context/AuthContext";
import { useRouter } from "next/navigation";
import { useEffect } from "react";
import toast from "react-hot-toast";
import { Plus, Package } from "lucide-react";

export default function AdminPage() {
  const { user, isAdmin } = useAuth();
  const router = useRouter();
  const [form, setForm] = useState({
    name: "", description: "", price: "", stock: "", imageUrl: "", categoryId: ""
  });
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (!user || !isAdmin()) {
      router.push("/");
      toast.error("Admin access required");
    }
  }, [user, isAdmin, router]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    try {
      await api.post("/api/products", {
        name: form.name,
        description: form.description,
        price: parseFloat(form.price),
        stock: parseInt(form.stock),
        imageUrl: form.imageUrl || null,
        categoryId: form.categoryId ? parseInt(form.categoryId) : null,
      });
      toast.success("Product created!");
      setForm({ name: "", description: "", price: "", stock: "", imageUrl: "", categoryId: "" });
    } catch (err: any) {
      toast.error(err.response?.data?.message || "Failed to create product");
    } finally {
      setLoading(false);
    }
  };

  if (!user || !isAdmin()) return null;

  return (
    <div className="max-w-2xl mx-auto px-4 py-10">
      <div className="flex items-center gap-3 mb-8">
        <div className="w-10 h-10 bg-indigo-600 rounded-xl flex items-center justify-center">
          <Package className="w-5 h-5 text-white" />
        </div>
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Admin Panel</h1>
          <p className="text-sm text-gray-500">Add new products to the catalog</p>
        </div>
      </div>

      <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-8">
        <h2 className="font-semibold text-gray-900 mb-6">Create Product</h2>
        <form onSubmit={handleSubmit} className="space-y-4">
          {[
            { label: "Product Name *", key: "name", type: "text", placeholder: "e.g. Cotton T-Shirt" },
            { label: "Price (₹) *", key: "price", type: "number", placeholder: "e.g. 499" },
            { label: "Stock *", key: "stock", type: "number", placeholder: "e.g. 100" },
            { label: "Image URL", key: "imageUrl", type: "url", placeholder: "https://..." },
            { label: "Category ID", key: "categoryId", type: "number", placeholder: "e.g. 1" },
          ].map(({ label, key, type, placeholder }) => (
            <div key={key}>
              <label className="text-sm font-medium text-gray-700 block mb-1.5">{label}</label>
              <input
                type={type}
                value={(form as any)[key]}
                onChange={(e) => setForm({ ...form, [key]: e.target.value })}
                placeholder={placeholder}
                required={label.includes("*")}
                step={type === "number" && key === "price" ? "0.01" : undefined}
                className="w-full border border-gray-200 rounded-xl px-4 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-300 transition"
              />
            </div>
          ))}
          <div>
            <label className="text-sm font-medium text-gray-700 block mb-1.5">Description</label>
            <textarea
              value={form.description}
              onChange={(e) => setForm({ ...form, description: e.target.value })}
              rows={3}
              placeholder="Product description..."
              className="w-full border border-gray-200 rounded-xl px-4 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-300 transition resize-none"
            />
          </div>
          <button
            type="submit"
            disabled={loading}
            className="w-full bg-indigo-600 hover:bg-indigo-700 text-white font-semibold py-3 rounded-xl transition flex items-center justify-center gap-2 disabled:opacity-60"
          >
            <Plus className="w-4 h-4" />
            {loading ? "Creating..." : "Create Product"}
          </button>
        </form>
      </div>
    </div>
  );
}
