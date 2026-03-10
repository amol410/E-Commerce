# Manual Testing Guide

How to manually test every feature of ShopEase end-to-end.

**Prerequisites:** Backend running on `http://localhost:8080`, frontend on `http://localhost:3000`

---

## 1. Start the Project

```bash
# Terminal 1 — Backend
cd backend
mvn spring-boot:run

# Terminal 2 — Frontend
cd frontend
npm run dev
```

---

## 2. User Registration & Login

### Register a new user
1. Go to `http://localhost:3000/register`
2. Fill in: Name, Email, Password
3. Click **Register**
4. Expected: Redirected to home page, "Hi, [FirstName]" shown in navbar

### Login
1. Go to `http://localhost:3000/login`
2. Enter credentials from registration
3. Click **Login**
4. Expected: Redirected to home, JWT stored, navbar shows username

### Login with wrong password
1. Enter correct email, wrong password
2. Expected: Error toast — "Invalid credentials"

---

## 3. Product Browsing

### View all products
1. Go to `http://localhost:3000`
2. Expected: Product grid loads with cards showing name, price, image

### Search products
1. Type in search bar (e.g. "phone")
2. Expected: Grid filters after 300ms debounce

### Filter by price
1. Set min/max price range in filter panel
2. Expected: Only products within range shown

### Filter by category
1. Click a category in the sidebar
2. Expected: Products filtered by that category

### View product detail
1. Click any product card
2. Go to `/products/{id}`
3. Expected: Full product details, description, price, stock count

---

## 4. Cart Operations

### Add to cart (must be logged in)
1. Log in, go to any product page
2. Click **Add to Cart**
3. Expected: Cart count in navbar increments, success toast

### View cart
1. Click cart icon in navbar
2. Expected: `/cart` shows items, quantities, subtotals, total

### Update quantity
1. On `/cart`, click **+** or **-** next to an item
2. Expected: Quantity and total update immediately

### Remove item
1. Click **Remove** next to an item
2. Expected: Item removed, total recalculates

### Clear cart
1. Click **Clear Cart** button
2. Expected: All items removed, empty cart message shown

### Add to cart when not logged in
1. Log out, try to add a product
2. Expected: Redirected to `/login`

---

## 5. Order Placement

### Place an order
1. Add items to cart
2. Go to `/cart`, click **Proceed to Checkout**
3. On `/checkout`, review order summary
4. Click **Place Order**
5. Expected: Success toast, cart cleared, redirected to `/orders`

### View order history
1. Go to `http://localhost:3000/orders`
2. Expected: List of past orders with status badge (PLACED, CONFIRMED, etc.)

### View single order
1. Click on an order in `/orders`
2. Expected: Order details with items, quantities, price at purchase time

### Place order with empty cart
1. Clear cart, try to place order directly via Swagger:
   `POST /api/orders/place` with JWT
2. Expected: `400 Bad Request` — "Cart is empty"

---

## 6. API Testing with Swagger UI

Open `http://localhost:8080/swagger-ui.html`

### Authenticate in Swagger
1. Click **Authorize** (top right)
2. Enter: `Bearer <your-jwt-token>`
3. Click **Authorize**

### Test endpoints
- Try `GET /api/products` — no auth needed
- Try `POST /api/products` without auth → `403 Forbidden`
- Try `POST /api/products` with ADMIN JWT → `201 Created`
- Try `GET /api/cart` with USER JWT → your cart

---

## 7. Admin Operations

### Create a product (Admin JWT required)

Get an admin JWT first (register a user, then manually update their role in MySQL to `ROLE_ADMIN`):
```sql
UPDATE users SET role = 'ROLE_ADMIN' WHERE email = 'admin@example.com';
```

Then via Swagger or Postman:
```
POST /api/products
Authorization: Bearer <admin-jwt>
Content-Type: application/json

{
  "name": "Test Product",
  "description": "A test product",
  "price": 999.99,
  "stock": 50,
  "categoryId": 1
}
```
Expected: `201 Created` with new product data

### Update a product
```
PUT /api/products/{id}
Authorization: Bearer <admin-jwt>

{ "price": 799.99 }
```
Expected: `200 OK` with updated product

### Delete a product
```
DELETE /api/products/{id}
Authorization: Bearer <admin-jwt>
```
Expected: `200 OK`

---

## 8. Category API

### Get category tree
```
GET /api/categories/tree
```
Expected: Nested JSON tree with parent → children structure

### Create category
```
POST /api/categories
Authorization: Bearer <admin-jwt>

{ "name": "Electronics", "description": "Electronic items" }
```

### Create subcategory
```
POST /api/categories
Authorization: Bearer <admin-jwt>

{ "name": "Phones", "description": "Mobile phones", "parentId": 1 }
```
Expected: Category appears under parent in tree

---

## 9. Running Automated Tests

```bash
cd backend
mvn test
```

Expected output:
```
[INFO] Tests run: 46, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

---

## 10. Edge Cases to Test

| Scenario | Expected Result |
|----------|----------------|
| Register with existing email | `409 Conflict` or validation error |
| Add product with stock=0 to cart then place order | `400` — insufficient stock |
| Access `/cart` without login | Redirect to `/login` |
| Access `/orders` without login | Redirect to `/login` |
| GET `/api/cart` without JWT | `401 Unauthorized` |
| POST `/api/products` as USER (not ADMIN) | `403 Forbidden` |
| Invalid JWT token | `401 Unauthorized` |
| Product not found (wrong ID) | `404 Not Found` |
| Register with invalid email format | `400` validation error |
| Create product with negative price | `400` validation error |
