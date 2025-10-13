#задайте название namespace
nameOfNamespace="prod"

helm delete redis -n $nameOfNamespace
helm delete keycloak -n $nameOfNamespace
helm delete kafka -n $nameOfNamespace
helm delete zipkin -n $nameOfNamespace
helm delete prometheus -n $nameOfNamespace
helm delete grafana -n $nameOfNamespace
kubectl delete secret grafana-secret
helm delete logstash -n $nameOfNamespace
helm delete elasticsearch -n $nameOfNamespace
helm delete kibana -n $nameOfNamespace
helm delete account -n $nameOfNamespace
helm delete blocker -n $nameOfNamespace
helm delete cash -n $nameOfNamespace
helm delete exchange -n $nameOfNamespace
helm delete exchange-generator -n $nameOfNamespace
helm delete notifications -n $nameOfNamespace
helm delete transfer -n $nameOfNamespace
helm delete front -n $nameOfNamespace
kubectl delete serviceaccounts "pre-install-kibana-kibana" -n $nameOfNamespace
kubectl delete roles.rbac.authorization.k8s.io "pre-install-kibana-kibana" -n $nameOfNamespace
kubectl delete configmap kibana-kibana-helm-scripts -n $nameOfNamespace
kubectl delete rolebindings.rbac.authorization.k8s.io "pre-install-kibana-kibana" -n $nameOfNamespace
kubectl delete jobs.batch "pre-install-kibana-kibana" -n $nameOfNamespace
kubectl delete service kibana-kibana -n $nameOfNamespace
kubectl delete secret kibana-kibana-es-token -n $nameOfNamespace
kubectl delete deploy kibana-kibana -n $nameOfNamespace