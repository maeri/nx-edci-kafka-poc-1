# EDCI

Monorepo EDCI is a monorepo style code repository that contains all the projects for the EDCI project.

## Projects

Frontend:
    
  - ngapp
  - reactapp

Backend:
  - bootparent (parent pom) - holds the common dependencies for all the backend projects
    - bootapp - Spring Boot application - kafka example 
    - bootlib - Spring Boot library - library for bootapp

## Backend

Spring boot profile to run application with:
  - docker
  - test
  - prod

e.g to run the application with docker profile

```angular2html
  nx run bootapp:run --args="-Dspring-boot.run.profiles=local"
```
e.g. to build the application with docker profile

```angular2html
  nx run bootapp:build-image --args="-Dspring.profiles.active=docker"
```

e.g. to skip the test while building the application

```angular2html
  nx run bootapp:build --args="-Dmaven.test.skip=true"
```

### Backend logging

Logging is configured in the application.yml file. The log level can be set for different packages.
Spring logging is set to ERROR and Kafka logging is set to ERROR. The application logging is set to DEBUG.

```angular2html
logging:
  level:
    org:
      # spring logging on error only
      springframework:
        web: ERROR
      # kafka logging on error only
      apache:
        kafka: ERROR
    eu:
      ec:
        empl:
          edci:
            async:
              poc: DEBUG
```

### Backend docker compose file

The backend application can be run with docker compose. The docker compose file is in the tools folder.

Bootapp is the backend application that depends on Kafka. The docker compose file runs Kafka and the backend application.
Bootapp start up script waits for Kafka to be ready before starting the application. Runs with the docker profile. 

```angular2html
    environment:
      - SPRING_PROFILES_ACTIVE=docker
```
application-docker.yml is the configuration file for the docker profile. It overrides the application.yml file when same properties are set in both files.

```angular2html
version: '3'
services:
  zookeeper:
    image: confluentinc/cp-zookeeper:latest
    environment:
      ZOOKEEPER_CLIENT_PORT: 2181

  kafka:
    image: confluentinc/cp-kafka:latest
    depends_on:
      - zookeeper
    ports:
      - "9092:9092"
    environment:
      KAFKA_BROKER_ID: 1
      KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://kafka:29092,PLAINTEXT_HOST://localhost:9092
      KAFKA_LISTENER_SECURITY_PROTOCOL_MAP: PLAINTEXT:PLAINTEXT,PLAINTEXT_HOST:PLAINTEXT
      KAFKA_INTER_BROKER_LISTENER_NAME: PLAINTEXT
      KAFKA_LISTENERS: PLAINTEXT://0.0.0.0:29092,PLAINTEXT_HOST://0.0.0.0:9092
      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1

  bootapp:
    image: bootapp:0.0.1-SNAPSHOT
    ports:
      - "8080:8080"
    depends_on:
      - kafka
    environment:
      - SPRING_PROFILES_ACTIVE=docker
    command: bash -c "
      echo 'Waiting for Kafka to be ready...' &&
      kafka-topics --bootstrap-server kafka:29092 --list &&
      echo 'Kafka is ready!' &&
      java -jar app.jar"

```

Run the backend application with docker compose

```angular2html
cd tools
docker compose up 
```



## Tools

In 'tools' folder, there are some tools that can be used to manage the monorepo.
docker-compose.yml - docker compose file to run the monorepo in a container

Passing arguments to a task
```angular2html
  nx run bootapp:build --args="--arg1=value1 --arg2=value2"
```
e.g
