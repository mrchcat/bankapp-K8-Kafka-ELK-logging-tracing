kubectl delete deploy cash --ignore-not-found=true

kubectl delete service cash --ignore-not-found=true

kubectl delete statefulset cash-db --ignore-not-found=true

kubectl delete service cash-db --ignore-not-found=true

kubectl delete secret cash --ignore-not-found=true

kubectl delete configmap cash --ignore-not-found=true