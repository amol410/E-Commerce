# Development Process

Step-by-step guide for how each version of ShopEase was built.

---

## V1 — Foundation: Entity Design & Product CRUD

### 1. Project Initialization
- Generated Spring Boot project via Spring Initializr:
  - Dependencies: Spring Web, Spring Data JPA, MySQL Driver, Lombok, Validation
- Created Next.js project with `create-next-app` (App Router + Tailwind CSS)

### 2. Database Design
- Designed 5 JPA entities:
  - `Category` — id, name, description, parent (self-referential `@ManyToOne`)
  - `Product` — id, name, description, price, stock, imageUrl, category (`@ManyToOne`)
  - `User` — id, name, email, password, role
  - `Cart` — id, user (`@OneToOne`), items (`@OneToMany`)
  - `CartItem` — id, cart, product, quantity
- Set `spring.jpa.hibernate.ddl-auto=update` for auto schema generation

### 3. Repository & Service Layers
- `JpaRepository` interfaces for all entities
- Custom queries: `findByCategoryId`, `findByNameContainingIgnoreCase`
- `ProductService`, `CategoryService` — CRUD with null checks, constructor injection

### 4. Controller Layer
- `ProductController` — GET all, GET by id, POST, PUT, DELETE at `/api/products`
- `CategoryController` — same pattern at `/api/categories`
- All responses wrapped in `ApiResponse<T>` standard wrapper

### 5. Exception Handling
- `ResourceNotFoundException` (extends `RuntimeException`)
- `GlobalExceptionHandler` with `@RestControllerAdvice`
- Handles `MethodArgumentNotValidException` for validation failures

### 6. Frontend — Next.js Setup
- Tailwind CSS with custom palette
- Reusable components: `Navbar`, `ProductCard`, `Footer`
- `/` — product listing grid fetching from backend
- `/products/[id]` — product detail page

---

## V2 — Authentication: Spring Security + JWT

### 1. Dependencies Added
- `spring-boot-starter-security`
- `jjwt-api`, `jjwt-impl`, `jjwt-jackson` (version 0.12.5)

### 2. JWT Utility
- `JwtUtil` — `generateToken(username)`, `extractUsername(token)`, `isTokenValid(token, userDetails)`
- Secret stored in `app.jwt.secret` property; expiry 24 hours

### 3. Security Components
- `UserDetailsServiceImpl` — loads user by email, maps roles to `GrantedAuthority`
- `JwtAuthFilter` extends `OncePerRequestFilter` — extracts Bearer token, validates, sets `SecurityContextHolder`
- `SecurityConfig`:
  - Permit: `/api/auth/**`, GET `/api/products/**`, GET `/api/categories/**`
  - Require auth: POST/PUT/DELETE products & categories, all `/api/cart/**`, `/api/orders/**`
  - Stateless session (`SessionCreationPolicy.STATELESS`)

### 4. Auth Controller
- `POST /api/auth/register` — validate, BCrypt encode password, save user + create cart, return JWT
- `POST /api/auth/login` — authenticate via `AuthenticationManager`, return JWT

### 5. Frontend — Auth UI
- `AuthContext` with React Context API for global user state
- `/login` and `/register` pages with form validation
- JWT stored in `localStorage`
- `Authorization: Bearer <token>` header on all authenticated API calls

---

## V3 — Shopping Cart & Orders

### 1. Cart Design
- `Cart` is 1-to-1 with `User`, auto-created on registration
- `CartItem` links `Cart` + `Product` with quantity
- `UNIQUE(cart_id, product_id)` constraint prevents duplicate items

### 2. Cart Service & Controller
- `CartService`:
  - `addItem` — checks existing item; increments if exists, creates if not
  - `removeItem` — validates ownership before delete
  - `updateQuantity` — sets new quantity
  - `getCart` — returns cart with all items and computed total
- `CartController` at `/api/cart` — all routes require authentication

### 3. Order Design
- `Order` — id, user, status (PLACED/CONFIRMED/SHIPPED/DELIVERED), totalAmount, createdAt
- `OrderItem` — id, order, product, quantity, **priceAtPurchase** (preserves historical price)

### 4. Order Service & Controller
- `OrderService.placeOrder(userId)`:
  1. Load cart, validate not empty
  2. Validate stock for each item
  3. Create `Order` + `OrderItem` records
  4. Decrement product stock
  5. Clear cart
- `GET /api/orders/my` — paginated order history

### 5. Frontend — Cart & Checkout
- `/cart` — item list with +/- quantity controls, subtotal, remove button
- `/checkout` — order summary + "Place Order" CTA
- `/orders` — order history with status badges
- Cart item count in Navbar via `CartContext`

---

## V4 — Catalog Features: Filters, Pagination, Category Tree & Swagger

