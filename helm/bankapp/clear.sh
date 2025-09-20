#задайте название namespace
nameOfNamespace="test"

helm delete keycloak -n $nameOfNamespace
helm delete kafka -n $nameOfNamespace
helm delete bankapp -n $nameOfNamespace
