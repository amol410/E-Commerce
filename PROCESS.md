# Development Process

This document describes the step-by-step process followed to build each version of the E-Commerce project.

---

## V1 — Foundation: Entity Design & Product CRUD

### 1. Project Initialization
- Generated Spring Boot project via Spring Initializr with dependencies:
  - Spring Web, Spring Data JPA, MySQL Driver, Lombok, Validation
- Created Next.js project with `create-next-app` using App Router + Tailwind CSS

### 2. Database Design
- Designed 5 entities:
  - `Category` — id, name, description, parent (self-referential `@ManyToOne`)
  - `Product` — id, name, description, price, stock, imageUrl, category (`@ManyToOne`)
  - `User` — id, name, email, password, role
  - `Cart` — id, user (`@OneToOne`), items (`@OneToMany`)
  - `CartItem` — id, cart, product, quantity
- Set `spring.jpa.hibernate.ddl-auto=update` for auto schema generation
- Added `spring.jpa.show-sql=true` for SQL visibility during development

### 3. Repository Layer
- Created `JpaRepository` interfaces for all 5 entities
- Added custom query methods: `findByCategoryId`, `findByNameContainingIgnoreCase`

### 4. Service Layer
- `ProductService` — CRUD operations with null checks
- `CategoryService` — CRUD operations
- Used constructor injection (no `@Autowired` on fields)

### 5. Controller Layer
- `ProductController` at `/api/products` — GET all, GET by id, POST, PUT, DELETE
- `CategoryController` at `/api/categories` — GET all, GET by id, POST, PUT, DELETE
- Wrapped all responses in a standard `ApiResponse<T>` wrapper class

### 6. Exception Handling
- Created `ResourceNotFoundException` (extends `RuntimeException`)
- Created `GlobalExceptionHandler` with `@RestControllerAdvice`
- Handled `MethodArgumentNotValidException` for validation errors

### 7. Frontend — Next.js Setup
- Configured Tailwind CSS with custom color palette
- Built reusable components: `Navbar`, `ProductCard`, `Footer`
- `/` — Product listing grid using `fetch` from backend API
- `/products/[id]` — Product detail page

### 8. Git & Push
- `git add .` → `git commit -m "v1: entity design, product CRUD, Next.js setup"`
- `git tag v1.0`
- `git push origin main --tags`

---

## V2 — Authentication: Spring Security + JWT

### 1. Dependencies Added
- `spring-boot-starter-security`
- `jjwt-api`, `jjwt-impl`, `jjwt-jackson` (io.jsonwebtoken, version 0.12.x)

### 2. JWT Utility
- `JwtUtil` class — `generateToken(username)`, `extractUsername(token)`, `isTokenValid(token, userDetails)`
- Secret key stored in `application.properties` as `app.jwt.secret`
- Expiry: 24 hours

### 3. UserDetailsService
- `UserDetailsServiceImpl` implements `UserDetailsService`
- Loads user from DB by email, maps roles to `GrantedAuthority`

### 4. JWT Filter
- `JwtAuthFilter` extends `OncePerRequestFilter`
- Extracts Bearer token from `Authorization` header
- Validates and sets `SecurityContextHolder` authentication

### 5. Security Configuration
- `SecurityConfig` with `@EnableWebSecurity`
- Permit: `/api/auth/**`, GET `/api/products/**`, GET `/api/categories/**`
- Require auth: POST/PUT/DELETE products, all `/api/cart/**`, `/api/orders/**`
- Stateless session management (`SessionCreationPolicy.STATELESS`)
- Registered `JwtAuthFilter` before `UsernamePasswordAuthenticationFilter`

### 6. Auth Controller
- `POST /api/auth/register` — validate, encode password, save user, return JWT
- `POST /api/auth/login` — authenticate via `AuthenticationManager`, return JWT

### 7. Frontend — Auth UI
- `AuthContext` with `useContext` for global user state
- `/login` and `/register` pages with Tailwind-styled forms
- JWT stored in `localStorage` (with note about `httpOnly` cookie alternative)
- Next.js `middleware.ts` to redirect unauthenticated users from protected pages
- `Authorization: Bearer <token>` header added to all authenticated API calls

### 8. Git & Push
- `git commit -m "v2: Spring Security + JWT auth, Next.js auth UI"`
- `git tag v2.0`
- `git push origin main --tags`

---

## V3 — Shopping Cart & Orders

### 1. Cart Design
- `Cart` is a 1-to-1 with `User`, auto-created on registration
- `CartItem` links `Cart` and `Product` with a quantity field
- Added `UNIQUE(cart_id, product_id)` constraint to prevent duplicates

### 2. Cart Service & Controller
- `CartService`:
  - `addItem(userId, productId, qty)` — creates or increments CartItem
  - `removeItem(userId, cartItemId)` — validates ownership before delete
  - `updateQuantity(userId, cartItemId, qty)` — updates qty
  - `getCart(userId)` — returns cart with all items and computed total
- `CartController` at `/api/cart` — all routes `@PreAuthorize("hasRole('USER')")`

