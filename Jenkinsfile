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
                                 -f ./helm/services/elasticsearch/elasticsearch-values.yaml \\
                                 --namespace=$TEST_NAMESPACE \\
                                 --create-namespace
                    echo "Keycloak"
                    helm upgrade --install keycloak ./helm/bankapp/charts/keycloak \\
                                 --namespace=$TEST_NAMESPACE \\
                                 --create-namespace
                    echo "Prometheus"
                    helm upgrade --install prometheus prometheus-community/prometheus \\
                                 -f ./helm/services/prometheus/prometheus-values.yaml \\
                                 --namespace=$TEST_NAMESPACE \\
                                 --create-namespace
                    echo "Grafana"
                    kubectl apply -f ./helm/services/grafana/secret.yaml \\
                                       --namespace=$TEST_NAMESPACE
                    helm upgrade --install grafana grafana/grafana \\
                                 -f ./helm/services/grafana/grafana-values.yaml \\
                                 --namespace=$TEST_NAMESPACE \\
                                 --create-namespace
                    echo "Logstash"
                    helm upgrade --install logstash elastic/logstash  \\
                                 -f ./helm/services/logstash/logstash-values.yaml \\
                                 --namespace=$TEST_NAMESPACE \\
                                 --create-namespace
                    echo "Zipkin"
                    helm upgrade --install zipkin zipkin/zipkin \\
                                 -f ./helm/services/zipkin/zipkin-values.yaml \\
                                 --namespace=$TEST_NAMESPACE \\
                                 --create-namespace
                    echo "Redis"
                    helm upgrade --install redis ./helm/bankapp/charts/redis \\
                                 --namespace=$TEST_NAMESPACE \\
                                 --create-namespace

                    echo "Deploy main services"
                    echo "account"
                    helm upgrade --install account ./helm/bankapp/charts/account \\
                                 --namespace=$TEST_NAMESPACE \\
                                 --create-namespace
                                 --set image.tag=$ACCOUNT_BUILD_TAG
                    echo "blocker"
                    helm upgrade --install blocker ./helm/bankapp/charts/blocker \\
                                 --namespace=$TEST_NAMESPACE \\
                                 --create-namespace
                                 --set image.tag=$BLOCKER_BUILD_TAG
                    echo "cash"
                    helm upgrade --install cash ./helm/bankapp/charts/cash \\
                                 --namespace=$TEST_NAMESPACE \\
                                 --create-namespace
                                 --set image.tag=$CASH_BUILD_TAG
                    echo "exchange"
                    helm upgrade --install exchange ./helm/bankapp/charts/exchange \\
                                 --namespace=$TEST_NAMESPACE \\
                                 --create-namespace
                                 --set image.tag=$EXCHANGE_BUILD_TAG
                    echo "exchange-generator"
                    helm upgrade --install exchange-generator ./helm/bankapp/charts/exchange-generator \\
                                 --namespace=$TEST_NAMESPACE \\
                                 --create-namespace
                                 --set image.tag=$EXCHANGE_GENERATOR_BUILD_TAG
                    echo "notifications"
                    helm upgrade --install notifications ./helm/bankapp/charts/notifications \\
                                 --namespace=$TEST_NAMESPACE \\
                                 --create-namespace
                                 --set image.tag=$NOTIFICATIONS_BUILD_TAG
                    echo "transfer"
                    helm upgrade --install transfer ./helm/bankapp/charts/transfer \\
                                 --namespace=$TEST_NAMESPACE \\
                                 --create-namespace
                                 --set image.tag=$TRANSFER_BUILD_TAG
                    echo "front"
                    helm upgrade --install front ./helm/bankapp/charts/front \\
                                 --namespace=$TEST_NAMESPACE \\
                                 --create-namespace
                                 --set image.tag=$FRONT_BUILD_TAG
                    """
                }
            }
        }
    }
}