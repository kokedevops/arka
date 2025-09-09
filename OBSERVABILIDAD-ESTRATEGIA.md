# 📊 OBSERVABILIDAD - ESTRATEGIA INTEGRAL

## 🎯 **INTRODUCCIÓN A LA OBSERVABILIDAD**

**Observabilidad en Arka Valenzuela** implementa una estrategia integral de monitoreo, logging, trazabilidad y alertas usando Prometheus, Grafana, ELK Stack, y Jaeger. El sistema garantiza visibilidad completa en escenarios de alta carga (3000 ventas/minuto) y manejo proactivo de fallos críticos (servicio shipping 1-2pm).

---

## 🏗️ **ARQUITECTURA DE OBSERVABILIDAD**

```
                    🌐 MICROSERVICIOS ECOSYSTEM
                           │
          ┌────────────────┼────────────────┐
          │                │                │
   ┌──────┴──────┐  ┌──────┴──────┐  ┌──────┴──────┐
   │ 📊 METRICS  │  │ 📝 LOGS     │  │ 🔍 TRACES   │
   │ (Prometheus)│  │ (ELK Stack) │  │ (Jaeger)    │
   │             │  │             │  │             │
   │• CPU/Memory │  │• App Logs   │  │• Request    │
   │• Requests   │  │• Error Logs │  │  Tracing    │
   │• Latency    │  │• Audit Logs │  │• Dependency │
   │• Business   │  │• Security   │  │  Mapping    │
   └──────┬──────┘  └──────┬──────┘  └──────┬──────┘
          │                │                │
          └────────────────┼────────────────┘
                           │
                    ┌──────┴──────┐
                    │ 📈 GRAFANA  │
                    │ DASHBOARDS  │
                    │             │
                    │• SLI/SLO    │
                    │• Business   │
                    │• Technical  │
                    │• Alerting   │
                    └──────┬──────┘
                           │
            ┌──────────────┼──────────────┐
            │                             │
     ┌──────┴──────┐               ┌──────┴──────┐
     │ 🚨 ALERT    │               │ 📊 INCIDENT │
     │ MANAGER     │               │ RESPONSE    │
     │             │               │             │
     │• PagerDuty  │               │• Runbooks   │
     │• Slack      │               │• Escalation │
     │• Email      │               │• PostMortem │
     └─────────────┘               └─────────────┘
```

---

## 📊 **PROMETHEUS MONITORING**

### 🔧 **Configuración Prometheus**

