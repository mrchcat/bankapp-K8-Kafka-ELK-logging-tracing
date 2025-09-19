#задайте название namespace
nameOfNamespace="test"

echo "разворачиваем keycloak"
helm install keycloak  ./charts/keycloak --namespace=$nameOfNamespace --create-namespace
echo "разворачиваем kafka"
helm install kafka ./charts/kafka --namespace=$nameOfNamespace --create-namespace

echo "ждем две минуты, пока приложения запускаются"
sleep 120
echo "устанавливаем все остальные микросервисы"
helm install bankapp  . --set enableKeycloak=false --set enableKafka=false --namespace=$nameOfNamespace --create-namespace