kubectl delete deploy notifications --ignore-not-found=true

kubectl delete service notifications --ignore-not-found=true

kubectl delete statefulset notifications-db --ignore-not-found=true

kubectl delete service notifications-db --ignore-not-found=true

kubectl delete secret notifications --ignore-not-found=true

kubectl delete configmap notifications --ignore-not-found=true