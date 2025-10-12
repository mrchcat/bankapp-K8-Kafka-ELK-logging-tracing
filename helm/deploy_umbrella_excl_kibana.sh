#задайте название namespace
nameOfNamespace="prod"

kubectl create namespace $nameOfNamespace
kubectl create secret generic grafana-secret --from-literal=admin-user=admin --from-literal=admin-password=admin --namespace=$nameOfNamespace
#kibana should be deployed separately
helm upgrade bankapp ./bankapp --install --namespace=$nameOfNamespace --create-namespace --set kibana.enabled=false
