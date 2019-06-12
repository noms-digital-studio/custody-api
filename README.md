# Custody API
New Tech Nomis: Offender API.

The service provides REST access to the Nomis Oracle DB offender information.

## Continuous Integration
https://circleci.com/gh/noms-digital-studio/custody-api

## Gradle commands

### Build and run tests
```
./gradlew build
```

### Assemble the app
```
./gradlew assemble
```

This makes the JAR executable by including a manifest. 

### Start the application dev profile
This profile starts the application additional configuration. This mode uses an in memory H2 (empty) database and is
populated with a sample data set

```
SPRING_PROFILES_ACTIVE=dev java -jar build/libs/custody-api.jar
```

### Start the application with Nomis Oracle db
```
SPRING_PROFILES_ACTIVE=oracle SPRING_DATASOURCE_URL=jdbc:oracle:thin:@<VM Oracle IP address>:1521:<sid> java -jar build/libs/custody-api.jar
```

### Additional configuration
The application is configured with conventional Spring parameters.
The Spring documentation can be found here:

https://docs.spring.io/spring-boot/docs/current/reference/html/common-application-properties.html

### Default port
Starts the application on port '8080'.
To override, set server.port (eg SERVER_PORT=8099 java -jar etc etc)

## Documentation
http://localhost:8080/api/swagger-ui.html

## Application health and info

- `/ping`: will respond `pong` to all requests.  This should be used by dependent systems to check connectivity to custody,
rather than calling the `/health` endpoint.
- `/health`: provides information about the application health and its dependencies.  This should only be used
by custody health monitoring (e.g. pager duty) and not other systems who wish to find out the state of custody.
- `/info`: provides information about the version of deployed application.

## Endpoints curl examples

### List offenders details
```
curl -X GET http://localhost:8080/offenders'
```

### Application info
```
curl -X GET http://localhost:8080/info
```

### Application health
```
curl -X GET http://localhost:8080/health
```

### Application Ping Endpoint
```
curl -X GET http://localhost:8080/ping
```




