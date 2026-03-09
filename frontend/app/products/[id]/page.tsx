"use client";
import { useEffect, useState } from "react";
import { useParams, useRouter } from "next/navigation";
import { api } from "@/lib/api";
import { Product } from "@/lib/types";
import { useCart } from "@/context/CartContext";
import { ShoppingCart, ArrowLeft, Tag, Package } from "lucide-react";
import Image from "next/image";
import Link from "next/link";
import toast from "react-hot-toast";

export default function ProductDetailPage() {
  const { id } = useParams();
  const router = useRouter();
  const [product, setProduct] = useState<Product | null>(null);
  const [loading, setLoading] = useState(true);
  const [quantity, setQuantity] = useState(1);
  const { incrementCart } = useCart();

  useEffect(() => {
    api.get(`/api/products/${id}`)
      .then((res) => setProduct(res.data.data))
      .catch(() => toast.error("Product not found"))
      .finally(() => setLoading(false));
  }, [id]);

  const addToCart = async () => {
    try {
      await api.post("/api/cart/add", { productId: product?.id, quantity });
      for (let i = 0; i < quantity; i++) incrementCart();
      toast.success(`${quantity}x ${product?.name} added to cart!`);
    } catch (err: any) {
      toast.error(err.response?.data?.message || "Please login to continue");
    }
  };

  if (loading) {
    return (
      <div className="max-w-6xl mx-auto px-4 py-12 animate-pulse">
        <div className="grid grid-cols-1 md:grid-cols-2 gap-12">
          <div className="h-96 bg-gray-200 rounded-3xl" />
          <div className="space-y-4">
            <div className="h-8 bg-gray-200 rounded w-3/4" />
            <div className="h-6 bg-gray-200 rounded w-1/3" />
            <div className="h-4 bg-gray-200 rounded w-full" />
            <div className="h-4 bg-gray-200 rounded w-5/6" />
          </div>
        </div>
      </div>
    );
  }

  if (!product) return <div className="text-center py-20 text-gray-400">Product not found.</div>;

  return (
    <div className="max-w-6xl mx-auto px-4 py-10">
      <Link href="/" className="inline-flex items-center gap-2 text-sm text-gray-500 hover:text-indigo-600 mb-8 transition">
        <ArrowLeft className="w-4 h-4" /> Back to Products
      </Link>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-12 bg-white rounded-3xl shadow-sm border border-gray-100 p-8">
        {/* Image */}
        <div className="relative h-96 bg-gray-50 rounded-2xl overflow-hidden">
          {product.imageUrl ? (
            <Image src={product.imageUrl} alt={product.name} fill className="object-cover" />
          ) : (
            <div className="w-full h-full flex items-center justify-center text-gray-200">
              <ShoppingCart className="w-24 h-24" />
            </div>
          )}
        </div>

        {/* Info */}
        <div className="flex flex-col justify-between">
          <div>
            {product.category && (
              <div className="flex items-center gap-1 text-indigo-500 text-sm font-medium mb-2">
                <Tag className="w-4 h-4" />
                {product.category.name}
              </div>
            )}
            <h1 className="text-3xl font-bold text-gray-900 mb-4">{product.name}</h1>
            <p className="text-4xl font-bold text-indigo-600 mb-4">
              ₹{product.price.toLocaleString("en-IN")}
            </p>
            <p className="text-gray-600 leading-relaxed mb-6">
              {product.description || "No description available."}
            </p>
            <div className="flex items-center gap-2 text-sm text-gray-500 mb-6">
              <Package className="w-4 h-4" />
              {product.stock > 0 ? (
                <span className="text-green-600 font-medium">{product.stock} units in stock</span>
              ) : (
                <span className="text-red-500 font-medium">Out of stock</span>
              )}
            </div>
          </div>

          {/* Quantity + Add to Cart */}
          <div className="space-y-4">
            <div className="flex items-center gap-3">
              <label className="text-sm font-medium text-gray-700">Quantity:</label>
              <div className="flex items-center border border-gray-200 rounded-xl overflow-hidden">
                <button
                  onClick={() => setQuantity((q) => Math.max(1, q - 1))}
                  className="px-3 py-2 hover:bg-gray-100 text-gray-600 font-bold transition"
                >−</button>
                <span className="px-4 py-2 text-sm font-semibold border-x border-gray-200">{quantity}</span>
                <button
                  onClick={() => setQuantity((q) => Math.min(product.stock, q + 1))}
                  disabled={quantity >= product.stock}
                  className="px-3 py-2 hover:bg-gray-100 text-gray-600 font-bold transition disabled:opacity-40"
                >+</button>
              </div>
            </div>
            <button
              onClick={addToCart}
              disabled={product.stock === 0}
              className="w-full bg-indigo-600 hover:bg-indigo-700 text-white font-semibold py-3 rounded-xl transition flex items-center justify-center gap-2 disabled:opacity-40 disabled:cursor-not-allowed"
            >
              <ShoppingCart className="w-5 h-5" />
              Add to Cart
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}
