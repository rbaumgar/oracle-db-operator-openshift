package org.acme.reactive.crud;

import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;

import io.vertx.mutiny.oracleclient.OraclePool;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import org.jboss.logging.Logger;

@ApplicationScoped
public class DBInit {

    private final OraclePool client;

    @ConfigProperty(name = "schema.create", defaultValue = "true")
    private boolean schemaCreate;
    @ConfigProperty(name = "username") 
    private String user;
    @ConfigProperty(name = "password") 
    private String password;
    @ConfigProperty(name = "connection", defaultValue = "myhost:1521/mydb")
    private String connection;

    private static final Logger LOGGER = Logger.getLogger("ListenerBean");

    public DBInit(OraclePool client)
    
    {
        this.client = client;

    }

    void onStart(@Observes StartupEvent ev) {
        LOGGER.info("The application is starting...");
        LOGGER.info("user: " + user);
        LOGGER.info("connection: " + connection);
        LOGGER.info("schema.create: " + schemaCreate);
        LOGGER.info("Class:" + client.getConnection().getClass());

        if (schemaCreate) {
            initdb();
        }
    }

    void onStop(@Observes ShutdownEvent ev) {               
        LOGGER.info("The application is stopping...");
        LOGGER.info("DROP TABLE fruits");
        client.query("DROP TABLE fruits").execute().await().indefinitely();
        LOGGER.info("DROP SEQUENCE fruits_seq");
        client.query("DROP SEQUENCE fruits_seq").execute().await().indefinitely();
    }

    private void initdb() {

        try {
            client.query("DROP TABLE fruits").execute().await().indefinitely();
            client.query("DROP SEQUENCE fruits_seq").execute().await().indefinitely();
        } catch (Exception e) {
            // TODO: handle exception
        }
        
        LOGGER.info("CREATE SEQUENCE fruits_seq + CREATE TABLE fruits + data");
        client.query("CREATE SEQUENCE fruits_seq START WITH 1").execute()
            .flatMap(r -> client.query("CREATE TABLE fruits (id NUMBER(10) DEFAULT fruits_seq.nextval NOT NULL, name VARCHAR2(30 BYTE) NOT NULL, PRIMARY KEY (id) )").execute())
            .flatMap(r -> client.query("INSERT INTO fruits (name) VALUES ('Kiwi')").execute())
            .flatMap(r -> client.query("INSERT INTO fruits (name) VALUES ('Durian')").execute())
            .flatMap(r -> client.query("INSERT INTO fruits (name) VALUES ('Pomelo')").execute())
            .flatMap(r -> client.query("INSERT INTO fruits (name) VALUES ('Lychee')").execute())
            .await().indefinitely();
            this.schemaCreate = false;
    }
}
