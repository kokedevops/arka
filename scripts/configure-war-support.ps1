# 🔧 Configure WAR Support for WildFly
# Script para habilitar empaquetado WAR en los servicios de negocio

$services = @("api-gateway", "arca-cotizador", "arca-gestor-solicitudes")

function Add-WARSupport {
    param($serviceName)
    
    $buildFile = "$serviceName\build.gradle"
    
    if (!(Test-Path $buildFile)) {
        Write-Host "❌ Build file not found: $buildFile" -ForegroundColor Red
        return
    }
    
    Write-Host "🔧 Configuring WAR support for $serviceName..." -ForegroundColor Yellow
    
    $content = Get-Content $buildFile -Raw
    
    # Add WAR plugin if not present
    if ($content -notmatch "id 'war'") {
        $content = $content -replace "(plugins \{[^}]*)", "`$1`n    id 'war'"
        Write-Host "  ✅ Added WAR plugin" -ForegroundColor Green
    }
    
    # Add Spring Boot WAR configuration
    $warConfig = @"

// WAR Configuration for WildFly deployment
war {
    enabled = true
    archiveFileName = '$serviceName.war'
}

// Configure Spring Boot to support both JAR and WAR
bootWar {
    enabled = true
    archiveFileName = '$serviceName.war'
    manifest {
        attributes 'Main-Class': 'org.springframework.boot.loader.WarLauncher'
    }
}

// Provide runtime for embedded container (Tomcat) - excluded in WAR for WildFly
providedRuntime 'org.springframework.boot:spring-boot-starter-tomcat'
"@
    
    if ($content -notmatch "bootWar") {
        $content += $warConfig
        Write-Host "  ✅ Added WAR configuration" -ForegroundColor Green
    }
    
    # Add WildFly specific dependencies
    $wildflyDeps = @"

// WildFly specific dependencies
dependencies {
    // Required for WildFly deployment
    providedRuntime 'org.springframework.boot:spring-boot-starter-tomcat'
    
    // WildFly compatibility
    implementation 'org.jboss.spec.javax.servlet:jboss-servlet-api_4.0_spec:2.0.0.Final'
}
"@
    
    if ($content -notmatch "jboss-servlet-api") {
        $content += $wildflyDeps
        Write-Host "  ✅ Added WildFly dependencies" -ForegroundColor Green
    }
    
    # Write updated content
    Set-Content -Path $buildFile -Value $content
    Write-Host "✅ $serviceName configured for WAR deployment" -ForegroundColor Green
}

Write-Host "🔧 Configuring WAR support for WildFly deployment..." -ForegroundColor Cyan

foreach ($service in $services) {
    Add-WARSupport $service
}

Write-Host "🎉 WAR configuration completed!" -ForegroundColor Green
Write-Host "📝 Services configured: $($services -join ', ')" -ForegroundColor White
Write-Host "🏗️ You can now build WARs with: gradlew bootWar" -ForegroundColor Yellow