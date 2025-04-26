```shell
cp src/main/resources/application.example.properties src/main/resources/application.properties

# for flyway DB_USER and DB_PASSWORD
CREATE ROLE secret WITH LOGIN PASSWORD 'your_db_password';
GRANT ALL PRIVILEGES ON DATABASE postgres TO secret;

# for flyway migration
export FLYWAY_USER=secret
export FLYWAY_PASSWORD=your_db_password
```