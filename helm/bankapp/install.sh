#задайте название namespace
nameOfNamespace="default"

echo "разворачиваем zipkin"
helm install bankapp . --set zipkin.enabled=true --set infrastructure.enabled=false --set services.enabled=false

#echo "разворачиваем kafka"
#helm install kafka ./charts/kafka --namespace=$nameOfNamespace --create-namespace
#echo "разворачиваем keycloak"
#helm install keycloak  ./charts/keycloak --namespace=$nameOfNamespace --create-namespace
#
#echo "ждем две минуты, пока приложения запускаются"
#sleep 120
#echo "устанавливаем все остальные микросервисы"
#helm install bankapp  . --set enableKeycloak=false --set enableKafka=false --set enableZipkin=false --namespace=$nameOfNamespace --create-namespace