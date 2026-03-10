# Spring Boot 3.2.4 → 4.0.3 Migration Guide

This document explains every change made to upgrade this project from Spring Boot 3.2.4
to Spring Boot 4.0.3. It covers what was changed, why it was needed, and what error
you would see if you skipped it. Applied to all versions v1.0 through v5.0.

---

## What Is Spring Boot 4?

Spring Boot 4 is a major version upgrade. A major version means breaking changes —
things that used to work simply stop working and need to be updated. Unlike minor
versions (3.1 → 3.2) where you mostly just bump the version number, Spring Boot 4
ships with newer versions of its core dependencies that have removed or renamed APIs.

Key internal upgrades Spring Boot 4 brings:

- Spring Framework upgraded from 6.1 to 6.2
- Spring Security upgraded from 6.2 to 7.0 (breaking changes)
- Hibernate ORM upgraded from 6.4 to 7.2 (breaking changes)
- Jakarta EE upgraded from version 10 to version 11
- Minimum recommended Java version is now 21
- springdoc OpenAPI must be 2.8.x or higher

---

## Change 1 — Spring Boot Parent Version

**File changed:** backend/pom.xml

**What was changed:** The version number in the Spring Boot parent block was updated
from 3.2.4 to 4.0.3.

**Why this is needed:** The parent POM is the master controller of all dependency
versions in a Spring Boot project. Every library Spring Boot includes — Spring
Security, Hibernate, Jackson, Tomcat — gets its version from the parent. Changing
this one number automatically pulls in all the updated compatible versions of every
dependency. Without this change, nothing else in this migration matters.

---

## Change 2 — Java Version Target Updated from 17 to 21

**File changed:** backend/pom.xml (properties section and maven-compiler-plugin section)

**What was changed:** java.version property changed from 17 to 21. The source and
target values in the compiler plugin also changed from 17 to 21.

**Why this is needed:** Our machine runs Java 24. When the project targets Java 17,
the annotation processors — Lombok and MapStruct — use internal Java compiler APIs
that were removed in Java 23. These internal APIs were never meant to be used by
external tools, but older versions of Lombok and MapStruct relied on them anyway.
Targeting Java 21 resolves this because Java 21 is a Long Term Support (LTS) release
and all tools have been updated to work with it properly on Java 24.

**Error without this fix:** The build crashes during compilation with a message about
TypeTag UNKNOWN in LombokProcessor. The project never compiles at all.

---

## Change 3 — MapStruct Version Upgraded from 1.5.5.Final to 1.6.3

**File changed:** backend/pom.xml (properties section)

**What was changed:** The mapstruct.version property was updated from 1.5.5.Final
to 1.6.3.

**Why this is needed:** MapStruct 1.5.5.Final was built for Java 17 and uses the
same internal Java compiler APIs that were removed in Java 23. When the project
runs on Java 24, MapStruct 1.5.5.Final crashes during annotation processing and
cannot generate the mapper implementation classes. Version 1.6.3 was rewritten to
avoid these removed APIs and officially supports Java 21 and Java 24. It is also
compatible with Hibernate 7 which Spring Boot 4 brings.

**Error without this fix:** Compilation fails saying MapStruct processor crashed.
None of the mapper classes get generated and the application cannot start.

---

## Change 4 — springdoc OpenAPI Version Upgraded from 2.4.0 to 2.8.6

**File changed:** backend/pom.xml (springdoc dependency version)

**What was changed:** The springdoc-openapi-starter-webmvc-ui version was updated
from 2.4.0 to 2.8.6.

**Why this is needed:** springdoc 2.4.0 was built for Spring Boot 3 and Spring
Framework 6.1. When Spring Boot 4 upgrades to Spring Framework 6.2, certain internal
Spring APIs that springdoc 2.4.0 relies on have been renamed or removed. When the
application starts, Spring tries to create the Swagger UI bean and crashes because
the methods it expects no longer exist. Version 2.8.6 is built specifically for
Spring Boot 4 and Spring Framework 6.2.

**Error without this fix:** The application fails to start with a BeanCreationException
or NoSuchMethodError pointing at springdoc's OpenAPI bean. The Swagger UI at
/swagger-ui.html is never available.

---

## Change 5 — H2 In-Memory Database Added for Tests

**File changed:** backend/pom.xml (new dependency added)

