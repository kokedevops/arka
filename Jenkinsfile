pipeline {
    agent { label 'nodo-dos' }
    
    parameters {
        string(name: 'RAMA', defaultValue: 'proyecto-arka', description: 'Valor de Branch o Rama')
        choice(name: 'ACCION', choices: ['full-deploy', 'build-only', 'deploy-only'], description: 'Acción a ejecutar')
        booleanParam(name: 'SKIP_TESTS', defaultValue: false, description: 'Omitir tests durante build')
    }
    
    environment {
        // WildFly Configuration
        WILDFLY_HOME = '/opt/wildfly'
        WILDFLY_CLI = '/opt/wildfly/bin/jboss-cli.sh'
        
        // Application Configuration
        SPRING_PROFILE = 'aws'
        GRADLE_HOME = '/opt/gradle/latest'
        
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
                        cd arkavalenzuela
                        /opt/sonar/bin/sonar-scanner \\
                            -Dsonar.projectKey=arka-microservices \\
                            -Dsonar.projectName="ARKA Microservices" \\
                            -Dsonar.sources=. \\
                            -Dsonar.java.binaries=*/build/classes \\
                            -Dsonar.exclusions="**/build/**,**/gradle/**,**/*.gradle" \\
                            -Dsonar.host.url=$SONAR_HOST \\
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
                        
                        # Verificar WildFly
                        echo "🔍 Verificando WildFly..."
                        if ! pgrep -f "jboss" > /dev/null; then
                            echo "⚠️ WildFly no está ejecutándose, iniciando..."
                            sudo systemctl start wildfly
                            sleep 15
                        fi
                        
                        # Esperar conexión WildFly
                        timeout 60 bash -c '
                            while ! $WILDFLY_CLI --connect --command=":whoami" >/dev/null 2>&1; do
                                echo "Esperando conexión WildFly..."
                                sleep 3
                            done
                        '
                        
                        echo "✅ WildFly conectado"
                        
                        # Configurar DataSource para AWS
                        echo "🔧 Configurando DataSource para AWS..."
                        $WILDFLY_CLI --connect --command="
                            if (outcome != success) of /subsystem=datasources/data-source=ArkaDS:read-resource
                                /subsystem=datasources/data-source=ArkaDS:add(
                                    jndi-name=java:jboss/datasources/ArkaDS,
                                    driver-name=mysql,
                                    connection-url=jdbc:mysql://arka-db.cluster-xyz.us-east-1.rds.amazonaws.com:3306/arka_db,
                                    user-name=arka_user,
                                    password=arka_pass123,
                                    min-pool-size=5,
                                    max-pool-size=20,
                                    enabled=true
                                )
                            end-if
                        " 2>/dev/null || echo "DataSource ya existe"
                        
                        # System properties para AWS
                        $WILDFLY_CLI --connect --command="
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
                                $WILDFLY_CLI --connect --command="undeploy ${service}.war" 2>/dev/null || echo "$service no estaba desplegado"
                                sleep 3
                                
                                # Deploy nuevo WAR
                                if $WILDFLY_CLI --connect --command="deploy $PWD/$war_file"; then
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
                        $WILDFLY_CLI --connect --command="deployment-info"
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