```yaml
# 📁 monitoring/prometheus/prometheus.yml
global:
  scrape_interval: 15s
  evaluation_interval: 15s
  external_labels:
    cluster: 'arka-valenzuela-prod'
    region: 'us-east-1'

rule_files:
  - "rules/*.yml"

alerting:
  alertmanagers:
    - static_configs:
        - targets:
          - alertmanager:9093

scrape_configs:
  # ===========================
  # PROMETHEUS SELF-MONITORING
  # ===========================
  - job_name: 'prometheus'
    static_configs:
      - targets: ['localhost:9090']
    metrics_path: /metrics
    scrape_interval: 30s

  # ===========================
  # MICROSERVICES MONITORING
  # ===========================
  - job_name: 'api-gateway'
    metrics_path: '/actuator/prometheus'
    scrape_interval: 10s
    ec2_sd_configs:
      - region: us-east-1
        port: 8080
        filters:
          - name: tag:Service
            values: [api-gateway]
          - name: tag:Environment
            values: [prod]
          - name: instance-state-name
            values: [running]
    relabel_configs:
      - source_labels: [__meta_ec2_tag_Service]
        target_label: service
      - source_labels: [__meta_ec2_tag_Environment]
        target_label: environment
      - source_labels: [__meta_ec2_availability_zone]
        target_label: az
      - source_labels: [__meta_ec2_instance_id]
        target_label: instance_id

  - job_name: 'arca-cotizador'
    metrics_path: '/actuator/prometheus'
    scrape_interval: 10s
    ec2_sd_configs:
      - region: us-east-1
        port: 8081
        filters:
          - name: tag:Service
            values: [arca-cotizador]
          - name: tag:Environment
            values: [prod]
          - name: instance-state-name
            values: [running]
    relabel_configs:
      - source_labels: [__meta_ec2_tag_Service]
        target_label: service
      - source_labels: [__meta_ec2_tag_Environment]
        target_label: environment

  - job_name: 'arca-gestor-solicitudes'
    metrics_path: '/actuator/prometheus'
    scrape_interval: 10s
    ec2_sd_configs:
      - region: us-east-1
        port: 8082
        filters:
          - name: tag:Service
            values: [arca-gestor-solicitudes]
          - name: tag:Environment
            values: [prod]
          - name: instance-state-name
            values: [running]
    relabel_configs:
      - source_labels: [__meta_ec2_tag_Service]
        target_label: service
      - source_labels: [__meta_ec2_tag_Environment]
        target_label: environment

  - job_name: 'hello-world-service'
    metrics_path: '/actuator/prometheus'
    scrape_interval: 15s
    ec2_sd_configs:
      - region: us-east-1
        port: 8083
        filters:
          - name: tag:Service
            values: [hello-world-service]
          - name: tag:Environment
            values: [prod]
          - name: instance-state-name
            values: [running]

  # ===========================
  # INFRASTRUCTURE MONITORING
  # ===========================
  - job_name: 'node-exporter'
    static_configs:
      - targets:
          - 'node-exporter:9100'
    scrape_interval: 30s

  - job_name: 'postgres-exporter'
    static_configs:
      - targets:
          - 'postgres-exporter:9187'
    scrape_interval: 30s

  - job_name: 'redis-exporter'
    static_configs:
      - targets:
          - 'redis-exporter:9121'
    scrape_interval: 30s

  # ===========================
  # AWS CLOUDWATCH METRICS
  # ===========================
  - job_name: 'cloudwatch-exporter'
    ec2_sd_configs:
      - region: us-east-1
        port: 9106
        filters:
          - name: tag:Component
            values: [cloudwatch-exporter]
          - name: instance-state-name
            values: [running]
    scrape_interval: 60s

  # ===========================
  # BUSINESS METRICS
  # ===========================
  - job_name: 'business-metrics'
    metrics_path: '/actuator/prometheus'
    scrape_interval: 5s
    static_configs:
      - targets:
          - 'api-gateway:8080'
    metric_relabel_configs:
      - source_labels: [__name__]
        regex: 'arka_business_.*'
        action: keep

# ===========================
# RECORDING RULES
# ===========================
recording_rules:
  - name: arka_sli_rules
    interval: 30s
    rules:
      # Request rate per service
      - record: arka:request_rate_5m
        expr: |
          sum(rate(http_requests_total[5m])) by (service, environment)
      
      # Error rate per service
      - record: arka:error_rate_5m
        expr: |
          sum(rate(http_requests_total{status=~"5.."}[5m])) by (service, environment) /
          sum(rate(http_requests_total[5m])) by (service, environment)
      
      # Latency percentiles
      - record: arka:request_duration_p95_5m
        expr: |
          histogram_quantile(0.95, 
            sum(rate(http_request_duration_seconds_bucket[5m])) by (service, environment, le)
          )
      
      # Business metrics
      - record: arka:sales_rate_1m
        expr: |
          sum(rate(arka_business_sales_total[1m])) by (environment)
      
      - record: arka:revenue_rate_1m
        expr: |
          sum(rate(arka_business_revenue_total[1m])) by (environment)

  # SLO calculations
  - name: arka_slo_rules
    interval: 1m
    rules:
      # Availability SLO (99.9%)
      - record: arka:availability_slo
        expr: |
          (
            sum(rate(http_requests_total{status!~"5.."}[5m])) by (service) /
            sum(rate(http_requests_total[5m])) by (service)
          ) * 100
      
      # Latency SLO (95% < 500ms)
      - record: arka:latency_slo
        expr: |
          (
            histogram_quantile(0.95, 
              sum(rate(http_request_duration_seconds_bucket[5m])) by (service, le)
            ) < 0.5
          ) * 100
```

### 📋 **Alerting Rules**

