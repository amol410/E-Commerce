# Interview Questions — ShopEase E-Commerce Project

These are the most important interview questions based on every technology used
in this project. Questions are grouped by topic. Each question has a clear answer
written the way you should explain it in an interview.

---

## SECTION 1 — Core Java

**Q1. What is the difference between == and .equals() in Java?**

== compares references — it checks if both variables point to the same object in
memory. .equals() compares content — it checks if the values inside the objects
are the same. For example, two String objects with the same text will return false
with == but true with .equals(). Always use .equals() when comparing String, Integer,
or any object values.

**Q2. What is the difference between ArrayList and LinkedList?**

ArrayList stores elements in a dynamic array. Accessing any element by index is
fast (O1 time) but inserting or deleting in the middle is slow because elements
need to shift. LinkedList stores elements as nodes where each node points to the
next. Inserting or deleting is fast but accessing by index is slow because you must
traverse from the start. Use ArrayList for most cases. Use LinkedList only when you
do a lot of insertions at the beginning or middle.

**Q3. What is the difference between HashMap and HashSet?**

HashMap stores key-value pairs. HashSet stores only keys (unique values). HashSet
is internally implemented using HashMap where the value is always a dummy object.
Both give O1 average time for add, remove, and contains.

**Q4. What are the four pillars of Object Oriented Programming?**

Encapsulation means hiding internal data using private fields and exposing it
through public getters and setters. Inheritance means a child class can reuse and
extend the behaviour of a parent class. Polymorphism means the same method name
behaves differently based on the object — method overloading (compile time) and
method overriding (runtime). Abstraction means hiding implementation details and
showing only the essential features using abstract classes or interfaces.

**Q5. What is the difference between abstract class and interface?**

An abstract class can have both implemented and abstract methods, constructors,
and instance variables. A class can extend only one abstract class. An interface
before Java 8 could only have abstract methods. From Java 8, interfaces can have
default and static methods. A class can implement multiple interfaces. Use an
abstract class when classes share common behaviour. Use an interface when you want
to define a contract that unrelated classes can follow.

**Q6. What is a functional interface in Java?**

A functional interface has exactly one abstract method. It can have default and
static methods. The @FunctionalInterface annotation ensures the compiler enforces
this rule. They are used with lambda expressions. Examples from Java standard
library are Runnable, Callable, Comparator, Predicate, Function, and Consumer.

**Q7. What is the difference between checked and unchecked exceptions?**

Checked exceptions must be handled using try-catch or declared with throws. They
represent recoverable situations like file not found or database connection failure.
Unchecked exceptions extend RuntimeException and do not need to be declared or
caught. They represent programming errors like NullPointerException or
ArrayIndexOutOfBoundsException. In this project, ResourceNotFoundException extends
RuntimeException so it is unchecked and is handled by the GlobalExceptionHandler.

**Q8. What is the Java Stream API?**

Streams allow you to process collections of data in a declarative, functional style.
You can filter, map, sort, collect, and reduce data without writing loops. Streams
are lazy — intermediate operations like filter and map are not executed until a
terminal operation like collect or forEach is called. They do not modify the original
collection. In this project, streams are used in service classes to process lists of
entities.

**Q9. What is Optional in Java and why is it used?**

Optional is a container that may or may not hold a value. It was introduced in Java 8
to avoid NullPointerException. Instead of returning null, a method returns
Optional.empty() or Optional.of(value). You then call isPresent(), get(),
orElse(), or orElseThrow() on it. In this project, repository methods like
findById() return Optional, and we call .orElseThrow() to throw
ResourceNotFoundException when the entity does not exist.

**Q10. What is the difference between String, StringBuilder, and StringBuffer?**

String is immutable — every modification creates a new object. StringBuilder is
mutable and not thread safe. It is faster and should be used when building strings
in single-threaded code. StringBuffer is mutable and thread safe but slower because
its methods are synchronized. Use StringBuilder unless you specifically need thread
safety.

---

## SECTION 2 — Spring Boot

