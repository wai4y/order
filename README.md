
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

**If your machine has JDK 17 installed, please run `./mvnw clean package` in the project root folder first**


### run `start.sh`
```shell
chmod +x start.sh  # In order to avoid issues with file permissions

./start.sh
```

### start application
**Command for machine with JDK 17 installed**
```shell
docker compose up -d
```

**Command for machine without JDK 17 installed**
```shell
docker compose -f docker-compose-dockerfile.yml up -d
```

## Tech Stack
* JDK 17
* Spring Boot
* Spring Data JPA
* Redisson
* Testcontainers
* Junit 5
* MySQL 8.4