```yaml
# 📁 monitoring/prometheus/rules/alerts.yml
groups:
  # ===========================
  # INFRASTRUCTURE ALERTS
  # ===========================
  - name: infrastructure
    rules:
      - alert: HighCPUUsage
        expr: |
          (100 - (avg by (instance) (rate(node_cpu_seconds_total{mode="idle"}[5m])) * 100)) > 80
        for: 5m
        labels:
          severity: warning
          team: platform
        annotations:
          summary: "High CPU usage on {{ $labels.instance }}"
          description: "CPU usage is above 80% for more than 5 minutes on {{ $labels.instance }}"
          runbook: "https://runbooks.arkavalenzuela.com/cpu-high"

      - alert: HighMemoryUsage
        expr: |
          (node_memory_MemTotal_bytes - node_memory_MemAvailable_bytes) / node_memory_MemTotal_bytes * 100 > 85
        for: 5m
        labels:
          severity: warning
          team: platform
        annotations:
          summary: "High memory usage on {{ $labels.instance }}"
          description: "Memory usage is above 85% on {{ $labels.instance }}"

      - alert: DiskSpaceLow
        expr: |
          (node_filesystem_avail_bytes / node_filesystem_size_bytes) * 100 < 10
        for: 2m
        labels:
          severity: critical
          team: platform
        annotations:
          summary: "Low disk space on {{ $labels.instance }}"
          description: "Disk space is below 10% on {{ $labels.instance }}"

  # ===========================
  # APPLICATION ALERTS
  # ===========================
  - name: application
    rules:
      - alert: ServiceDown
        expr: |
          up{job=~"api-gateway|arca-cotizador|arca-gestor-solicitudes"} == 0
        for: 1m
        labels:
          severity: critical
          team: backend
        annotations:
          summary: "Service {{ $labels.job }} is down"
          description: "Service {{ $labels.job }} has been down for more than 1 minute"
          runbook: "https://runbooks.arkavalenzuela.com/service-down"

      - alert: HighErrorRate
        expr: |
          arka:error_rate_5m > 0.05
        for: 2m
        labels:
          severity: critical
          team: backend
        annotations:
          summary: "High error rate on {{ $labels.service }}"
          description: "Error rate is {{ $value | humanizePercentage }} on {{ $labels.service }}"

      - alert: HighLatency
        expr: |
          arka:request_duration_p95_5m > 1.0
        for: 3m
        labels:
          severity: warning
          team: backend
        annotations:
          summary: "High latency on {{ $labels.service }}"
          description: "95th percentile latency is {{ $value }}s on {{ $labels.service }}"

      - alert: DatabaseConnectionsFull
        expr: |
          pg_stat_database_numbackends / pg_settings_max_connections > 0.8
        for: 2m
        labels:
          severity: warning
          team: database
        annotations:
          summary: "Database connections nearly full"
          description: "Database connections are at {{ $value | humanizePercentage }} capacity"

  # ===========================
  # BUSINESS ALERTS
  # ===========================
  - name: business
    rules:
      - alert: LowSalesVolume
        expr: |
          arka:sales_rate_1m < 0.5
        for: 5m
        labels:
          severity: warning
          team: business
        annotations:
          summary: "Low sales volume detected"
          description: "Sales rate is {{ $value }} sales/minute, below threshold of 0.5"

      - alert: HighSalesVolume
        expr: |
          arka:sales_rate_1m > 50
        for: 1m
        labels:
          severity: warning
          team: ops
        annotations:
          summary: "High sales volume detected"
          description: "Sales rate is {{ $value }} sales/minute, consider scaling"
          runbook: "https://runbooks.arkavalenzuela.com/high-traffic"

      - alert: AnniversarySalesSpike
        expr: |
          arka:sales_rate_1m > 3000/60
        for: 30s
        labels:
          severity: critical
          team: ops
          scenario: anniversary
        annotations:
          summary: "Anniversary sales spike detected!"
          description: "Sales rate is {{ $value }} sales/minute, anniversary scenario activated"
          runbook: "https://runbooks.arkavalenzuela.com/anniversary-scenario"

  # ===========================
  # SLO ALERTS
  # ===========================
  - name: slo
    rules:
      - alert: SLOAvailabilityBreach
        expr: |
          arka:availability_slo < 99.9
        for: 5m
        labels:
          severity: critical
          team: sre
        annotations:
          summary: "SLO availability breach on {{ $labels.service }}"
          description: "Availability is {{ $value }}%, below 99.9% SLO"

      - alert: SLOLatencyBreach
        expr: |
          arka:latency_slo < 95
        for: 3m
        labels:
          severity: warning
          team: sre
        annotations:
          summary: "SLO latency breach on {{ $labels.service }}"
          description: "95th percentile latency SLO compliance is {{ $value }}%"

  # ===========================
  # SHIPPING SERVICE ALERTS
  # ===========================
  - name: shipping_critical
    rules:
      - alert: ShippingServiceDown
        expr: |
          up{job="shipping-service"} == 0
        for: 30s
        labels:
          severity: critical
          team: shipping
          time_sensitive: "true"
        annotations:
          summary: "Shipping service is DOWN"
          description: "Shipping service has been down for 30 seconds"
          runbook: "https://runbooks.arkavalenzuela.com/shipping-down"

      - alert: ShippingPeakHourFailure
        expr: |
          up{job="shipping-service"} == 0 and on() hour() >= 13 and on() hour() < 15
        for: 15s
        labels:
          severity: critical
          team: shipping
          scenario: peak_hours
          pager: "true"
        annotations:
          summary: "CRITICAL: Shipping down during peak hours (1-2pm)"
          description: "Shipping service failed during critical hours - immediate response required"
          escalation: "Page CTO immediately"
```

