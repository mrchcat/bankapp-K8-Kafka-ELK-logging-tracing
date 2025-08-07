kubectl delete deploy transfer --ignore-not-found=true

kubectl delete service transfer --ignore-not-found=true

kubectl delete statefulset transfer-db --ignore-not-found=true

kubectl delete service transfer-db --ignore-not-found=true

kubectl delete secret transfer --ignore-not-found=true

kubectl delete configmap transfer --ignore-not-found=true

kubectl apply -f db.yaml

kubectl apply -f secret.yaml

kubectl apply -f configmap.yaml

kubectl apply -f service.yaml

kubectl apply -f deployment.yaml