**Q11. What is Spring Boot and how is it different from Spring Framework?**

Spring Framework is a large framework that requires a lot of manual configuration —
XML files, bean definitions, server setup. Spring Boot is built on top of Spring
Framework and uses auto-configuration and convention over configuration to remove
all that boilerplate. You get an embedded Tomcat server, auto-configured beans, and
starter dependencies that bundle related libraries. Spring Boot does not replace
Spring — it makes Spring easier to use.

**Q12. What is auto-configuration in Spring Boot?**

Auto-configuration means Spring Boot automatically configures beans based on the
dependencies present in the classpath. For example, if spring-boot-starter-data-jpa
is on the classpath and a DataSource is configured in application.properties, Spring
Boot automatically creates EntityManagerFactory, TransactionManager, and repository
beans. You can override any auto-configured bean by defining your own. This is
controlled by the @EnableAutoConfiguration annotation inside @SpringBootApplication.

**Q13. What is the difference between @Component, @Service, @Repository, and @Controller?**

All four are specializations of @Component and cause Spring to register the class
as a bean. @Service marks business logic classes. @Repository marks data access
classes and additionally enables Spring to translate JPA exceptions into Spring's
DataAccessException hierarchy. @Controller marks MVC controllers. @RestController
is @Controller plus @ResponseBody combined. Using the correct annotation makes
the code more readable and enables specific Spring features for each layer.

**Q14. What is @SpringBootApplication?**

It is a convenience annotation that combines three annotations. @Configuration
marks the class as a source of bean definitions. @EnableAutoConfiguration tells
Spring Boot to automatically configure beans based on the classpath.
@ComponentScan tells Spring to scan the current package and its sub-packages for
components, services, repositories, and controllers.

**Q15. What is the difference between @RequestBody and @RequestParam?**

@RequestParam extracts a value from the URL query string. For example,
GET /api/products?name=phone extracts name as a request param. @RequestBody
extracts the entire HTTP request body and deserializes it into a Java object using
Jackson. It is used with POST and PUT requests that send JSON. In this project,
register and login endpoints use @RequestBody to receive the JSON payload, while
the product listing endpoint uses @RequestParam for filters like name, minPrice,
and maxPrice.

**Q16. What is @PathVariable?**

@PathVariable extracts a value from the URL path itself. For example,
GET /api/products/5 extracts 5 as the product ID. In the controller method the
parameter is annotated with @PathVariable Long id. The difference from @RequestParam
is that @PathVariable is part of the URL structure itself, not the query string.

**Q17. What is the purpose of @Transactional?**

@Transactional wraps a method in a database transaction. If the method completes
successfully, the transaction is committed. If an exception is thrown, the
transaction is rolled back and no partial changes are saved. In this project,
service methods like placeOrder use @Transactional because they involve multiple
database writes — creating the order, creating order items, decrementing stock, and
clearing the cart. If any step fails, all changes roll back.

**Q18. What is dependency injection and what types does Spring support?**

Dependency injection means Spring creates objects and injects their dependencies
rather than the objects creating their own dependencies. Spring supports three types.
Constructor injection is the recommended approach — dependencies are declared as
final fields and injected through the constructor. This is what @RequiredArgsConstructor
from Lombok does automatically. Setter injection uses setter methods. Field injection
uses @Autowired directly on fields but is not recommended because it makes testing
harder and hides dependencies.

**Q19. What is the difference between @Bean and @Component?**

@Component is placed on a class and Spring automatically detects and registers it
during component scanning. @Bean is placed on a method inside a @Configuration class
and the method's return value is registered as a Spring bean. Use @Component for
your own classes. Use @Bean when you need to configure a third-party class that you
cannot annotate directly, like BCryptPasswordEncoder or ObjectMapper.

**Q20. What is application.properties and what is it used for?**

application.properties is the central configuration file in Spring Boot. It
controls the database URL, port number, JPA settings, JWT secrets, and any other
externalized configuration. Spring Boot reads this file automatically at startup.
Values can be injected into beans using @Value("${property.name}"). In this project
it configures the MySQL connection, JWT secret, JWT expiry time, and Swagger paths.

