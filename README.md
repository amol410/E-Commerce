# E-Commerce Backend + Frontend

A full-stack e-commerce application built with **Spring Boot 3.x**, **Spring Data JPA**, **MySQL**, **Spring Security + JWT**, and a **Next.js + Tailwind CSS** frontend.

> Target: 5–10 LPA roles at Accenture, Capgemini, Persistent Systems, and D2C startups.

---

## Tech Stack

| Layer       | Technology                                      |
|-------------|--------------------------------------------------|
| Backend     | Spring Boot 3.2.4, Java 21 (runs on Java 24)    |
| Database    | MySQL 8, Hibernate, H2 (tests)                  |
| Security    | Spring Security 6, JWT (jjwt 0.12.5)            |
| Mapping     | MapStruct 1.6.3                                 |
| Docs        | Swagger / OpenAPI 3.0 (springdoc)               |
| Testing     | JUnit 5, Mockito 5.14.2, MockMvc (46 tests)     |
| Frontend    | Next.js 14 (App Router), Tailwind CSS            |
| Deployment  | Render.com (backend), Vercel (frontend)          |

---

## Project Structure

```
Ecom/
├── backend/               # Spring Boot project (single evolving project)
│   └── src/
├── frontend/              # Next.js project (single evolving project)
│   └── app/
├── README.md
├── PROCESS.md
└── ISSUES.md
```

---

## Versions

### V1 — Foundation: Entity Design & Product CRUD
**Tag:** `v1.0`

**Backend:**
- 5 JPA entities: `Category` (self-referential), `Product`, `User`, `Cart`, `CartItem`
- MySQL schema with proper relationships and constraints
- `ProductController` — full CRUD REST API (`/api/products`)
- `CategoryController` — basic CRUD (`/api/categories`)
- Global exception handler (`@RestControllerAdvice`)
- Standard API response wrapper

**Frontend:**
- Next.js 14 App Router setup with Tailwind CSS
- Product listing page with grid layout
- Product detail page
- Basic responsive navbar

---

### V2 — Authentication: Spring Security + JWT
**Tag:** `v2.0`

**Backend (additions over V1):**
- `UserService` with BCrypt password encoding
- JWT token generation, validation, refresh
- `AuthController` — `/api/auth/register`, `/api/auth/login`
- Role-based access: `ROLE_USER`, `ROLE_ADMIN`
- Security filter chain — public vs protected routes
- `SecurityConfig`, `JwtFilter`, `UserDetailsServiceImpl`

**Frontend (additions over V1):**
- Login & Register pages with form validation
- JWT stored in `httpOnly` cookie / localStorage
- Protected routes using Next.js middleware
- Auth context with React hooks

---

### V3 — Shopping Cart & Orders
**Tag:** `v3.0`

**Backend (additions over V2):**
- `CartService` — add/remove/update items, view cart
- `CartController` — `/api/cart/**` (auth-protected)
- `Order`, `OrderItem` entities
- `OrderService` — place order from cart, order history
- `OrderController` — `/api/orders/**`
- Stock validation on order placement

**Frontend (additions over V2):**
- Cart page — item list, quantity controls, total
- Checkout page — place order flow
- Order history page
- Toast notifications for cart actions

---

### V4 — Catalog Features: Filters, Pagination, Category Tree & Swagger
**Tag:** `v4.0`

**Backend (additions over V3):**
- Recursive category tree (self-referential JPA, `@OneToMany(mappedBy="parent")`)
- Product count per category
- Product search by name/category/price range
- Pagination & sorting via Spring Data `Pageable`
- Swagger UI (`springdoc-openapi`) at `/swagger-ui.html`
- MapStruct DTOs for all entities

**Frontend (additions over V3):**
- Sidebar category tree with expand/collapse
- Filter panel — price range, category, sort order
- Paginated product grid
- Search bar with debounce

---

### V5 — Production Polish: Validation, Tests & Deployment
**Tag:** `v5.0`

**Backend (additions over V4):**
- Bean Validation (`@Valid`, `@NotBlank`, etc.) on all DTOs
- Custom validation annotations
- JUnit 5 unit tests for services (`AuthServiceTest`, `CartServiceTest`, `ProductServiceTest`)
- MockMvc integration tests for all 5 controllers (46 tests total, 100% passing)
- H2 in-memory database for test isolation (`src/test/resources/application.properties`)
- `application-prod.properties` for deployment
- CORS configuration for frontend domain

**Java 24 compatibility fixes (post-v5):**
- Upgraded Lombok to `1.18.42` (Java 24 support)
- Upgraded MapStruct to `1.6.3`
- Upgraded Mockito to `5.14.2` + Byte Buddy to `1.15.10`
- Added `-XX:+EnableDynamicAgentLoading -Dnet.bytebuddy.experimental=true` to Surefire
- Changed Java target from 17 → 21

**Frontend (additions over V4):**
- Full Tailwind theming (dark mode toggle)
- Loading skeletons on all data-fetching pages
- SEO metadata with Next.js `metadata` API
- Error boundary and 404 page
- Deployment config for Vercel

---

## API Endpoints Summary

| Method | Endpoint                        | Auth       | Description              |
|--------|---------------------------------|------------|--------------------------|
| GET    | /api/products                   | Public     | List all products        |
| GET    | /api/products/{id}              | Public     | Get product by ID        |
| POST   | /api/products                   | ADMIN      | Create product           |
| PUT    | /api/products/{id}              | ADMIN      | Update product           |
| DELETE | /api/products/{id}              | ADMIN      | Delete product           |
| GET    | /api/categories                 | Public     | List all categories      |
| GET    | /api/categories/tree            | Public     | Recursive category tree  |
| GET    | /api/categories/{id}            | Public     | Get category by ID       |
| POST   | /api/categories                 | ADMIN      | Create category          |
| PUT    | /api/categories/{id}            | ADMIN      | Update category          |
| DELETE | /api/categories/{id}            | ADMIN      | Delete category          |
| POST   | /api/auth/register              | Public     | Register user            |
| POST   | /api/auth/login                 | Public     | Login, returns JWT       |
| GET    | /api/cart                       | USER       | View cart                |
| POST   | /api/cart/add                   | USER       | Add item to cart         |
| PUT    | /api/cart/update/{itemId}       | USER       | Update item quantity      |
| DELETE | /api/cart/remove/{itemId}       | USER       | Remove item              |
| DELETE | /api/cart/clear                 | USER       | Clear entire cart        |
| POST   | /api/orders/place               | USER       | Place order              |
| GET    | /api/orders/my                  | USER       | Order history            |
| GET    | /api/orders/{id}                | USER       | Get order by ID          |

---

## Running Locally

### Backend
```bash
cd backend
# Update src/main/resources/application.properties with your MySQL credentials
./mvnw spring-boot:run
```

### Frontend
```bash
cd frontend
npm install
npm run dev
```

Open [http://localhost:3000](http://localhost:3000)

---

## Database Setup

```sql
CREATE DATABASE ecom_db;
```

Update `backend/src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/ecom_db
spring.datasource.username=root
spring.datasource.password=yourpassword
```

---

## Deployment

- **Backend:** Deploy to [Render.com](https://render.com) — connect GitHub, set env vars
- **Frontend:** Deploy to [Vercel](https://vercel.com) — connect GitHub, set `NEXT_PUBLIC_API_URL`
