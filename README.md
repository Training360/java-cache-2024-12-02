# Cache megoldások

* Cache: adattár, mely egy kérésre a választ gyorsabban adja vissza, azáltal, hogy már előzetesen kiszámolásra került, pl. egy korábbi kérés válaszaként
* Cache hit: találat a cache-ben
* Cache miss: nincs találat
* Hit rate/hit ratio: cache hit aránya az összes kéréshez képest

"There are only two hard things in Computer Science: cache invalidation and naming things." - Phil Karlton

## Források

### Building Microservices: Designing Fine-Grained Systems

* Nagyobb teljesítmény
  * Kiküszöböli a hálózati késleltetést
  * Bonyolult join eredményét nem kell kivárni
* Skálázhatóság
  * Leveszi a terhelést a háttérrendszerekről, adatbázisról, stb.
* Hibatűrés
  * Nem előállítható adat betölthető a cache-ből

Probléma: invalidálás

* Cache helye:
  * Client side cache
  * Server side
    * Black box, változtatható implementáció, de a hálózati forgalmat nem csökkenti

* Cache formája:
  * Not shared cache
  * Shared cache - hálózati overhead

* Reading strategy
  * Cache Aside (Lazy Loading)
  * Cache-through (Proxy between client and server)
  * Read Ahead (Prefetching)

* Writing policies/strategy
  * Write-through: egyszerre
  * Write-behind: először a cache-be, mint egy puffer
  * Write-Around: írás nem módosítja a cache-t, csak olvasás

* Invalidation, replacement policy
  * TTL
  * LRU
  * Conditional - ETag
  * Notification based

"Coming back to the famous quote from Knuth earlier, premature optimization can
cause issues. Caching adds complexity, and we want to add as little complexity as
possible. The ideal number of places to cache is zero. Anything else should be an
optimization you have to make—but be aware of the complexity it can bring."

[Architecture and Design — Cache Strategies for Distributed Applications](https://anjireddy-kata.medium.com/architecture-and-design-cache-strategies-for-distributed-applications-1185e0efd74f)

### Microservices Patterns

* API gateway
* REST API client, API composer, BFF (pl. GraphQL)

### Beyond the 12-Factor App

* Disposability: alkalmazás indításkor a cache feltöltése ellentmond ennek, érdemes backing service-be szervezni

### Cloud Native Patterns

* Minden microservice önálló
* Elosztottság, az adat is elosztottan helyezkedik el
* Hálózati késleltetés
* _Every microservice need cache_
* Event sourcing

## Perzisztens réteg

```sh
docker run -d -e POSTGRES_DB=employees -e POSTGRES_USER=employees  -e POSTGRES_PASSWORD=employees  -p 5432:5432  --name employees-postgres postgres
```

### First level cache

[JPA több one-to-many kapcsolat](https://www.jtechlog.hu/2013/03/17/jpa-tobb-one-to-many-kapcsolat.html)

### Second level cache

[Hibernate User Guide - Caching](https://docs.jboss.org/hibernate/stable/orm/userguide/html_single/Hibernate_User_Guide.html#caching)

```sh
docker run -d -p 6379:6379 --name employees-redis redis
```

```xml
<dependency>
    <groupId>org.redisson</groupId>
    <artifactId>redisson-hibernate-6</artifactId>
    <version>3.39.0</version>
</dependency>
```

```yaml
spring:
  jpa:
    properties:
      hibernate:
        cache:
          use_query_cache: true
          use_second_level_cache: true
          factory_class: org.redisson.hibernate.RedissonRegionFactory
          redisson:
            fallback: true
            config: redisson.yaml
```

```yaml
singleServerConfig:
  address: "redis://localhost:6379"
```

```java
@org.hibernate.annotations.Cache(region = "employeeCache", usage = CacheConcurrencyStrategy.READ_WRITE)
public class Employee {
    // ...
}

```java
@QueryHints(
        @QueryHint(name = "org.hibernate.cacheable", value = "true")
    )
List<EmployeeResource> findAllResources();
```

```sh
redis-cli ping
redis-cli --scan
redis-cli get employee::1  
redis-cli get "employees::SimpleKey []"
```

IDEA DataSource

## Cache abstraction

```java
@EnableCaching
```

```java
@Configuration(proxyBeanMethods = false)
public class RedisConfiguration {

    @Bean
    public RedisCacheConfiguration cacheConfiguration() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.activateDefaultTyping(objectMapper.getPolymorphicTypeValidator(), ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY);

        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10))
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer(objectMapper))
                );
    }

}
```

```yaml
spring
  cache:
    cache-names: employee
