#задайте название namespace
nameOfNamespace="test"

echo "установка в 2 этапа: на первом развертываем keycloak, ждем его старта, а после этого стартуем микросервисы"
helm install keycloak  ./charts/keycloak --namespace=$nameOfNamespace --create-namespace
echo "разворачиваем keycloak"
sleep 130
echo "устанавливаем все остальные микросервисы"
helm install bankapp  . --set enableKeycloak=false --namespace=$nameOfNamespace --create-namespace