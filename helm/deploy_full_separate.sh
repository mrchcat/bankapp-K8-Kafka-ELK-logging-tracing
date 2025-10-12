#задайте название namespace
nameOfNamespace="default"

echo "разворачиваем сервисы"
#Kafka
helm upgrade kafka ./bankapp/charts/kafka --install --namespace=$nameOfNamespace --create-namespace
sleep 30
#Elasticsearch
helm upgrade elasticsearch elastic/elasticsearch --install -f ./services/elasticsearch/elasticsearch-values.yaml --namespace=$nameOfNamespace --create-namespace
sleep 30
#Kibana
helm upgrade kibana elastic/kibana  --install -f ./services/kibana/kibana-values.yaml --namespace=$nameOfNamespace --create-namespace
sleep 30
#Keycloak
helm upgrade keycloak ./bankapp/charts/keycloak --install --namespace=$nameOfNamespace --create-namespace
#Prometheus
helm upgrade prometheus prometheus-community/prometheus --install -f ./services/prometheus/prometheus-values.yaml --namespace=$nameOfNamespace --create-namespace
#Grafana
kubectl apply -f ./services/grafana/secret.yaml --namespace=$nameOfNamespace
helm upgrade grafana grafana/grafana --install -f ./services/grafana/grafana-values.yaml --namespace=$nameOfNamespace --create-namespace
#Logstash
helm upgrade logstash elastic/logstash  --install -f ./services/logstash/logstash-values.yaml --namespace=$nameOfNamespace --create-namespace
#Zipkin
helm upgrade zipkin zipkin/zipkin --install --namespace=$nameOfNamespace --create-namespace -f ./services/zipkin/zipkin-values.yaml
#Redis
helm upgrade redis ./bankapp/charts/redis --install --namespace=$nameOfNamespace --create-namespace

echo "разворачиваем приложения"
#account
helm upgrade account ./bankapp/charts/account --install --namespace=$nameOfNamespace --create-namespace
#blocker
helm upgrade blocker ./bankapp/charts/blocker --install --namespace=$nameOfNamespace --create-namespace
#cash
helm upgrade cash ./bankapp/charts/cash --install --namespace=$nameOfNamespace --create-namespace
#exchange
helm upgrade exchange ./bankapp/charts/exchange --install --namespace=$nameOfNamespace --create-namespace
#exchange-generator
helm upgrade exchange-generator ./bankapp/charts/exchange-generator --install --namespace=$nameOfNamespace --create-namespace
#notifications
helm upgrade notifications ./bankapp/charts/notifications --install --namespace=$nameOfNamespace --create-namespace
#transfer
helm upgrade transfer ./bankapp/charts/transfer --install --namespace=$nameOfNamespace --create-namespace
sleep 60
#front
helm upgrade front ./bankapp/charts/front --install --namespace=$nameOfNamespace --create-namespace

