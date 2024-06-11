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
    private boolean schemaCreate;

    private static final Logger LOGGER = Logger.getLogger("ListenerBean");

    public DBInit(OraclePool client, 
    @ConfigProperty(name = "schema.create", defaultValue = "true") boolean schemaCreate)
    
    {
        this.client = client;
        this.schemaCreate = schemaCreate;

    }

    void onStart(@Observes StartupEvent ev) {
        LOGGER.info("The application is starting...");

        if (schemaCreate) {
            initdb();
        }
    }

    void onStop(@Observes ShutdownEvent ev) {               
        LOGGER.info("The application is stopping...");
        LOGGER.info("DROP TABLE fruits");
        client.query("DROP TABLE fruits").execute();
        LOGGER.info("DROP SEQUENCE fruits_seq");
        client.query("DROP SEQUENCE fruits_seq").execute();
    }

    private void initdb() {
        LOGGER.info("CREATE SEQUENCE fruits_seq START WITH 1");
        LOGGER.info("CREATE TABLE fruits");
        client.query("CREATE SEQUENCE fruits_seq START WITH 1").execute()
                .flatMap(r ->client.query("CREATE TABLE fruits (id NUMBER(10) DEFAULT fruits_seq.nextval NOT NULL, name VARCHAR2(30 BYTE) NOT NULL, PRIMARY KEY (id) )").execute())
                .flatMap(r -> client.query("INSERT INTO fruits (name) VALUES ('Kiwi')").execute())
                .flatMap(r -> client.query("INSERT INTO fruits (name) VALUES ('Durian')").execute())
                .flatMap(r -> client.query("INSERT INTO fruits (name) VALUES ('Pomelo')").execute())
                .flatMap(r -> client.query("INSERT INTO fruits (name) VALUES ('Lychee')").execute())
                .await().indefinitely();
                this.schemaCreate = false;
    }
}
