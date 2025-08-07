kubectl delete deploy exchange --ignore-not-found=true

kubectl delete service exchange --ignore-not-found=true

kubectl delete statefulset exchange-db --ignore-not-found=true

kubectl delete service exchange-db --ignore-not-found=true

kubectl delete secret exchange --ignore-not-found=true

kubectl delete configmap exchange --ignore-not-found=true


kubectl apply -f db.yaml

kubectl apply -f secret.yaml

kubectl apply -f configmap.yaml

kubectl apply -f service.yaml

kubectl apply -f deployment.yaml