### 3. Order Design
- `Order` entity — id, user, status (PLACED/CONFIRMED/SHIPPED/DELIVERED), totalAmount, createdAt
- `OrderItem` entity — id, order, product, quantity, priceAtPurchase
- Stored `priceAtPurchase` to preserve historical price

### 4. Order Service & Controller
- `OrderService.placeOrder(userId)`:
  1. Load cart, validate not empty
  2. Validate stock for each item
  3. Create `Order` + `OrderItem` records
  4. Decrement product stock
  5. Clear cart
- `OrderController`:
  - `POST /api/orders/place`
  - `GET /api/orders/my` — paginated order history

### 5. Frontend — Cart & Checkout
- `/cart` — cart items table with +/- quantity buttons, subtotal, remove button
- `/checkout` — order summary + "Place Order" button
- `/orders` — order history with status badges
- Global cart item count in Navbar using React state

### 6. Git & Push
- `git commit -m "v3: cart, cartitem, order entities + APIs, cart/checkout UI"`
- `git tag v3.0`
- `git push origin main --tags`

---

## V4 — Catalog Features: Filters, Pagination, Category Tree & Swagger

### 1. Recursive Category Tree
- Added `children` field to `Category`: `@OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)`
- `CategoryService.getTree()` — fetches only root categories (`parent IS NULL`), JPA loads children lazily
- Used `@JsonIgnoreProperties("parent")` on the `parent` field to avoid circular JSON

### 2. Product Count Per Category
- Added JPQL query: `SELECT c.id, COUNT(p) FROM Category c JOIN c.products p GROUP BY c.id`
- Returned as `CategoryTreeDto` with `productCount` field

### 3. Product Filters & Pagination
- Updated `ProductRepository` with `JpaSpecificationExecutor<Product>`
- Created `ProductSpecification` — builds `Specification<Product>` from filter params:
  - `name` (LIKE), `categoryId` (JOIN), `minPrice`, `maxPrice`
- `ProductController.getAll(...)` accepts `@RequestParam` for filters + `Pageable` (page, size, sort)

### 4. MapStruct DTOs
- Added MapStruct dependency
- Created DTOs: `ProductDto`, `CategoryDto`, `UserDto`, `CartItemDto`, `OrderDto`
- Created mappers: `ProductMapper`, `CategoryMapper` — interfaces with `@Mapper(componentModel = "spring")`
- All controllers now return DTOs, not entities

### 5. Swagger / OpenAPI
- Added `springdoc-openapi-starter-webmvc-ui` dependency
- Configured `OpenApiConfig` bean with API info, JWT `SecurityScheme`
- Annotated controllers with `@Tag`, endpoints with `@Operation`, `@ApiResponse`
- Accessible at `http://localhost:8080/swagger-ui.html`

### 6. Frontend — Filters & Category Tree
- Sidebar component rendering recursive category tree
- Filter panel — price range slider, sort dropdown
- Pagination component — prev/next, page number display
- Search input with 300ms debounce using `useCallback`

### 7. Git & Push
- `git commit -m "v4: category tree, filters, pagination, MapStruct DTOs, Swagger"`
- `git tag v4.0`
- `git push origin main --tags`

---

## V5 — Production Polish: Validation, Tests & Deployment

### 1. Bean Validation
- Added `@Valid` on all controller method parameters accepting request bodies
- DTOs annotated with `@NotBlank`, `@NotNull`, `@Min`, `@Max`, `@Email`, `@Size`
- `GlobalExceptionHandler` updated to handle `MethodArgumentNotValidException` cleanly

### 2. JUnit 5 Unit Tests
- `ProductServiceTest` — tests for `create`, `findById` (found/not found), `update`, `delete`
- `AuthServiceTest` — tests for register (success/duplicate email), login (success/bad credentials)
- `CartServiceTest` — tests for `addItem`, `removeItem`, stock validation
- Used `@ExtendWith(MockitoExtension.class)`, `Mockito.when(...).thenReturn(...)`

### 3. MockMvc Integration Tests
- `ProductControllerTest` — tests GET all, GET by id, POST (admin), unauthorized POST
- `AuthControllerTest` — tests register, login success/failure
- Used `@SpringBootTest`, `@AutoConfigureMockMvc`, JWT token generation in test setup

### 4. Deployment Configuration
- `application-prod.properties`:
  - datasource URL from environment variable `${DB_URL}`
  - JWT secret from `${JWT_SECRET}`
  - `ddl-auto=validate`
- `Dockerfile` for containerized backend deployment
- `render.yaml` for Render.com auto-deploy config
- CORS allowed origins set to Vercel frontend URL

### 5. Frontend — Final Polish
- Dark mode toggle with Tailwind `dark:` classes and `localStorage` persistence
- Loading skeletons (`animate-pulse`) on product grid, cart, orders
- `not-found.tsx` custom 404 page
- `layout.tsx` metadata with Open Graph tags
- Error boundary component for API failures

### 6. Vercel Deployment
- `NEXT_PUBLIC_API_URL` environment variable set to Render backend URL
- `next.config.js` configured with `images.domains` for product image hosting

### 7. Git & Push
- `git commit -m "v5: validation, JUnit5 tests, MockMvc, deployment config, UI polish"`
- `git tag v5.0`
- `git push origin main --tags`
