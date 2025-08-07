kubectl delete deploy cash --ignore-not-found=true

kubectl delete service cash --ignore-not-found=true

kubectl delete statefulset cash-db --ignore-not-found=true

kubectl delete service cash-db --ignore-not-found=true

kubectl delete secret cash --ignore-not-found=true

kubectl delete configmap cash --ignore-not-found=true


kubectl apply -f db.yaml

kubectl apply -f secret.yaml

kubectl apply -f configmap.yaml

kubectl apply -f service.yaml

kubectl apply -f deployment.yaml


