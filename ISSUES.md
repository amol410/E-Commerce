# Known Issues & Common Problems

This document tracks issues encountered during development and common problems you may face, organized by version.

---

## V1 — Foundation: Entity Design & Product CRUD

### Faced During Development

**Issue: `spring.jpa.hibernate.ddl-auto=create` drops data on restart**
- **Cause:** `create` drops and recreates schema on every app start.
- **Fix:** Use `update` during development so existing data is preserved. Use `validate` in production.

**Issue: Circular JSON serialization with `Category` self-reference**
- **Cause:** `Category` has a `parent` field pointing back to another `Category`, causing infinite recursion during JSON serialization.
- **Fix:** Add `@JsonIgnoreProperties("children")` on the `parent` field, or use a DTO to break the cycle.

**Issue: `LazyInitializationException` when accessing related entities**
- **Cause:** JPA loads relations lazily by default; accessing them outside a transaction throws this error.
- **Fix:** Add `@Transactional` to service methods, or use `JOIN FETCH` in JPQL queries, or set fetch to `EAGER` (not recommended for lists).

**Issue: Next.js `fetch` fails with CORS error**
- **Cause:** Spring Boot doesn't allow cross-origin requests by default.
- **Fix:** Add `@CrossOrigin(origins = "http://localhost:3000")` on controllers, or add a global `WebMvcConfigurer` CORS config bean.

### Potential Issues

- **MySQL timezone mismatch** — `DATETIME` columns may show wrong times. Fix: add `?serverTimezone=UTC` to JDBC URL.
- **Lombok not generating code** — Ensure annotation processing is enabled in IDE (IntelliJ: Settings → Build → Compiler → Annotation Processors).
- **Port conflict** — Spring Boot defaults to `8080`, Next.js to `3000`. If ports are taken, set `server.port=8081` in `application.properties` or run Next.js with `npm run dev -- -p 3001`.

---

## V2 — Authentication: Spring Security + JWT

### Faced During Development

**Issue: `403 Forbidden` on all endpoints after adding Spring Security**
- **Cause:** Spring Security auto-secures all endpoints once the dependency is added.
- **Fix:** Define a `SecurityFilterChain` bean in `SecurityConfig` explicitly permitting public routes.

**Issue: `NullPointerException` in `JwtAuthFilter` when no Authorization header is present**
- **Cause:** Calling `.substring(7)` on a null header.
- **Fix:** Always null-check: `if (authHeader == null || !authHeader.startsWith("Bearer ")) return;`

**Issue: JWT secret too short causing `WeakKeyException`**
- **Cause:** jjwt requires a minimum key length (256 bits for HS256).
- **Fix:** Use a Base64-encoded secret of at least 32 characters, or generate with `Keys.secretKeyFor(SignatureAlgorithm.HS256)`.

**Issue: `UserDetailsService` bean conflict**
- **Cause:** Spring Security auto-configures a `UserDetailsService` but you've defined your own, causing a bean conflict.
- **Fix:** Remove `spring.security.user.*` from `application.properties` and ensure your `UserDetailsServiceImpl` is the only `UserDetailsService` bean.

**Issue: Next.js middleware redirecting API routes**
- **Cause:** Middleware matcher pattern is too broad, intercepting `/api` routes meant for the Next.js API folder.
- **Fix:** Update `matcher` in `middleware.ts` to exclude `/api/` and only match page routes.

### Potential Issues

- **Token expiry not handled on frontend** — User gets `401` errors silently. Fix: Intercept 401 responses, redirect to `/login`, and clear stored token.
- **Password not encoded** — If `BCryptPasswordEncoder` bean is not registered, `AuthenticationManager` will fail to match passwords. Always register it as a `@Bean`.
- **CORS blocks preflight (OPTIONS) requests** — Spring Security blocks OPTIONS requests before CORS headers are applied. Fix: add `.requestMatchers(HttpMethod.OPTIONS).permitAll()` to security config.

---

## V3 — Shopping Cart & Orders

### Faced During Development

**Issue: Duplicate `CartItem` created instead of updating quantity**
- **Cause:** `save()` creates a new record when no `id` is set instead of updating existing.
- **Fix:** In `CartService.addItem()`, check `existsByCartAndProduct(cart, product)` first; if exists, find and increment, then save.

**Issue: `DataIntegrityViolationException` when placing an order with no stock**
- **Cause:** No stock validation before order placement; DB constraint fires too late.
- **Fix:** Add explicit stock check in `OrderService.placeOrder()` before any DB writes; throw a custom `InsufficientStockException`.