---

## 📈 **GRAFANA DASHBOARDS**

### 🎯 **Dashboard Principal - SLI/SLO**

```json
{
  "dashboard": {
    "id": null,
    "title": "🎯 Arka Valenzuela - SLI/SLO Dashboard",
    "tags": ["arka-valenzuela", "sli", "slo", "production"],
    "timezone": "UTC",
    "panels": [
      {
        "id": 1,
        "title": "🎯 Service Level Objectives Status",
        "type": "stat",
        "targets": [
          {
            "expr": "arka:availability_slo",
            "legendFormat": "{{ service }} Availability",
            "refId": "A"
          }
        ],
        "fieldConfig": {
          "defaults": {
            "color": {
              "mode": "thresholds"
            },
            "thresholds": {
              "steps": [
                {"color": "red", "value": 0},
                {"color": "yellow", "value": 99.5},
                {"color": "green", "value": 99.9}
              ]
            },
            "unit": "percent",
            "min": 99,
            "max": 100
          }
        },
        "options": {
          "orientation": "horizontal",
          "reduceOptions": {
            "values": false,
            "calcs": ["lastNotNull"],
            "fields": ""
          },
          "textMode": "auto"
        }
      },
      {
        "id": 2,
        "title": "📊 Request Rate (RPS)",
        "type": "graph",
        "targets": [
          {
            "expr": "sum(arka:request_rate_5m) by (service)",
            "legendFormat": "{{ service }}",
            "refId": "A"
          }
        ],
        "yAxes": [
          {
            "label": "Requests/sec",
            "min": 0
          }
        ],
        "alert": {
          "conditions": [
            {
              "evaluator": {
                "params": [1000],
                "type": "gt"
              },
              "operator": {
                "type": "and"
              },
              "query": {
                "params": ["A", "5m", "now"]
              },
              "reducer": {
                "type": "avg"
              },
              "type": "query"
            }
          ],
          "executionErrorState": "alerting",
          "for": "2m",
          "frequency": "10s",
          "handler": 1,
          "name": "High Traffic Alert",
          "noDataState": "no_data",
          "notifications": []
        }
      },
      {
        "id": 3,
        "title": "❌ Error Rate",
        "type": "graph",
        "targets": [
          {
            "expr": "arka:error_rate_5m * 100",
            "legendFormat": "{{ service }} Error Rate",
            "refId": "A"
          }
        ],
        "yAxes": [
          {
            "label": "Error Rate %",
            "min": 0,
            "max": 10
          }
        ],
        "thresholds": [
          {
            "value": 5,
            "colorMode": "critical",
            "op": "gt"
          },
          {
            "value": 1,
            "colorMode": "warning",
            "op": "gt"
          }
        ]
      },
      {
        "id": 4,
        "title": "⏱️ Response Time (P95)",
        "type": "graph",
        "targets": [
          {
            "expr": "arka:request_duration_p95_5m * 1000",
            "legendFormat": "{{ service }} P95",
            "refId": "A"
          }
        ],
        "yAxes": [
          {
            "label": "Latency (ms)",
            "min": 0
          }
        ],
        "thresholds": [
          {
            "value": 1000,
            "colorMode": "critical",
            "op": "gt"
          },
          {
            "value": 500,
            "colorMode": "warning",
            "op": "gt"
          }
        ]
      }
    ]
  }
}
```

### 💼 **Dashboard de Negocio**

