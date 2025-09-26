pipeline {
    agent { label 'nodo-dos' }
    
    parameters {
        string(name: 'RAMA', defaultValue: 'proyecto-arka', description: 'Valor de Branch o Rama')
        choice(name: 'ACCION', choices: ['full-deploy', 'build-only', 'deploy-only'], description: 'Acción a ejecutar')
        booleanParam(name: 'SKIP_TESTS', defaultValue: false, description: 'Omitir tests durante build')
        string(name: 'AWS_DB_HOST', defaultValue: '172.31.48.25', description: 'Host de la base de datos RDS (usar endpoint completo preferiblemente)')
        string(name: 'AWS_DB_NAME', defaultValue: 'arka-base', description: 'Nombre de la base de datos en RDS')
        string(name: 'AWS_DB_USERNAME', defaultValue: 'admin', description: 'Usuario de conexión a RDS')
        password(name: 'AWS_DB_PASSWORD', defaultValue: 'Koke1988*', description: 'Contraseña de conexión a RDS')
    }
    
    environment {
        // WildFly Configuration
        WILDFLY_HOME = '/opt/wildfly'
        WILDFLY_CLI = '/opt/wildfly/bin/jboss-cli.sh'
        WILDFLY_CONTROLLER = 'remote+http://18.117.120.169:9990'
        WILDFLY_USER = 'arka'
        WILDFLY_PASSWORD = 'Koke1988*'
        
        // Application Configuration
        SPRING_PROFILE = 'aws'
        GRADLE_HOME = '/opt/gradle/latest'
        AWS_DB_HOST = "${params.AWS_DB_HOST}"
        AWS_DB_NAME = "${params.AWS_DB_NAME}"
        AWS_DB_USERNAME = "${params.AWS_DB_USERNAME}"
        AWS_DB_PASSWORD = "${params.AWS_DB_PASSWORD}"
        
        // Nexus Configuration
        NEXUS_WEB = 'http://3.14.80.146:8081'
        NEXUS_REPO = 'repositorio-uno'
        
        // SonarQube Configuration
        SONAR_HOST = 'http://18.188.248.253:9000'
        
        // Deployment paths
        ARKA_HOME = '/opt/arka'
    }
    
    options {
        buildDiscarder(logRotator(numToKeepStr: '10'))
        timeout(time: 30, unit: 'MINUTES')
        timestamps()
    }
    
    stages {
        stage('📥 Clonar Código') {
            steps {
                script {
                    echo 'Clonando el código desde GitHub...'
                    sh '''
                        rm -rf *
                        git clone https://github.com/kokedevops/arkavalenzuela.git -b ${RAMA}
                        cd arkavalenzuela
                        echo "Commit actual: $(git rev-parse --short HEAD)"
                        
                        # CRÍTICO: Eliminar ServletInitializer problemático de API Gateway
                        echo "🔧 Eliminando ServletInitializer de API Gateway (WebFlux no lo necesita)..."
                        rm -f api-gateway/src/main/java/com/arka/gateway/ServletInitializer.java
                        
                        ls -la
                    '''
                }
            }
        }

        stage('🔍 Calidad de Código') {
            when { 
                expression { params.ACCION in ['full-deploy', 'build-only'] }
            }
            steps {
                script {
                    echo 'Ejecutando análisis SonarQube...'
                    sh '''
                        cd $WORKSPACE/arkavalenzuela
                        /opt/sonar/bin/sonar-scanner \\
                        -Dsonar.projectKey=arka \\
                        -Dsonar.sources=$WORKSPACE/arkavalenzuela/src \\
                        -Dsonar.java.binaries=$WORKSPACE/arkavalenzuela/src/main/java \\
                        -Dsonar.host.url=http://18.188.248.253:9000/ \\
                        -Dsonar.login=sqp_6b873adbd1458354e6838be57d1700d9e697f231
                    '''
                }
            }
        }
        
        stage('🔨 Compilar y Construir') {
            when { 
                expression { params.ACCION in ['full-deploy', 'build-only'] }
            }
            steps {
                script {
                    echo 'Compilando microservicios...'
                    sh '''
                        cd arkavalenzuela
                        export PATH=$PATH:$GRADLE_HOME/bin
                        
                        echo "🏗️ Construyendo todos los microservicios..."
                        gradle clean
                        
                        # Build infrastructure services (JARs)
                        echo "📦 Construyendo servicios de infraestructura (JARs)..."
                        gradle :eureka-server:bootJar :config-server:bootJar :api-gateway:bootJar --parallel
                        
                        # Build business services (WARs) 
                        echo "📦 Construyendo servicios de negocio (WARs)..."
                        if [ "$SKIP_TESTS" = "true" ]; then
                            gradle :arca-cotizador:bootWar :arca-gestor-solicitudes:bootWar --parallel -x test
                        else
                            gradle :arca-cotizador:bootWar :arca-gestor-solicitudes:bootWar --parallel
                        fi
                        
                        echo "📦 Artefactos generados:"
                        find . -name "*.jar" -o -name "*.war" | grep build/libs
                    '''
                }
            }
        }

        stage('📤 Subida a Nexus') {
            when { 
                expression { params.ACCION in ['full-deploy', 'build-only'] }
            }
            steps {
                script {
                    echo 'Publicando artefactos en Nexus...'
                    sh '''
                        cd arkavalenzuela
                        
                        # Subir WARs de servicios de negocio
                        for service in arca-cotizador arca-gestor-solicitudes; do
                            war_file="${service}/build/libs/${service}.war"
                            if [ -f "$war_file" ]; then
                                echo "📤 Subiendo ${service}.war"
                                curl -f -v -u "admin:Koke1988*" \\
                                    --upload-file "$war_file" \\
                                    "$NEXUS_WEB/repository/$NEXUS_REPO/com/arka/$service/$BUILD_NUMBER/${service}-$BUILD_NUMBER.war"
                            fi
                        done
                        
                        # Subir JARs de infraestructura y gateway
                        for service in eureka-server config-server api-gateway; do
                            jar_file="${service}/build/libs/${service}.jar"
                            if [ -f "$jar_file" ]; then
                                echo "📤 Subiendo ${service}.jar"
                                curl -f -v -u "admin:Koke1988*" \\
                                    -H "Content-Type: application/java-archive" \\
                                    --upload-file "$jar_file" \\
                                    "$NEXUS_WEB/repository/$NEXUS_REPO/com/arka/$service/$BUILD_NUMBER/${service}-$BUILD_NUMBER.jar"
                            fi
                        done
                    '''
                }
            }
        }

        stage('🚀 Desplegar en WildFly') {
            when { 
                expression { params.ACCION in ['full-deploy', 'deploy-only'] }
            }
            steps {
                script {
                    echo 'Desplegando servicios ARKA en WildFly...'
                    sh '''
                        cd arkavalenzuela
                        
                        CONTROLLER="${WILDFLY_CONTROLLER:-remote+http://18.117.120.169:9990}"
                        MANAGEMENT_USER="${WILDFLY_USER:-arka}"
                        MANAGEMENT_PASS="${WILDFLY_PASSWORD:-Koke1988*}"

                        # Verificar conexión remota a WildFly
                        echo "🔍 Verificando WildFly (${CONTROLLER})..."
                        ATTEMPTS=0
                        until "$WILDFLY_CLI" --connect --controller="$CONTROLLER" --user="$MANAGEMENT_USER" --password="$MANAGEMENT_PASS" --command=":whoami" >/dev/null 2>&1; do
                            ATTEMPTS=$((ATTEMPTS + 1))
                            if [ $ATTEMPTS -ge 20 ]; then
                                echo "❌ No se pudo establecer conexión con WildFly en $CONTROLLER"
                                exit 1
                            fi
                            echo "Esperando conexión WildFly..."
                            sleep 3
                        done
                        echo "✅ WildFly conectado"

                        run_cli() {
                            "$WILDFLY_CLI" --connect --controller="$CONTROLLER" --user="$MANAGEMENT_USER" --password="$MANAGEMENT_PASS" "$@"
                        }
                        
                        # Configurar DataSource para AWS
                        echo "🔧 Configurando DataSource para AWS..."
                        DB_HOST="${AWS_DB_HOST:-172.31.48.25}"
                        DB_NAME="${AWS_DB_NAME:-arka-base}"
                        DB_USER="${AWS_DB_USERNAME:-admin}"
                        DB_PASSWORD="${AWS_DB_PASSWORD:-Koke1988*}"
                        DB_CONNECTION_URL="jdbc:mysql://${DB_HOST}:3306/${DB_NAME}?sslMode=REQUIRED&allowPublicKeyRetrieval=true&serverTimezone=UTC"
                        RELOAD_REQUIRED=false

                        echo "🔌 Validando conectividad MySQL desde el agente Jenkins..."
                        if bash -c "timeout 5 bash -c 'cat < /dev/null > /dev/tcp/${DB_HOST}/3306'" 2>/dev/null; then
                            echo "✅ Puerto 3306 accesible desde Jenkins"
                        else
                            echo "⚠️ No se pudo abrir conexión TCP hacia ${DB_HOST}:3306 desde Jenkins. Verificar reglas de red si el despliegue falla."
                        fi

                        # Asegurar driver JDBC MySQL en WildFly
                        MYSQL_DRIVER_VERSION="${MYSQL_DRIVER_VERSION:-8.3.0}"
                        MYSQL_DRIVER_JAR="mysql-connector-j-${MYSQL_DRIVER_VERSION}.jar"
                        MYSQL_DRIVER_URL="https://repo1.maven.org/maven2/com/mysql/mysql-connector-j/${MYSQL_DRIVER_VERSION}/${MYSQL_DRIVER_JAR}"

                        if ! run_cli --command="/subsystem=datasources/jdbc-driver=mysql:read-resource" >/dev/null 2>&1; then
                            echo "⬇️  Descargando driver MySQL ${MYSQL_DRIVER_VERSION}..."
                            if [ ! -f "$MYSQL_DRIVER_JAR" ]; then
                                curl -fSL "$MYSQL_DRIVER_URL" -o "$MYSQL_DRIVER_JAR" || {
                                    echo "❌ No se pudo descargar ${MYSQL_DRIVER_JAR}"
                                    exit 1
                                }
                            fi

                            echo "🧩 Registrando módulo com.mysql en WildFly..."
                            if run_cli --command=":module-info(name=com.mysql)" >/dev/null 2>&1; then
                                echo "ℹ️ Módulo com.mysql ya existe"
                            else
                                run_cli --command="module add --name=com.mysql --resources=$PWD/${MYSQL_DRIVER_JAR} --dependencies=java.logging,javax.transaction.api" || {
                                    echo "❌ Error creando módulo com.mysql"
                                    exit 1
                                }
                                RELOAD_REQUIRED=true
                            fi

                            echo "📦 Registrando driver MySQL en WildFly..."
                            run_cli --command="/subsystem=datasources/jdbc-driver=mysql:add(driver-name=mysql,driver-module-name=com.mysql,driver-class-name=com.mysql.cj.jdbc.Driver,driver-datasource-class-name=com.mysql.cj.jdbc.MysqlDataSource,driver-xa-datasource-class-name=com.mysql.cj.jdbc.MysqlXADataSource)" || {
                                echo "❌ Error registrando driver MySQL"
                                exit 1
                            }
                            RELOAD_REQUIRED=true
                        else
                            echo "ℹ️ Driver MySQL ya registrado en WildFly"
                        fi

                        if run_cli --command="/subsystem=datasources/data-source=ArkaDS:read-resource" >/dev/null 2>&1; then
                            echo "ℹ️ DataSource ArkaDS ya existe; eliminando para recrear con la configuración actual"
                            if run_cli --command="/subsystem=datasources/data-source=ArkaDS:remove"; then
                                echo "✅ DataSource ArkaDS eliminado"
                                sleep 2
                                RELOAD_REQUIRED=true
                            else
                                echo "❌ No se pudo eliminar ArkaDS existente"
                                exit 1
                            fi
                        else
                            echo "ℹ️ DataSource ArkaDS no existe; se creará nuevo"
                        fi

                        echo "➕ Creando DataSource ArkaDS apuntando a ${DB_HOST}/${DB_NAME}"
                        run_cli --command="data-source add --name=ArkaDS --jndi-name=java:jboss/datasources/ArkaDS --driver-name=mysql --connection-url='${DB_CONNECTION_URL}' --user-name='${DB_USER}' --password='${DB_PASSWORD}' --min-pool-size=5 --max-pool-size=20 --enabled=true" || {
                            echo "❌ Error creando DataSource ArkaDS"
                            exit 1
                        }
                        RELOAD_REQUIRED=true

                        if [ "$RELOAD_REQUIRED" = "true" ]; then
                            echo "♻️ Aplicando reload de WildFly para activar cambios pendientes..."
                            run_cli --command=":reload" || {
                                echo "❌ Falló el reload de WildFly"
                                exit 1
                            }
                            echo "⏳ Esperando a que WildFly vuelva a estar disponible..."
                            sleep 5
                            RELOAD_ATTEMPTS=0
                            until run_cli --command=":whoami" >/dev/null 2>&1; do
                                RELOAD_ATTEMPTS=$((RELOAD_ATTEMPTS + 1))
                                if [ $RELOAD_ATTEMPTS -ge 20 ]; then
                                    echo "❌ WildFly no respondió tras el reload"
                                    exit 1
                                fi
                                sleep 3
                            done
                            echo "✅ WildFly recargado y activo"
                        fi

                        run_cli --command="data-source enable --name=ArkaDS" 2>/dev/null || true
                        run_cli --command="/subsystem=datasources/data-source=ArkaDS:flush-all-connection-in-pool()" 2>/dev/null || true
                        if run_cli --command="data-source test-connection-in-pool --name=ArkaDS"; then
                            echo "✅ Conexión a ArkaDS validada correctamente"
                        else
                            echo "❌ No se pudo validar la conexión del pool ArkaDS. Revisar credenciales y reglas de red (SG/NACL)."
                            exit 1
                        fi
                        
                        # System properties para AWS
                        run_cli --command="
                            /system-property=spring.profiles.active:add(value=aws)
                        " 2>/dev/null || echo "Property ya existe"
                        
                        # Detener servicios previos
                        echo "🛑 Deteniendo servicios previos..."
                        pkill -f "config-server.jar" || true
                        pkill -f "eureka-server.jar" || true  
                        pkill -f "api-gateway.jar" || true
                        sleep 5
                        
                        # Crear directorio de despliegue
                        mkdir -p $ARKA_HOME
                        
                        # Desplegar servicios de infraestructura (JARs)
                        echo "📦 Desplegando servicios de infraestructura..."
                        
                        # Config Server
                        if [ -f "config-server/build/libs/config-server.jar" ]; then
                            echo "🚀 Iniciando Config Server..."
                            cp config-server/build/libs/config-server.jar $ARKA_HOME/
                            nohup java -jar -Xms256m -Xmx512m $ARKA_HOME/config-server.jar \\
                                --spring.profiles.active=aws \\
                                > $ARKA_HOME/config-server.log 2>&1 &
                            echo "✅ Config Server iniciado"
                            sleep 10
                        fi
                        
                        # Eureka Server
                        if [ -f "eureka-server/build/libs/eureka-server.jar" ]; then
                            echo "🚀 Iniciando Eureka Server..."
                            cp eureka-server/build/libs/eureka-server.jar $ARKA_HOME/
                            nohup java -jar -Xms256m -Xmx512m $ARKA_HOME/eureka-server.jar \\
                                --spring.profiles.active=aws \\
                                > $ARKA_HOME/eureka-server.log 2>&1 &
                            echo "✅ Eureka Server iniciado"
                            sleep 15
                        fi
                        
                        # API Gateway (JAR - WebFlux no soporta WAR)
                        if [ -f "api-gateway/build/libs/api-gateway.jar" ]; then
                            echo "🚀 Iniciando API Gateway..."
                            cp api-gateway/build/libs/api-gateway.jar $ARKA_HOME/
                            nohup java -jar -Xms512m -Xmx1024m $ARKA_HOME/api-gateway.jar \\
                                --spring.profiles.active=aws \\
                                --server.port=8085 \\
                                > $ARKA_HOME/api-gateway.log 2>&1 &
                            echo "✅ API Gateway iniciado en puerto 8085"
                            sleep 15
                        fi
                        
                        # Desplegar WARs en WildFly (solo servicios de negocio)
                        echo "🏢 Desplegando WARs en WildFly..."
                        
                        for service in arca-cotizador arca-gestor-solicitudes; do
                            war_file="${service}/build/libs/${service}.war"
                            if [ -f "$war_file" ]; then
                                echo "📦 Desplegando $service..."
                                
                                # Undeploy si existe
                                run_cli --command="undeploy ${service}.war" 2>/dev/null || echo "$service no estaba desplegado"
                                sleep 3
                                
                                # Deploy nuevo WAR
                                if run_cli --command="deploy $PWD/$war_file"; then
                                    echo "✅ $service desplegado exitosamente"
                                else
                                    echo "❌ Error desplegando $service"
                                    exit 1
                                fi
                                sleep 10
                            else
                                echo "⚠️ WAR no encontrado: $war_file"
                            fi
                        done
                        
                        echo "🎉 Todos los servicios desplegados!"
                        
                        # Mostrar status
                        echo "📋 Status de deployments:"
                        run_cli --command="deployment-info"
                    '''
                }
            }
        }

        stage('🏥 Health Checks') {
            when { 
                expression { params.ACCION in ['full-deploy', 'deploy-only'] }
            }
            steps {
                script {
                    echo 'Verificando salud de servicios...'
                    sh '''
                        echo "⏳ Esperando inicialización de servicios..."
                        sleep 45
                        
                        # Health checks simples
                        echo "🔍 Verificando servicios..."
                        
                        # Config Server
                        if curl -f -s "http://localhost:8888/actuator/health" >/dev/null 2>&1; then
                            echo "✅ Config Server está saludable"
                        else
                            echo "⚠️ Config Server no responde"
                        fi
                        
                        # Eureka Server
                        if curl -f -s "http://localhost:8761/actuator/health" >/dev/null 2>&1; then
                            echo "✅ Eureka Server está saludable"
                        else
                            echo "⚠️ Eureka Server no responde"
                        fi
                        
                        # API Gateway 
                        if curl -f -s "http://localhost:8085/actuator/health" >/dev/null 2>&1; then
                            echo "✅ API Gateway está saludable"
                        else
                            echo "⚠️ API Gateway no responde"
                        fi
                        
                        # Servicios en WildFly
                        if curl -f -s "http://localhost:8080/actuator/health" >/dev/null 2>&1; then
                            echo "✅ Servicios WildFly responden"
                        else
                            echo "⚠️ Servicios WildFly no responden"
                        fi
                        
                        echo "🎉 Health checks completados"
                    '''
                }
            }
        }
    }
    
    post {
        always {
            script {
                sh '''
                    echo "📋 Recolectando logs..."
                    mkdir -p deployment-logs
                    
                    # Logs de WildFly
                    if [ -f "$WILDFLY_HOME/standalone/log/server.log" ]; then
                        tail -200 "$WILDFLY_HOME/standalone/log/server.log" > deployment-logs/wildfly-server.log
                    fi
                    
                    # Logs de servicios
                    if [ -f "$ARKA_HOME/config-server.log" ]; then
                        tail -100 "$ARKA_HOME/config-server.log" > deployment-logs/config-server.log
                    fi
                    
                    if [ -f "$ARKA_HOME/eureka-server.log" ]; then
                        tail -100 "$ARKA_HOME/eureka-server.log" > deployment-logs/eureka-server.log
                    fi
                    
                    if [ -f "$ARKA_HOME/api-gateway.log" ]; then
                        tail -100 "$ARKA_HOME/api-gateway.log" > deployment-logs/api-gateway.log
                    fi
                '''
                
                archiveArtifacts artifacts: 'deployment-logs/*.log', allowEmptyArchive: true
            }
        }
        
        success {
            echo """
            🎉 ARKA desplegado exitosamente en WildFly!
            =========================================
            Build: #${env.BUILD_NUMBER}
            Rama: ${params.RAMA}
            Perfil: AWS
            
            Servicios JAR (Standalone):
            - Config Server :8888 
            - Eureka Server :8761  
            - API Gateway :8085 (WebFlux)
            
            Servicios WAR (WildFly :8080):
            - Arca Cotizador
            - Gestor Solicitudes
            """
        }
        
        failure {
            echo """
            ❌ Error en despliegue ARKA
            ==========================
            Build: #${env.BUILD_NUMBER}
            Revisar logs para más detalles.
            """
        }
    }
}