---

## SECTION 3 — Spring Data JPA and Hibernate

**Q21. What is JPA and what is Hibernate?**

JPA stands for Jakarta Persistence API. It is a specification — a set of rules and
interfaces — that defines how Java objects should be mapped to database tables and
how to perform database operations. Hibernate is the most popular implementation of
that specification. It provides the actual engine that converts JPA method calls into
SQL queries and sends them to the database. Spring Data JPA wraps Hibernate and adds
the repository pattern on top.

**Q22. What is the difference between @OneToOne, @OneToMany, @ManyToOne, and @ManyToMany?**

These annotations define the relationship between two entities. @OneToOne means one
record in table A relates to exactly one record in table B. In this project User and
Cart have a OneToOne relationship — each user has exactly one cart. @ManyToOne means
many records in table A relate to one record in table B. Product and Category have
ManyToOne — many products belong to one category. @OneToMany is the reverse — one
category has many products. @ManyToMany means many records on both sides — for
example, students and courses.

**Q23. What is the difference between FetchType.LAZY and FetchType.EAGER?**

LAZY means related entities are not loaded from the database until you actually access
them. EAGER means related entities are loaded immediately along with the parent entity
in the same query. LAZY is the default for collections (OneToMany, ManyToMany) and
is preferred because loading everything eagerly causes performance problems. If you
access a lazy-loaded field outside a transaction you get LazyInitializationException.
The fix is to use @Transactional on the service method or use JOIN FETCH in the query.

**Q24. What is the N+1 query problem?**

N+1 happens when you fetch a list of N parent entities and then Hibernate issues N
separate queries to load the related child entities of each parent — one query per
parent. For example, fetching 10 categories and then loading the products for each
category separately results in 1 + 10 = 11 queries. The fix is to use JOIN FETCH in
JPQL, which loads everything in a single query. This was relevant in the category
tree endpoint in this project.

**Q25. What is the difference between save() and saveAndFlush() in JPA?**