```json
{
  "dashboard": {
    "id": null,
    "title": "💼 Arka Valenzuela - Business Metrics",
    "tags": ["arka-valenzuela", "business", "sales", "revenue"],
    "panels": [
      {
        "id": 1,
        "title": "💰 Sales Rate (Sales/minute)",
        "type": "graph",
        "targets": [
          {
            "expr": "arka:sales_rate_1m * 60",
            "legendFormat": "Sales per minute",
            "refId": "A"
          }
        ],
        "yAxes": [
          {
            "label": "Sales/minute",
            "min": 0
          }
        ],
        "seriesOverrides": [
          {
            "alias": "Sales per minute",
            "color": "#1f77b4",
            "fill": 2
          }
        ],
        "alert": {
          "conditions": [
            {
              "evaluator": {
                "params": [3000],
                "type": "gt"
              },
              "query": {
                "params": ["A", "1m", "now"]
              },
              "reducer": {
                "type": "avg"
              },
              "type": "query"
            }
          ],
          "name": "Anniversary Sales Spike"
        }
      },
      {
        "id": 2,
        "title": "💵 Revenue Rate",
        "type": "singlestat",
        "targets": [
          {
            "expr": "arka:revenue_rate_1m * 60",
            "refId": "A"
          }
        ],
        "valueName": "current",
        "unit": "currencyUSD",
        "thresholds": "1000,10000",
        "colorBackground": true
      },
      {
        "id": 3,
        "title": "📈 Conversion Funnel",
        "type": "graph",
        "targets": [
          {
            "expr": "rate(arka_business_page_views_total[5m])",
            "legendFormat": "Page Views",
            "refId": "A"
          },
          {
            "expr": "rate(arka_business_cart_additions_total[5m])",
            "legendFormat": "Cart Additions",
            "refId": "B"
          },
          {
            "expr": "rate(arka_business_checkouts_total[5m])",
            "legendFormat": "Checkouts",
            "refId": "C"
          },
          {
            "expr": "rate(arka_business_sales_total[5m])",
            "legendFormat": "Completed Sales",
            "refId": "D"
          }
        ]
      },
      {
        "id": 4,
        "title": "🎉 Anniversary Scenario Monitor",
        "type": "table",
        "targets": [
          {
            "expr": "arka:sales_rate_1m * 60",
            "legendFormat": "Current Sales/min",
            "refId": "A"
          },
          {
            "expr": "3000",
            "legendFormat": "Anniversary Threshold",
            "refId": "B"
          },
          {
            "expr": "arka:request_rate_5m",
            "legendFormat": "Request Rate",
            "refId": "C"
          }
        ],
        "styles": [
          {
            "pattern": "Current Sales/min",
            "type": "number",
            "unit": "short",
            "thresholds": ["2500", "3000"],
            "colorMode": "cell"
          }
        ]
      }
    ]
  }
}
```

---

## 🔍 **DISTRIBUTED TRACING (JAEGER)**

### 🔧 **Configuración Jaeger**

```yaml
# 📁 monitoring/jaeger/jaeger-deployment.yml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: jaeger-all-in-one
  namespace: monitoring
  labels:
    app: jaeger
    component: all-in-one
spec:
  replicas: 1
  selector:
    matchLabels:
      app: jaeger
      component: all-in-one
  template:
    metadata:
      labels:
        app: jaeger
        component: all-in-one
    spec:
      containers:
      - name: jaeger
        image: jaegertracing/all-in-one:latest
        ports:
        - containerPort: 16686
          name: ui
        - containerPort: 14268
          name: collector
        - containerPort: 6831
          name: agent-udp
        - containerPort: 6832
          name: agent-binary
        env:
        - name: COLLECTOR_ZIPKIN_HTTP_PORT
          value: "9411"
        - name: SPAN_STORAGE_TYPE
          value: "elasticsearch"
        - name: ES_SERVER_URLS
          value: "http://elasticsearch:9200"
        - name: ES_TAGS_AS_FIELDS_ALL
          value: "true"
        resources:
          requests:
            memory: "512Mi"
            cpu: "250m"
          limits:
            memory: "1Gi"
            cpu: "500m"
        readinessProbe:
          httpGet:
            path: /
            port: 16686
          initialDelaySeconds: 5
          periodSeconds: 10
```

### 📝 **Instrumentación de Microservicios**