### 1. Recursive Category Tree
- `children` field on `Category`: `@OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)`
- `CategoryService.getTree()` — fetches root categories only; JPA loads children lazily
- `@JsonIgnoreProperties("parent")` on `parent` field to break circular reference

### 2. Product Filters & Pagination
- `ProductRepository` extends `JpaSpecificationExecutor<Product>`
- `ProductSpecification` — builds `Specification<Product>` from params: `name` (LIKE), `categoryId`, `minPrice`, `maxPrice`
- Controller accepts `@RequestParam` filters + `Pageable` (page, size, sort)

### 3. MapStruct DTOs
- DTOs: `ProductDto`, `CategoryDto`, `UserDto`, `CartItemDto`, `OrderDto`
- Mappers: `ProductMapper`, `CategoryMapper` — `@Mapper(componentModel = "spring")`
- All controllers return DTOs, not raw entities

### 4. Swagger / OpenAPI
- `springdoc-openapi-starter-webmvc-ui` added
- `OpenApiConfig` bean with JWT `SecurityScheme`
- Controllers annotated with `@Tag`, endpoints with `@Operation`, `@ApiResponse`
- Accessible at `http://localhost:8080/swagger-ui.html`

### 5. Frontend — Filters & Category Tree
- Sidebar rendering recursive category tree
- Filter panel — price range, sort dropdown
- Pagination component with prev/next and page number
- Search input with 300ms debounce using `useCallback`

---

## V5 — Production Polish: Validation, Tests & Deployment

### 1. Bean Validation
- `@Valid` on all controller method parameters with request bodies
- DTOs annotated with `@NotBlank`, `@NotNull`, `@Min`, `@Max`, `@Email`, `@Size`
- `GlobalExceptionHandler` returns structured validation error responses

### 2. JUnit 5 Unit Tests
- `ProductServiceTest` — create, findById (found/not found), update, delete
- `AuthServiceTest` — register (success, duplicate email), login (success, bad credentials)
- `CartServiceTest` — addItem, removeItem, stock validation
- Pattern: `@ExtendWith(MockitoExtension.class)`, `Mockito.when(...).thenReturn(...)`

### 3. MockMvc Integration Tests
- `ProductControllerTest`, `AuthControllerTest`, `CategoryControllerTest`, `CartControllerTest`, `OrderControllerTest`
- Used `@SpringBootTest`, `@AutoConfigureMockMvc`, `@WithMockUser`, `@MockBean`
- **46 tests total, all passing**

### 4. Test Infrastructure
- `src/test/resources/application.properties` — H2 datasource (`MODE=MySQL`), `ddl-auto=create-drop`
- Tests run without any MySQL connection

### 5. Deployment Configuration
- `application-prod.properties` — all secrets from environment variables
- CORS allowed origin set to Vercel frontend URL

### 6. Frontend — Final Polish
- Loading skeletons (`animate-pulse`) on product grid, cart, orders pages
- Custom `not-found.tsx` 404 page
- `layout.tsx` metadata with Open Graph tags

---

## V6 — Java 24 Compatibility Fix

### Problem
System Java was **24.0.2** but project targeted Java 17. Three annotation processors failed:
- **Lombok 1.18.30** — uses removed internal `javac` API (`TypeTag :: UNKNOWN`)
- **MapStruct 1.5.5.Final** — same internal javac API issue
- **Mockito 5.7.0 / Byte Buddy 1.14.12** — Byte Buddy only supported Java up to version 22

### Fixes Applied

| Property           | Before             | After      |
|--------------------|--------------------|------------|
| `java.version`     | `17`               | `21`       |
| `lombok.version`   | managed → 1.18.30  | `1.18.42`  |
| `mapstruct.version`| `1.5.5.Final`      | `1.6.3`    |
| `mockito.version`  | managed → 5.7.0    | `5.14.2`   |
| `bytebuddy.version`| managed → 1.14.12  | `1.15.10`  |

Surefire JVM args added:
```xml
<argLine>-XX:+EnableDynamicAgentLoading -Dnet.bytebuddy.experimental=true</argLine>
```

H2 added (test scope) with `src/test/resources/application.properties` for MySQL-mode in-memory testing.

---

## V7 — Spring Boot 4.0.3 Upgrade

### Changes Required
Spring Boot 4.0.3 bundles Hibernate ORM 7.2 and Jakarta EE 11, requiring these fixes:

- Replaced deprecated `HttpMethod` import path
- Updated `SecurityFilterChain` lambda config style for Spring Security 7
- Removed `spring.jpa.properties.hibernate.dialect` (auto-detected in Hibernate 7)
- Updated Swagger/springdoc to `2.8.6` for Spring Boot 4 compatibility
- Fixed `@Query` annotations for Hibernate 7 HQL dialect changes
- Replaced deprecated Spring Data `Page`/`Pageable` usages

All 46 tests remain passing after upgrade.