```

```java
@Cacheable("employee")
public EmployeeDto findEmployeeById(long id) {
  // ...
}
```

```java
@CacheEvict(value = "employees", allEntries = true)
// @CacheEvict(value = "employee", key = "#id")
@CachePut(value = "employee", key = "#id")
public EmployeeDto updateEmployee(long id, UpdateEmployeeCommand command) {
  // ...
}
```

```java
@CachePut(value = "employee", key = "#result.id")
public EmployeeDto createEmployee(CreateEmployeeCommand command) {
  // ...
}
```

```yaml
spring:
  cache:
    redis:
      enable-statistics: true
logging:
  level:
    org.springframework.cache: trace
```

```http
###
GET http://localhost:8082/actuator/caches
```

```http
###
GET http://localhost:8082/actuator/caches/employee

```http
###
GET http://localhost:8082/actuator/metrics
```

```http
###
GET http://localhost:8082/actuator/metrics
```

```http
###
GET http://localhost:8082/actuator/metrics/cache.gets?tag=name:employees-frontend&tag=result:hit
```

## HTTP cache

### Cache control és ETag

```java
return ResponseEntity
        .ok()
        .cacheControl(CacheControl.maxAge(1, TimeUnit.HOURS))
        // .eTag(Integer.toString(employeeDto.getVersion()))
        .eTag(Integer.toString(employeeDto.hashCode()))
        .body(employeeDto);
```

## Spring Cloud gateway

[LocalResponseCache](https://docs.spring.io/spring-cloud-gateway/reference/spring-cloud-gateway/gatewayfilter-factories/local-cache-response-filter.html)

[Rework LocalResponseCache into a general ResponseCache to support Caffiene and Redis CacheManagers #3145](https://github.com/spring-cloud/spring-cloud-gateway/pull/3145)

Minta:

* Amennyiben a háttérrendszer hívása nem sikerül (timeout, retry után), adja vissza az értéket cache-ből

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-cache</artifactId>
</dependency>

<dependency>
    <groupId>com.github.ben-manes.caffeine</groupId>
    <artifactId>caffeine</artifactId>
</dependency>
```

```yaml
server:
  port: 8081
spring:
  application:
    name: employees-gateway
  cloud:
    gateway:
      filter:
        local-response-cache:
          enabled: true
      routes:
        - id: employees
          uri: http://localhost:8080/
          predicates:
            - Path=/api/employees/**
          filters:
            - LocalResponseCache=30m,500MB
```

## Client

### Cache

`org.springframework.boot:spring-boot-starter-data-redis`

`@EnableCaching`

`RedisConfiguration`

`cache-names`

```java
@Cacheable("employees-frontend")
```

### ETag

```java
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EtaggedValue<T> {
    private String etag;
    private T value;
}
```

```java
@Service
@AllArgsConstructor
@Slf4j
public class CacheableRestClientEmployeesClient implements EmployeesClient {

    private final RestClient restClient;

    private final CacheManager cacheManager;

    @Override
    public List<Employee> listEmployees() {
        var wrapper = cacheManager.getCache("employees-etag").get("employees");
        EtaggedValue<List<Employee >> value = null;
        if (wrapper != null) {
            value = (EtaggedValue<List<Employee >>) wrapper.get();
        }

        var request = restClient
                .get()
                .uri("/api/employees");
        if (value != null) {
            request
                    .header("If-None-Match", "%s".formatted(value.getEtag()));
        }
        ResponseEntity<List<Employee>> response = request.retrieve()
                .toEntity(new ParameterizedTypeReference<List<Employee>>() {});

        if (response.getStatusCode() == HttpStatus.NOT_MODIFIED) {
            log.info("Not modified");
            return value.getValue();
        }
        else {
            log.info("Modified");
            cacheManager.getCache("employees-etag").put("employees",
                    new EtaggedValue<>(response.getHeaders().getETag(), response.getBody()));
            return response.getBody();
        }
    }

    @Override
    public Employee createEmployee(Employee employee) {
        return restClient.post().uri("/api/employees").body(employee).retrieve().body(Employee.class);
    }
}
```

REST Client Request Interceptor

[GraphQL](https://www.graphql-java.com/documentation/batching/#per-request-data-loaders)

## Cache evict

[Spring Cloud Bus](https://spring.io/projects/spring-cloud-bus)

## Spring Session

(Spring Session)[https://spring.io/projects/spring-session]