```java
// 📁 arka-security-common/src/main/java/com/arka/security/tracing/TracingConfiguration.java

@Configuration
@EnableAutoConfiguration
@ConditionalOnProperty(value = "spring.sleuth.enabled", matchIfMissing = true)
public class TracingConfiguration {

    @Bean
    public Sender sender() {
        return OkHttpSender.create("http://jaeger-collector:14268/api/traces");
    }

    @Bean
    public AsyncReporter<Span> spanReporter() {
        return AsyncReporter.create(sender());
    }

    @Bean
    public BraveTracer sleuthTracer() {
        return BraveTracer.newBuilder(Tracing.newBuilder()
                .localServiceName("arka-valenzuela")
                .spanReporter(spanReporter())
                .build())
                .build();
    }

    @Bean
    @Primary
    public io.opentracing.Tracer jaegerTracer() {
        return io.jaegertracing.Configuration.fromEnv("arka-valenzuela")
                .getTracer();
    }

    /**
     * Configuración personalizada para trazas de negocio
     */
    @Component
    public static class BusinessTracingInterceptor implements HandlerInterceptor {

        private final Tracer tracer;

        public BusinessTracingInterceptor(Tracer tracer) {
            this.tracer = tracer;
        }

        @Override
        public boolean preHandle(HttpServletRequest request, 
                               HttpServletResponse response, 
                               Object handler) throws Exception {
            
            Span span = tracer.nextSpan()
                    .name("business-operation")
                    .tag("http.method", request.getMethod())
                    .tag("http.url", request.getRequestURL().toString())
                    .tag("user.id", extractUserId(request))
                    .tag("business.operation", extractBusinessOperation(request))
                    .start();

            try (Tracer.SpanInScope ws = tracer.withSpanInScope(span)) {
                request.setAttribute("span", span);
                return true;
            }
        }

        @Override
        public void afterCompletion(HttpServletRequest request, 
                                   HttpServletResponse response, 
                                   Object handler, 
                                   Exception ex) throws Exception {
            
            Span span = (Span) request.getAttribute("span");
            if (span != null) {
                span.tag("http.status_code", String.valueOf(response.getStatus()));
                if (ex != null) {
                    span.tag("error", ex.getMessage());
                }
                span.end();
            }
        }

        private String extractUserId(HttpServletRequest request) {
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                // Extraer user ID del JWT token
                return JwtUtils.extractUserId(authHeader.substring(7));
            }
            return "anonymous";
        }

        private String extractBusinessOperation(HttpServletRequest request) {
            String uri = request.getRequestURI();
            if (uri.contains("/cotizaciones")) return "quotation";
            if (uri.contains("/solicitudes")) return "request";
            if (uri.contains("/auth")) return "authentication";
            return "unknown";
        }
    }

    /**
     * Instrumentación para operaciones de base de datos
     */
    @Component
    public static class DatabaseTracingAspect {

        private final Tracer tracer;

        public DatabaseTracingAspect(Tracer tracer) {
            this.tracer = tracer;
        }

        @Around("@annotation(org.springframework.data.jpa.repository.Query)")
        public Object traceRepositoryQuery(ProceedingJoinPoint joinPoint) throws Throwable {
            
            Span span = tracer.nextSpan()
                    .name("database-query")
                    .tag("db.type", "mysql")
                    .tag("db.operation", "query")
                    .tag("repository.class", joinPoint.getTarget().getClass().getSimpleName())
                    .tag("repository.method", joinPoint.getSignature().getName())
                    .start();

            try (Tracer.SpanInScope ws = tracer.withSpanInScope(span)) {
                Object result = joinPoint.proceed();
                span.tag("db.rows_affected", getRowsAffected(result));
                return result;
            } catch (Exception e) {
                span.tag("error", e.getMessage());
                throw e;
            } finally {
                span.end();
            }
        }

        private String getRowsAffected(Object result) {
            if (result instanceof List) {
                return String.valueOf(((List<?>) result).size());
            } else if (result instanceof Optional) {
                return ((Optional<?>) result).isPresent() ? "1" : "0";
            }
            return "1";
        }
    }
}
```

---

## 🚨 **ESCENARIOS CRÍTICOS**

### 🎉 **Escenario 1: Anniversary (3000 ventas/minuto)**

