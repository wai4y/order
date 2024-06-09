
## Please follow bellow steps to run the application
### `export` variables in your machine
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