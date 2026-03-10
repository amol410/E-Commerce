# Known Issues & Solutions

Issues encountered during development, organized by version.

---

## V1 — Foundation

**`ddl-auto=create` drops data on restart**
- Cause: `create` drops and recreates schema every start.
- Fix: Use `update` in dev, `validate` in prod.

**Circular JSON on `Category` self-reference**
- Cause: `parent` → `Category` → infinite recursion during serialization.
- Fix: `@JsonIgnoreProperties("children")` on the `parent` field, or use a DTO.

**`LazyInitializationException` on related entities**
- Cause: JPA loads relations lazily; accessing outside a transaction fails.
- Fix: `@Transactional` on service methods, or `JOIN FETCH` in JPQL.

**Next.js CORS error**
- Cause: Spring Boot blocks cross-origin by default.
- Fix: Global `WebMvcConfigurer` CORS bean, or `@CrossOrigin` on controllers.

---

## V2 — Authentication

**`403 Forbidden` on all endpoints after adding Spring Security**
- Fix: Define `SecurityFilterChain` bean explicitly permitting public routes.

**`NullPointerException` in `JwtAuthFilter`**
- Cause: Calling `.substring(7)` on a null `Authorization` header.
- Fix: `if (authHeader == null || !authHeader.startsWith("Bearer ")) return;`

**`WeakKeyException` on JWT signing**
- Cause: Secret key too short (jjwt requires 256 bits for HS256).
- Fix: Use a Base64-encoded secret of at least 32 characters.

**CORS blocks OPTIONS preflight**
- Fix: Add `.requestMatchers(HttpMethod.OPTIONS).permitAll()` to security config.

---

## V3 — Shopping Cart & Orders

**Duplicate `CartItem` created instead of updating quantity**
- Cause: `save()` creates new record when no `id` is set.
- Fix: Check `existsByCartAndProduct()` first; increment if exists.

**Cart not cleared after order placement**
- Cause: `deleteAll(cartItems)` called but Hibernate session still caches the list.
- Fix: Call `cart.getItems().clear()` + `cartRepository.save(cart)` after deletion.

**`priceAtPurchase` always null**
- Cause: Forgetting to copy `product.getPrice()` into `OrderItem`.
- Fix: `orderItem.setPriceAtPurchase(product.getPrice())` before saving.

---

## V4 — Filters, Pagination, Category Tree & Swagger

**`StackOverflowError` on Category tree JSON**
- Cause: `children` → `parent` → `children` → infinite loop.
- Fix: Use `CategoryTreeDto` (no `parent` field), or `@JsonManagedReference` / `@JsonBackReference`.

**MapStruct mapper `NoSuchBeanDefinitionException`**
- Cause: `annotationProcessorPaths` not configured for `mapstruct-processor`.
- Fix: Add MapStruct annotation processor in `maven-compiler-plugin` config.

**Swagger UI blocked by Spring Security**
- Fix: Add `/swagger-ui/**` and `/v3/api-docs/**` to `permitAll()`.

**`Specification` with null filter params throws error**
- Fix: Guard every param: `if (value == null) return null;` inside each Specification.

---

## V5 — Validation, Tests & Deployment

**MockMvc tests fail with 403**
- Fix: Use `@WithMockUser(roles = "ADMIN")` for admin endpoints in tests.

**`@Valid` not triggering on nested DTOs**
- Fix: Add `@Valid` on the nested DTO field inside the parent DTO class, not just the controller param.

**Render.com cold start timeout**
- Cause: Free-tier instances spin down after inactivity.
- Fix: Add `/actuator/health` endpoint; configure uptime monitor (e.g., UptimeRobot).

**`NEXT_PUBLIC_API_URL` not picked up at runtime on Vercel**
- Cause: `NEXT_PUBLIC_` vars are baked in at build time.
- Fix: Set env vars in Vercel dashboard **before** triggering a build.

---

## V6 — Java 24 Compatibility

**`TypeTag :: UNKNOWN` compilation error**
- Cause: Lombok 1.18.30 uses removed internal javac API in Java 23+.
- Fix: Pin `lombok.version=1.18.42` in `pom.xml`.

**MapStruct fails to compile on Java 24**
- Fix: Upgrade `mapstruct.version` to `1.6.3`.

**`Mockito cannot mock this class` — all `@MockBean` tests fail**
- Cause: Byte Buddy 1.14.12 only supports up to Java 22 (class file version 66).
- Fix: Override `mockito.version=5.14.2` and `bytebuddy.version=1.15.10`. Add to Surefire:
  ```xml
  <argLine>-XX:+EnableDynamicAgentLoading -Dnet.bytebuddy.experimental=true</argLine>
  ```

**`@SpringBootTest` fails with `Access denied for user 'root'@'localhost'`**
- Cause: Full application context tries to connect to MySQL during tests.
- Fix: Add H2 (test scope) + `src/test/resources/application.properties`:
  ```properties
  spring.datasource.url=jdbc:h2:mem:testdb;MODE=MySQL
  spring.jpa.hibernate.ddl-auto=create-drop
  ```

---

## V7 — Spring Boot 4.0.3 Upgrade

**Port 8080 already in use**
- Cause: Previous app instance still running.
- Fix (Windows): `cmd /c "taskkill /PID <pid> /F"`. Find PID with `netstat -ano | grep :8080`.

**`user.name.split()` NullPointerException in Navbar**
- Cause: Backend returns `user` object with `name: null`.
- Fix: Use optional chaining — `user.name?.split(" ")[0] ?? "User"` in `Navbar.tsx`.

---

## General Issues (All Versions)

| Issue | Cause | Fix |
|-------|-------|-----|
| `Connection refused` to MySQL | MySQL not running | Start MySQL service; check `application.properties` |
| `Unknown database 'ecom_db'` | DB not created | `CREATE DATABASE ecom_db;` in MySQL |
| Lombok not working | Annotation processing disabled in IDE | Enable in IDE: Settings → Build → Compiler → Annotation Processors |
| Port conflict (8080 / 3000) | Another process using the port | Use `server.port=8081` or `npm run dev -- -p 3001` |
| Next.js hydration mismatch | `window`/`localStorage` accessed on server | Use `useEffect` to guard client-only APIs |
| `400 Bad Request` on POST | Missing `Content-Type: application/json` | Add header in all frontend POST/PUT calls |
