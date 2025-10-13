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
                    echo "Deploy infrastructure"
                    echo "Kafka"
                    helm upgrade --install kafka ./helm/bankapp/charts/kafka \\
                                 --namespace=$TEST_NAMESPACE \\
                                 --create-namespace
                    echo "Elasticsearch"
                    helm upgrade elasticsearch elastic/elasticsearch \\
                                       --install \\
                                       -f ./services/elasticsearch/elasticsearch-values.yaml \\
                                       --namespace=$TEST_NAMESPACE \\
                                       --create-namespace
                    echo "Kibana"
                    helm upgrade kibana elastic/kibana  \\
                                       --install \\
                                       -f ./services/kibana/kibana-values.yaml \\
                                       --namespace=$TEST_NAMESPACE --create-namespace
                    echo "Keycloak"
                    helm upgrade keycloak ./helm/bankapp/charts/keycloak \\
                                       --install \\
                                       --namespace=$TEST_NAMESPACE \\
                                       --create-namespace
                    echo "Prometheus"
                    helm upgrade prometheus prometheus-community/prometheus \\
                                       --install \\
                                       -f ./services/prometheus/prometheus-values.yaml \\
                                       --namespace=$TEST_NAMESPACE \\
                                       --create-namespace
                    echo "Grafana"
                    kubectl apply -f ./helm/services/grafana/secret.yaml \\
                                       --namespace=$TEST_NAMESPACE
                    helm upgrade grafana grafana/grafana \\
                                       --install \\
                                       -f ./services/grafana/grafana-values.yaml \\
                                       --namespace=$TEST_NAMESPACE \\
                                       --create-namespace
                    echo "Logstash"
                    helm upgrade logstash elastic/logstash  \\
                                       --install \\
                                       -f ./services/logstash/logstash-values.yaml \\
                                       --namespace=$TEST_NAMESPACE \\
                                       --create-namespace
                    echo "Zipkin"
                    helm upgrade zipkin zipkin/zipkin \\
                                       --install \\
                                       -f ./services/zipkin/zipkin-values.yaml \\
                                       --namespace=$TEST_NAMESPACE \\
                                       --create-namespace
                    echo "Redis"
                    helm upgrade redis ./helm/bankapp/charts/redis \\
                                       --install \\
                                       --namespace=$TEST_NAMESPACE \\
                                       --create-namespace

                    echo "Deploy main services"
                    echo "account"
                    helm upgrade account ./helm/bankapp/charts/account \\
                                       --install \\
                                       --namespace=$TEST_NAMESPACE \\
                                       --create-namespace
                                       --set image.tag=$ACCOUNT_BUILD_TAG
                    echo "blocker"
                    helm upgrade blocker ./helm/bankapp/charts/blocker \\
                                       --install \\
                                       --namespace=$TEST_NAMESPACE \\
                                       --create-namespace
                                       --set image.tag=$BLOCKER_BUILD_TAG
                    echo "cash"
                    helm upgrade cash ./helm/bankapp/charts/cash \\
                                       --install \\
                                       --namespace=$TEST_NAMESPACE \\
                                       --create-namespace
                                       --set image.tag=$CASH_BUILD_TAG
                    echo "exchange"
                    helm upgrade exchange ./helm/bankapp/charts/exchange \\
                                       --install \\
                                       --namespace=$TEST_NAMESPACE \\
                                       --create-namespace
                                       --set image.tag=$EXCHANGE_BUILD_TAG
                    echo "exchange-generator"
                    helm upgrade exchange-generator ./helm/bankapp/charts/exchange-generator \\
                                       --install \\
                                       --namespace=$TEST_NAMESPACE \\
                                       --create-namespace
                                       --set image.tag=$EXCHANGE_GENERATOR_BUILD_TAG
                    echo "notifications"
                    helm upgrade notifications ./helm/bankapp/charts/notifications \\
                                       --install \\
                                       --namespace=$TEST_NAMESPACE \\
                                       --create-namespace
                                       --set image.tag=$NOTIFICATIONS_BUILD_TAG
                    echo "transfer"
                    helm upgrade transfer ./helm/bankapp/charts/transfer \\
                                       --install \\
                                       --namespace=$TEST_NAMESPACE \\
                                       --create-namespace
                                       --set image.tag=$TRANSFER_BUILD_TAG
                    echo "front"
                    helm upgrade front ./helm/bankapp/charts/front \\
                                       --install \\
                                       --namespace=$TEST_NAMESPACE \\
                                       --create-namespace
                                       --set image.tag=$FRONT_BUILD_TAG
                    sleep 180
                    echo "приложение станет доступно по адресу $FRONT_BUILD_TAG.bankapp.internal.com"
                """
                }
            }
        }
    }
}