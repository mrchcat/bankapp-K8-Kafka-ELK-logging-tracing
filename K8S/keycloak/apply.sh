kubectl delete deploy keycloak --ignore-not-found=true

kubectl delete service keycloak --ignore-not-found=true

kubectl delete configmap keycloak --ignore-not-found=true

kubectl apply -f configmap.yaml

kubectl apply -f deployment.yaml

kubectl apply -f service.yaml