**What was changed:** A new dependency for H2 database was added with test scope.
Test scope means it is only used during mvn test and is not included in the final
packaged application JAR.

**Why this is needed:** When you run mvn test, Spring Boot's @SpringBootTest loads
the complete application context including the database connection. Without H2, it
tries to connect to the MySQL database using the credentials from
application.properties. If MySQL is not running, or the password is different, or
the database does not exist, every single test fails immediately before even running.
H2 is an in-memory database that runs inside the JVM with no installation needed.
It starts instantly and is destroyed when the tests finish, leaving no data behind.

**Error without this fix:** Every test fails with "Access denied for user root" or
"Connection refused to MySQL on port 3306" before any test logic even runs.

---

## Change 6 — Test application.properties File Created

**File changed:** backend/src/test/resources/application.properties (new file)

**What was changed:** A new application.properties file was created inside the test
resources folder. Spring Boot automatically uses this file instead of the main
application.properties when running tests.

**Why this is needed:** Even with H2 added as a dependency, Spring Boot still uses
the main application.properties during tests unless you override it. The main file
points to MySQL. The test file overrides the datasource URL to point to H2 in-memory,
sets ddl-auto to create-drop so tables are created fresh for each test run and then
dropped afterward, and sets the Hibernate dialect to H2Dialect. It also includes the
JWT secret so auth-related tests can generate valid tokens.

The MODE=MySQL setting in the H2 URL tells H2 to behave like MySQL — same SQL
syntax, same data type handling — so Hibernate's generated queries work correctly
in both test and production environments.

**Error without this fix:** Even with H2 in the classpath, the datasource URL still
points to MySQL and tests fail connecting to the real database.

---

## Change 7 — Surefire Plugin JVM Arguments Added

**File changed:** backend/pom.xml (new plugin configuration added)

**What was changed:** The maven-surefire-plugin (which runs tests) was configured
with two JVM arguments: -XX:+EnableDynamicAgentLoading and
-Dnet.bytebuddy.experimental=true.

**Why this is needed:** Mockito works by generating mock classes at runtime using a
library called Byte Buddy. Byte Buddy does this by loading a Java agent dynamically
into the running JVM. Starting from Java 21, the JVM prints warnings about dynamically
loaded agents, and from Java 23 onwards it can refuse them without explicit permission.
The -XX:+EnableDynamicAgentLoading flag gives explicit JVM permission for this. The
-Dnet.bytebuddy.experimental=true flag tells Byte Buddy to attempt to work on JVM
versions it has not officially certified yet, which includes Java 24 at the time of
this project.

**Error without this fix:** All tests using @Mock or @MockitoBean fail with "Mockito
cannot mock this class" or "Could not modify all classes". This means every service
test and controller test fails even though the code itself is correct.

---

## Change 8 — Lombok Version Tag Removed from Annotation Processor Path

**File changed:** backend/pom.xml (annotationProcessorPaths section inside compiler plugin)

**What was changed:** The version tag inside the Lombok annotation processor path
entry was removed. Previously it referenced ${lombok.version} which is a managed
property.

**Why this is needed:** In Spring Boot 3, ${lombok.version} was a known managed
property in the parent POM. In Spring Boot 4, this property name changed. When Maven
tries to resolve ${lombok.version} in Spring Boot 4, it gets null. Passing a null
version to an annotation processor path causes Maven to crash with an invalid POM
error before compilation even starts. Removing the version tag entirely tells Maven
to use whatever version the Spring Boot 4 parent POM manages automatically, which
is always the correct compatible version.

**Error without this fix:** Maven fails immediately saying the POM for
org.projectlombok:lombok:jar:null is invalid and refusing to build.

---

## Change 9 — DaoAuthenticationProvider Constructor Changed

**File changed:** backend/src/main/java/com/ecom/ecommerce/config/SecurityConfig.java

**What was changed:** The authenticationProvider() method previously created a
DaoAuthenticationProvider using the no-argument constructor and then called
setUserDetailsService() as a separate step. This was changed to pass the
UserDetailsService directly into the constructor instead.

**Why this is needed:** Spring Security 7 (which Spring Boot 4 uses) removed the
setUserDetailsService() setter method from DaoAuthenticationProvider entirely. This
is a deliberate breaking change by the Spring Security team to enforce a cleaner
constructor-based configuration style. Because the setter no longer exists, the old
code simply does not compile. The fix is to use the constructor that accepts a
UserDetailsService as its argument, which Spring Security 7 provides as the
replacement API.

