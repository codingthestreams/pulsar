---
id: io-debezium-source
title: Debezium source connector
sidebar_label: "Debezium source connector"
---

The Debezium source connector pulls messages from MySQL or PostgreSQL and persists the messages to Pulsar topics.

## Configuration

The configuration of the Debezium source connector has the following properties.

| Name | Required | Default | Description |
|------|----------|---------|-------------|
| `task.class` | true | null | A source task class that implemented in Debezium. |
| `database.hostname` | true | null | The address of a database server. |
| `database.port` | true | null | The port number of a database server.|
| `database.user` | true | null | The name of a database user that has the required privileges. |
| `database.password` | true | null | The password for a database user that has the required privileges. |
| `database.server.id` | true | null | The connector’s identifier that must be unique within a database cluster and similar to the database’s server-id configuration property. |
| `database.server.name` | true | null | The logical name of a database server/cluster, which forms a namespace and it is used in all the names of Kafka topics to which the connector writes, the Kafka Connect schema names, and the namespaces of the corresponding Avro schema when the Avro Connector is used. |
| `database.whitelist` | false | null | A list of all databases hosted by this server which is monitored by the  connector.<br /><br /> This is optional, and there are other properties for listing databases and tables to include or exclude from monitoring. |
| `key.converter` | true | null | The converter provided by Kafka Connect to convert record key. |
| `value.converter` | true | null | The converter provided by Kafka Connect to convert record value.  |
| `database.history` | true | null | The name of the database history class. |
| `database.history.pulsar.topic` | true | null | The name of the database history topic where the connector writes and recovers DDL statements. <br /><br />**Note: this topic is for internal use only and should not be used by consumers.** |
| `database.history.pulsar.service.url` | true | null | Pulsar cluster service URL for history topic. |
| `offset.storage.topic` | true | null | Record the last committed offsets that the connector successfully completes. |
| `json-with-envelope` | false | false | Present the message that only consists of payload. |
| `database.history.pulsar.reader.config` | false | null | The configs of the reader for the database schema history topic, in the form of a JSON string with key-value pairs. |
| `offset.storage.reader.config` | false | null | The configs of the reader for the kafka connector offsets topic, in the form of a JSON string with key-value pairs. |

### Converter Options

1. org.apache.kafka.connect.json.JsonConverter

This config `json-with-envelope` is valid only for the JsonConverter. The default value is false, and the consumer uses the schema `Schema.KeyValue(Schema.AUTO_CONSUME(), Schema.AUTO_CONSUME(), KeyValueEncodingType.SEPARATED)`, and the message only consist of payload.

If the config `json-with-envelope` value is true, the consumer uses the schema `Schema.KeyValue(Schema.BYTES, Schema.BYTES`, the message consists of schema and payload.

2. org.apache.pulsar.kafka.shade.io.confluent.connect.avro.AvroConverter

If users select the AvroConverter, then the pulsar consumer should use the schema `Schema.KeyValue(Schema.AUTO_CONSUME(),
Schema.AUTO_CONSUME(), KeyValueEncodingType.SEPARATED)`, and the message consist of payload.

### MongoDB Configuration
| Name | Required | Default | Description |
|------|----------|---------|-------------|
| `mongodb.hosts` | true | null | The comma-separated list of hostname and port pairs (in the form 'host' or 'host:port') of the MongoDB servers in the replica set. The list contains a single hostname and a port pair. If mongodb.members.auto.discover is set to false, the host and port pair are prefixed with the replica set name (e.g., rs0/localhost:27017). |
| `mongodb.name` | true | null | A unique name that identifies the connector and/or MongoDB replica set or shared cluster that this connector monitors. Each server should be monitored by at most one Debezium connector, since this server name prefixes all persisted Kafka topics emanating from the MongoDB replica set or cluster. |
| `mongodb.user` | true | null | Name of the database user to be used when connecting to MongoDB. This is required only when MongoDB is configured to use authentication. |
| `mongodb.password` | true | null | Password to be used when connecting to MongoDB. This is required only when MongoDB is configured to use authentication. |
| `mongodb.task.id` | true | null | The taskId of the MongoDB connector that attempts to use a separate task for each replica set. |

### Customize the Reader config for the metadata topics

