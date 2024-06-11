# getting-started-reactive-crud-oracle

This project is based on the Quarkus Getting Started sample but it is using Oracle instead of PostgreSQL. See https://github.com/quarkusio/quarkus-quickstarts/tree/main/getting-started-reactive-crud.
Some minor updates for `create table` and the SQL statements.


This project uses Quarkus, the Supersonic Subatomic Java Framework.

If you want to learn more about Quarkus, please visit its website: https://quarkus.io/.

## Running the application in dev mode

You can run your application in dev mode that enables live coding using:
```shell script
./mvnw compile quarkus:dev
```

> **_NOTE:_**  Quarkus now ships with a Dev UI, which is available in dev mode only at http://localhost:8080/q/dev/.

## Packaging and running the application

The application can be packaged using:
```shell script
./mvnw package
```
It produces the `quarkus-run.jar` file in the `target/quarkus-app/` directory.
Be aware that it’s not an _über-jar_ as the dependencies are copied into the `target/quarkus-app/lib/` directory.

The application is now runnable using `java -jar target/quarkus-app/quarkus-run.jar`.

If you want to build an _über-jar_, execute the following command:
```shell script
./mvnw package -Dquarkus.package.jar.type=uber-jar
```

The application, packaged as an _über-jar_, is now runnable using `java -jar target/*-runner.jar`.

## Create image

```shell script
./mvnw compile package -DskipTests
podman build -f src/main/docker/Dockerfile.jvm -t quarkus/getting-started-reactive-crud-jvm .
```

Deploy it to a registry, eg Quay
```shell script
./mvnw compile package -DskipTests
podman build -f src/main/docker/Dockerfile.jvm -t quay.io/rbaumgar/oracle-crud-jvm .
podman push quay.io/rbaumgar/oracle-crud-jvm
podman rmi quay.io/rbaumgar/oracle-crud-jvm

```

## Run image

```shell script
podman run -i --rm -p 8080:8080 quarkus/getting-started-reactive-crud-jvm
# run with a differnet db -e ...
# also user= and password= is available
podman run -i --rm -p 8080:8080 -e quarkus.datasource.reactive.url=vertx-reactive:oracle:thin:@localhost:42699/quarkus quarkus/getting-started-reactive-crud-jvm
```

## Creating a native executable

You can create a native executable using: 
```shell script
./mvnw package -Dnative
```

Or, if you don't have GraalVM installed, you can run the native executable build in a container using: 
```shell script
./mvnw package -Dnative -Dquarkus.native.container-build=true
```

You can then execute your native executable with `./target/getting-started-reactive-rest-oracle-1.0.0-SNAPSHOT-runner`

If you want to learn more about building native executables, please consult https://quarkus.io/guides/maven-tooling.

## Provided Code

### REST

Easily start your REST Web Services

[Related guide section...](https://quarkus.io/guides/getting-started-reactive#reactive-jax-rs-resources)
