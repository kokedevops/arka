pipeline {
    agent { label 'nodo-uno' }

    environment {
        LOG_DIR   = "${WORKSPACE}/logs"
        PID_DIR   = "${WORKSPACE}/.pids"
        STARTER   = "${WORKSPACE}/start_service.sh"
    }

    stages {
        stage('Checkout') {
            steps {
                sh '''
                    rm -rf arkavalenzuela arkavalenzuela@tmp ${LOG_DIR} ${PID_DIR} ${STARTER}
                    git clone https://github.com/kokedevops/arkavalenzuela.git -b proyecto-entrega arkavalenzuela
                    mkdir -p ${LOG_DIR} ${PID_DIR}

                    # Crear script auxiliar para lanzar servicios
                    cat > ${STARTER} <<'EOF'
#!/bin/bash
NAME=$1
TASK=$2
ARGS=$3

echo "==> Iniciando $NAME..."
setsid nohup bash -lc "gradle $TASK --console=plain --no-daemon --args='$ARGS'" \
  </dev/null >> "${LOG_DIR}/${NAME}.log" 2>&1 &
echo $! > "${PID_DIR}/${NAME}.pid"
sleep 5

if ps -p $(cat ${PID_DIR}/${NAME}.pid) > /dev/null 2>&1; then
  echo "==> $NAME OK (PID: $(cat ${PID_DIR}/${NAME}.pid))"
else
  echo "⚠️ ERROR: $NAME no arrancó, revisa ${LOG_DIR}/${NAME}.log"
fi
EOF

                    chmod +x ${STARTER}
                '''
            }
        }

        stage('Arrancar base (Config Server → Eureka)') {
            steps {
                dir('arkavalenzuela') {
                    sh '''
                        ${STARTER} config-server :config-server:bootRun "--spring.profiles.active=aws" || true
                        ${STARTER} eureka-server :eureka-server:bootRun "--spring.profiles.active=aws" || true
                    '''
                }
            }
        }

        stage('Arrancar apps') {
            steps {
                dir('arkavalenzuela') {
                    script {
                        parallel(
                            "API Gateway": {
                                sh '${STARTER} api-gateway :api-gateway:bootRun "--spring.profiles.active=aws" || true'
                            },
                            "Arca Cotizador": {
                                sh '${STARTER} arca-cotizador :arca-cotizador:bootRun "--spring.profiles.active=aws" || true'
                            },
                            "Arca Gestor Solicitudes": {
                                sh '${STARTER} arca-gestor-solicitudes :arca-gestor-solicitudes:bootRun "--spring.profiles.active=aws" || true'
                            },
                            "Hello World Service": {
                                sh '${STARTER} hello-world-service :hello-world-service:bootRun "--spring.profiles.active=aws" || true'
                            },
                            "Arka App": {
                                sh '${STARTER} arka-app bootRun "--spring.profiles.active=aws" || true'
                            }
                        )
                    }
                }
            }
        }

        stage('Resumen') {
            steps {
                sh '''
                    echo "========== PIDs =========="
                    ls -l ${PID_DIR} || true
                    echo
                    echo "========== LOGS =========="
                    ls -l ${LOG_DIR} || true
                '''
            }
        }

        stage('Mantener vivo') {
            steps {
                sh '''
                    echo "🔵 Pipeline en ejecución infinita. Detén el job manualmente en Jenkins."
                    # Aseguramos al menos un archivo para tail
                    touch ${LOG_DIR}/dummy.log
                    tail -f ${LOG_DIR}/*.log || true
                '''
            }
        }
    }

    post {
        always {
            sh '''
                echo "🧹 Limpiando procesos..."
                if [ -d "${PID_DIR}" ]; then
                  for f in ${PID_DIR}/*.pid; do
                    [ -f "$f" ] || continue
                    PID=$(cat "$f")
                    echo "Matando $f con PID=$PID"
                    kill -9 $PID || true
                  done
                fi
            '''
        }
    }
}