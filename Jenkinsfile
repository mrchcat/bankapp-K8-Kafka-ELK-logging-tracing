pipeline {
    agent any
    environment {
        DOCKER_REGISTRY='mcat1980' //укажите наименование своего dockerhub

        //скорректируйте, если вы присвоили другое имя при сохранении credentials в Jenkins
        DOCKER_CREDENTIAL_ID='DOCKER'
        KUBER_CREDENTIAL_ID='KUBER_CONFIG_YAML'

        //при корректировке указанных ниже параметров синхронизируйте изменения с файлом values.yaml чарта
        PROD_NAMESPACE='prod'
        TEST_NAMESPACE='test'

        FRONT_IMAGE_NAME='bank-front'
        FRONT_BUILD_NUMBER='10.0'

        ACCOUNT_IMAGE_NAME='bank-account'
        ACCOUNT_BUILD_NUMBER='10.0'

        BLOCKER_IMAGE_NAME='bank-blocker'
        BLOCKER_BUILD_NUMBER='10.0'

        CASH_IMAGE_NAME='bank-cash'
        CASH_BUILD_NUMBER='10.0'

        EXCHANGE_IMAGE_NAME='bank-exchange'
        EXCHANGE_BUILD_NUMBER='10.0'

        EXCHANGE_GENERATOR_IMAGE_NAME='bank-exchange-generator'
        EXCHANGE_GENERATOR_BUILD_NUMBER='10.0'

        NOTIFICATIONS_IMAGE_NAME='bank-notifications'
        NOTIFICATIONS_BUILD_NUMBER='10.0'

        TRANSFER_IMAGE_NAME='bank-transfer'
        TRANSFER_BUILD_NUMBER='10.0'
    }

    stages {
//         stage('Build & Unit Tests') {
//             steps {
//                 sh """
//                 echo 'Build & Unit Tests'
//                 mvn clean package
//                 """
//             }
//         }
//         stage('Build Docker Images') {
//             steps {
//                 sh """
//                 echo 'Build Docker Images'
//                 docker build ./front -t $DOCKER_REGISTRY/$FRONT_IMAGE_NAME:$FRONT_BUILD_NUMBER
//                 docker build ./account -t $DOCKER_REGISTRY/$ACCOUNT_IMAGE_NAME:$ACCOUNT_BUILD_NUMBER
//                 docker build ./blocker -t $DOCKER_REGISTRY/$BLOCKER_IMAGE_NAME:$BLOCKER_BUILD_NUMBER
//                 docker build ./cash -t $DOCKER_REGISTRY/$CASH_IMAGE_NAME:$CASH_BUILD_NUMBER
//                 docker build ./exchange -t $DOCKER_REGISTRY/$EXCHANGE_IMAGE_NAME:$EXCHANGE_BUILD_NUMBER
//                 docker build ./exchange-generator -t $DOCKER_REGISTRY/$EXCHANGE_GENERATOR_IMAGE_NAME:$EXCHANGE_GENERATOR_BUILD_NUMBER
//                 docker build ./notification -t $DOCKER_REGISTRY/$NOTIFICATIONS_IMAGE_NAME:$NOTIFICATIONS_BUILD_NUMBER
//                 docker build ./transfer -t $DOCKER_REGISTRY/$TRANSFER_IMAGE_NAME:$TRANSFER_BUILD_NUMBER
//                 """
//             }
//         }
//         stage('Push Docker Images') {
//             steps {
//                 withCredentials([string(credentialsId: DOCKER_CREDENTIAL_ID, variable: 'TOKEN')]) {
//                     sh """
//                     echo 'Push Docker Images'
//                     echo 'Начинаем аутентификацию на DockerHub'
//                     echo $TOKEN | docker login --username $DOCKER_REGISTRY --password-stdin
//                     echo 'Аутентификация успешно завершена'
//                     echo 'Переносим образы'
//                     docker push $DOCKER_REGISTRY/$FRONT_IMAGE_NAME:$FRONT_BUILD_NUMBER
//                     docker push $DOCKER_REGISTRY/$ACCOUNT_IMAGE_NAME:$ACCOUNT_BUILD_NUMBER
//                     docker push $DOCKER_REGISTRY/$BLOCKER_IMAGE_NAME:$BLOCKER_BUILD_NUMBER
//                     docker push $DOCKER_REGISTRY/$CASH_IMAGE_NAME:$CASH_BUILD_NUMBER
//                     docker push $DOCKER_REGISTRY/$EXCHANGE_IMAGE_NAME:$EXCHANGE_BUILD_NUMBER
//                     docker push $DOCKER_REGISTRY/$EXCHANGE_GENERATOR_IMAGE_NAME:$EXCHANGE_GENERATOR_BUILD_NUMBER
//                     docker push $DOCKER_REGISTRY/$NOTIFICATIONS_IMAGE_NAME:$NOTIFICATIONS_BUILD_NUMBER
//                     docker push $DOCKER_REGISTRY/$TRANSFER_IMAGE_NAME:$TRANSFER_BUILD_NUMBER
//                     """
//                 }
//             }
//         }
//         stage('Deploy to TEST') {
//             steps {
//                 withKubeConfig([credentialsId: KUBER_CREDENTIAL_ID]) {
//                     sh """
//                     echo 'Deploy to TEST'
//                     echo 'Устанавливаем kafka'
//                     helm upgrade --install kafka  ./helm/bankapp/charts/kafka \\
//                                  --namespace=$TEST_NAMESPACE \\
//                                  --create-namespace
//                     echo 'Kafka поднимается.'
//                     echo 'Устанавливаем keycloak'
//                     helm upgrade --install keycloak  ./helm/bankapp/charts/keycloak \\
//                                  --namespace=$TEST_NAMESPACE \\
//                                  --create-namespace
//                     echo 'Keycloak поднимается.'
//                     echo 'Ожидание 2 минуты.'
//                     sleep 130
//
//                     echo 'Устанавливаем базы данных и микросервисы.'
//                     helm upgrade --install bankapp  ./helm/bankapp \\
//                                  --set enableKeycloak=false \\
//                                  --set enableKafka=false \\
//                                  --namespace=$TEST_NAMESPACE
//                     echo 'Микросервисы полностью развернутся через 3-5 минут'
//                     sleep 180
//                 """
//                 }
//             }
//         }
        stage('Manual Approval for PROD') {
            steps {
                input message: 'Deploy to PROD environment?', ok: 'Yes, deploy'
            }
        }
        stage('CLear TEST') {
            steps {
               ./helm/clear.sh
            }
        }
        stage('Deploy to PROD') {
            steps {
                withKubeConfig([credentialsId: KUBER_CREDENTIAL_ID]) {
                    sh """
                    echo 'Deploy to PROD'
                    echo 'Устанавливаем kafka'
                    helm upgrade --install kafka  ./helm/bankapp/charts/kafka \\
                                 --namespace=$PROD_NAMESPACE \\
                                 --create-namespace
                    echo 'Kafka поднимается.'
                    echo 'Устанавливаем keycloak'
                    helm upgrade --install keycloak  ./helm/bankapp/charts/keycloak \\
                                 --namespace=$PROD_NAMESPACE \\
                                 --create-namespace
                    echo 'Keycloak поднимается.'
                    echo 'Ожидание 2 минуты.'
                    sleep 130

                    echo 'Устанавливаем базы данных и микросервисы.'
                    helm upgrade --install bankapp  ./helm/bankapp \\
                                 --set enableKeycloak=false \\
                                 --set enableKafka=false \\
                                 --namespace=$PROD_NAMESPACE
                    echo 'Микросервисы полностью развернутся через 3-5 минут'
                    sleep 180
                    """
                }
            }
        }
    }
}