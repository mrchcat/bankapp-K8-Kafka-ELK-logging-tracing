#задайте название namespace
nameOfNamespace="default"

helm delete keycloak -n $nameOfNamespace
helm delete kafka -n $nameOfNamespace
helm delete zipkin -n $nameOfNamespace
helm delete prometheus -n $nameOfNamespace
helm delete grafana -n $nameOfNamespace
kubectl delete secret grafana-secret
helm delete logstash -n $nameOfNamespace
helm delete elasticsearch -n $nameOfNamespace
helm delete kibana -n $nameOfNamespace
helm delete bankapp -n $nameOfNamespace
