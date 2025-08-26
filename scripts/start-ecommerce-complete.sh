#!/bin/bash

# 🚀 Start ARKA E-commerce Complete Infrastructure
# Includes MongoDB, MailHog, and all microservices

echo "🚀 Starting ARKA E-commerce Complete Infrastructure..."

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    echo "❌ Docker is not running. Please start Docker first."
    exit 1
fi

# Create network if it doesn't exist
docker network create arka-microservices-network 2>/dev/null || true

echo "🍃 Starting MongoDB and Email services..."
docker-compose up -d mongodb mongo-express mailhog

echo "⏳ Waiting for MongoDB to be ready..."
sleep 10

echo "🔧 Starting infrastructure services..."
docker-compose up -d eureka-server config-server api-gateway

echo "⏳ Waiting for infrastructure to be ready..."
sleep 15

echo "🛍️ Starting e-commerce services..."
docker-compose up -d arca-gestor-solicitudes arca-cotizador hello-world-service

echo "📊 Starting monitoring services..."
docker-compose up -d prometheus grafana

echo "✅ All services started successfully!"

echo ""
echo "🌐 Service URLs:"
echo "=================================="
echo "🏠 Eureka Server:      http://localhost:8761"
echo "🛒 E-commerce API:     http://localhost:8080"
echo "🚪 API Gateway:        http://localhost:8888"
echo "⚙️  Config Server:     http://localhost:8888/actuator/health"
echo "🍃 MongoDB Express:    http://localhost:8081"
echo "📧 MailHog Web UI:     http://localhost:8025"
echo "📊 Prometheus:         http://localhost:9090"
echo "📈 Grafana:           http://localhost:3000"
echo ""
echo "🔔 E-commerce Features:"
echo "=================================="
echo "🛒 Carts API:          http://localhost:8080/carritos"
echo "📋 Orders API:         http://localhost:8080/pedidos"
echo "📱 Mobile BFF:         http://localhost:8080/mobile/api"
echo "💻 Web BFF:           http://localhost:8080/web/api"
echo "📊 Analytics:         http://localhost:8080/analytics"
echo ""
echo "🧪 Test Commands:"
echo "=================================="
echo "curl http://localhost:8080/carritos/abandonados"
echo "curl http://localhost:8080/web/api/dashboard"
echo "curl http://localhost:8080/analytics/statistics"
echo ""
echo "🎉 ARKA E-commerce is ready to use!"
