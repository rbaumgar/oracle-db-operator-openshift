/*
 * Copyright 2019 Red Hat, Inc.
 *
 * Red Hat licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package org.acme.reactive.crud;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;

import io.vertx.mutiny.oracleclient.OraclePool;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.RowSet;
import io.vertx.mutiny.sqlclient.Tuple;

import org.jboss.logging.Logger;

public class Fruit {

    public Long id;

    public String name;

    private static final Logger LOGGER = Logger.getLogger("ListenerBean");

    public Fruit() {
        // default constructor.
    }

    public Fruit(String name) {
        this.name = name;
    }

    public Fruit(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public static Multi<Fruit> findAll(OraclePool client) {
        LOGGER.info("Find all Fruits");
        return client.query("SELECT id, name FROM fruits ORDER BY name ASC").execute()
                .onItem().transformToMulti(set -> Multi.createFrom().iterable(set))
                .onItem().transform(Fruit::from);
    }

    public static Uni<Fruit> findById(OraclePool client, Long id) {
        LOGGER.info("Find Fruit id: " + id);
        return client.preparedQuery("SELECT id, name FROM fruits WHERE id = ?").execute(Tuple.of(id))
                .onItem().transform(RowSet::iterator)
                .onItem().transform(iterator -> iterator.hasNext() ? from(iterator.next()) : null);
    }

    public Uni<Long> save(OraclePool client) {
        LOGGER.info("Create Fruit: " + name);
        return client.preparedQuery("INSERT INTO fruits (id, name) VALUES (fruits_seq.nextval, ?)").execute(Tuple.of(name)) 
                .onItem().transform(RowSet::iterator)
                .onItem().transform(iterator -> iterator.hasNext() ? from(iterator.next()).id : null);     
    }

    public Uni<Boolean> update(OraclePool client, Long id) {
        LOGGER.info("Update id: " + id);
        return client.preparedQuery("UPDATE fruits SET name = ? WHERE id = ?").execute(Tuple.of(name, id))
                .onItem().transform(oracleRowSet -> oracleRowSet.rowCount() == 1);
    }

    public static Uni<Boolean> delete(OraclePool client, Long id) {
        LOGGER.info("Delete id: " + id);
        return client.preparedQuery("DELETE FROM fruits WHERE id = ?").execute(Tuple.of(id))
                .onItem().transform(oracleRowSet -> oracleRowSet.rowCount() == 1);
    }

    private static Fruit from(Row row) {
        return new Fruit(row.getLong("id"), row.getString("name"));
    }
}
