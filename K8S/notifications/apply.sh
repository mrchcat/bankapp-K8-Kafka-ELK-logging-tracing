kubectl delete deploy notifications --ignore-not-found=true

kubectl delete service notifications --ignore-not-found=true

kubectl delete statefulset notifications-db --ignore-not-found=true

kubectl delete service notifications-db --ignore-not-found=true

kubectl delete secret notifications --ignore-not-found=true

kubectl delete configmap notifications --ignore-not-found=true

kubectl apply -f db.yaml

kubectl apply -f secret.yaml

kubectl apply -f configmap.yaml

kubectl apply -f service.yaml

kubectl apply -f deployment.yaml