save() saves the entity to the persistence context (Hibernate's first-level cache)
and schedules an INSERT or UPDATE for the next flush. The SQL may not hit the
database immediately. saveAndFlush() saves and immediately flushes to the database,
executing the SQL right away. Use save() in most cases. Use saveAndFlush() when
you need the ID generated by the database immediately or when you need to verify a
constraint before continuing.

**Q26. What is ddl-auto and what are its values?**

ddl-auto is a JPA property that controls how Hibernate manages the database schema.
create drops the existing schema and creates a new one on every startup — dangerous
in production, loses all data. create-drop is like create but also drops the schema
when the application shuts down — useful for tests. update compares the entity
classes to the existing schema and adds missing tables or columns without dropping
anything — used during development. validate checks that the schema matches the
entity classes but makes no changes — used in production. none disables all
schema management.

**Q27. What is a JPA Specification?**

A Specification is a way to build dynamic database queries programmatically. Instead
of writing a separate repository method for every combination of filters, you create
a Specification that builds criteria based on what parameters are actually provided.
In this project, ProductSpecification builds a query that filters by name, categoryId,
minPrice, and maxPrice only when those values are not null. The repository interface
extends JpaSpecificationExecutor to support this.

**Q28. What is the difference between JPQL and native SQL?**

JPQL stands for Jakarta Persistence Query Language. It is written against entity
class names and field names, not table names and column names. Hibernate translates
it to the correct SQL dialect for your database. Native SQL is actual database SQL.
JPQL is portable across databases. Native SQL is database-specific. Use JPQL for
most queries. Use @Query with nativeQuery=true only when you need a database-specific
feature that JPQL cannot express.

---

## SECTION 4 — Spring Security and JWT

**Q29. What is Spring Security and what does it do?**

Spring Security is a framework that handles authentication and authorization.
Authentication means verifying who the user is — checking the username and password.
Authorization means checking what the user is allowed to do — whether they have
permission to access a specific endpoint. In this project, Spring Security uses a
filter chain that intercepts every incoming HTTP request, checks for a JWT token,
validates it, and sets the authenticated user in the security context.

**Q30. What is JWT and how does it work?**

JWT stands for JSON Web Token. It is a compact, self-contained token that carries
information about the user. A JWT has three parts separated by dots: header, payload,
and signature. The header says which algorithm was used. The payload contains claims
like the username and expiry time. The signature is created by hashing the header
and payload with a secret key. When a user logs in, the server creates a JWT and
sends it to the client. For every subsequent request, the client sends the JWT in
the Authorization header. The server validates the signature — if it is valid, the
user is authenticated without querying the database again.

**Q31. What is the difference between authentication and authorization?**

Authentication is the process of verifying identity — proving you are who you say
you are. Logging in with email and password is authentication. Authorization is the
process of checking permissions — deciding what an authenticated user is allowed to
do. In this project, any user who sends a valid JWT token is authenticated. But only
users with the ROLE_ADMIN role are authorized to create, update, or delete products.

**Q32. What is stateless authentication and why is JWT stateless?**

In stateful authentication, the server stores session information. Every request
carries a session ID and the server looks up the session to identify the user. This
requires shared storage when you have multiple servers. In stateless authentication,
the token itself contains all the information needed to authenticate the user. The
server does not store any session data. JWT is stateless because the payload carries
the username and the signature proves it was issued by the server. This project uses
SessionCreationPolicy.STATELESS to enforce this.

**Q33. What is BCrypt and why is it used for passwords?**

BCrypt is a password hashing algorithm. Passwords are never stored as plain text
in the database. BCrypt hashes the password with a randomly generated salt, meaning
the same password produces a different hash each time. It is also intentionally slow
— designed to make brute-force attacks impractical. When a user logs in, BCrypt
hashes the input password and compares it to the stored hash. Spring provides
BCryptPasswordEncoder as a ready-made implementation.

**Q34. What is @PreAuthorize and how is it used?**

@PreAuthorize is a method-level security annotation that checks a condition before
the method executes. If the condition is false, access is denied and a 403 response
is returned. In this project it is used on cart and order controller methods with
expressions like hasRole('USER') or hasRole('ADMIN'). It requires
@EnableMethodSecurity on the security configuration class to activate.

**Q35. What is a SecurityFilterChain?**

SecurityFilterChain is the main configuration bean in Spring Security. It defines
which endpoints are public, which require authentication, and which require specific
roles. In this project, the filterChain() method configures CSRF to be disabled
(since the API is stateless), CORS to allow the frontend origin, session management
to be stateless, URL-based access rules, and registers the JwtAuthFilter to run
before the standard username-password filter.

**Q36. What is CORS and why is it configured in this project?**

CORS stands for Cross-Origin Resource Sharing. Browsers block JavaScript from
making API calls to a different domain by default. The frontend runs on
localhost:3000 and the backend runs on localhost:8080 — different ports mean
different origins. Without CORS configuration, every API call from the frontend
would be blocked by the browser with a CORS error. The backend configures CORS
to allow requests from localhost:3000 and the Vercel domain, allowing specific
HTTP methods and headers.

---

## SECTION 5 — REST API Design

**Q37. What is REST and what are its key principles?**

REST stands for Representational State Transfer. It is an architectural style for
designing APIs. Key principles are: statelessness — each request contains all the
information needed; resource-based URLs — URLs represent nouns not actions; correct
HTTP methods — GET for reading, POST for creating, PUT for updating, DELETE for
deleting; and standard HTTP status codes to communicate the result. A REST API that
follows all these principles strictly is called RESTful.

**Q38. What is the difference between PUT and PATCH?**

PUT replaces the entire resource with the data provided in the request body. If you
send a PUT with only the price field, all other fields get replaced with null or
defaults. PATCH partially updates a resource — only the fields provided in the
request are updated, everything else stays the same. In this project PUT is used for
updates, which means the client should send the full updated object.

**Q39. What are the common HTTP status codes and what do they mean?**

200 OK means the request succeeded and there is a response body. 201 Created means
a resource was successfully created — used after POST. 204 No Content means success
but no response body — used after DELETE. 400 Bad Request means the client sent
invalid data — used for validation errors. 401 Unauthorized means the request has no
valid authentication credentials. 403 Forbidden means the user is authenticated but
does not have permission. 404 Not Found means the resource does not exist. 500
Internal Server Error means something went wrong on the server side.

**Q40. What is the difference between @RestController and @Controller?**

@Controller is the traditional Spring MVC annotation where methods return view names
that resolve to HTML templates. @RestController is @Controller plus @ResponseBody
combined — every method automatically serializes its return value to JSON and writes
it directly to the HTTP response. In a REST API you always use @RestController. This
project uses @RestController on all five controller classes.

**Q41. What is an API response wrapper and why is it used in this project?**

Instead of returning raw data directly, all endpoints return an ApiResponse object
that wraps the actual data. It contains a success flag (true or false), a message
string, and the data payload. This gives every API response a consistent structure
that the frontend can rely on. Without it, success responses and error responses
would have completely different shapes, making frontend code more complex.

**Q42. What is @RestControllerAdvice and how does it work?**

@RestControllerAdvice is a global exception handler for all REST controllers. Instead
of wrapping every controller method in try-catch blocks, you define a central class
with @ExceptionHandler methods. When any controller throws an exception, Spring
automatically routes it to the matching handler method. In this project,
GlobalExceptionHandler handles ResourceNotFoundException with 404,
BadRequestException with 400, and MethodArgumentNotValidException with 400 including
validation error details.

---

## SECTION 6 — Database Design

**Q43. What is the database schema in this project and how are the tables related?**

There are seven tables. User stores customer accounts with name, email, hashed
password, and role. Category is self-referential — each category has an optional
parent_id pointing to another category, allowing nested categories. Product belongs
to a category via category_id. Cart has a one-to-one relationship with User —
each user gets exactly one cart created at registration. CartItem links Cart and
Product with a quantity field and has a unique constraint on cart_id plus product_id
to prevent duplicate items. Order stores placed orders with status and total amount.
OrderItem links Order and Product and stores the price at the time of purchase.

**Q44. Why is priceAtPurchase stored in OrderItem?**

Product prices can change over time. If we only stored the product ID in OrderItem
and calculated the price by joining to the Product table, then changing a product's
price later would retroactively change the price shown in old orders. That is wrong.
By storing the price at the moment the order is placed, the order history always
shows what the customer actually paid, regardless of future price changes. This is a
fundamental e-commerce database design principle.

**Q45. Why is Cart auto-created when a user registers?**

It creates a better user experience — the cart always exists for an authenticated
user, so the frontend never has to handle a null cart case. It also simplifies the
cart service code. The alternative would be to create the cart lazily on the first
add-to-cart action, which adds complexity and edge cases. In AuthService, after
saving the new User, a Cart is immediately created and saved with that user.

**Q46. What is a self-referential relationship in JPA?**

A self-referential relationship is when an entity has a relationship with itself.
In this project, Category has a @ManyToOne relationship with Category for the
parent field — a category can have a parent category of the same type. It also has
a @OneToMany relationship with Category for the children field — a category can
have many child categories. This allows building a tree structure of unlimited depth
using a single table with a parent_id column.

---

## SECTION 7 — Testing

**Q47. What is the difference between unit tests and integration tests?**

Unit tests test a single class or method in isolation. All dependencies are replaced
with mocks using Mockito. They are fast because no database, server, or network is
involved. Integration tests test multiple layers working together. In this project,
controller integration tests use @SpringBootTest which loads the full application
context, and MockMvc sends HTTP requests through the actual security filter chain
and controller. They are slower but test realistic scenarios.

**Q48. What is Mockito and what is @MockitoBean?**

Mockito is a library for creating mock objects in tests. A mock is a fake
implementation of a class that you control. You define what the mock returns when
specific methods are called using when().thenReturn(). This lets you test a class
without its real dependencies. @MockitoBean (Spring Boot 4) creates a Mockito mock
and registers it as a Spring bean, replacing any real bean of that type in the
application context. In controller tests, the service layer is mocked so tests do
not hit the database.

**Q49. What is MockMvc and how is it used?**

MockMvc is a testing utility that lets you simulate HTTP requests to your Spring MVC
controllers without starting a real HTTP server. You can perform GET, POST, PUT, and
DELETE requests and assert the response status code, headers, and JSON body. In
controller tests, MockMvc is built from the WebApplicationContext with the Spring
Security filter chain applied, so authentication and authorization rules are tested
as well.

**Q50. What is @WithMockUser?**

@WithMockUser is a Spring Security test annotation that sets up a fake authenticated
user in the security context for that test. By default it creates a user with role
ROLE_USER. You can specify roles like @WithMockUser(roles = "ADMIN") to test admin
endpoints. Without this annotation, controller tests for protected endpoints get 401
or 403 even before reaching the controller logic.

**Q51. What is H2 and why is it used in tests?**

H2 is an in-memory relational database written in Java. It runs inside the JVM
with no installation. It is used in tests because it starts instantly, requires no
external process, and is destroyed when the JVM exits leaving no test data behind.
H2 supports a MySQL compatibility mode where it accepts MySQL SQL syntax so Hibernate
can use the same queries in tests as it does against the real MySQL database.

---

## SECTION 8 — MapStruct and DTOs

**Q52. What is a DTO and why is it used?**

DTO stands for Data Transfer Object. It is a simple object that carries data between
layers. Instead of exposing JPA entity classes directly in API responses, you map
entities to DTOs. This gives you control over exactly which fields are exposed — you
can hide internal fields like password, exclude lazy-loaded relationships that would
cause N+1 queries, and reshape the data to match what the frontend needs. It also
decouples the API contract from the database schema so either can change independently.

**Q53. What is MapStruct and how does it work?**

MapStruct is a code generation library that automatically generates the code to
convert between entity classes and DTO classes. You define a mapper interface with
@Mapper(componentModel = "spring") and declare method signatures like
ProductDto toDto(Product product). MapStruct reads these at compile time and
generates a concrete implementation class with all the field mapping code. The
generated class is registered as a Spring bean so it can be injected where needed.
This eliminates hundreds of lines of repetitive manual mapping code.

**Q54. Why is MapStruct preferred over manual mapping or ModelMapper?**

MapStruct generates code at compile time so it is as fast as hand-written code and
adds zero runtime overhead. Mapping errors are caught at compile time, not at runtime.
ModelMapper works by reflection at runtime, which is slower and can produce hard to
debug errors when field names do not match. Manual mapping means writing repetitive
setter code for every field. MapStruct is the industry standard for this reason.

---

## SECTION 9 — Project-Specific Questions

**Q55. Walk me through the flow when a user places an order.**

The user sends a POST to /api/orders/place with their JWT token. The JwtAuthFilter
extracts the username from the token and sets the authenticated user in the security
context. Spring Security checks that the user has the authenticated role. The
OrderController gets the authenticated user principal and calls
OrderService.placeOrder() with the user ID. OrderService loads the user's cart.
If the cart is empty it throws a BadRequestException. For each cart item it checks
that the product has enough stock, throwing an exception if not. It creates an Order
entity and for each cart item creates an OrderItem storing the product, quantity, and
current price. It decrements product stock for each item. It saves the order and all
order items. Finally it clears the cart. The controller returns 201 Created with the
order details.

**Q56. How does JWT authentication work in this project step by step?**

At login, the user sends email and password to POST /api/auth/login. AuthService
calls AuthenticationManager.authenticate() which internally uses
DaoAuthenticationProvider to load the user by email and compare the BCrypt hashed
passwords. If authentication succeeds, JwtUtil generates a JWT signed with the
secret key. The token is returned to the client. For all subsequent requests the
client adds Authorization: Bearer token in the header. The JwtAuthFilter runs before
every request. It extracts the token from the header, validates the signature and
expiry using JwtUtil, loads the UserDetails from the database, and sets the
Authentication in SecurityContextHolder. From that point, Spring Security treats
the request as authenticated for that user.

**Q57. What happens if you try to add a product to cart that has no stock?**

The addItem method in CartService checks the product's stock before adding. If stock
is zero or the requested quantity exceeds available stock, it throws a
BadRequestException with an appropriate message. This is caught by
GlobalExceptionHandler which returns a 400 response with the error message. The cart
is not modified.

**Q58. How is the category tree endpoint implemented?**

GET /api/categories/tree calls CategoryService.getTree(). The repository queries for
all categories where parent is null — these are the root categories. Because Category
has a @OneToMany children relationship, when the root categories are serialized to
JSON, Hibernate lazily loads their children, and their children's children, building
the full tree recursively. To prevent circular JSON serialization from the parent
back-reference, @JsonIgnoreProperties("parent") is applied to the parent field,
breaking the cycle.

**Q59. How is pagination implemented in the products endpoint?**

The GET /api/products endpoint accepts page, size, and sort as query parameters.
Spring Data JPA automatically reads these and creates a Pageable object. The
ProductRepository extends JpaSpecificationExecutor which has a findAll method that
accepts both a Specification and a Pageable. The result is a Page object that
contains the list of products for that page along with metadata like total elements,
total pages, and whether there is a next or previous page.

**Q60. What is the unique constraint on CartItem and why does it exist?**

CartItem has a unique constraint on the combination of cart_id and product_id. This
means the same product can appear only once in a cart. Without this constraint, a
user could end up with two separate rows for the same product. Instead, when a user
adds a product that is already in the cart, the service checks if a cart item for
that product already exists. If yes, it increments the quantity. If no, it creates
a new cart item. The unique constraint at the database level is a safety net in case
the application logic fails.

---

## SECTION 10 — Spring Boot 4 and Migration

**Q61. What are the major breaking changes in Spring Boot 4?**

Spring Security 7 removed the setUserDetailsService() setter on
DaoAuthenticationProvider — you must now pass it via the constructor. The @MockBean
annotation was moved to a completely new package and renamed to @MockitoBean.
@AutoConfigureMockMvc no longer automatically injects MockMvc as a bean — you
build it manually. springdoc OpenAPI must be version 2.8.x or higher. Hibernate
ORM upgraded from 6 to 7 with query dialect changes. Java minimum target is 21.

**Q62. Why does the DaoAuthenticationProvider fix matter?**

In Spring Security 6, DaoAuthenticationProvider had a no-argument constructor and
you used setUserDetailsService() to inject the service. Spring Security 7 removed
that setter and made UserDetailsService a required constructor argument. This enforces
immutability — once created, the provider's UserDetailsService cannot be changed.
The fix is a one-line change from calling the setter to passing the service in the
constructor, but without knowing this the project will not compile at all.

**Q63. What is the difference between @MockBean and @MockitoBean?**

They do the same thing — create a Mockito mock and register it as a Spring bean
in the test application context, replacing any real bean of that type. The difference
is the package. @MockBean was in org.springframework.boot.test.mock.mockito and was
deleted in Spring Boot 4. @MockitoBean is in
org.springframework.test.context.bean.override.mockito and is the Spring Boot 4
replacement. The behaviour and usage are identical — only the import changes.

---

## SECTION 11 — Tools and Deployment

**Q64. What is Swagger and why is it useful?**

Swagger UI is an automatically generated interactive API documentation page. It reads
your API annotations and generates a web page at /swagger-ui.html where anyone can
see all endpoints, their request/response structure, and even test them directly in
the browser. In this project, @Tag annotates controllers, @Operation annotates
endpoints, and @ApiResponse documents possible response codes. It is especially
useful for frontend developers who consume the API and for testing without needing
Postman.

**Q65. What is Maven and what is a pom.xml?**

Maven is a build tool and dependency manager for Java projects. pom.xml stands for
Project Object Model and is the configuration file that defines the project name,
version, parent, all dependencies, plugins, and build configuration. When you run
mvn spring-boot:run, Maven downloads all dependencies from Maven Central repository,
compiles the source code, and starts the application. When you run mvn test, Maven
compiles and runs all test classes. mvn package creates an executable JAR file.

**Q66. What is the difference between mvn compile and mvn package?**

mvn compile only compiles the main source code and puts class files in the target
directory. mvn package compiles both main and test source code, runs all tests, and
then packages everything into a JAR or WAR file in the target directory. You use
mvn package when deploying to a server or building a Docker image.

**Q67. What is Lombok and what annotations does this project use?**

Lombok is a code generation library that removes boilerplate Java code. You annotate
your class and Lombok generates the code at compile time. This project uses
@Data which generates getters, setters, equals, hashCode, and toString.
@Builder which generates a fluent builder pattern. @NoArgsConstructor and
@AllArgsConstructor which generate constructors. @RequiredArgsConstructor which
generates a constructor for all final fields, used in service and config classes
for constructor injection.

**Q68. What is the difference between Render.com and Vercel deployment?**

Render.com is used for the Spring Boot backend. It runs as a web service on a JVM.
The build command runs mvn package and the start command runs java -jar with the
packaged JAR. Environment variables like DB_URL and JWT_SECRET are set in the
Render dashboard. Vercel is used for the Next.js frontend. It is purpose-built for
JavaScript frameworks and automatically detects Next.js, builds it, and deploys
it globally on a CDN. The NEXT_PUBLIC_API_URL environment variable points the
frontend to the Render backend URL.

---

## SECTION 12 — Scenario and Behavioural Questions

**Q69. Your Spring Boot application is running fine locally but fails on the
production server with a database connection error. How do you debug it?**

First check the application logs on the server for the exact error message — it
will say either connection refused, access denied, or unknown database. Check that
the environment variables DB_URL, DB_USERNAME, and DB_PASSWORD are correctly set
in the production environment. Verify the production database is actually running
and the server can reach it by checking firewall rules and network access. Check
that the database and user exist on the production MySQL instance. Check that
ddl-auto is set to validate or update, not create, to avoid data loss.

**Q70. A user reports that adding a product to the cart is slow. How do you investigate?**

Enable SQL logging in application.properties by setting spring.jpa.show-sql=true.
Reproduce the slow request and count how many SQL queries are executed. If there are
many queries for one action it is likely an N+1 problem — a collection is being
loaded lazily in a loop. Look at the addItem method in CartService and check if
it is loading the full cart or iterating over items without a JOIN FETCH. Add
indexes to the database on frequently queried columns like user_id on Cart and
cart_id on CartItem. Check if the product lookup by ID has an index.

**Q71. How would you add a product review feature to this project?**

Create a Review entity with fields: id, user (ManyToOne), product (ManyToOne),
rating (integer 1 to 5), comment (String), and createdAt. Create a ReviewRepository
extending JpaRepository. Create a ReviewService with methods to add a review and
get reviews for a product. Create a ReviewController with POST /api/products/{id}/reviews
(authenticated) and GET /api/products/{id}/reviews (public). Add validation that a
user can only review a product they have ordered. Add the average rating to the
ProductDto by computing it in the service layer.

**Q72. Why should you never store the JWT secret in the code?**

If the JWT secret is committed to the repository, anyone with access to the code —
including attackers who find the repository — can create valid JWT tokens for any
user, including admin accounts. They can fully impersonate any user. The secret must
be stored in environment variables on the server and injected at runtime via
@Value("${app.jwt.secret}"). Add application-prod.properties to .gitignore to
prevent committing production secrets. Use a long randomly generated secret of at
least 256 bits.
