helm delete elasticsearch
helm delete kibana
kubectl delete serviceaccounts "pre-install-kibana-kibana"
kubectl delete roles.rbac.authorization.k8s.io "pre-install-kibana-kibana"
kubectl delete configmap kibana-kibana-helm-scripts
kubectl delete rolebindings.rbac.authorization.k8s.io "pre-install-kibana-kibana"
kubectl delete jobs.batch "pre-install-kibana-kibana"