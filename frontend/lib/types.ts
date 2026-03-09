export interface Category {
  id: number;
  name: string;
  description?: string;
  parent?: Category;
  children?: Category[];
}

export interface Product {
  id: number;
  name: string;
  description?: string;
  price: number;
  stock: number;
  imageUrl?: string;
  active: boolean;
  category?: Category;
  createdAt: string;
}

export interface User {
  userId: number;
  name: string;
  email: string;
  role: "ROLE_USER" | "ROLE_ADMIN";
  token: string;
}

export interface CartItem {
  id: number;
  product: Product;
  quantity: number;
}

export interface Cart {
  id: number;
  items: CartItem[];
}

export interface OrderItem {
  id: number;
  product: Product;
  quantity: number;
  priceAtPurchase: number;
}

export interface Order {
  id: number;
  status: "PLACED" | "CONFIRMED" | "SHIPPED" | "DELIVERED" | "CANCELLED";
  totalAmount: number;
  items: OrderItem[];
  createdAt: string;
}

export interface PageResponse<T> {
  content: T[];
  totalPages: number;
  totalElements: number;
  number: number;
  size: number;
}

export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
}
