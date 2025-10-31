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

//import io.vertx.mutiny.oracleclient.OraclePool;
import io.vertx.mutiny.sqlclient.Pool;
import io.vertx.mutiny.sqlclient.Row;

import org.jboss.logging.Logger;

public class Banner {

    public String value;

    private static final Logger LOGGER = Logger.getLogger("ListenerBean");

    public Banner() {
        // default constructor.
    }

    public Banner(String name) {
        this.value = name;
    }

    public static Multi<Banner> findAllBanner(Pool client) {
        LOGGER.info("Find all Banners");
        return client.query("SELECT banner_full from v$version").execute()
                .onItem().transformToMulti(set -> Multi.createFrom().iterable(set))
                .onItem().transform(Banner::from);
    }

    private static Banner from(Row row) {
        return new Banner(row.getString("banner_full"));
    }

    public static Multi<Banner> findAllTables(Pool client) {
        LOGGER.info("Find all Banners");
        return client.query("SELECT TABLE_NAME from ALL_TABLES").execute()
                .onItem().transformToMulti(set -> Multi.createFrom().iterable(set))
                .onItem().transform(Banner::fromTable);
    }

    private static Banner fromTable(Row row) {
        return new Banner(row.getString("table_name"));
    }

    public static Multi<Banner> findAllUsers(Pool client) {
        LOGGER.info("Find all Banners");
        return client.query("SELECT USERNAME from ALL_USERS").execute()
                .onItem().transformToMulti(set -> Multi.createFrom().iterable(set))
                .onItem().transform(Banner::fromUsers);
    }

    private static Banner fromUsers(Row row) {
        return new Banner(row.getString("username"));
    }
}
