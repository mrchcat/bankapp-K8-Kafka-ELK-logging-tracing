#задайте название namespace
nameOfNamespace="default"

echo "разворачиваем сервисы"
#Kafka
helm upgrade kafka ./bankapp/charts/kafka --install --namespace=$nameOfNamespace --create-namespace
sleep 30
#Prometheus
helm upgrade prometheus prometheus-community/prometheus --install -f ./services/prometheus/prometheus-values.yaml --namespace=$nameOfNamespace --create-namespace
sleep 30
#Keycloak
helm upgrade keycloak ./bankapp/charts/keycloak --install --namespace=$nameOfNamespace --create-namespace
#Grafana
kubectl apply -f ./services/grafana/secret.yaml --namespace=$nameOfNamespace
helm upgrade grafana grafana/grafana --install -f ./services/grafana/grafana-values.yaml --namespace=$nameOfNamespace --create-namespace --set-file dashboards.default.bankapp.json=./services/grafana/dashboards/bankapp.json
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

