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

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
 
import io.smallrye.mutiny.Multi;

//import io.vertx.mutiny.oracleclient.OraclePool;
import io.vertx.mutiny.sqlclient.Pool;

@Path("/db")
public class DBParam {

    public String name;

    private final Pool client;

    public DBParam(Pool client) {
        this.client = client;
    }

    @GET
    @Path("params")
    public Multi<Banner> get() {
        return Banner.findAllBanner(client);        
    }

        @GET
    @Path("tables")
    public Multi<Banner> getTables() {
        return Banner.findAllUsers(client);        
    }

    @GET
    @Path("users")
    public Multi<Banner> getUsers() {
        return Banner.findAllUsers(client);        
    }
}
 