#to start local kafka claster with 2 topics
KAFKA_CLUSTER_ID="$(bin/kafka-storage.sh random-uuid)"
bin/kafka-storage.sh format --standalone -t "$KAFKA_CLUSTER_ID" -c config/server.properties
bin/kafka-server-start.sh config/server.properties
bin/kafka-topics.sh --create --topic bank-notifications --bootstrap-server localhost:9092
bin/kafka-topics.sh --create --topic bank-exchange-rates2 --bootstrap-server localhost:9092 --partitions 1 --replication-factor 1