
# Банковское приложение

Сервис представляет собой группу микросервисов, реализующих регистрацию и авторизацию пользователей, изменение
пользовательских данных, создание и удаление банковских аккаунтов в 3 валютах, операции в наличной и безналичной форме, 
просмотр динамики курсов валют.  
Безопасность обмена сообщений между микросервисами обеспечивает Keycloak, а паттерн Service Discovery и 
Externalized Config - Kubernetes.   
![](/readme/front.png)


Версия: Java 21

Зависимости: 
* Spring Boot 3.5 
* Spring Security
* Spring WebMVC
* Spring Data
* Postgres
* Resilience4j
* Thymeleaf
* Maven
* JUnit 5
* Lombok
* Keycloak
* Ingress

Для запуска программы на локальном компьютере необходимы: 
* Maven
* Docker
* Helm
* Jenkins (плагины по умолчанию + дополнительно https://plugins.jenkins.io/kubernetes-cli/)
* Minikube 
* VirtualBox. 
Minikube v1.36.0 протестирован в связке с VirtualBox 7.1 на Windows 10. Запуск команд осуществлялся в Git Bash
Все указанные программы установлены локально на хост. 

Порядок запуска: 
1) создайте узел Kubernetes: "minikube start --driver=virtualbox" 
(при наличии ошибок попробуйте отключить проверку и увеличить ресурсы:
  * "minikube start --driver=virtualbox --no-vtx-check";
  * "minikube start --driver=virtualbox --cpus=4 --memory=30000 --no-vtx-check")
2) установите в minikube Ingress Controller: "minikube addons enable ingress"
3) в отдельном окне с правами администратора введите "minikube tunnel" и не закрывайте окно
(при возникновении ошибок в Windows может помочь смена языка по умолчанию на английский)
4) получите ip-aдрес ноды командой "minikube ip" и добавьте его в файл hosts вместе с будущим адресом сайта.
Файл hosts в Windows находится в "C:\Windows\System32\drivers\etc".
Адрес сайта будет представлен в виде \<namespace\>.bankapp.internal.com (например, "192.168.59.107 prod.bankapp.internal.com").
При развертывании с настройками по умолчанию, приложение станет доступно 
по адресам "prod.bankapp.internal.com" и "test.bankapp.internal.com", поэтому надо добавить строки
"192.168.59.107 prod.bankapp.internal.com" и "192.168.59.107 test.bankapp.internal.com", где вместо 192.168.59.107 укажите 
адрес "minikube ip"
5) Клонируйте репозиторий на свой Github  
6) Получите токен доступа на Github и сохраните его в Jenkins в раздел Credential с областью видимости "global" как 
"secret text". В Jenkins в разделе System заполните группу настроек GitHub: 
* API URL:  https://api.github.com ; 
* credential - вставьте сохраненный токен. 
7) Получите токен доступа на DockerHub и сохраните его в Jenkins раздел Credential с областью видимости "global" как
   "secret text" с credentialsId="DOCKER"
8) Найдите на хосте файл с настройкой Kubernetes (обычно находится по адресу "~/.kube/config", "C:\Users\User\.kube"). 
Скопируйте его, переименуйте в config.yaml и сохраните в Jenkins в Credentials с областью видимости "global" 
как "secret file " с credentialsId="KUBER_CONFIG_YAML"    
9) В Jenkinsfile заполните блок environment 
* DOCKER_REGISTRY - имя своего DockerHub 
* DOCKER_CREDENTIAL_ID - наименование credentialId в Jenkins, если отличается от указанных выше
* KUBER_CREDENTIAL_ID- наименование credentialId в Jenkins, если отличается от указанных выше
10) Сохраните изменения на Github
11) Настройте pipeline в Jenkins:
* создайте Item "BankApp" с типом Pipeline
* В Definition указать "Pipeline script from SCM", SCM указать свой репозиторий на github, ветка: main
12) Запустите задачу "build now"
13) Зайдите в задачу и откройте Console Output 
14) После развертывания микросервисов в тестовом пространстве, Jenkins запросит подтверждение на развертывание в продакшн. 
Дождитесь запуска микросервисов в тестовом пространстве. Проверите можно командой "kubectl get pods -n test"
- все сервисы должны быть RUN; "kubectl get ingress -n test" - ingress должен быть запущен и ADDRESS должен быть присвоен.
Если адрес не присвоен, то удостоверьтесь, что ранее вы запустили команду "minikube addons enable ingress" и "minikube tunnel"
Зайдите по адресу http://test.bankapp.internal.com/. Если он недоступен, то убедитесь, что вы внесли его в файл hosts. 
Введите логин "anna", пароль "12345" и удостоверьтесь, что все работает. 
Подтвердите в консоли Jenkins продолжение развертывания уже в production

Приложение будет доступно по адресу \<namespace\>.bankapp.internal.com (например, "prod.bankapp.internal.com")

Также можно развернуть приложение в Kubernetes без использования Jenkins. Для этого запустите скрипт helm/bankapp/install.sh

По умолчанию доступны 3 пользователя со следующими username, паролем и правами:
'anna'/'12345'/'CLIENT'; 'boris'/'12345'/'CLIENT'; 'ivanov'/'12345'/'MANAGER'
Клиенты и менеджеры имеют доступ к главной странице, тогда как зарегистрировать нового пользователя может только
менеджер (в нашем случае это 'ivanov').
При первоначальном запуске пользователи не имеют аккаунтов, для их создания поставьте галочки напротив соответствующей валюты.   

**Общая структура микросервисов**:
![](/readme/all_micro.png)

**Схема базы данных accounts:**

![](/readme/accounts_db.png)
* users - таблица пользователей
* accounts - таблица аккаунтов
* amount_blocks - таблица заблокированных средств при совершении операций с наличными средствами. При запросе на выдачу 
определенной суммы средства сначала блокируются в таблице accounts, а затем при подтверждении выдачи - списываются со счета, 
либо блокировка просто удаляется, если клиент не забрал из банкомата деньги   
* log - таблица для записи всех совершенных операций со счетами

Более подробно схема операций выдачи денег показана на схеме
![](/readme/withdrawal.png)

**Схема базы данных cash:**

![](/readme/cash_db.png)

* cash_transactions - лог для операций с наличностью   

**Схема базы данных transfer:**

![](/readme/transfer_db.png)
* transfers - лог для безналичных операций

**Схема базы данных exchange:**

![](/readme/exchange_db.png)
* exchange_rates - курсы валют на определенную дату 

**Схема базы данных notifications:**

![](/readme/notifications_db.png)
* notifications - лог пришедших и отправленных сообщений

При коммуникации между сервисами Accounts, Cash и Transfer во избежание повторного проведения уже проведенных ранее 
операций используется UUID номер транзакции. 
Сервис проверки транзакций (blocker) подтверждает или отклоняет транзакции случайным образом.
По умолчанию вероятность подтвердить транзакцию составляет CONFIRM_PROBABILITY = 0.97%  
Контракты тестируются на примере сервиса "exchange".