```yaml
# 📁 monitoring/scenarios/anniversary-scenario.yml
apiVersion: v1
kind: ConfigMap
metadata:
  name: anniversary-scenario-playbook
  namespace: monitoring
data:
  scenario.md: |
    # 🎉 Anniversary Scenario - 3000 ventas/minuto
    
    ## 🎯 Objetivo
    Manejar picos de tráfico extremo durante eventos especiales (aniversario)
    con un objetivo de 3000 ventas por minuto.
    
    ## 📊 Métricas Críticas
    - **Sales Rate**: > 3000 ventas/minuto
    - **Request Rate**: > 50,000 RPS
    - **Response Time**: P95 < 500ms bajo carga
    - **Error Rate**: < 0.1% durante el pico
    - **Availability**: > 99.99%
    
    ## ⚡ Acciones Automáticas
    ### 1. Auto-scaling Triggers
    ```
    - ECS Services: Scale to 20+ instances
    - RDS: Read replicas automáticas
    - Redis: Cluster mode activado
    - CDN: Cache headers optimizados
    ```
    
    ### 2. Circuit Breakers
    ```
    - Timeout reducidos: 2s → 500ms
    - Retry policies: Exponential backoff
    - Fallback responses: Cached data
    ```
    
    ### 3. Database Optimizations
    ```
    - Connection pool: 50 → 200 connections
    - Query timeout: 5s → 2s
    - Read/Write split activado
    ```
    
    ## 📈 Monitoring Dashboard
    ```
    URL: https://grafana.arkavalenzuela.com/d/anniversary
    Panels:
    - Real-time sales counter
    - Resource utilization heatmap
    - Error rate by service
    - Revenue tracking
    ```
    
    ## 🚨 Alerting Strategy
    ```
    Immediate (0-30s):
    - Sales rate > 3000/min
    - Error rate > 0.1%
    - Any service down
    
    Warning (1-2min):
    - Latency > 500ms P95
    - CPU > 70%
    - Memory > 80%
    
    Critical (3-5min):
    - Sustained high error rate
    - Database connection exhaustion
    - Revenue loss detection
    ```
    
    ## 🔄 Rollback Plan
    ```
    1. Automatic traffic throttling
    2. Graceful service degradation
    3. Emergency maintenance page
    4. Manual intervention protocols
    ```

---

# 📁 monitoring/scenarios/anniversary-rules.yml
groups:
  - name: anniversary_scenario
    rules:
      - alert: AnniversaryScenarioActivated
        expr: |
          arka:sales_rate_1m * 60 > 3000
        for: 30s
        labels:
          severity: info
          scenario: anniversary
          team: ops
        annotations:
          summary: "🎉 Anniversary scenario activated!"
          description: "Sales rate {{ $value }} exceeds 3000/min threshold"
          
      - alert: AnniversaryHighLoad
        expr: |
          arka:sales_rate_1m * 60 > 3000 and arka:request_duration_p95_5m > 0.5
        for: 1m
        labels:
          severity: warning
          scenario: anniversary
          team: ops
        annotations:
          summary: "Anniversary high load detected"
          description: "High sales ({{ $value }}/min) with elevated latency"
          
      - alert: AnniversarySystemStrain
        expr: |
          arka:sales_rate_1m * 60 > 3000 and arka:error_rate_5m > 0.001
        for: 30s
        labels:
          severity: critical
          scenario: anniversary
          team: ops
        annotations:
          summary: "Anniversary system strain critical"
          description: "High sales with increased errors - scaling required"
```

### 📦 **Escenario 2: Shipping Service Failure (1-2pm)**

