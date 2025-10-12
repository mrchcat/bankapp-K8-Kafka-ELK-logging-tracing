#задайте название namespace
nameOfNamespace="default"

helm delete kibana logstash -n $nameOfNamespace
helm delete elasticsearch -n $nameOfNamespace
helm delete kibana -n $nameOfNamespace
kubectl delete serviceaccounts "pre-install-kibana-kibana" -n $nameOfNamespace
kubectl delete roles.rbac.authorization.k8s.io "pre-install-kibana-kibana" -n $nameOfNamespace
kubectl delete configmap kibana-kibana-helm-scripts -n $nameOfNamespace
kubectl delete rolebindings.rbac.authorization.k8s.io "pre-install-kibana-kibana" -n $nameOfNamespace
kubectl delete jobs.batch "pre-install-kibana-kibana" -n $nameOfNamespace
kubectl delete service kibana-kibana -n $nameOfNamespace
kubectl delete secret kibana-kibana-es-token -n $nameOfNamespace
kubectl delete deploy kibana-kibana -n $nameOfNamespace
