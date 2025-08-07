kubectl delete deploy exchange-generator --ignore-not-found=true

kubectl delete service exchange-generator --ignore-not-found=true

kubectl delete secret exchange-generator --ignore-not-found=true

kubectl delete configmap exchange-generator --ignore-not-found=true
