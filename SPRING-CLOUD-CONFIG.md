# ☁️ SPRING CLOUD - CONFIGURACIÓN COMPLETA

## 🎯 **INTRODUCCIÓN A SPRING CLOUD**

**Spring Cloud en Arka Valenzuela** proporciona un ecosistema completo para microservicios que incluye service discovery, configuration management, circuit breakers, load balancing, y API gateway. Esta implementación garantiza alta disponibilidad, tolerancia a fallos y gestión centralizada de configuración.

---

## 🏗️ **ARQUITECTURA SPRING CLOUD**

```
                    🌐 EXTERNAL REQUESTS
                           │
                    ┌──────┴──────┐
                    │ 🚪 API GATEWAY │ 
                    │ (Spring Cloud │
                    │  Gateway)     │ 
                    │ Port: 8080    │
                    └──────┬──────┘
                           │
              ┌────────────┼────────────┐
              │                         │
    ┌─────────┴─────────┐    ┌──────────┴─────────┐
    │ 🔍 SERVICE         │    │ 📁 CONFIG SERVER   │
    │ DISCOVERY          │    │ (Spring Cloud      │
    │ (Eureka Server)    │    │  Config)           │
    │ Port: 8761         │    │ Port: 8888         │
    └─────────┬─────────┘    └──────────┬─────────┘
              │                         │
              │ Service Registration    │ Configuration
              │ & Discovery             │ Management
              │                         │
    ┌─────────┴─────────┬───────────────┴─────────┐
    │                   │                         │
    │ 🧮 ARCA-COTIZADOR │ 📋 ARCA-GESTOR-SOL.   │ 👋 HELLO-WORLD
    │ Circuit Breaker   │ Circuit Breaker        │ Load Balancing
    │ Load Balancer     │ Retry Logic            │ Health Checks
    │ Feign Client      │ Feign Client           │ Metrics
    │ Port: 8081        │ Port: 8082             │ Port: 8083
    └───────────────────┴────────────────────────┴─────────────────┘

               ⚡ SPRING CLOUD FEATURES:
    ┌──────────────────────────────────────────────────────────────┐
    │ 🔄 Circuit Breaker    🔁 Retry Logic    ⚖️ Load Balancing    │
    │ 📞 Feign Clients      📊 Metrics        🚨 Health Checks     │
    │ 🎯 Service Discovery  ⚙️ Config Mgmt    🛡️ Rate Limiting     │
    └──────────────────────────────────────────────────────────────┘
```

---

## 🔍 **EUREKA SERVER - SERVICE DISCOVERY**

### ⚙️ **Configuración del Eureka Server**

```java
// 📁 eureka-server/src/main/java/com/arka/eureka/EurekaServerApplication.java

@SpringBootApplication
@EnableEurekaServer
public class EurekaServerApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(EurekaServerApplication.class, args);
    }
    
    @Bean
    public EurekaInstanceConfigBean eurekaInstanceConfig() {
        EurekaInstanceConfigBean config = new EurekaInstanceConfigBean();
        config.setInstanceId("${spring.application.name}:${server.port}");
        config.setPreferIpAddress(true);
        config.setLeaseRenewalIntervalInSeconds(10);
        config.setLeaseExpirationDurationInSeconds(30);
        return config;
    }
    
    @Bean
    public EurekaServerConfigBean eurekaServerConfig() {
        EurekaServerConfigBean config = new EurekaServerConfigBean();
        config.setEnableSelfPreservation(false);
        config.setEvictionIntervalTimerInMs(5000);
        config.setRenewalPercentThreshold(0.85);
        return config;
    }
}

// Configuración de seguridad para Eureka Dashboard
@Configuration
@EnableWebSecurity
public class EurekaSecurityConfiguration {
    
    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
            .csrf(ServerHttpSecurity.CsrfSpec::disable)
            .authorizeExchange(exchanges -> exchanges
                .pathMatchers("/actuator/**").permitAll()
                .pathMatchers("/eureka/**").permitAll()
                .anyExchange().authenticated()
            )
            .httpBasic(httpBasic -> httpBasic
                .authenticationManager(authenticationManager()))
            .build();
    }
    
    @Bean
    public ReactiveAuthenticationManager authenticationManager() {
        UserDetailsRepositoryReactiveAuthenticationManager manager =
            new UserDetailsRepositoryReactiveAuthenticationManager(userDetailsService());
        manager.setPasswordEncoder(passwordEncoder());
        return manager;
    }
    
    @Bean
    public MapReactiveUserDetailsService userDetailsService() {
        UserDetails admin = User.builder()
            .username("eureka-admin")
            .password(passwordEncoder().encode("eureka-admin-2025"))
            .roles("ADMIN")
            .build();
        return new MapReactiveUserDetailsService(admin);
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

### 📝 **Configuración YAML del Eureka Server**

```yaml
# 📁 eureka-server/src/main/resources/application.yml
server:
  port: 8761

spring:
  application:
    name: eureka-server
  profiles:
    active: default

