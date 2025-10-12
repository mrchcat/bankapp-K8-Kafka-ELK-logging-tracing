#задайте название namespace
nameOfNamespace="default"

helm delete kibana logstash -n $nameOfNamespace
helm delete elasticsearch -n $nameOfNamespace
helm delete kibana -n $nameOfNamespace
kubectl delete serviceaccounts "pre-install-bankapp-kibana" -n $nameOfNamespace
kubectl delete roles.rbac.authorization.k8s.io "pre-install-bankapp-kibana" -n $nameOfNamespace
kubectl delete configmap "bankapp-kibana-helm-scripts" -n $nameOfNamespace
kubectl delete rolebindings.rbac.authorization.k8s.io "pre-install-bankapp-kibana" -n $nameOfNamespace
kubectl delete jobs.batch "pre-install-bankapp-kibana" -n $nameOfNamespace
kubectl delete service "bankapp-kibana" -n $nameOfNamespace
kubectl delete secret "bankapp-kibana-es-token" -n $nameOfNamespace
kubectl delete deploy "bankapp-kibana" -n $nameOfNamespace
