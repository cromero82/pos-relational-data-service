# Installation

## Running the application

Use the following command to run the microservice on port 8080:

```bash
java -jar pos-relational-data-service-0.0.1-SNAPSHOT.jar --server.port=8080
```

## Database Setup in PostgreSQL

Setting up a new database in PostgreSQL is a two-connection process.

**Important Note:** PostgreSQL does not have a `USE database_name;` command like MySQL or SQL Server. You cannot switch databases in the middle of a script. You must disconnect and reconnect to the new database.

---

**Step 1: Create the User and Database**

Connect to your PostgreSQL server with any SQL client. Your initial connection will typically be to the default `postgres` database. From there, run the following script:

```sql
-- This script creates the user and database as defined in application.properties.
-- It must be run from a maintenance database like 'postgres'.

CREATE USER "romax-admin" WITH PASSWORD 'f4ast3rv3rs10n*';

CREATE DATABASE controlneg_rmx_db;

GRANT ALL PRIVILEGES ON DATABASE controlneg_rmx_db TO "romax-admin";
```

---

**Step 2: Create the Schema and Tables**

Now, you must **end your current database connection** and **start a new connection** directly to the `controlneg_rmx_db` database you just created.

Once you are connected to `controlneg_rmx_db`, you can run scripts to create your schema and tables. The following script creates the schema and ensures the `romax-admin` user will use it by default for any new tables.

```sql
-- This script must be run AFTER connecting to the 'controlneg_rmx_db' database.

-- Create the schema for the application
CREATE SCHEMA "romax-admin";

-- Grant privileges on the new schema to the user
GRANT ALL ON SCHEMA "romax-admin" TO "romax-admin";

-- This command makes it so that any future tables created by 'romax-admin'
-- will automatically be placed inside the 'romax-admin' schema.
ALTER USER "romax-admin" IN DATABASE controlneg_rmx_db SET search_path = "romax-admin", public;

-- You can now run your CREATE TABLE scripts here. For example:
-- CREATE TABLE my_table (id serial primary key, name varchar(100));
-- This table will be created in the "romax-admin" schema.
```
