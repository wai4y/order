
## Please following bellow steps to run the application
### `export` variables in your machine
```shell
    # set up Google Map API key
    export GOOGLE_MAP_API_KEY=<api-key>
    
    # set up MySQL root password
    export MYSQL_ROOT_PASSWORD=<root-password>
    
    # set up MySQL new user password for order DB
    export NEW_USER_PASSWORD=<user-password>
```

PS: since run `./mvnw package` needs to install JDK first, I attached one jar file in root folder for Dockerfile usage
then you don't have to set up `JAVA_HOME` 

### run `start.sh`
```shell
chmod +x start.sh  # In order to avoid issues with file permissions

./start.sh
```

### start application
```shell
docker compose up -d
```

## Tech Stack
* JDK 17
* Spring Boot
* Spring Data JPA
* Redisson
* Testcontainers
* Junit 5
* MySQL 8.4