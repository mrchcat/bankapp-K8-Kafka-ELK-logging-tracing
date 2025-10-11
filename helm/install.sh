#задайте название namespace
nameOfNamespace="default"

#echo "разворачиваем сервисы"
#Kafka
helm upgrade kafka ./bankapp/charts/kafka --install --namespace=$nameOfNamespace --create-namespace
sleep 30
#Kibana
#Elasticsearch
helm upgrade elasticsearch elastic/elasticsearch --install -f ./services/elasticsearch/elasticsearch-values.yaml --namespace=$nameOfNamespace --create-namespace
sleep 60
helm upgrade kibana elastic/kibana  --install -f ./services/kibana/kibana-values.yaml --namespace=$nameOfNamespace --create-namespace
sleep 30
#Keycloak
helm upgrade keycloak ./bankapp/charts/keycloak --install --namespace=$nameOfNamespace --create-namespace
#Zipkin
helm upgrade zipkin zipkin/zipkin --install --namespace=$nameOfNamespace --create-namespace -f ./services/zipkin/zipkin-values.yaml
#Prometheus
helm upgrade prometheus prometheus-community/prometheus --install -f ./services/prometheus/prometheus-values.yaml --namespace=$nameOfNamespace --create-namespace
#Grafana
kubectl apply -f ./services/grafana/secret.yaml
helm upgrade grafana grafana/grafana --install -f ./services/grafana/grafana-values.yaml --namespace=$nameOfNamespace --create-namespace
#Logstash
helm upgrade logstash elastic/logstash  --install -f ./services/logstash/logstash-values.yaml --namespace=$nameOfNamespace --create-namespace
sleep 30

#echo "разворачиваем приложения"
helm upgrade bankapp ./bankapp --install --namespace=$nameOfNamespace --create-namespace --set infrastructure.enabled=false


#kubectl create secret generic grafana --from-literal=admin-user=admin --from-literal=admin-password=admin
#helm install bankapp  . --namespace=$nameOfNamespace --create-namespace

#echo "разворачиваем zipkin"
#helm install bankapp . --set zipkin.enabled=true --set infrastructure.enabled=false --set services.enabled=false

#echo "разворачиваем kafka"
#helm install kafka ./charts/kafka --namespace=$nameOfNamespace --create-namespace
#echo "разворачиваем keycloak"
#helm install keycloak  ./charts/keycloak --namespace=$nameOfNamespace --create-namespace
#
#echo "ждем две минуты, пока приложения запускаются"
#sleep 120
#echo "устанавливаем все остальные микросервисы"
#helm install bankapp  . --set enableKeycloak=false --set enableKafka=false --set enableZipkin=false --namespace=$nameOfNamespace --create-namespace