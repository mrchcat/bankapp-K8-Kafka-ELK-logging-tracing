kubectl delete deploy blocker --ignore-not-found=true

kubectl delete service blocker --ignore-not-found=true

kubectl delete configmap blocker --ignore-not-found=true

kubectl apply -f configmap.yaml

kubectl apply -f service.yaml

kubectl apply -f deployment.yaml

