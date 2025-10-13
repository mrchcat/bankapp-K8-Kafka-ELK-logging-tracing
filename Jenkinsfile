pipeline {
    agent any
    environment {
        DOCKER_REGISTRY=credentials('DOCKER_REGISTRY') //укажите наименование своего dockerhub

        //скорректируйте, если вы присвоили другое имя при сохранении credentials в Jenkins
        DOCKER_CREDENTIAL_ID='DOCKER'
        KUBER_CREDENTIAL_ID='KUBER_CONFIG_YAML'

        PROD_NAMESPACE='prod'
        TEST_NAMESPACE='test'

        FRONT_IMAGE_NAME='bank-front'
        FRONT_BUILD_TAG='10.0'

        ACCOUNT_IMAGE_NAME='bank-account'
        ACCOUNT_BUILD_TAG='10.0'

        BLOCKER_IMAGE_NAME='bank-blocker'
        BLOCKER_BUILD_TAG='10.0'

        CASH_IMAGE_NAME='bank-cash'
        CASH_BUILD_TAG='10.0'

        EXCHANGE_IMAGE_NAME='bank-exchange'
        EXCHANGE_BUILD_TAG='10.0'

        EXCHANGE_GENERATOR_IMAGE_NAME='bank-exchange-generator'
        EXCHANGE_GENERATOR_BUILD_TAG='10.0'

        NOTIFICATIONS_IMAGE_NAME='bank-notifications'
        NOTIFICATIONS_BUILD_TAG='10.0'

        TRANSFER_IMAGE_NAME='bank-transfer'
        TRANSFER_BUILD_TAG='10.0'
    }

    stages {
        stage('Deploy to TEST') {
            steps {
                withKubeConfig([credentialsId: KUBER_CREDENTIAL_ID]) {
                    sh """
                    echo 'Deploy to TEST'

                    echo "Add helm repositories"
                    helm repo add elastic https://helm.elastic.co
                    helm repo add grafana https://grafana.github.io/helm-charts
                    helm repo add prometheus-community https://prometheus-community.github.io/helm-charts
                    helm repo add zipkin https://zipkin.io/zipkin-helm
                    helm repo update

                    echo "Deploy infrastructure"
                    echo "Kafka"
                    helm upgrade --install kafka ./helm/bankapp/charts/kafka \\
                                 --namespace=$TEST_NAMESPACE \\
                                 --create-namespace
                    echo "Elasticsearch"
                    helm upgrade --install elasticsearch elastic/elasticsearch \\
                                 -f ./services/elasticsearch/elasticsearch-values.yaml \\
                                 --namespace=$TEST_NAMESPACE \\
                                 --create-namespace
                    echo "Kibana"
                    helm upgrade --install kibana elastic/kibana  \\
                                 -f ./services/kibana/kibana-values.yaml \\
                                 --namespace=$TEST_NAMESPACE --create-namespace
                    echo "Keycloak"
                    helm upgrade --install keycloak ./helm/bankapp/charts/keycloak \\
                                 --namespace=$TEST_NAMESPACE \\
                                 --create-namespace
                    echo "Prometheus"
                    helm upgrade --install prometheus prometheus-community/prometheus \\
                                 -f ./services/prometheus/prometheus-values.yaml \\
                                 --namespace=$TEST_NAMESPACE \\
                                 --create-namespace
                    """
                }
            }
        }
    }
}