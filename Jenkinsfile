pipeline {
    agent any
    environment {
        DOCKER_REGISTRY='mcat1980'
        DOCKER_CREDENTIAL_ID='DOCKER'

        FRONT_NAME='front'
        FRONT_BUILD_NUMBER='1.0'

        KUBER_CREDENTIAL_ID='KUBER_CONGIG_YAML'
    }

    stages {
        stage('Build & Unit Tests') {
            steps {
                sh 'mvn clean package'
            }
        }
    }
}