The Debezium Connector exposes `database.history.pulsar.reader.config` and `offset.storage.reader.config` to configure the reader of database schema history topic and the Kafka connector offsets topic. For example, it can be used to configure the subscription name and other reader configurations. You can find the available configurations at [ReaderConfigurationData](https://github.com/apache/pulsar/blob/master/pulsar-client/src/main/java/org/apache/pulsar/client/impl/conf/ReaderConfigurationData.java).

For example, to configure the subscription name for both Readers, you can add the following configuration:
* JSON

   ```json
   {
     "configs": {
        "database.history.pulsar.reader.config": "{\"subscriptionName\":\"history-reader\"}",
        "offset.storage.reader.config": "{\"subscriptionName\":\"offset-reader\"}",
     }
   }
   ```

* YAML

   ```yaml
   configs:
      database.history.pulsar.reader.config: "{\"subscriptionName\":\"history-reader\"}"
      offset.storage.reader.config: "{\"subscriptionName\":\"offset-reader\"}"
   ```

## Example of MySQL

You need to create a configuration file before using the Pulsar Debezium connector.

### Configuration

You can use one of the following methods to create a configuration file.

* JSON

  ```json
  {
     "configs": {
        "database.hostname": "localhost",
        "database.port": "3306",
        "database.user": "debezium",
        "database.password": "dbz",
        "database.server.id": "184054",
        "database.server.name": "dbserver1",
        "database.whitelist": "inventory",
        "database.history": "org.apache.pulsar.io.debezium.PulsarDatabaseHistory",
        "database.history.pulsar.topic": "history-topic",
        "database.history.pulsar.service.url": "pulsar://127.0.0.1:6650",
        "key.converter": "org.apache.kafka.connect.json.JsonConverter",
        "value.converter": "org.apache.kafka.connect.json.JsonConverter",
        "offset.storage.topic": "offset-topic"
     }
  }
  ```

* YAML

  You can create a `debezium-mysql-source-config.yaml` file and copy the [contents](https://github.com/apache/pulsar/blob/master/pulsar-io/debezium/mysql/src/main/resources/debezium-mysql-source-config.yaml) below to the `debezium-mysql-source-config.yaml` file.

  ```yaml
  tenant: "public"
  namespace: "default"
  name: "debezium-mysql-source"
  inputs: [ "debezium-mysql-topic" ]
  archive: "connectors/pulsar-io-debezium-mysql-@pulsar:version@.nar"
  parallelism: 1

  configs:

      ## config for mysql, docker image: debezium/example-mysql:0.8
      database.hostname: "localhost"
      database.port: "3306"
      database.user: "debezium"
      database.password: "dbz"
      database.server.id: "184054"
      database.server.name: "dbserver1"
      database.whitelist: "inventory"
      database.history: "org.apache.pulsar.io.debezium.PulsarDatabaseHistory"
      database.history.pulsar.topic: "history-topic"
      database.history.pulsar.service.url: "pulsar://127.0.0.1:6650"

      ## KEY_CONVERTER_CLASS_CONFIG, VALUE_CONVERTER_CLASS_CONFIG
      key.converter: "org.apache.kafka.connect.json.JsonConverter"
      value.converter: "org.apache.kafka.connect.json.JsonConverter"

      ## OFFSET_STORAGE_TOPIC_CONFIG
      offset.storage.topic: "offset-topic"
  ```

### Usage

This example shows how to change the data of a MySQL table using the Pulsar Debezium connector.

1. Start a MySQL server with a database from which Debezium can capture changes.

   ```bash
   docker run -it --rm \
   --name mysql \
   -p 3306:3306 \
   -e MYSQL_ROOT_PASSWORD=debezium \
   -e MYSQL_USER=mysqluser \
   -e MYSQL_PASSWORD=mysqlpw debezium/example-mysql:0.8
   ```

2. Start a Pulsar service locally in standalone mode.

   ```bash
   bin/pulsar standalone
   ```

3. Start the Pulsar Debezium connector in local run mode using one of the following methods.

    * Use the **JSON** configuration file as shown previously. 
   
       Make sure the NAR file is available at `connectors/pulsar-io-debezium-mysql-@pulsar:version@.nar`.

       ```bash
       bin/pulsar-admin source localrun \
       --archive connectors/pulsar-io-debezium-mysql-@pulsar:version@.nar \
       --name debezium-mysql-source --destination-topic-name debezium-mysql-topic \
       --tenant public \
       --namespace default \
       --source-config '{"database.hostname": "localhost","database.port": "3306","database.user": "debezium","database.password": "dbz","database.server.id": "184054","database.server.name": "dbserver1","database.whitelist": "inventory","database.history": "org.apache.pulsar.io.debezium.PulsarDatabaseHistory","database.history.pulsar.topic": "history-topic","database.history.pulsar.service.url": "pulsar://127.0.0.1:6650","key.converter": "org.apache.kafka.connect.json.JsonConverter","value.converter": "org.apache.kafka.connect.json.JsonConverter","pulsar.service.url": "pulsar://127.0.0.1:6650","offset.storage.topic": "offset-topic"}'
       ```

     :::note

     Currently, the destination topic (specified by the `destination-topic-name` option ) is a required configuration but it is not used for the Debezium connector to save data. The Debezium connector saves data in the following 4 types of topics:
      
       - One topic named with the database server name (`database.server.name`) for storing the database metadata messages, such as `public/default/database.server.name`.
       - One topic (`database.history.pulsar.topic`) for storing the database history information. The connector writes and recovers DDL statements on this topic.
       - One topic (`offset.storage.topic`) for storing the offset metadata messages. The connector saves the last successfully-committed offsets on this topic.
       - One per-table topic. The connector writes change events for all operations that occur in a table to a single Pulsar topic that is specific to that table.

     If the automatic topic creation is disabled on your broker, you need to manually create the above 4 types of topics and the destination topic.

     :::

   * Use the **YAML** configuration file as shown previously.

       ```bash
       bin/pulsar-admin source localrun \
       --source-config-file debezium-mysql-source-config.yaml
       ```

4. Subscribe to the topic _sub-products_ for the table _inventory.products_.

   ```bash
   bin/pulsar-client consume -s "sub-products" public/default/dbserver1.inventory.products -n 0
   ```

5. Start a MySQL client in docker.

   ```bash
   docker run -it --rm \
   --name mysqlterm \
   --link mysql \
   --rm mysql:5.7 sh \
   -c 'exec mysql -h"$MYSQL_PORT_3306_TCP_ADDR" -P"$MYSQL_PORT_3306_TCP_PORT" -uroot -p"$MYSQL_ENV_MYSQL_ROOT_PASSWORD"'
   ```

6. A MySQL client pops out.

   Use the following commands to change the data of the table _products_.

   ```bash
   mysql> use inventory;
   mysql> show tables;
   mysql> SELECT * FROM  products;
   mysql> UPDATE products SET name='1111111111' WHERE id=101;
   mysql> UPDATE products SET name='1111111111' WHERE id=107;
   ```

   In the terminal window of subscribing topic, you can find the data changes have been kept in the _sub-products_ topic.

## Example of PostgreSQL

You need to create a configuration file before using the Pulsar Debezium connector.

### Configuration

You can use one of the following methods to create a configuration file.

* JSON

  ```json
  {
      "database.hostname": "localhost",
      "database.port": "5432",
      "database.user": "postgres",
      "database.password": "changeme",
      "database.dbname": "postgres",
      "database.server.name": "dbserver1",
      "plugin.name": "pgoutput",
      "schema.whitelist": "public",
      "table.whitelist": "public.users",
      "database.history.pulsar.service.url": "pulsar://127.0.0.1:6650"
  }
  ```

* YAML

  You can create a `debezium-postgres-source-config.yaml` file and copy the [contents](https://github.com/apache/pulsar/blob/master/pulsar-io/debezium/postgres/src/main/resources/debezium-postgres-source-config.yaml) below to the `debezium-postgres-source-config.yaml` file.

  ```yaml
  tenant: "public"
  namespace: "default"
  name: "debezium-postgres-source"
  inputs: [ "debezium-postgres-topic" ]
  archive: "connectors/pulsar-io-debezium-postgres-@pulsar:version@.nar"
  parallelism: 1

  configs:

      ## config for postgres version 10+, official docker image: postgres:<10+>
      database.hostname: "localhost"
      database.port: "5432"
      database.user: "postgres"
      database.password: "changeme"
      database.dbname: "postgres"
      database.server.name: "dbserver1"
      plugin.name: "pgoutput"
      schema.whitelist: "public"
      table.whitelist: "public.users"

      ## PULSAR_SERVICE_URL_CONFIG
      database.history.pulsar.service.url: "pulsar://127.0.0.1:6650"
  ```

Notice that `pgoutput` is a standard plugin of Postgres introduced in version 10 - see [Postgres architecture docu](https://www.postgresql.org/docs/10/logical-replication-architecture.html). You don't need to install anything, just make sure the WAL level is set to `logical` (see docker command below and [Postgres docu](https://www.postgresql.org/docs/current/runtime-config-wal.html)).

### Usage

This example shows how to change the data of a PostgreSQL table using the Pulsar Debezium connector.


1. Start a PostgreSQL server with a database from which Debezium can capture changes.

   ```bash
   docker run -d -it --rm \
   --name pulsar-postgres \
   -p 5432:5432 \
   -e POSTGRES_PASSWORD=changeme \
   postgres:13.3 -c wal_level=logical
   ```

2. Start a Pulsar service locally in standalone mode.

   ```bash
   bin/pulsar standalone
   ```

3. Start the Pulsar Debezium connector in local run mode using one of the following methods.

   * Use the **JSON** configuration file as shown previously. 
    
     Make sure the NAR file is available at `connectors/pulsar-io-debezium-postgres-@pulsar:version@.nar`.

       ```bash
       bin/pulsar-admin source localrun \
       --archive connectors/pulsar-io-debezium-postgres-@pulsar:version@.nar \
       --name debezium-postgres-source \
       --destination-topic-name debezium-postgres-topic \
       --tenant public \
       --namespace default \
       --source-config '{"database.hostname": "localhost","database.port": "5432","database.user": "postgres","database.password": "changeme","database.dbname": "postgres","database.server.name": "dbserver1","schema.whitelist": "public","table.whitelist": "public.users","pulsar.service.url": "pulsar://127.0.0.1:6650"}'
       ```

     :::note

     Currently, the destination topic (specified by the `destination-topic-name` option ) is a required configuration but it is not used for the Debezium connector to save data. The Debezium connector saves data in the following 4 types of topics:

       - One topic named with the database server name (`database.server.name`) for storing the database metadata messages, such as `public/default/database.server.name`.
       - One topic (`database.history.pulsar.topic`) for storing the database history information. The connector writes and recovers DDL statements on this topic.
       - One topic (`offset.storage.topic`) for storing the offset metadata messages. The connector saves the last successfully-committed offsets on this topic.
       - One per-table topic. The connector writes change events for all operations that occur in a table to a single Pulsar topic that is specific to that table.

     If the automatic topic creation is disabled on your broker, you need to manually create the above 4 types of topics and the destination topic.

     :::

   * Use the **YAML** configuration file as shown previously.

       ```bash
       bin/pulsar-admin source localrun  \
       --source-config-file debezium-postgres-source-config.yaml
       ```

4. Subscribe to the topic _sub-users_ for the _public.users_ table.

   ```bash
   bin/pulsar-client consume -s "sub-users" public/default/dbserver1.public.users -n 0
   ```

5. Start a PostgreSQL client in docker.

   ```bash
   docker exec -it pulsar-postgresql /bin/bash
   ```

6. A PostgreSQL client pops out.

   Use the following commands to create sample data in the table _users_.

   ```bash
   psql -U postgres -h localhost -p 5432
   Password for user postgres:

   CREATE TABLE users(
     id BIGINT GENERATED ALWAYS AS IDENTITY, PRIMARY KEY(id),
     hash_firstname TEXT NOT NULL,
     hash_lastname TEXT NOT NULL,
     gender VARCHAR(6) NOT NULL CHECK (gender IN ('male', 'female'))
   );

   INSERT INTO users(hash_firstname, hash_lastname, gender)
     SELECT md5(RANDOM()::TEXT), md5(RANDOM()::TEXT), CASE WHEN RANDOM() < 0.5 THEN 'male' ELSE 'female' END FROM generate_series(1, 100);

   postgres=# select * from users;

     id   |          hash_firstname          |          hash_lastname           | gender
   -------+----------------------------------+----------------------------------+--------
        1 | 02bf7880eb489edc624ba637f5ab42bd | 3e742c2cc4217d8e3382cc251415b2fb | female
        2 | dd07064326bb9119189032316158f064 | 9c0e938f9eddbd5200ba348965afbc61 | male
        3 | 2c5316fdd9d6595c1cceb70eed12e80c | 8a93d7d8f9d76acfaaa625c82a03ea8b | female
        4 | 3dfa3b4f70d8cd2155567210e5043d2b | 32c156bc28f7f03ab5d28e2588a3dc19 | female


   postgres=# UPDATE users SET hash_firstname='maxim' WHERE id=1;
   UPDATE 1
   ```

   In the terminal window of subscribing topic, you can receive the following messages.

   ```bash
   ----- got message -----
   {"before":null,"after":{"id":1,"hash_firstname":"maxim","hash_lastname":"292113d30a3ccee0e19733dd7f88b258","gender":"male"},"source:{"version":"1.0.0.Final","connector":"postgresql","name":"foobar","ts_ms":1624045862644,"snapshot":"false","db":"postgres","schema":"public","table":"users","txId":595,"lsn":24419784,"xmin":null},"op":"u","ts_ms":1624045862648}
   ...many more
   ```

## Example of MongoDB

You need to create a configuration file before using the Pulsar Debezium connector.

### Configuration

You can use one of the following methods to create a configuration file.

* JSON

  ```json
  {
      "mongodb.hosts": "rs0/mongodb:27017",
      "mongodb.name": "dbserver1",
      "mongodb.user": "debezium",
      "mongodb.password": "dbz",
      "mongodb.task.id": "1",
      "database.whitelist": "inventory",
      "database.history.pulsar.service.url": "pulsar://127.0.0.1:6650"
  }
  ```

* YAML

  You can create a `debezium-mongodb-source-config.yaml` file and copy the [contents](https://github.com/apache/pulsar/blob/master/pulsar-io/debezium/mongodb/src/main/resources/debezium-mongodb-source-config.yaml) below to the `debezium-mongodb-source-config.yaml` file.

  ```yaml
  tenant: "public"
  namespace: "default"
  name: "debezium-mongodb-source"
  inputs: [ "debezium-mongodb-topic" ]
  archive: "connectors/pulsar-io-debezium-mongodb-@pulsar:version@.nar"
  parallelism: 1

  configs:

      ## config for pg, docker image: debezium/example-mongodb:0.10
      mongodb.hosts: "rs0/mongodb:27017"
      mongodb.name: "dbserver1"
      mongodb.user: "debezium"
      mongodb.password: "dbz"
      mongodb.task.id: "1"
      database.whitelist: "inventory"
      database.history.pulsar.service.url: "pulsar://127.0.0.1:6650"
  ```

### Usage

This example shows how to change the data of a MongoDB table using the Pulsar Debezium connector.


1. Start a MongoDB server with a database from which Debezium can capture changes.

   ```bash
   docker pull debezium/example-mongodb:0.10
   docker run -d -it --rm --name pulsar-mongodb -e MONGODB_USER=mongodb -e MONGODB_PASSWORD=mongodb -p 27017:27017  debezium/example-mongodb:0.10
   ```

   Use the following commands to initialize the data.

   ```bash
   ./usr/local/bin/init-inventory.sh
   ```

   If the local host cannot access the container network, you can update the file `/etc/hosts` and add a rule `127.0.0.1 6 f114527a95f`. f114527a95f is container id, you can try to get by using`docker ps -a`.


2. Start a Pulsar service locally in standalone mode.

   ```bash
   bin/pulsar standalone
   ```

3. Start the Pulsar Debezium connector in local run mode using one of the following methods.

   * Use the **JSON** configuration file as shown previously. 
    
      Make sure the NAR file is available at `connectors/pulsar-io-mongodb-@pulsar:version@.nar`.

       ```bash
       bin/pulsar-admin source localrun \
       --archive connectors/pulsar-io-debezium-mongodb-@pulsar:version@.nar \
       --name debezium-mongodb-source \
       --destination-topic-name debezium-mongodb-topic \
       --tenant public \
       --namespace default \
       --source-config '{"mongodb.hosts": "rs0/mongodb:27017","mongodb.name": "dbserver1","mongodb.user": "debezium","mongodb.password": "dbz","mongodb.task.id": "1","database.whitelist": "inventory","database.history.pulsar.service.url": "pulsar://127.0.0.1:6650"}'
       ```

     :::note

     Currently, the destination topic (specified by the `destination-topic-name` option ) is a required configuration but it is not used for the Debezium connector to save data. The Debezium connector saves data in the following 4 types of topics:

       - One topic named with the database server name ( `database.server.name`) for storing the database metadata messages, such as `public/default/database.server.name`.
       - One topic (`database.history.pulsar.topic`) for storing the database history information. The connector writes and recovers DDL statements on this topic.
       - One topic (`offset.storage.topic`) for storing the offset metadata messages. The connector saves the last successfully-committed offsets on this topic.
       - One per-table topic. The connector writes change events for all operations that occur in a table to a single Pulsar topic that is specific to that table.

     If the automatic topic creation is disabled on your broker, you need to manually create the above 4 types of topics and the destination topic.

     :::

   * Use the **YAML** configuration file as shown previously.

       ```bash
       bin/pulsar-admin source localrun  \
       --source-config-file debezium-mongodb-source-config.yaml
       ```

4. Subscribe to the topic _sub-products_ for the _inventory.products_ table.

   ```bash
   bin/pulsar-client consume -s "sub-products" public/default/dbserver1.inventory.products -n 0
   ```

5. Start a MongoDB client in docker.

   ```bash
   docker exec -it pulsar-mongodb /bin/bash
   ```

6. A MongoDB client pops out.

   ```bash
   mongo -u debezium -p dbz --authenticationDatabase admin localhost:27017/inventory
   db.products.update({"_id":NumberLong(104)},{$set:{weight:1.25}})
   ```

   In the terminal window of subscribing topic, you can receive the following messages.

   ```bash
   ----- got message -----
   {"schema":{"type":"struct","fields":[{"type":"string","optional":false,"field":"id"}],"optional":false,"name":"dbserver1.inventory.products.Key"},"payload":{"id":"104"}}, value = {"schema":{"type":"struct","fields":[{"type":"string","optional":true,"name":"io.debezium.data.Json","version":1,"field":"after"},{"type":"string","optional":true,"name":"io.debezium.data.Json","version":1,"field":"patch"},{"type":"struct","fields":[{"type":"string","optional":false,"field":"version"},{"type":"string","optional":false,"field":"connector"},{"type":"string","optional":false,"field":"name"},{"type":"int64","optional":false,"field":"ts_ms"},{"type":"string","optional":true,"name":"io.debezium.data.Enum","version":1,"parameters":{"allowed":"true,last,false"},"default":"false","field":"snapshot"},{"type":"string","optional":false,"field":"db"},{"type":"string","optional":false,"field":"rs"},{"type":"string","optional":false,"field":"collection"},{"type":"int32","optional":false,"field":"ord"},{"type":"int64","optional":true,"field":"h"}],"optional":false,"name":"io.debezium.connector.mongo.Source","field":"source"},{"type":"string","optional":true,"field":"op"},{"type":"int64","optional":true,"field":"ts_ms"}],"optional":false,"name":"dbserver1.inventory.products.Envelope"},"payload":{"after":"{\"_id\": {\"$numberLong\": \"104\"},\"name\": \"hammer\",\"description\": \"12oz carpenter's hammer\",\"weight\": 1.25,\"quantity\": 4}","patch":null,"source":{"version":"0.10.0.Final","connector":"mongodb","name":"dbserver1","ts_ms":1573541905000,"snapshot":"true","db":"inventory","rs":"rs0","collection":"products","ord":1,"h":4983083486544392763},"op":"r","ts_ms":1573541909761}}.
   ```

## Example of Oracle

### Packaging

Oracle connector does not include Oracle JDBC driver and you need to package it with the connector.
Major reasons for not including the drivers are the variety of versions and Oracle licensing. It is recommended to use the driver provided with your Oracle DB installation, or you can [download](https://www.oracle.com/database/technologies/appdev/jdbc.html) one.
Integration tests have an [example](https://github.com/apache/pulsar/blob/e2bc52d40450fa00af258c4432a5b71d50a5c6e0/tests/docker-images/latest-version-image/Dockerfile#L110-L122) of packaging the driver into the connector NAR file. 

### Configuration

Debezium [requires](https://debezium.io/documentation/reference/1.5/connectors/oracle.html#oracle-overview) Oracle DB with LogMiner or XStream API enabled.
Supported options and steps for enabling them vary from version to version of Oracle DB.
The steps outlined in the [documentation](https://debezium.io/documentation/reference/1.5/connectors/oracle.html#oracle-overview) and used in the [integration test](https://github.com/apache/pulsar/blob/master/tests/integration/src/test/java/org/apache/pulsar/tests/integration/io/sources/debezium/DebeziumOracleDbSourceTester.java) may or may not work for the version and edition of Oracle DB you are using.
Please refer to the [documentation for Oracle DB](https://docs.oracle.com/en/database/oracle/oracle-database/) as needed.

Similarly to other connectors, you can use JSON or YAML to configure the connector.
Using YAML as an example, you can create a `debezium-oracle-source-config.yaml` file like:

* JSON

```json
{
  "database.hostname": "localhost",
  "database.port": "1521",
  "database.user": "dbzuser",
  "database.password": "dbz",
  "database.dbname": "XE",
  "database.server.name": "XE",
  "schema.exclude.list": "system,dbzuser",
  "snapshot.mode": "initial",
  "topic.namespace": "public/default",
  "task.class": "io.debezium.connector.oracle.OracleConnectorTask",
  "value.converter": "org.apache.kafka.connect.json.JsonConverter",
  "key.converter": "org.apache.kafka.connect.json.JsonConverter",
  "typeClassName": "org.apache.pulsar.common.schema.KeyValue",
  "database.history": "org.apache.pulsar.io.debezium.PulsarDatabaseHistory",
  "database.tcpKeepAlive": "true",
  "decimal.handling.mode": "double",
  "database.history.pulsar.topic": "debezium-oracle-source-history-topic",
  "database.history.pulsar.service.url": "pulsar://127.0.0.1:6650"
}
```

* YAML

```yaml
tenant: "public"
namespace: "default"
name: "debezium-oracle-source"
inputs: [ "debezium-oracle-topic" ]
parallelism: 1

className: "org.apache.pulsar.io.debezium.oracle.DebeziumOracleSource"
database.dbname: "XE"

configs:
    database.hostname: "localhost"
    database.port: "1521"
    database.user: "dbzuser"
    database.password: "dbz"
    database.dbname: "XE"
    database.server.name: "XE"
    schema.exclude.list: "system,dbzuser"
    snapshot.mode: "initial"
    topic.namespace: "public/default"
    task.class: "io.debezium.connector.oracle.OracleConnectorTask"
    value.converter: "org.apache.kafka.connect.json.JsonConverter"
    key.converter: "org.apache.kafka.connect.json.JsonConverter"
    typeClassName: "org.apache.pulsar.common.schema.KeyValue"
    database.history: "org.apache.pulsar.io.debezium.PulsarDatabaseHistory"
    database.tcpKeepAlive: "true"
    decimal.handling.mode: "double"
    database.history.pulsar.topic: "debezium-oracle-source-history-topic"
    database.history.pulsar.service.url: "pulsar://127.0.0.1:6650"
```

For the full list of configuration properties supported by Debezium, see [Debezium Connector for Oracle](https://debezium.io/documentation/reference/1.5/connectors/oracle.html#oracle-connector-properties).

## Example of Microsoft SQL

### Configuration

Debezium [requires](https://debezium.io/documentation/reference/1.5/connectors/sqlserver.html#sqlserver-overview) SQL Server with CDC enabled.
Steps outlined in the [documentation](https://debezium.io/documentation/reference/1.5/connectors/sqlserver.html#setting-up-sqlserver) and used in the [integration test](https://github.com/apache/pulsar/blob/master/tests/integration/src/test/java/org/apache/pulsar/tests/integration/io/sources/debezium/DebeziumMsSqlSourceTester.java).
For more information, see [Enable and disable change data capture in Microsoft SQL Server](https://docs.microsoft.com/en-us/sql/relational-databases/track-changes/enable-and-disable-change-data-capture-sql-server).

Similarly to other connectors, you can use JSON or YAML to configure the connector.

* JSON

```json
{
  "database.hostname": "localhost",
  "database.port": "1433",
  "database.user": "sa",
  "database.password": "MyP@ssw0rd!",
  "database.dbname": "MyTestDB",
  "database.server.name": "mssql",
  "snapshot.mode": "schema_only",
  "topic.namespace": "public/default",
  "task.class": "io.debezium.connector.sqlserver.SqlServerConnectorTask",
  "value.converter": "org.apache.kafka.connect.json.JsonConverter",
  "key.converter": "org.apache.kafka.connect.json.JsonConverter",
  "typeClassName": "org.apache.pulsar.common.schema.KeyValue",
  "database.history": "org.apache.pulsar.io.debezium.PulsarDatabaseHistory",
  "database.tcpKeepAlive": "true",
  "decimal.handling.mode": "double",
  "database.history.pulsar.topic": "debezium-mssql-source-history-topic",
  "database.history.pulsar.service.url": "pulsar://127.0.0.1:6650"
}
```

* YAML

```yaml
tenant: "public"
namespace: "default"
name: "debezium-mssql-source"
inputs: [ "debezium-mssql-topic" ]
parallelism: 1

className: "org.apache.pulsar.io.debezium.mssql.DebeziumMsSqlSource"
database.dbname: "mssql"

configs:
    database.hostname: "localhost"
    database.port: "1433"
    database.user: "sa"
    database.password: "MyP@ssw0rd!"
    database.dbname: "MyTestDB"
    database.server.name: "mssql"
    snapshot.mode: "schema_only"
    topic.namespace: "public/default"
    task.class: "io.debezium.connector.sqlserver.SqlServerConnectorTask"
    value.converter: "org.apache.kafka.connect.json.JsonConverter"
    key.converter: "org.apache.kafka.connect.json.JsonConverter"
    typeClassName: "org.apache.pulsar.common.schema.KeyValue"
    database.history: "org.apache.pulsar.io.debezium.PulsarDatabaseHistory"
    database.tcpKeepAlive: "true"
    decimal.handling.mode: "double"
    database.history.pulsar.topic: "debezium-mssql-source-history-topic"
    database.history.pulsar.service.url: "pulsar://127.0.0.1:6650"
```

For the full list of configuration properties supported by Debezium, see [Debezium Connector for MS SQL](https://debezium.io/documentation/reference/1.5/connectors/sqlserver.html#sqlserver-connector-properties).

## FAQ
 
### Debezium postgres connector will hang when creating snap

```
#18 prio=5 os_prio=31 tid=0x00007fd83096f800 nid=0xa403 waiting on condition [0x000070000f534000]
    java.lang.Thread.State: WAITING (parking)
     at sun.misc.Unsafe.park(Native Method)
     - parking to wait for  <0x00000007ab025a58> (a java.util.concurrent.locks.AbstractQueuedSynchronizer$ConditionObject)
     at java.util.concurrent.locks.LockSupport.park(LockSupport.java:175)
     at java.util.concurrent.locks.AbstractQueuedSynchronizer$ConditionObject.await(AbstractQueuedSynchronizer.java:2039)
     at java.util.concurrent.LinkedBlockingDeque.putLast(LinkedBlockingDeque.java:396)
     at java.util.concurrent.LinkedBlockingDeque.put(LinkedBlockingDeque.java:649)
     at io.debezium.connector.base.ChangeEventQueue.enqueue(ChangeEventQueue.java:132)
     at io.debezium.connector.postgresql.PostgresConnectorTask$Lambda$203/385424085.accept(Unknown Source)
     at io.debezium.connector.postgresql.RecordsSnapshotProducer.sendCurrentRecord(RecordsSnapshotProducer.java:402)
     at io.debezium.connector.postgresql.RecordsSnapshotProducer.readTable(RecordsSnapshotProducer.java:321)
     at io.debezium.connector.postgresql.RecordsSnapshotProducer.lambda$takeSnapshot$6(RecordsSnapshotProducer.java:226)
     at io.debezium.connector.postgresql.RecordsSnapshotProducer$Lambda$240/1347039967.accept(Unknown Source)
     at io.debezium.jdbc.JdbcConnection.queryWithBlockingConsumer(JdbcConnection.java:535)
     at io.debezium.connector.postgresql.RecordsSnapshotProducer.takeSnapshot(RecordsSnapshotProducer.java:224)
     at io.debezium.connector.postgresql.RecordsSnapshotProducer.lambda$start$0(RecordsSnapshotProducer.java:87)
     at io.debezium.connector.postgresql.RecordsSnapshotProducer$Lambda$206/589332928.run(Unknown Source)
     at java.util.concurrent.CompletableFuture.uniRun(CompletableFuture.java:705)
     at java.util.concurrent.CompletableFuture.uniRunStage(CompletableFuture.java:717)
     at java.util.concurrent.CompletableFuture.thenRun(CompletableFuture.java:2010)
     at io.debezium.connector.postgresql.RecordsSnapshotProducer.start(RecordsSnapshotProducer.java:87)
     at io.debezium.connector.postgresql.PostgresConnectorTask.start(PostgresConnectorTask.java:126)
     at io.debezium.connector.common.BaseSourceTask.start(BaseSourceTask.java:47)
     at org.apache.pulsar.io.kafka.connect.KafkaConnectSource.open(KafkaConnectSource.java:127)
     at org.apache.pulsar.io.debezium.DebeziumSource.open(DebeziumSource.java:100)
     at org.apache.pulsar.functions.instance.JavaInstanceRunnable.setupInput(JavaInstanceRunnable.java:690)
     at org.apache.pulsar.functions.instance.JavaInstanceRunnable.setupJavaInstance(JavaInstanceRunnable.java:200)
     at org.apache.pulsar.functions.instance.JavaInstanceRunnable.run(JavaInstanceRunnable.java:230)
     at java.lang.Thread.run(Thread.java:748)
```

If you encounter the above problems in synchronizing data, please refer to [this](https://github.com/apache/pulsar/issues/4075) and add the following configuration to the configuration file:

```
max.queue.size=
```
