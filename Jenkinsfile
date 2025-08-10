pipeline {
    agent any
    environment {
        DOCKER_REGISTRY='mcat1980' //укажите наименование своего dockerhub

        DOCKER_CREDENTIAL_ID='DOCKER'
        KUBER_CREDENTIAL_ID='KUBER_CONGIG_YAML'

        FRONT_IMAGE_NAME='bank-front'
        FRONT_BUILD_NUMBER='1.0'

        ACCOUNT_IMAGE_NAME='bank-account'
        ACCOUNT_BUILD_NUMBER='1.0'

        BLOCKER_IMAGE_NAME='bank-blocker'
        BLOCKER_BUILD_NUMBER='1.0'

        CASH_IMAGE_NAME='bank-cash'
        CASH_BUILD_NUMBER='1.0'

        EXCHANGE_IMAGE_NAME='bank-exchange'
        EXCHANGE_BUILD_NUMBER='1.0'

        EXCHANGE_GENERATOR_IMAGE_NAME='bank-exchange-generator'
        EXCHANGE_GENERATOR_BUILD_NUMBER='1.0'

        NOTIFICATIONS_IMAGE_NAME='bank-notifications'
        NOTIFICATIONS_BUILD_NUMBER='1.0'

        TRANSFER_IMAGE_NAME='bank-transfer'
        TRANSFER_BUILD_NUMBER='1.0'

        PROD_NAMESPACE='prod'
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
//                 withCredentials([string(credentialsId: 'DOCKER', variable: 'TOKEN')]) {
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
        stage('Deploy to PROD') {
            steps {
                withKubeConfig([credentialsId: 'KUBER_CONGIG_YAML']) {
                    sh """
                    echo 'Deploy to PROD'
                    echo 'Устанавливаем keycloak'
                    helm install keycloak  ./helm/bankapp/charts/keycloak --namespace=$PROD_NAMESPACE --create-namespace
                    echo 'Keycloak поднимается. Ожидание 130 секунд.'
                    sleep 130
                    echo "Устанавливаем базы данных и микросервисы"
                    helm install bankapp  ./helm/bankapp --set enableKeycloak=false --namespace=$PROD_NAMESPACE
                    """
                }
            }
        }
    }
}