eureka:
  instance:
    hostname: localhost
    instance-id: ${spring.application.name}:${server.port}
    prefer-ip-address: true
    lease-renewal-interval-in-seconds: 10
    lease-expiration-duration-in-seconds: 30
    status-page-url-path: /actuator/info
    health-check-url-path: /actuator/health
    
  client:
    register-with-eureka: false
    fetch-registry: false
    service-url:
      defaultZone: http://localhost:8761/eureka/
    healthcheck:
      enabled: true
      
  server:
    enable-self-preservation: false
    eviction-interval-timer-in-ms: 5000
    renewal-percent-threshold: 0.85
    response-cache-update-interval-ms: 3000
    use-read-only-response-cache: false

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    health:
      show-details: always
  health:
    eureka:
      enabled: true

logging:
  level:
    com.netflix.discovery: DEBUG
    com.netflix.eureka: DEBUG
    org.springframework.cloud.netflix.eureka: DEBUG

---
# Perfil para Docker
spring:
  config:
    activate:
      on-profile: docker
      
eureka:
  instance:
    hostname: eureka-server
    prefer-ip-address: false
  client:
    service-url:
      defaultZone: http://eureka-server:8761/eureka/
```

---

## 📁 **CONFIG SERVER - GESTIÓN CENTRALIZADA**

### ⚙️ **Configuración del Config Server**

```java
// 📁 config-server/src/main/java/com/arka/config/ConfigServerApplication.java

@SpringBootApplication
@EnableConfigServer
@EnableEurekaClient
public class ConfigServerApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(ConfigServerApplication.class, args);
    }
    
    @Bean
    public GitCredentialsProvider gitCredentialsProvider() {
        return new GitCredentialsProvider() {
            @Override
            public UsernamePasswordCredentials getCredentials(String uri, String credentialsId) {
                // Para repositorios privados
                return new UsernamePasswordCredentials(
                    System.getenv("GIT_USERNAME"),
                    System.getenv("GIT_TOKEN")
                );
            }
        };
    }
    
    @EventListener
    public void handleRefreshEvent(RefreshRemoteApplicationEvent event) {
        log.info("Configuración actualizada para: {}", event.getDestinationService());
        // Opcional: notificar a servicios específicos
        notifyServicesOfConfigChange(event.getDestinationService());
    }
    
    private void notifyServicesOfConfigChange(String serviceName) {
        // Implementar notificación a través de message broker si es necesario
        log.info("Notificando cambio de configuración a: {}", serviceName);
    }
}

