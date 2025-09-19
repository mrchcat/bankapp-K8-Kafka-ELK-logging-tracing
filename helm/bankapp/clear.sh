#задайте название namespace
nameOfNamespace="prod"

helm delete keycloak -n $nameOfNamespace
helm delete kafka -n $nameOfNamespace
helm delete bankapp -n $nameOfNamespace
