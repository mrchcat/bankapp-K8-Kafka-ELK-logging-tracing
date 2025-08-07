kubectl delete deploy front --ignore-not-found=true

kubectl delete service front --ignore-not-found=true

kubectl delete secret front --ignore-not-found=true

kubectl delete configmap front --ignore-not-found=true

kubectl delete ingress ingress --ignore-not-found=true

kubectl apply -f ingress.yaml

kubectl apply -f secret.yaml

kubectl apply -f configmap.yaml

kubectl apply -f service.yaml

kubectl apply -f deployment.yaml