**Error without this fix:** Compilation fails with "cannot find symbol: method
setUserDetailsService(UserDetailsServiceImpl)". The project never builds.

---

## Change 10 — Test Annotations Updated for Spring Boot 4 (v5 only)

**File changed:** backend/src/test/java/.../controller/ProductControllerTest.java

**What was changed:** Three things changed in this test file:

First, the @MockBean annotation and its import from
org.springframework.boot.test.mock.mockito was replaced with @MockitoBean from
org.springframework.test.context.bean.override.mockito.

Second, the @AutoConfigureMockMvc annotation was removed and MockMvc is now built
manually using MockMvcBuilders.webAppContextSetup() instead of being autowired
directly.

Third, the WebApplicationContext is now autowired and passed to the MockMvc builder.
The springSecurity() configurer is applied when building MockMvc so that the full
security filter chain is active during tests.

**Why this is needed:**

For @MockBean: The entire org.springframework.boot.test.mock.mockito package was
deleted in Spring Boot 4. Keeping it means the code does not compile. The replacement
@MockitoBean does exactly the same thing — it creates a Mockito mock and registers
it as a Spring bean — just from a different package.

For @AutoConfigureMockMvc: In Spring Boot 4 with @SpringBootTest, MockMvc is no
longer automatically configured as an injectable bean. The annotation moved and the
auto-configuration behavior changed. The manual approach using
MockMvcBuilders.webAppContextSetup() is the recommended pattern in Spring Boot 4
and gives you explicit control over what filters and configurers are applied.

For springSecurity(): When building MockMvc manually you must explicitly add the
Spring Security filter chain. Without it, security rules are bypassed and your tests
would pass even for requests that should return 403 Forbidden.

**Error without this fix:** Compilation fails with "package
org.springframework.boot.test.mock.mockito does not exist" and "cannot find symbol:
class AutoConfigureMockMvc". All 4 controller tests fail to compile.

---

## Full Change Summary

| Change | File | Applied To |
|--------|------|-----------|
| Spring Boot 3.2.4 → 4.0.3 | pom.xml | v1 to v5 |
| Java target 17 → 21 | pom.xml | v1 to v5 |
| MapStruct 1.5.5.Final → 1.6.3 | pom.xml | v1 to v5 |
| springdoc 2.4.0 → 2.8.6 | pom.xml | v1 to v5 |
| H2 test dependency added | pom.xml | v1 to v5 |
| Test application.properties created | test/resources | v1 to v5 |
| Surefire JVM args added | pom.xml | v1 to v5 |
| Lombok version tag removed | pom.xml | v1 to v5 |
| DaoAuthenticationProvider constructor fix | SecurityConfig.java | v1 to v5 |
| @MockBean → @MockitoBean + MockMvc fix | ProductControllerTest.java | v5 only |

---

## Test Results After Migration

| Version | Compiles | Boots | Endpoints | Tests |
|---------|----------|-------|-----------|-------|
| v1.0 | Yes | Yes | All passed | No tests in this version |
| v2.0 | Yes | Yes | All passed | No tests in this version |
| v3.0 | Yes | Yes | All passed | No tests in this version |
| v4.0 | Yes | Yes | All passed | No tests in this version |
| v5.0 | Yes | Yes | All passed | 14 out of 14 passed |
| v6.0 | Yes | Yes | All passed | 46 out of 46 passed |
| v7.0 | Yes | Yes | All passed | 46 out of 46 passed |

---

## Key Lesson for Students

When you move between major versions of any framework — Spring Boot, Django, Rails,
Angular — you will always face breaking changes. The process is always the same:

1. Read the official migration guide for that version
2. Upgrade one dependency at a time if possible
3. Fix compile errors first before worrying about runtime errors
4. Run tests after each fix to confirm you have not broken something else
5. Document every change so the next developer understands what happened and why

The changes in this migration are exactly the type of questions asked in Java
developer interviews at companies like Infosys, TCS, Wipro, and product companies.
Being able to explain why Spring Security 7 changed DaoAuthenticationProvider
or why @MockBean was replaced shows you understand the framework, not just how to
use it.
