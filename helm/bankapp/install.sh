#задайте название namespace
nameOfNamespace="default"

#echo "задать пароль для grafana"
kubectl create secret generic grafana-secret --from-literal=admin-user=admin --from-literal=admin-password=admin
#echo "установить чарт"
helm upgrade bankapp . --install --namespace=$nameOfNamespace --create-namespace