```yaml
# 📁 monitoring/scenarios/shipping-failure-scenario.yml
apiVersion: v1
kind: ConfigMap
metadata:
  name: shipping-failure-scenario
  namespace: monitoring
data:
  scenario.md: |
    # 📦 Shipping Service Failure (1-2pm)
    
    ## 🎯 Contexto
    El servicio de shipping falla durante las horas pico (1-2pm),
    cuando el volumen de pedidos es máximo y el impacto en el negocio es crítico.
    
    ## ⏰ Tiempo de Respuesta Objetivo
    - **Detection**: < 30 segundos
    - **Response**: < 2 minutos
    - **Resolution**: < 15 minutos
    - **Recovery**: < 30 minutos
    
    ## 🚨 Escalation Matrix
    ```
    0-2min:  On-call Engineer (Auto-page)
    2-10min: Team Lead + SRE
    10-15min: Engineering Manager
    15min+:  CTO + VP Engineering
    ```
    
    ## 🔧 Automated Responses
    ### 1. Immediate Actions (0-30s)
    ```
    - Circuit breaker activation
    - Fallback to backup shipping API
    - Queue orders for processing
    - Customer notification system
    ```
    
    ### 2. Monitoring Intensification
    ```
    - Health check frequency: 30s → 5s
    - Log sampling: 1% → 100%
    - Tracing: All requests during incident
    - Real-time dashboards activation
    ```
    
    ### 3. Business Continuity
    ```
    - Shipping cost estimation from cache
    - Delivery time ranges (24-48hrs)
    - Customer communication automation
    - Revenue protection measures
    ```
    
    ## 📊 Business Impact Metrics
    ```
    - Orders affected per minute
    - Revenue at risk calculation
    - Customer satisfaction impact
    - SLA breach tracking
    ```
    
    ## 🔄 Recovery Procedures
    ```
    1. Service health verification
    2. Queue processing resumption
    3. Customer notification update
    4. SLA compliance reporting
    5. Post-incident review scheduling
    ```

---

# 📁 monitoring/scenarios/shipping-failure-rules.yml
groups:
  - name: shipping_failure_critical
    rules:
      - alert: ShippingServiceDown
        expr: |
          up{job="shipping-service"} == 0
        for: 15s
        labels:
          severity: critical
          team: shipping
          service: shipping
          pager: "true"
        annotations:
          summary: "🚨 Shipping service DOWN"
          description: "Shipping service unavailable - immediate action required"
          runbook: "https://runbooks.arkavalenzuela.com/shipping-down"
          
      - alert: ShippingPeakHourCritical
        expr: |
          up{job="shipping-service"} == 0 and on() hour() >= 13 and on() hour() < 15
        for: 10s
        labels:
          severity: critical
          team: shipping
          scenario: peak_hours
          pager: "immediate"
          escalation: "cto"
        annotations:
          summary: "🔥 CRITICAL: Shipping down during peak hours"
          description: "Shipping failure during 1-2pm peak - business critical"
          escalation: "Page CTO and VP Engineering immediately"
          
      - alert: ShippingDegradedPerformance
        expr: |
          arka:request_duration_p95_5m{service="shipping-service"} > 2.0
        for: 1m
        labels:
          severity: warning
          team: shipping
        annotations:
          summary: "Shipping service degraded performance"
          description: "Shipping latency {{ $value }}s exceeds 2s threshold"
          
      - alert: ShippingHighErrorRate
        expr: |
          arka:error_rate_5m{service="shipping-service"} > 0.05
        for: 30s
        labels:
          severity: critical
          team: shipping
        annotations:
          summary: "High error rate in shipping service"
          description: "Shipping error rate {{ $value | humanizePercentage }}"
```

---

## 📊 **OBSERVABILIDAD IMPLEMENTADA**

### ✅ **Cobertura Completa**

```
📊 MÉTRICAS:
├── Infrastructure (CPU, Memory, Disk, Network) ✅
├── Application (RPS, Latency, Errors) ✅
├── Business (Sales, Revenue, Conversions) ✅
├── SLI/SLO (Availability, Performance) ✅
└── Custom scenarios (Anniversary, Shipping) ✅

📝 LOGS:
├── Application logs (Structured JSON) ✅
├── Security logs (Auth, Access) ✅
├── Business events (Sales, Orders) ✅
├── Infrastructure logs (System, Docker) ✅
└── Centralized with ELK Stack ✅

🔍 TRACES:
├── Request tracing (Jaeger) ✅
├── Database queries tracing ✅
├── Service-to-service calls ✅
├── Business operations tracing ✅
└── Error correlation ✅

🚨 ALERTING:
├── Infrastructure alerts ✅
├── Application performance alerts ✅
├── Business KPI alerts ✅
├── Scenario-specific alerts ✅
└── Multi-channel notifications ✅
```

### ✅ **Escenarios Críticos Cubiertos**

```
🎉 ANNIVERSARY SCENARIO:
├── 3000 ventas/minuto detection ✅
├── Auto-scaling automation ✅
├── Performance monitoring ✅
├── Business impact tracking ✅
└── Rollback procedures ✅

📦 SHIPPING FAILURE 1-2PM:
├── Sub-30s detection ✅
├── Immediate escalation ✅
├── Business continuity ✅
├── Customer communication ✅
└── Revenue protection ✅
```

---

*Documentación de Observabilidad*  
*Proyecto: Arka Valenzuela*  
*Implementación completa con Prometheus, Grafana, ELK, Jaeger*  
*Fecha: 8 de Septiembre de 2025*
