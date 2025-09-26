# 🛠️ Guía de Instalación de Herramientas - Proyecto Arka

## 📋 Índice de Instalación

- [🐳 Docker & Docker Compose](#-docker--docker-compose)
- [☕ Java 21](#-java-21)
- [🔧 Gradle](#-gradle)
- [🔄 Jenkins](#-jenkins)
- [📦 Nexus Repository](#-nexus-repository)
- [🔍 SonarQube](#-sonarqube)

---

## 🐳 Docker & Docker Compose

### Instalación de Docker (RedHat/CentOS/Fedora)

```bash
# 1. Instalar Docker
sudo dnf -y install docker
sudo systemctl enable --now docker

# 2. Verificar instalación
docker --version
sudo systemctl status docker
```

### Instalación de Docker Compose V2

```bash
# 1. Asegúrate de que Docker está corriendo
sudo systemctl enable --now docker

# 2. Crear directorio de plugins (si no existe)
sudo install -d /usr/libexec/docker/cli-plugins

# 3. Descargar binario de Compose v2 para tu arquitectura
ARCH=$(uname -m)   # x86_64 o aarch64
sudo curl -fL \
  "https://github.com/docker/compose/releases/latest/download/docker-compose-linux-${ARCH}" \
  -o /usr/libexec/docker/cli-plugins/docker-compose

# 4. Dar permisos de ejecución
sudo chmod +x /usr/libexec/docker/cli-plugins/docker-compose

# 5. Verificar instalación
docker compose version
```

---

## ☕ Java 21

### Instalación de Amazon Corretto 21

```bash
# 1. Instalar Java 21 Amazon Corretto
sudo dnf install -y java-21-amazon-corretto-devel

# 2. Configurar variables de entorno
echo 'export JAVA_HOME=/usr/lib/jvm/java-21-amazon-corretto' | sudo tee /etc/profile.d/java.sh
echo 'export PATH=$JAVA_HOME/bin:$PATH' | sudo tee -a /etc/profile.d/java.sh
sudo chmod +x /etc/profile.d/java.sh

# 3. Recargar variables de entorno
source /etc/profile.d/java.sh

# 4. Verificar instalación
java -version
javac -version
echo $JAVA_HOME
```

---

## 🔧 Gradle

### Instalación Manual de Gradle 8.14.3

```bash
# 1. Crear directorio para Gradle
sudo mkdir -p /opt/gradle
cd /opt/gradle

# 2. Descargar Gradle 8.14.3
wget https://services.gradle.org/distributions/gradle-8.14.3-bin.zip -P /tmp

# 3. Extraer e instalar
sudo unzip -d /opt/gradle /tmp/gradle-8.14.3-bin.zip
sudo ln -s /opt/gradle/gradle-8.14.3 /opt/gradle/latest

# 4. Configurar PATH
echo 'export PATH=$PATH:/opt/gradle/latest/bin' >> ~/.bashrc
source ~/.bashrc

# 5. Verificar instalación
gradle --version
```

---

## 🔄 Jenkins

### Instalación en RedHat/CentOS

```bash
# 1. Agregar repositorio de Jenkins
sudo wget -O /etc/yum.repos.d/jenkins.repo \
    https://pkg.jenkins.io/redhat/jenkins.repo
sudo rpm --import https://pkg.jenkins.io/redhat/jenkins.io-2023.key

# 2. Actualizar sistema
sudo yum upgrade -y

# 3. Instalar dependencias requeridas
sudo yum install -y fontconfig java-21

# 4. Instalar Jenkins
sudo yum install -y jenkins

# 5. Habilitar e iniciar Jenkins
sudo systemctl enable jenkins
sudo systemctl start jenkins
sudo systemctl status jenkins

# 6. Obtener contraseña inicial
sudo cat /var/lib/jenkins/secrets/initialAdminPassword
```

### Configuración Post-instalación

```bash
# Jenkins estará disponible en: http://your-server:8080
# Usar la contraseña inicial mostrada arriba
# Instalar plugins sugeridos
# Crear usuario administrador
```

---

## 📦 Nexus Repository

### Descarga e Instalación

```bash
# 1. Descargar Nexus 3.84.0-03
wget https://download.sonatype.com/nexus/3/nexus-3.84.0-03-linux-x86_64.tar.gz

# 2. Extraer archivos
tar -xvf nexus-3.84.0-03-linux-x86_64.tar.gz

# 3. Mover a directorio de instalación
sudo mv nexus-3.84.0-03 /opt/nexus
sudo mv sonatype-work /opt/

# 4. Crear usuario para Nexus
sudo useradd -r -d /opt/nexus -s /bin/bash nexus
sudo chown -R nexus:nexus /opt/nexus /opt/sonatype-work

# 5. Iniciar Nexus
sudo -u nexus /opt/nexus/bin/nexus start

# 6. Verificar estado
sudo -u nexus /opt/nexus/bin/nexus status
```

### Configuración

```bash
# Puerto por defecto: 8081
# URL: http://your-server:8081
# Usuario inicial: admin
# Contraseña inicial en: /opt/sonatype-work/nexus3/admin.password
sudo cat /opt/sonatype-work/nexus3/admin.password
```

---

## 🔍 SonarQube

### Requisitos del Sistema (Linux Host)

```bash
# 1. Configurar parámetros del kernel para SonarQube
sudo sysctl -w vm.max_map_count=524288
sudo sysctl -w fs.file-max=131072

# 2. Hacer cambios permanentes
echo -e "vm.max_map_count=524288\nfs.file-max=131072" | sudo tee /etc/sysctl.d/99-sonarqube.conf
sudo sysctl --system

# 3. Verificar configuración
sysctl vm.max_map_count
sysctl fs.file-max
```

### Instalación con Docker

```bash
# 1. Ejecutar SonarQube con Docker
docker run -d \
  --name sonarqube \
  -p 9000:9000 \
  -v sonarqube_data:/opt/sonarqube/data \
  -v sonarqube_logs:/opt/sonarqube/logs \
  -v sonarqube_extensions:/opt/sonarqube/extensions \
  sonarqube:community

# 2. Acceder a SonarQube
# URL: http://your-server:9000
# Usuario inicial: admin
# Contraseña inicial: admin
```

---

## 🔧 Configuración de Herramientas para el Proyecto

### Variables de Entorno Recomendadas

```bash
# Agregar al ~/.bashrc o ~/.profile
export JAVA_HOME=/usr/lib/jvm/java-21-amazon-corretto
export GRADLE_HOME=/opt/gradle/latest
export PATH=$JAVA_HOME/bin:$GRADLE_HOME/bin:$PATH

# URLs de servicios
export JENKINS_URL=http://localhost:8080
export NEXUS_URL=http://localhost:8081
export SONARQUBE_URL=http://localhost:9000
```

### Script de Verificación

```bash
#!/bin/bash
echo "🔍 Verificando instalaciones..."
echo "Java: $(java -version 2>&1 | head -n 1)"
echo "Gradle: $(gradle --version | head -n 1)"
echo "Docker: $(docker --version)"
echo "Docker Compose: $(docker compose version)"

echo "🌐 Verificando servicios..."
curl -s http://localhost:8080 > /dev/null && echo "✅ Jenkins OK" || echo "❌ Jenkins DOWN"
curl -s http://localhost:8081 > /dev/null && echo "✅ Nexus OK" || echo "❌ Nexus DOWN"
curl -s http://localhost:9000 > /dev/null && echo "✅ SonarQube OK" || echo "❌ SonarQube DOWN"
```

---

## 📝 Notas Importantes

1. **Orden de instalación recomendado**: Java → Gradle → Docker → Jenkins → Nexus → SonarQube
2. **Puertos utilizados**: 8080 (Jenkins), 8081 (Nexus), 9000 (SonarQube)
3. **Memoria RAM**: Mínimo 8GB recomendado para todas las herramientas
4. **Espacio en disco**: Mínimo 50GB libre

⚠️ **Importante**: Reiniciar la sesión después de instalar para que las variables de entorno tomen efecto.