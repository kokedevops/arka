pipeline {
    agent { label 'nodo-uno' }
    
    parameters {
        string(name: 'RAMA', defaultValue: 'proyecto-arka', description: 'Valor de Branch o Rama')
    }
    
    stages {
        stage('Clonar el codigo') {
            steps {
                script {
                    echo 'Clonando el codigo'
                    sh '''
                    rm -rf *
                    git clone https://github.com/kokedevops/arkavalenzuela.git -b ${RAMA}
                    ls -l
                    '''
                }
            }
        }

        stage('Calidad de codigo'){
            steps {
                script {
                    echo 'Ejecutando SonarQube'
                    sh '''
                    cd $WORKSPACE/arkavalenzuela
                    /opt/sonar/bin/sonar-scanner \
                        -Dsonar.projectKey=arka \
                        -Dsonar.sources=$WORKSPACE/arkavalenzuela/src \
                        -Dsonar.java.binaries=$WORKSPACE/arkavalenzuela/src/main/java \
                        -Dsonar.host.url=http://18.188.248.253:9000/ \
                        -Dsonar.login=sqp_6b873adbd1458354e6838be57d1700d9e697f231
                    '''
                }
            }
        }
        
        stage('Compila codigo') {
            steps {
                script {
                    echo 'Compilando el codigo'
                    sh '''
                    cd arkavalenzuela
                    export PATH=$PATH:/opt/gradle/latest/bin
                    gradle clean build -x test
                    '''
                }
            }
        }

        stage('Subida Nexus') {
            steps {
                script {
                    echo 'Publicando en Nexus'
                    sh '''
                    cd $WORKSPACE/arkavalenzuela/build/libs
                    ls
                    NEXUS_WEB="http://3.14.80.146:8081"
                    NEXUS_REPOSITORIO="repositorio-uno"
                    GROUP_ID="com"
                    ARTIFACT_ID="arka"
                    VERSION=$BUILD_NUMBER
                    ARTIFACT_FILE="arkajvalenzuela-0.0.1-SNAPSHOT.war"
                    ARTIFACT_PATH="$WORKSPACE/arkavalenzuela/build/libs"
                    curl -v -u "admin:Koke1988*" --upload-file "$ARTIFACT_PATH/$ARTIFACT_FILE" "$NEXUS_WEB/repository/$NEXUS_REPOSITORIO/$GROUP_ID/$ARTIFACT_ID/$VERSION/$ARTIFACT_FILE"
                    '''
                }
            }
        }

        stage('Despliega ARKA') {
            steps {
                script {
                    echo 'Desplegando ARKA en nodo-uno'
                    sh '''
                    mkdir -p /opt/pids /opt/logs /opt/arka || true
                    cp $WORKSPACE/arkavalenzuela/config-server/build/libs/config-server.jar /opt/arka
                    # matar proceso anterior si existe
                    if [ -f /opt/pids/arka.pid ]; then
                      kill -9 $(cat /opt/pids/arka.pid) || true
                      rm -f /opt/pids/arka.pid
                    fi

                    nohup java -jar -Xms512m -Xmx1024m -XX:+UseG1GC \
                      -Dspring.profiles.active=native,aws &
                    '''
                }
            }
        }
    }
}
