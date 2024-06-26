#!/bin/bash

echo "Start setting up MySQL..."

MYSQL_CONTAINER_NAME="mysql-db"
NEW_USER="order"
DATABASE_NAME="order"

if [ -z "$MYSQL_ROOT_PASSWORD" ] || [ -z "$GOOGLE_MAP_API_KEY" ] || [ -z "$NEW_USER_PASSWORD" ]; then
  echo "Error: MYSQL_ROOT_PASSWORD, NEW_USER_PASSWORD and GOOGLE_MAP_API_KEY environment variable is not set."
   echo "Please export it before running the script."
  exit 1
fi

if docker ps --filter "name=$MYSQL_CONTAINER_NAME" --quiet; then
  echo "Starting MySQL container in detached mode..."
  # if no jdk 17 installed
  docker compose -f docker-compose-dockerfile.yml up -d $MYSQL_CONTAINER_NAME
else
  echo "MySQL container is already running."
fi

# Wait for MySQL to start
while ! docker exec $MYSQL_CONTAINER_NAME mysqladmin ping -h localhost -uroot -p"$MYSQL_ROOT_PASSWORD" > /dev/null 2>&1; do
    echo "Waiting for MySQL to start..."
    sleep 2
done

echo "MySQL is up! Creating user..."

SQL_COMMAND="CREATE USER IF NOT EXISTS '$NEW_USER'@'%' IDENTIFIED BY '$NEW_USER_PASSWORD';
             GRANT SELECT, INSERT, UPDATE, DELETE, ALTER, CREATE, DROP ON \`$DATABASE_NAME\`.* TO \`$NEW_USER\`@'%';
             FLUSH PRIVILEGES;"
# Create the user and grant specific privileges
docker exec -i $MYSQL_CONTAINER_NAME mysql -uroot -p"$MYSQL_ROOT_PASSWORD" -e "$SQL_COMMAND"

echo "Finish setting up MySQL."

echo "Please run 'docker compose -f docker-compose-dockerfile.yml up -d' to start the application."