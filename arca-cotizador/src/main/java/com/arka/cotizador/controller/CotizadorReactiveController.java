package com.arka.cotizador.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Instant;
import java.util.List;
import java.util.stream.IntStream;

@RestController
@RequestMapping("/")
public class CotizadorReactiveController {

    @Value("${server.port:8080}")
    private String port;

    @GetMapping
    public ResponseEntity<String> home() {
        return ResponseEntity.ok("Arca Cotizador Service - Servlet mode (WildFly ready)!");
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        try {
            String hostName = InetAddress.getLocalHost().getHostName();
            return ResponseEntity.status(HttpStatus.OK)
                .body(String.format("Cotizador Service is UP! Running on %s:%s", hostName, port));
        } catch (UnknownHostException e) {
            return ResponseEntity.status(HttpStatus.OK).body("Cotizador Service is UP!");
        }
    }

    @GetMapping("/info")
    public ResponseEntity<CotizadorInfo> info() {
        String hostName;
        try {
            hostName = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            hostName = "unknown";
        }

        var info = new CotizadorInfo(
            "Arca Cotizador Service",
            "1.0.0",
            "Servicio de cotización desplegado en WildFly",
            hostName,
            port,
            Instant.now().toEpochMilli()
        );
        return ResponseEntity.ok(info);
    }

    @GetMapping("/stream")
    public ResponseEntity<List<String>> stream() {
        List<String> events = IntStream.rangeClosed(1, 10)
            .mapToObj(i -> "Cotizador Event #" + i + " from port " + port)
            .toList();
        return ResponseEntity.ok(events);
    }

    @GetMapping("/reactive-test")
    public ResponseEntity<List<CotizacionEvent>> reactiveTest() {
        List<CotizacionEvent> events = IntStream.rangeClosed(1, 5)
            .mapToObj(i -> new CotizacionEvent(
                "COTIZ-" + i,
                "Cotización #" + i,
                "PENDING",
                Instant.now().toEpochMilli()
            ))
            .toList();
        return ResponseEntity.ok(events);
    }

    public static class CotizadorInfo {
        private String serviceName;
        private String version;
        private String description;
        private String hostname;
        private String port;
        private long timestamp;

        public CotizadorInfo(String serviceName, String version, String description,
                              String hostname, String port, long timestamp) {
            this.serviceName = serviceName;
            this.version = version;
            this.description = description;
            this.hostname = hostname;
            this.port = port;
            this.timestamp = timestamp;
        }

        public String getServiceName() {
            return serviceName;
        }

        public String getVersion() {
            return version;
        }

        public String getDescription() {
            return description;
        }

        public String getHostname() {
            return hostname;
        }

        public String getPort() {
            return port;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public void setServiceName(String serviceName) {
            this.serviceName = serviceName;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public void setHostname(String hostname) {
            this.hostname = hostname;
        }

        public void setPort(String port) {
            this.port = port;
        }

        public void setTimestamp(long timestamp) {
            this.timestamp = timestamp;
        }
    }

    public static class CotizacionEvent {
        private String id;
        private String description;
        private String status;
        private long timestamp;

        public CotizacionEvent(String id, String description, String status, long timestamp) {
            this.id = id;
            this.description = description;
            this.status = status;
            this.timestamp = timestamp;
        }

        public String getId() {
            return id;
        }

        public String getDescription() {
            return description;
        }

        public String getStatus() {
            return status;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public void setId(String id) {
            this.id = id;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public void setTimestamp(long timestamp) {
            this.timestamp = timestamp;
        }
    }
}