**Issue: Cart not cleared after order placement**
- **Cause:** `cartItemRepository.deleteAll(cartItems)` was called but `cart.getItems()` still cached in Hibernate session.
- **Fix:** Call `cart.getItems().clear()` and `cartRepository.save(cart)` after deleting cart items, or use `cartItemRepository.deleteByCartId(cartId)` with `@Modifying @Query`.

**Issue: `priceAtPurchase` not set — always null**
- **Cause:** Forgetting to copy `product.getPrice()` into `OrderItem` at the time of placement.
- **Fix:** Explicitly set `orderItem.setPriceAtPurchase(product.getPrice())` before saving.

### Potential Issues

- **Race condition on stock decrement** — Two users placing orders simultaneously for the same last item can both succeed. Fix (V5): Use `@Lock(LockModeType.PESSIMISTIC_WRITE)` on product fetch during order placement.
- **Cart total not recalculated** — After removing an item, the cart total may be stale if cached. Always recompute total server-side.
- **Order placed for deleted product** — If product is soft-deleted (or hard-deleted), existing cart items referencing it will cause FK constraint errors. Fix: Add `isActive` flag on product and check before order placement.

---

## V4 — Catalog Features: Filters, Pagination, Category Tree & Swagger

### Faced During Development

**Issue: Circular JSON on Category tree (`StackOverflowError`)**
- **Cause:** `children` → each child has `parent` → parent has `children` → infinite loop.
- **Fix:** Use `CategoryTreeDto` (no `parent` field), or annotate `parent` with `@JsonBackReference` and `children` with `@JsonManagedReference`.

**Issue: `N+1 query problem` on category tree fetch**
- **Cause:** Loading root categories, then Hibernate lazily fetches `children` for each with separate queries.
- **Fix:** Use `JOIN FETCH c.children` in JPQL or add `@EntityGraph(attributePaths = {"children"})` on the repository method.

**Issue: MapStruct mapper not found — `NoSuchBeanDefinitionException`**
- **Cause:** MapStruct annotation processor not configured in `pom.xml`, so mapper implementations are not generated.
- **Fix:** Add `annotationProcessorPaths` for `mapstruct-processor` in the `maven-compiler-plugin` config.

**Issue: Swagger UI blocked by Spring Security**
- **Cause:** `/swagger-ui/**` and `/v3/api-docs/**` paths are secured.
- **Fix:** Add these paths to `permitAll()` in `SecurityConfig`.

**Issue: Pagination returning wrong results with sort**
- **Cause:** Sort field name doesn't match the entity field name (e.g., `"Price"` vs `"price"`).
- **Fix:** Validate sort fields against a whitelist, or use `Sort.by(Sort.Direction.ASC, "price")` explicitly.

### Potential Issues

- **Infinite category depth causing deep recursion** — If categories are 10+ levels deep, recursive tree fetching can be slow. Fix: Limit recursion depth or use a materialized path pattern.
- **`Specification` with null filter params** — If a filter param is null, the Specification must explicitly return null (ignored) otherwise Hibernate throws errors. Always guard: `if (value == null) return null;`
- **MapStruct breaking on Lombok builders** — MapStruct and Lombok both use annotation processors; order matters. Fix: Ensure Lombok processor runs before MapStruct in `annotationProcessorPaths`.

---

## V5 — Production Polish: Validation, Tests & Deployment

### Faced During Development

**Issue: `@Valid` not triggering validation on nested DTOs**
- **Cause:** Nested objects need `@Valid` on the field inside the parent DTO, not just on the controller parameter.
- **Fix:** Add `@Valid` on nested DTO fields in the parent DTO class.

**Issue: MockMvc tests fail with 403 after adding Spring Security**
- **Cause:** Test context loads security config; requests without auth get 403.
- **Fix:** Use `@WithMockUser(roles = "ADMIN")` for admin endpoints, or manually add JWT Bearer token in test setup.

**Issue: `@Transactional` on test method causes rollback — test data not visible**
- **Cause:** Integration tests with `@Transactional` roll back after each test, which is usually correct but can cause issues when checking DB state in separate transactions.
- **Fix:** Use `@Commit` when you explicitly need data to persist in a test, or restructure assertions within the same transaction.

**Issue: Render.com cold start causing initial request to fail**
- **Cause:** Free-tier Render instances spin down after inactivity; first request after sleep gets a timeout.
- **Fix:** Add a health check endpoint (`/actuator/health`) and configure Render to keep the service alive, or document this behavior for users.

