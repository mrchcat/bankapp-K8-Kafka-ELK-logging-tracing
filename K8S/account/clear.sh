kubectl delete deploy account --ignore-not-found=true

kubectl delete service account --ignore-not-found=true

kubectl delete statefulset account-db --ignore-not-found=true

kubectl delete service account-db --ignore-not-found=true

kubectl delete secret account --ignore-not-found=true

kubectl delete configmap account --ignore-not-found=true