kubectl delete deploy front --ignore-not-found=true

kubectl delete service front --ignore-not-found=true

kubectl delete secret front --ignore-not-found=true

kubectl delete configmap front --ignore-not-found=true

kubectl delete ingress ingress --ignore-not-found=true
