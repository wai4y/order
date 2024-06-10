
## Please following bellow steps to run the application
### Prerequisite
- [ ] **Having JDK 17 installed in your machine**
- [ ] **Having Docker installed in your machine**
- [ ]  **`export` variables in your machine**
```shell
    # set up Google Map API key
    export GOOGLE_MAP_API_KEY=<api-key>
    
    # set up MySQL root password
    export MYSQL_ROOT_PASSWORD=<root-password>
    
    # set up MySQL new user password for order DB
    export NEW_USER_PASSWORD=<user-password>
```

### run `start.sh`
```shell
chmod +x start.sh  # In order to avoid issues with file permissions

./start.sh 
```

### start application
```shell
docker compose up -d
```
---

### In case you don't have JDK 17 installed
You can use provided Dockerfile in the root folder
First run
```shell
chomd +x start.sh
./start_dockerfile.sh
```
Then start with:
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