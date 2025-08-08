name="test"

helm install keycloak  ./charts/keycloak --namespace=$name --create-namespace
sleep 130
helm install bankapp  . --set enableKeycloak=false --namespace=$name --create-namespace