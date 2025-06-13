# Kafka Test Metrics

This project explores options to monitor an Apache Kafka deployment.

## Disclaimer

The code and/or instructions here available are NOT intended for production usage. It's only meant to serve as an example or reference and does not replace the need to follow actual and official documentation of referenced products.

## Prerequisites

This project assumes that you already have:

- A Linux/UNIX environment.
- [Java 17](https://www.oracle.com/java/technologies/downloads/#java21), [Gradle 8.12](https://gradle.org/install/) and [Docker](https://docs.docker.com/engine/install/) installed.

## Usage

To start the environment, simply run

```shell
docker compose up -d
```

And an environment with:
- 3 kafka brokers
- 3 kafka controllers
- 1 kafka-exporter instance
- 1 prometheus instance
- 1 grafana instance
- 1 producer
- 1 consumer group with a single consumer
- 1 consumer group with 3 consumers

Once everything has started, you can access Grafana at https://localhost:3000 using the credentials admin/prom-operator (configurable in the docker-compose file) and two dashboards will be available:
- Kafka Overview provides a general overview of the status of the cluster
- Kafka Topics and Consumer Groups provides specific details on topics or condumer groups behaviours

### Additional instructions:

You can manually start new producers and/or consumers with the following commands:

```shell
java -cp clients/build/libs/java-client-0.0.1.jar example.SimpleProducer ./clients/local.client.properties

java -cp clients/build/libs/java-client-0.0.1.jar example.SimpleConsumer ./clients/local.client.properties

```

Modify the config of both in the ./clients/local.client.properties file to adapt it to your needs. The changes in the producing and consuming patterns should be reflected in the Grafana dashboards.

Finally, when finished, just run

```shell
docker compose down -v
```

to stop everything and clean the environment.

### Rebalancing

If the partitions of the cluster are not well balanced among the brokers, you can execute the following command to ensure a correct balance:

```shell
kafka-reassign-partitions --bootstrap-server localhost:19092 --reassignment-json-file ./etc/cluster-reassignment.json --execute
```

For this, you need to either have a version of kafka installed locally, or make sure the cluster-reassignment.json is available from any of the broker or controller containers and execute the command inside that container.
