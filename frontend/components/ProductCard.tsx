"use client";
import { Product } from "@/lib/types";
import { api } from "@/lib/api";
import { useCart } from "@/context/CartContext";
import { ShoppingCart, Star } from "lucide-react";
import Link from "next/link";
import Image from "next/image";
import toast from "react-hot-toast";

interface Props {
  product: Product;
}

export default function ProductCard({ product }: Props) {
  const { incrementCart } = useCart();

  const addToCart = async (e: React.MouseEvent) => {
    e.preventDefault();
    try {
      await api.post("/api/cart/add", { productId: product.id, quantity: 1 });
      incrementCart();
      toast.success(`${product.name} added to cart!`);
    } catch (err: any) {
      const msg = err.response?.data?.message || "Please login to add items";
      toast.error(msg);
    }
  };

  return (
    <Link href={`/products/${product.id}`}>
      <div className="bg-white rounded-2xl shadow-sm border border-gray-100 overflow-hidden hover:shadow-md hover:-translate-y-1 transition-all duration-200 group">
        {/* Image */}
        <div className="relative h-52 bg-gray-100 overflow-hidden">
          {product.imageUrl ? (
            <Image
              src={product.imageUrl}
              alt={product.name}
              fill
              className="object-cover group-hover:scale-105 transition-transform duration-300"
            />
          ) : (
            <div className="w-full h-full flex items-center justify-center text-gray-300">
              <ShoppingCart className="w-16 h-16" />
            </div>
          )}
          {product.stock === 0 && (
            <div className="absolute inset-0 bg-black/40 flex items-center justify-center">
              <span className="bg-red-500 text-white text-xs font-bold px-3 py-1 rounded-full">Out of Stock</span>
            </div>
          )}
        </div>

        {/* Content */}
        <div className="p-4">
          {product.category && (
            <span className="text-xs text-indigo-500 font-medium uppercase tracking-wide">
              {product.category.name}
            </span>
          )}
          <h3 className="text-gray-900 font-semibold mt-1 text-sm line-clamp-2 leading-snug">
            {product.name}
          </h3>

          <div className="flex items-center justify-between mt-3">
            <span className="text-lg font-bold text-indigo-600">
              ₹{product.price.toLocaleString("en-IN")}
            </span>
            <button
              onClick={addToCart}
              disabled={product.stock === 0}
              className="bg-indigo-600 hover:bg-indigo-700 text-white text-xs font-semibold px-3 py-2 rounded-lg transition disabled:opacity-40 disabled:cursor-not-allowed flex items-center gap-1"
            >
              <ShoppingCart className="w-3 h-3" />
              Add
            </button>
          </div>

          <p className="text-xs text-gray-400 mt-2">
            {product.stock > 0 ? `${product.stock} in stock` : "Unavailable"}
          </p>
        </div>
      </div>
    </Link>
  );
}
