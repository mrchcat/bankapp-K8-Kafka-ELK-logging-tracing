pipeline {
    agent any
    environment {
        DOCKER_REGISTRY='mcat1980' //укажите свое имя
        DOCKER_CREDENTIAL_ID='DOCKER'

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

        KUBER_CREDENTIAL_ID='KUBER_CONGIG_YAML'
    }

    stages {
//         stage('Build & Unit Tests') {
//             steps {
//                 sh 'mvn clean package'
//             }
//         }
        stage('Build Docker Images') {
            steps {
                sh """
                docker build ./front -t $DOCKER_REGISTRY/$FRONT_IMAGE_NAME:$FRONT_IMAGE_NAME
                docker build ./account -t $DOCKER_REGISTRY/$ACCOUNT_IMAGE_NAME:$ACCOUNT_IMAGE_NAME
                docker build ./blocker -t $DOCKER_REGISTRY/$BLOCKER_IMAGE_NAME:$BLOCKER_IMAGE_NAME
                docker build ./cash -t $DOCKER_REGISTRY/$CASH_IMAGE_NAME:$CASH_IMAGE_NAME
                docker build ./exchange -t $DOCKER_REGISTRY/$EXCHANGE_IMAGE_NAME:$EXCHANGE_IMAGE_NAME
                docker build ./exchange-generator -t $DOCKER_REGISTRY/$EXCHANGE_GENERATOR_IMAGE_NAME:$EXCHANGE_GENERATOR_IMAGE_NAME
                docker build ./notification -t $DOCKER_REGISTRY/$NOTIFICATIONS_IMAGE_NAME:$NOTIFICATIONS_IMAGE_NAME
                docker build ./transfer -t $DOCKER_REGISTRY/$TRANSFER_IMAGE_NAME:$TRANSFER_IMAGE_NAME
                """
            }
        }
    }
}