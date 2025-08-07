kubectl delete deploy transfer --ignore-not-found=true

kubectl delete service transfer --ignore-not-found=true

kubectl delete statefulset transfer-db --ignore-not-found=true

kubectl delete service transfer-db --ignore-not-found=true

kubectl delete secret transfer --ignore-not-found=true

kubectl delete configmap transfer --ignore-not-found=true