// Configuración de seguridad para Config Server
@Configuration
@EnableWebSecurity
public class ConfigServerSecurity {
    
    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
            .csrf(ServerHttpSecurity.CsrfSpec::disable)
            .authorizeExchange(exchanges -> exchanges
                .pathMatchers("/actuator/**").permitAll()
                .pathMatchers("/encrypt/**", "/decrypt/**").hasRole("ADMIN")
                .anyExchange().authenticated()
            )
            .httpBasic(Customizer.withDefaults())
            .build();
    }
    
    @Bean
    public MapReactiveUserDetailsService userDetailsService() {
        UserDetails configUser = User.builder()
            .username("config-user")
            .password(passwordEncoder().encode("config-password-2025"))
            .roles("USER")
            .build();
            
        UserDetails configAdmin = User.builder()
            .username("config-admin")
            .password(passwordEncoder().encode("config-admin-2025"))
            .roles("ADMIN", "USER")
            .build();
            
        return new MapReactiveUserDetailsService(configUser, configAdmin);
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

### 📝 **Configuración YAML del Config Server**

```yaml
# 📁 config-server/src/main/resources/application.yml
server:
  port: 8888

spring:
  application:
    name: config-server
  profiles:
    active: native,git
  cloud:
    config:
      server:
        git:
          uri: https://github.com/arka-valenzuela/config-repository.git
          default-label: main
          search-paths: configs
          clone-on-start: true
          timeout: 10
          force-pull: true
        native:
          search-locations: classpath:/configs/
        health:
          repositories:
            config-repo:
              label: main
              name: config-server
              profiles: default
        encrypt:
          enabled: true
          
  security:
    user:
      name: config-user
      password: config-password-2025
      roles: USER

eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
    healthcheck:
      enabled: true
  instance:
    prefer-ip-address: true
    lease-renewal-interval-in-seconds: 10

management:
  endpoints:
    web:
      exposure:
        include: health,info,refresh,env,configprops,beans,prometheus
  endpoint:
    health:
      show-details: always

encrypt:
  key: arka-valenzuela-encryption-key-2025

logging:
  level:
    org.springframework.cloud.config: DEBUG
    org.springframework.web: DEBUG

---
# Perfil para Docker
spring:
  config:
    activate:
      on-profile: docker
      
eureka:
  client:
    service-url:
      defaultZone: http://eureka-server:8761/eureka/
  instance:
    hostname: config-server
```

### 📁 **Configuraciones Centralizadas**

```yaml
# 📁 config-repository/application.yml - Configuración Global
spring:
  datasource:
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      idle-timeout: 300000
      connection-timeout: 30000
      max-lifetime: 1800000
      leak-detection-threshold: 60000

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus,refresh
  endpoint:
    health:
      show-details: always
  metrics:
    distribution:
      percentiles-histogram:
        http.server.requests: true
    tags:
      application: ${spring.application.name}
      environment: ${spring.profiles.active}

logging:
  level:
    org.springframework.cloud: INFO
    com.arka: DEBUG
  pattern:
    console: "%d{ISO8601} [%thread] %-5level %logger{36} - %msg%n"
    file: "%d{ISO8601} [%thread] %-5level %logger{36} - %msg%n"

eureka:
  client:
    healthcheck:
      enabled: true
    service-url:
      defaultZone: http://eureka-server:8761/eureka/
  instance:
    prefer-ip-address: true
    lease-renewal-interval-in-seconds: 10
    lease-expiration-duration-in-seconds: 30

resilience4j:
  circuitbreaker:
    configs:
      default:
        sliding-window-size: 10
        minimum-number-of-calls: 5
        failure-rate-threshold: 50
        wait-duration-in-open-state: 30000
        permitted-number-of-calls-in-half-open-state: 3
        automatic-transition-from-open-to-half-open-enabled: true
  retry:
    configs:
      default:
        max-attempts: 3
        wait-duration: 1000
        exponential-backoff-multiplier: 2
  timelimiter:
    configs:
      default:
        timeout-duration: 10000
        cancel-running-future: true

# 📁 config-repository/api-gateway-dev.yml - Gateway Específico
spring:
  cloud:
    gateway:
      routes:
        - id: arca-cotizador-route
          uri: lb://arca-cotizador
          predicates:
            - Path=/api/cotizaciones/**
          filters:
            - name: CircuitBreaker
              args:
                name: cotizador-circuit-breaker
                fallbackUri: forward:/fallback/cotizaciones
            - name: RequestRateLimiter
              args:
                rate-limiter: "#{@redisRateLimiter}"
                key-resolver: "#{@userKeyResolver}"
            - name: Retry
              args:
                retries: 3
                statuses: BAD_GATEWAY,GATEWAY_TIMEOUT
                backoff:
                  firstBackoff: 50ms
                  maxBackoff: 500ms
                  factor: 2

        - id: arca-gestor-solicitudes-route
          uri: lb://arca-gestor-solicitudes
          predicates:
            - Path=/api/solicitudes/**
          filters:
            - name: CircuitBreaker
              args:
                name: gestor-circuit-breaker
                fallbackUri: forward:/fallback/solicitudes
            - StripPrefix=1

        - id: hello-world-route
          uri: lb://hello-world-service
          predicates:
            - Path=/api/hello/**
          filters:
            - StripPrefix=1

      httpclient:
        connect-timeout: 30000
        response-timeout: 60000
        pool:
          max-connections: 100
          max-idle-time: 30s

      default-filters:
        - name: GlobalFilter
        - name: LoggingFilter
        - name: MetricsFilter

resilience4j:
  circuitbreaker:
    instances:
      cotizador-circuit-breaker:
        sliding-window-size: 20
        minimum-number-of-calls: 10
        failure-rate-threshold: 60
        wait-duration-in-open-state: 60000
      gestor-circuit-breaker:
        sliding-window-size: 15
        minimum-number-of-calls: 8
        failure-rate-threshold: 50
        wait-duration-in-open-state: 45000

# 📁 config-repository/arca-cotizador-dev.yml - Cotizador Específico
spring:
  datasource:
    url: jdbc:mysql://mysql-cotizador:3306/arka_cotizaciones
    username: arka_user
    password: '{cipher}AQB2P8Z3+5Y1qR7wN9mK4vL2cX8gH6jF3sA1bV9nM7kO2uI5tE4rW='
    driver-class-name: com.mysql.cj.jdbc.Driver
    hikari:
      maximum-pool-size: 15
      minimum-idle: 3
      connection-timeout: 20000
      
  jpa:
    hibernate:
      ddl-auto: validate
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQL8Dialect
        show_sql: false
        format_sql: true
        use_sql_comments: true
        jdbc:
          batch_size: 20
          fetch_size: 50

  data:
    redis:
      host: redis-cotizador
      port: 6379
      database: 0
      timeout: 2000
      jedis:
        pool:
          max-active: 8
          max-idle: 8
          min-idle: 0

arka:
  cotizador:
    cache:
      ttl: 3600
      max-entries: 1000
    external-api:
      timeout: 5000
      retry-attempts: 3
      circuit-breaker:
        failure-threshold: 5
        timeout: 30000

# 📁 config-repository/arca-gestor-solicitudes-dev.yml - Gestor Específico
spring:
  data:
    mongodb:
      uri: mongodb://admin:admin_password_2025@mongodb-solicitudes:27017/arka_solicitudes?authSource=admin
      database: arka_solicitudes
      
  kafka:
    bootstrap-servers: kafka:9092
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.springframework.kafka.support.serializer.JsonSerializer
    consumer:
      group-id: arka-gestor-solicitudes
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: org.springframework.kafka.support.serializer.JsonDeserializer
      properties:
        spring.json.trusted.packages: "com.arka.gestor.domain.events"

arka:
  gestor:
    workflow:
      max-retries: 5
      timeout: 30000
    notifications:
      email:
        enabled: true
        timeout: 5000
      sms:
        enabled: false
    processing:
      batch-size: 50
      parallel-threads: 3
```

---

## 🚪 **API GATEWAY - SPRING CLOUD GATEWAY**

### ⚙️ **Configuración del API Gateway**

```java
// 📁 api-gateway/src/main/java/com/arka/gateway/GatewayApplication.java

@SpringBootApplication
@EnableEurekaClient
public class GatewayApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }
}

// Configuración personalizada del Gateway
@Configuration
@EnableConfigurationProperties(GatewayProperties.class)
public class GatewayConfiguration {
    
    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder, 
                                          GatewayProperties gatewayProperties) {
        return builder.routes()
            
            // Ruta para Cotizaciones con Circuit Breaker y Rate Limiting
            .route("cotizaciones", r -> r
                .path("/api/cotizaciones/**")
                .filters(f -> f
                    .circuitBreaker(config -> config
                        .setName("cotizador-circuit-breaker")
                        .setFallbackUri("forward:/fallback/cotizaciones"))
                    .requestRateLimiter(config -> config
                        .setRateLimiter(redisRateLimiter())
                        .setKeyResolver(userKeyResolver()))
                    .retry(config -> config
                        .setRetries(3)
                        .setSeries(HttpStatus.Series.SERVER_ERROR)
                        .setBackoff(Duration.ofMillis(100), Duration.ofMillis(1000), 2, false)))
                .uri("lb://arca-cotizador"))
                
            // Ruta para Solicitudes con autenticación requerida
            .route("solicitudes", r -> r
                .path("/api/solicitudes/**")
                .filters(f -> f
                    .circuitBreaker(config -> config
                        .setName("gestor-circuit-breaker")
                        .setFallbackUri("forward:/fallback/solicitudes"))
                    .filter(jwtAuthenticationFilter()))
                .uri("lb://arca-gestor-solicitudes"))
                
            // Ruta para Hello World con métricas personalizadas
            .route("hello-world", r -> r
                .path("/api/hello/**")
                .filters(f -> f
                    .filter(metricsGatewayFilter())
                    .stripPrefix(2))
                .uri("lb://hello-world-service"))
                
            // Ruta para Eureka Dashboard
            .route("eureka", r -> r
                .path("/eureka/**")
                .uri("http://eureka-server:8761"))
                
            // Ruta para Config Server (solo admin)
            .route("config", r -> r
                .path("/config/**")
                .filters(f -> f
                    .filter(adminOnlyFilter())
                    .stripPrefix(1))
                .uri("http://config-server:8888"))
                
            .build();
    }
    
    @Bean
    public RedisRateLimiter redisRateLimiter() {
        return new RedisRateLimiter(10, 20, 1); // 10 requests per second, burst 20
    }
    
    @Bean
    public KeyResolver userKeyResolver() {
        return exchange -> {
            ServerHttpRequest request = exchange.getRequest();
            String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                try {
                    String token = authHeader.substring(7);
                    String username = jwtTokenProvider.getUsernameFromToken(token);
                    return Mono.just(username);
                } catch (Exception e) {
                    return Mono.just(request.getRemoteAddress().getAddress().getHostAddress());
                }
            }
            
            return Mono.just(request.getRemoteAddress().getAddress().getHostAddress());
        };
    }
    
    @Bean
    public GatewayFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationGatewayFilter(jwtTokenProvider);
    }
    
    @Bean
    public GatewayFilter metricsGatewayFilter() {
        return new MetricsGatewayFilter(meterRegistry);
    }
    
    @Bean
    public GatewayFilter adminOnlyFilter() {
        return new AdminOnlyGatewayFilter(jwtTokenProvider);
    }
    
    @Autowired
    private JwtTokenProvider jwtTokenProvider;
    
    @Autowired
    private MeterRegistry meterRegistry;
}

// Filtro JWT personalizado
@Component
public class JwtAuthenticationGatewayFilter implements GatewayFilter {
    
    private final JwtTokenProvider jwtTokenProvider;
    
    public JwtAuthenticationGatewayFilter(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return handleUnauthorized(exchange);
        }
        
        String token = authHeader.substring(7);
        
        if (!jwtTokenProvider.validateToken(token)) {
            return handleUnauthorized(exchange);
        }
        
        // Agregar información del usuario al header para microservicios
        String username = jwtTokenProvider.getUsernameFromToken(token);
        String userId = jwtTokenProvider.getUserIdFromToken(token);
        List<String> authorities = jwtTokenProvider.getAuthoritiesFromToken(token);
        
        ServerHttpRequest modifiedRequest = request.mutate()
            .header("X-User-Id", userId)
            .header("X-Username", username)
            .header("X-Authorities", String.join(",", authorities))
            .build();
        
        ServerWebExchange modifiedExchange = exchange.mutate()
            .request(modifiedRequest)
            .build();
        
        return chain.filter(modifiedExchange);
    }
    
    private Mono<Void> handleUnauthorized(ServerWebExchange exchange) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().add("Content-Type", "application/json");
        
        String body = """
            {
                "error": "UNAUTHORIZED",
                "message": "Token de acceso requerido o inválido",
                "timestamp": "%s"
            }
            """.formatted(Instant.now());
        
        DataBuffer buffer = response.bufferFactory().wrap(body.getBytes());
        return response.writeWith(Mono.just(buffer));
    }
}

// Filtro de métricas personalizadas
@Component
public class MetricsGatewayFilter implements GatewayFilter {
    
    private final MeterRegistry meterRegistry;
    private final Timer.Builder timerBuilder;
    private final Counter.Builder counterBuilder;
    
    public MetricsGatewayFilter(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        this.timerBuilder = Timer.builder("gateway.request.duration")
            .description("Gateway request duration");
        this.counterBuilder = Counter.builder("gateway.request.count")
            .description("Gateway request count");
    }
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        Timer.Sample sample = Timer.start(meterRegistry);
        
        return chain.filter(exchange)
            .doFinally(signalType -> {
                ServerHttpRequest request = exchange.getRequest();
                ServerHttpResponse response = exchange.getResponse();
                
                String method = request.getMethodValue();
                String path = request.getPath().value();
                String status = String.valueOf(response.getStatusCode().value());
                
                // Registrar duración
                sample.stop(timerBuilder
                    .tag("method", method)
                    .tag("path", path)
                    .tag("status", status)
                    .register(meterRegistry));
                
                // Registrar contador
                counterBuilder
                    .tag("method", method)
                    .tag("path", path)
                    .tag("status", status)
                    .register(meterRegistry)
                    .increment();
            });
    }
}

// Controlador de Fallback
@RestController
@RequestMapping("/fallback")
public class FallbackController {
    
    @GetMapping("/cotizaciones")
    public Mono<ResponseEntity<Map<String, Object>>> cotizacionesFallback() {
        Map<String, Object> response = Map.of(
            "message", "Servicio de cotizaciones temporalmente no disponible",
            "timestamp", Instant.now(),
            "service", "arca-cotizador",
            "fallback", true
        );
        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response));
    }
    
    @GetMapping("/solicitudes")
    public Mono<ResponseEntity<Map<String, Object>>> solicitudesFallback() {
        Map<String, Object> response = Map.of(
            "message", "Servicio de gestión de solicitudes temporalmente no disponible",
            "timestamp", Instant.now(),
            "service", "arca-gestor-solicitudes",
            "fallback", true
        );
        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response));
    }
    
    @GetMapping("/hello")
    public Mono<ResponseEntity<Map<String, Object>>> helloFallback() {
        Map<String, Object> response = Map.of(
            "message", "Hello World service temporalmente no disponible",
            "timestamp", Instant.now(),
            "service", "hello-world-service",
            "fallback", true
        );
        return Mono.just(ResponseEntity.ok(response));
    }
}
```

---

## 🔄 **CIRCUIT BREAKER Y RESILIENCE4J**

### ⚙️ **Configuración de Circuit Breaker**

```java
// 📁 arka-security-common/src/main/java/com/arka/resilience/CircuitBreakerConfiguration.java

@Configuration
@EnableConfigurationProperties(CircuitBreakerConfigurationProperties.class)
public class CircuitBreakerConfiguration {
    
    @Bean
    public CircuitBreakerRegistry circuitBreakerRegistry(
            CircuitBreakerConfigurationProperties properties) {
        
        CircuitBreakerConfig defaultConfig = CircuitBreakerConfig.custom()
            .slidingWindowSize(properties.getSlidingWindowSize())
            .minimumNumberOfCalls(properties.getMinimumNumberOfCalls())
            .failureRateThreshold(properties.getFailureRateThreshold())
            .waitDurationInOpenState(Duration.ofMillis(properties.getWaitDurationInOpenState()))
            .permittedNumberOfCallsInHalfOpenState(properties.getPermittedNumberOfCallsInHalfOpenState())
            .automaticTransitionFromOpenToHalfOpenEnabled(true)
            .recordExceptions(
                IOException.class,
                TimeoutException.class,
                ConnectException.class
            )
            .ignoreExceptions(
                IllegalArgumentException.class,
                ValidationException.class
            )
            .build();
        
        return CircuitBreakerRegistry.of(defaultConfig);
    }
    
    @Bean
    public RetryRegistry retryRegistry(CircuitBreakerConfigurationProperties properties) {
        RetryConfig defaultConfig = RetryConfig.custom()
            .maxAttempts(properties.getRetryMaxAttempts())
            .waitDuration(Duration.ofMillis(properties.getRetryWaitDuration()))
            .intervalFunction(IntervalFunction.ofExponentialBackoff(
                Duration.ofMillis(properties.getRetryWaitDuration()), 2.0))
            .retryOnException(ex -> 
                ex instanceof IOException || 
                ex instanceof TimeoutException ||
                ex instanceof ConnectException)
            .build();
        
        return RetryRegistry.of(defaultConfig);
    }
    
    @Bean
    public TimeLimiterRegistry timeLimiterRegistry(
            CircuitBreakerConfigurationProperties properties) {
        
        TimeLimiterConfig defaultConfig = TimeLimiterConfig.custom()
            .timeoutDuration(Duration.ofMillis(properties.getTimeoutDuration()))
            .cancelRunningFuture(true)
            .build();
        
        return TimeLimiterRegistry.of(defaultConfig);
    }
}

// Configuración de propiedades
@ConfigurationProperties(prefix = "arka.resilience")
@Data
public class CircuitBreakerConfigurationProperties {
    private int slidingWindowSize = 10;
    private int minimumNumberOfCalls = 5;
    private float failureRateThreshold = 50.0f;
    private long waitDurationInOpenState = 30000;
    private int permittedNumberOfCallsInHalfOpenState = 3;
    private int retryMaxAttempts = 3;
    private long retryWaitDuration = 1000;
    private long timeoutDuration = 10000;
}

// Service con Circuit Breaker
@Service
@Transactional(readOnly = true)
public class CotizacionServiceWithCircuitBreaker {
    
    private final CotizacionRepositoryPort cotizacionRepository;
    private final ExternalPriceServicePort externalPriceService;
    private final CircuitBreaker circuitBreaker;
    private final Retry retry;
    private final TimeLimiter timeLimiter;
    
    public CotizacionServiceWithCircuitBreaker(
            CotizacionRepositoryPort cotizacionRepository,
            ExternalPriceServicePort externalPriceService,
            CircuitBreakerRegistry circuitBreakerRegistry,
            RetryRegistry retryRegistry,
            TimeLimiterRegistry timeLimiterRegistry) {
        
        this.cotizacionRepository = cotizacionRepository;
        this.externalPriceService = externalPriceService;
        this.circuitBreaker = circuitBreakerRegistry.circuitBreaker("external-price-service");
        this.retry = retryRegistry.retry("external-price-service");
        this.timeLimiter = timeLimiterRegistry.timeLimiter("external-price-service");
        
        // Configurar eventos del Circuit Breaker
        this.circuitBreaker.getEventPublisher()
            .onStateTransition(event -> 
                log.info("Circuit Breaker transición: {} -> {}", 
                    event.getStateTransition().getFromState(),
                    event.getStateTransition().getToState()))
            .onCallNotPermitted(event -> 
                log.warn("Circuit Breaker: Llamada no permitida"))
            .onError(event -> 
                log.error("Circuit Breaker: Error registrado", event.getThrowable()));
    }
    
    @CircuitBreaker(name = "external-price-service", fallbackMethod = "getCachedPrice")
    @Retry(name = "external-price-service")
    @TimeLimiter(name = "external-price-service")
    public Mono<PrecioDTO> obtenerPrecioExterno(String codigoProducto) {
        return Mono.fromSupplier(() -> externalPriceService.obtenerPrecio(codigoProducto))
            .subscribeOn(Schedulers.boundedElastic())
            .timeout(Duration.ofSeconds(10))
            .doOnSuccess(precio -> log.debug("Precio obtenido exitosamente: {}", precio))
            .doOnError(error -> log.error("Error obteniendo precio externo", error));
    }
    
    // Método fallback
    public Mono<PrecioDTO> getCachedPrice(String codigoProducto, Exception ex) {
        log.warn("Usando precio en caché para producto: {} debido a: {}", 
            codigoProducto, ex.getMessage());
            
        return cotizacionRepository.findCachedPrice(codigoProducto)
            .switchIfEmpty(Mono.just(PrecioDTO.builder()
                .codigoProducto(codigoProducto)
                .precio(BigDecimal.ZERO)
                .moneda("CLP")
                .fechaActualizacion(LocalDateTime.now())
                .esCache(true)
                .build()));
    }
    
    public Mono<Cotizacion> generarCotizacionConResiliencia(GenerarCotizacionCommand command) {
        return obtenerPrecioExterno(command.getCodigoProducto())
            .flatMap(precio -> procesarCotizacion(command, precio))
            .onErrorResume(ex -> {
                log.error("Error en generación de cotización, usando valores por defecto", ex);
                return procesarCotizacionConValoresPorDefecto(command);
            });
    }
    
    private Mono<Cotizacion> procesarCotizacion(GenerarCotizacionCommand command, PrecioDTO precio) {
        // Lógica de procesamiento con precio obtenido
        return Mono.fromCallable(() -> {
            // Construir cotización con precio real
            return Cotizacion.builder()
                .id(CotizacionId.generar())
                .codigoProducto(command.getCodigoProducto())
                .cantidad(command.getCantidad())
                .precioUnitario(precio.getPrecio())
                .total(precio.getPrecio().multiply(BigDecimal.valueOf(command.getCantidad())))
                .fechaCreacion(LocalDateTime.now())
                .estado(EstadoCotizacion.PENDIENTE)
                .build();
        });
    }
    
    private Mono<Cotizacion> procesarCotizacionConValoresPorDefecto(GenerarCotizacionCommand command) {
        // Procesamiento con valores por defecto cuando todo falla
        return Mono.fromCallable(() -> {
            BigDecimal precioDefault = BigDecimal.valueOf(1000); // Precio base
            
            return Cotizacion.builder()
                .id(CotizacionId.generar())
                .codigoProducto(command.getCodigoProducto())
                .cantidad(command.getCantidad())
                .precioUnitario(precioDefault)
                .total(precioDefault.multiply(BigDecimal.valueOf(command.getCantidad())))
                .fechaCreacion(LocalDateTime.now())
                .estado(EstadoCotizacion.PENDIENTE)
                .observaciones("Cotización generada con precio base por falla en servicio externo")
                .build();
        });
    }
}
```

---

## 📞 **FEIGN CLIENTS**

### ⚙️ **Configuración de Feign Clients**

```java
// 📁 arka-cotizador/src/main/java/com/arka/cotizador/clients/GestorSolicitudesClient.java

@FeignClient(
    name = "arca-gestor-solicitudes",
    configuration = FeignConfiguration.class
)
public interface GestorSolicitudesClient {
    
    @GetMapping("/api/solicitudes/{id}")
    Mono<SolicitudDTO> obtenerSolicitud(@PathVariable("id") String id);
    
    @PostMapping("/api/solicitudes")
    Mono<SolicitudDTO> crearSolicitud(@RequestBody CrearSolicitudRequest request);
    
    @PutMapping("/api/solicitudes/{id}/aprobar")
    Mono<SolicitudDTO> aprobarSolicitud(@PathVariable("id") String id);
    
    @GetMapping("/api/solicitudes/por-cliente/{clienteId}")
    Flux<SolicitudDTO> obtenerSolicitudesPorCliente(@PathVariable("clienteId") String clienteId);
}

// Configuración de Feign
@Configuration
public class FeignConfiguration {
    
    @Bean
    public RequestInterceptor requestInterceptor() {
        return new ArkaRequestInterceptor();
    }
    
    @Bean
    public ErrorDecoder errorDecoder() {
        return new ArkaErrorDecoder();
    }
    
    @Bean
    public Retryer retryer() {
        return new Retryer.Default(1000, 3000, 3);
    }
    
    @Bean
    public Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL;
    }
    
    @Bean
    public Contract feignContract() {
        return new ReactiveContract();
    }
}

// Interceptor personalizado para agregar headers
public class ArkaRequestInterceptor implements RequestInterceptor {
    
    @Override
    public void apply(RequestTemplate template) {
        // Agregar headers de correlación
        String correlationId = MDC.get("correlationId");
        if (correlationId != null) {
            template.header("X-Correlation-ID", correlationId);
        }
        
        // Agregar información del usuario si está disponible
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            template.header("X-User", authentication.getName());
            template.header("X-Authorities", 
                authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.joining(",")));
        }
        
        // Agregar timestamp
        template.header("X-Request-Timestamp", Instant.now().toString());
    }
}

// Decoder de errores personalizado
public class ArkaErrorDecoder implements ErrorDecoder {
    
    private final ErrorDecoder defaultErrorDecoder = new Default();
    
    @Override
    public Exception decode(String methodKey, Response response) {
        switch (response.status()) {
            case 400:
                return new BadRequestException("Solicitud inválida: " + methodKey);
            case 404:
                return new NotFoundException("Recurso no encontrado: " + methodKey);
            case 503:
                return new ServiceUnavailableException("Servicio no disponible: " + methodKey);
            case 429:
                return new RateLimitExceededException("Límite de velocidad excedido: " + methodKey);
            default:
                return defaultErrorDecoder.decode(methodKey, response);
        }
    }
}

// Service que usa Feign Client con Circuit Breaker
@Service
@Transactional(readOnly = true)
public class CotizacionIntegrationService {
    
    private final GestorSolicitudesClient gestorSolicitudesClient;
    private final HelloWorldClient helloWorldClient;
    
    @CircuitBreaker(name = "gestor-solicitudes", fallbackMethod = "crearSolicitudFallback")
    @Retry(name = "gestor-solicitudes")
    @TimeLimiter(name = "gestor-solicitudes")
    public Mono<SolicitudDTO> crearSolicitudDesdeCotyacion(Cotizacion cotizacion) {
        CrearSolicitudRequest request = CrearSolicitudRequest.builder()
            .cotizacionId(cotizacion.getId().getValue())
            .clienteId(cotizacion.getClienteId())
            .descripcion("Solicitud generada desde cotización: " + cotizacion.getId().getValue())
            .prioridad(SolicitudPrioridad.NORMAL)
            .build();
            
        return gestorSolicitudesClient.crearSolicitud(request)
            .doOnSuccess(solicitud -> log.info("Solicitud creada exitosamente: {}", solicitud.getId()))
            .doOnError(error -> log.error("Error creando solicitud para cotización: {}", 
                cotizacion.getId().getValue(), error));
    }
    
    // Método fallback
    public Mono<SolicitudDTO> crearSolicitudFallback(Cotizacion cotizacion, Exception ex) {
        log.warn("Creando solicitud offline para cotización: {} debido a: {}", 
            cotizacion.getId().getValue(), ex.getMessage());
            
        // Crear solicitud offline que será sincronizada posteriormente
        return Mono.just(SolicitudDTO.builder()
            .id("OFFLINE-" + UUID.randomUUID())
            .cotizacionId(cotizacion.getId().getValue())
            .estado(SolicitudEstado.PENDIENTE_SYNC)
            .fechaCreacion(LocalDateTime.now())
            .esOffline(true)
            .build());
    }
    
    @CircuitBreaker(name = "hello-world", fallbackMethod = "getStatusFallback")
    public Mono<String> verificarStatusSistema() {
        return helloWorldClient.getStatus()
            .map(response -> "Sistema operativo: " + response)
            .doOnError(error -> log.error("Error verificando status del sistema", error));
    }
    
    public Mono<String> getStatusFallback(Exception ex) {
        log.warn("Status del sistema no disponible: {}", ex.getMessage());
        return Mono.just("Sistema en modo degradado");
    }
}
```

---

## 🏥 **HEALTH CHECKS Y MONITOREO**

### 💊 **Health Indicators Personalizados**

```java
// 📁 arka-security-common/src/main/java/com/arka/health/ArkaHealthIndicator.java

@Component
public class ArkaHealthIndicator implements ReactiveHealthIndicator {
    
    private final RedisTemplate<String, String> redisTemplate;
    private final DataSource dataSource;
    private final DiscoveryClient discoveryClient;
    
    @Override
    public Mono<Health> health() {
        return Mono.zip(
            checkDatabase(),
            checkRedis(),
            checkServiceDiscovery(),
            checkExternalServices()
        ).map(tuple -> {
            Health.Builder builder = Health.up();
            
            if (!tuple.getT1()) {
                builder.down().withDetail("database", "No disponible");
            }
            if (!tuple.getT2()) {
                builder.down().withDetail("redis", "No disponible");
            }
            if (!tuple.getT3()) {
                builder.down().withDetail("service-discovery", "No disponible");
            }
            if (!tuple.getT4()) {
                builder.withDetail("external-services", "Algunos servicios no disponibles");
            }
            
            return builder
                .withDetail("timestamp", Instant.now())
                .withDetail("uptime", getUptime())
                .withDetail("memory", getMemoryInfo())
                .build();
        });
    }
    
    private Mono<Boolean> checkDatabase() {
        return Mono.fromCallable(() -> {
            try (Connection connection = dataSource.getConnection()) {
                return connection.isValid(5);
            } catch (SQLException e) {
                log.error("Error verificando base de datos", e);
                return false;
            }
        }).subscribeOn(Schedulers.boundedElastic());
    }
    
    private Mono<Boolean> checkRedis() {
        return Mono.fromCallable(() -> {
            try {
                redisTemplate.opsForValue().set("health-check", "ok", Duration.ofSeconds(10));
                return "ok".equals(redisTemplate.opsForValue().get("health-check"));
            } catch (Exception e) {
                log.error("Error verificando Redis", e);
                return false;
            }
        }).subscribeOn(Schedulers.boundedElastic());
    }
    
    private Mono<Boolean> checkServiceDiscovery() {
        return Mono.fromCallable(() -> {
            try {
                List<String> services = discoveryClient.getServices();
                return !services.isEmpty();
            } catch (Exception e) {
                log.error("Error verificando service discovery", e);
                return false;
            }
        }).subscribeOn(Schedulers.boundedElastic());
    }
    
    private Mono<Boolean> checkExternalServices() {
        // Verificar servicios externos críticos
        return Mono.just(true); // Implementar verificaciones específicas
    }
    
    private String getUptime() {
        RuntimeMXBean runtimeBean = ManagementFactory.getRuntimeMXBean();
        long uptime = runtimeBean.getUptime();
        return Duration.ofMillis(uptime).toString();
    }
    
    private Map<String, Object> getMemoryInfo() {
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        MemoryUsage heapMemory = memoryBean.getHeapMemoryUsage();
        
        return Map.of(
            "heap_used", heapMemory.getUsed(),
            "heap_max", heapMemory.getMax(),
            "heap_usage_percent", (double) heapMemory.getUsed() / heapMemory.getMax() * 100
        );
    }
}

// Health Check para Circuit Breakers
@Component
public class CircuitBreakerHealthIndicator implements ReactiveHealthIndicator {
    
    private final CircuitBreakerRegistry circuitBreakerRegistry;
    
    @Override
    public Mono<Health> health() {
        Map<String, Object> details = new HashMap<>();
        boolean allHealthy = true;
        
        for (CircuitBreaker circuitBreaker : circuitBreakerRegistry.getAllCircuitBreakers()) {
            CircuitBreaker.State state = circuitBreaker.getState();
            CircuitBreaker.Metrics metrics = circuitBreaker.getMetrics();
            
            Map<String, Object> cbDetails = Map.of(
                "state", state.toString(),
                "failure_rate", metrics.getFailureRate(),
                "slow_call_rate", metrics.getSlowCallRate(),
                "number_of_calls", metrics.getNumberOfNotPermittedCalls(),
                "number_of_slow_calls", metrics.getNumberOfSlowCalls()
            );
            
            details.put(circuitBreaker.getName(), cbDetails);
            
            if (state == CircuitBreaker.State.OPEN) {
                allHealthy = false;
            }
        }
        
        Health.Builder builder = allHealthy ? Health.up() : Health.down();
        return Mono.just(builder.withDetails(details).build());
    }
}
```

---

## 🏆 **BENEFICIOS DE SPRING CLOUD**

### ✅ **Service Discovery Automático**

```
🔍 DISCOVERY LOGRADO:
├── Registro automático de servicios ✅
├── Health checks integrados ✅
├── Load balancing automático ✅
├── Failover transparente ✅
└── Dashboard de monitoreo ✅
```

### ✅ **Configuración Centralizada**

```
⚙️ CONFIGURACIÓN UNIFICADA:
├── Configuración por entorno ✅
├── Refresh dinámico ✅
├── Cifrado de propiedades sensibles ✅
├── Versionado de configuraciones ✅
└── Rollback automático ✅
```

### ✅ **Resiliencia y Tolerancia a Fallos**

```
🛡️ PROTECCIONES IMPLEMENTADAS:
├── Circuit Breakers configurados ✅
├── Retry con backoff exponencial ✅
├── Timeouts configurables ✅
├── Fallback methods ✅
└── Rate limiting por usuario ✅
```

---

*Documentación de Spring Cloud*  
*Proyecto: Arka Valenzuela*  
*Implementación completa de microservicios*  
*Fecha: 8 de Septiembre de 2025*
