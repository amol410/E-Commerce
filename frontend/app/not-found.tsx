import Link from "next/link";
import { SearchX } from "lucide-react";

export default function NotFound() {
  return (
    <div className="min-h-[calc(100vh-64px)] flex items-center justify-center">
      <div className="text-center">
        <SearchX className="w-20 h-20 text-gray-200 mx-auto mb-4" />
        <h1 className="text-5xl font-bold text-gray-900 mb-2">404</h1>
        <p className="text-xl text-gray-500 mb-2">Page Not Found</p>
        <p className="text-gray-400 text-sm mb-8">The page you're looking for doesn't exist.</p>
        <Link href="/" className="bg-indigo-600 text-white px-6 py-3 rounded-xl font-medium hover:bg-indigo-700 transition">
          Go Home
        </Link>
      </div>
    </div>
  );
}