**Issue: Vercel not picking up `NEXT_PUBLIC_API_URL` at runtime**
- **Cause:** `NEXT_PUBLIC_` variables are baked in at build time; setting them after deploy doesn't work.
- **Fix:** Always set env vars in Vercel dashboard **before** triggering a build/deploy.

### Potential Issues

- **`HikariPool` connection exhaustion in tests** — Running many integration tests simultaneously can exhaust the DB connection pool. Fix: Set `spring.datasource.hikari.maximum-pool-size=5` in `application-test.properties`.
- **Docker build fails — JAR not found** — If the Maven build isn't run before Docker build, the target JAR is missing. Fix: Use a multi-stage Dockerfile (builder stage runs `mvn package`, runner stage copies the JAR).
- **CORS in production** — `@CrossOrigin` annotations on controllers are overridden by the global `SecurityConfig` CORS configuration. Ensure only one CORS configuration method is used in production.
- **JWT secret in version control** — Never commit `application.properties` with real secrets. Use environment variables and `.gitignore` the properties file with credentials.

---

## Post-V5 — Java 24 Compatibility

### Faced During Migration

**Issue: `Fatal error compiling: java.lang.ExceptionInInitializerError: com.sun.tools.javac.code.TypeTag :: UNKNOWN`**
- **Cause:** Lombok 1.18.30 uses internal javac API (`TypeTag`) that was removed in Java 23+. The error surfaces in `LombokProcessor.placePostCompileAndDontMakeForceRoundDummiesHook`.
- **Fix:** Upgrade Lombok to `1.18.42` (first version with Java 24 support). Pin it explicitly in `pom.xml` because Spring Boot 3.2.x manages it at 1.18.30.

**Issue: MapStruct annotation processor also fails on Java 24**
- **Cause:** MapStruct 1.5.5.Final uses the same removed internal javac APIs.
- **Fix:** Upgrade `mapstruct.version` to `1.6.3`.

**Issue: `Mockito cannot mock this class` — all `@MockBean` / `@Mock` tests fail**
- **Cause:** Byte Buddy 1.14.12 (bundled with Mockito 5.7.0) only supports Java up to version 22 (class file version 66). Java 24 produces class version 68, causing `Could not modify all classes`.
- **Fix:** Override both `mockito-core` and `mockito-junit-jupiter` to `5.14.2`, and `byte-buddy` + `byte-buddy-agent` to `1.15.10`. Also add JVM args to Surefire:
  ```xml
  <argLine>-XX:+EnableDynamicAgentLoading -Dnet.bytebuddy.experimental=true</argLine>
  ```

**Issue: `@SpringBootTest` controller tests fail with `Access denied for user 'root'@'localhost'`**
- **Cause:** `@SpringBootTest` loads the full application context including `DataSource`, which tries to connect to MySQL. No MySQL is available in the test environment.
- **Fix:** Add H2 (`com.h2database:h2`, test scope) and create `src/test/resources/application.properties` with:
  ```properties
  spring.datasource.url=jdbc:h2:mem:testdb;MODE=MySQL
  spring.jpa.hibernate.ddl-auto=create-drop
  spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
  ```

### Potential Issues

- **Lombok 1.18.42 not in Maven Central** — If it wasn't in the local `.m2` cache, it needs to be downloaded. Ensure internet access during first build.
- **H2 `MODE=MySQL` limitations** — H2's MySQL compatibility mode doesn't support all MySQL-specific SQL. Avoid MySQL-specific JPQL or native queries in tests, or use Testcontainers for full MySQL compatibility.
- **`net.bytebuddy.experimental` flag** — This flag allows Byte Buddy to run on unsupported JVM versions. It may produce warnings. Remove it once Byte Buddy officially supports your Java version.

---

## General Issues (All Versions)

| Issue | Cause | Fix |
|-------|-------|-----|
| `Connection refused` to MySQL | MySQL not running or wrong port | Start MySQL service; check `application.properties` |
| `Unknown database 'ecom_db'` | Database not created | Run `CREATE DATABASE ecom_db;` in MySQL |
| Lombok not working | Annotation processing disabled | Enable in IDE settings |
| `Could not initialize plugin` Maven error | Java version mismatch | Ensure Java 21+ is set as project SDK (project targets Java 21, tested on Java 24) |
| Next.js hydration mismatch | Server/client render diff | Avoid `window`/`localStorage` on server; use `useEffect` |
| `400 Bad Request` on POST | Missing `Content-Type: application/json` | Add header in frontend fetch calls |
