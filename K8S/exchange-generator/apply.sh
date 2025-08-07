kubectl delete deploy exchange-generator --ignore-not-found=true

kubectl delete service exchange-generator --ignore-not-found=true

kubectl delete secret exchange-generator --ignore-not-found=true

kubectl delete configmap exchange-generator --ignore-not-found=true

kubectl apply -f secret.yaml

kubectl apply -f configmap.yaml

kubectl apply -f service.yaml

kubectl apply -f deployment.yaml

