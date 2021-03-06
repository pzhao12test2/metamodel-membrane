/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.metamodel.membrane.controllers;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.ws.rs.core.UriBuilder;

import org.apache.metamodel.membrane.app.TenantContext;
import org.apache.metamodel.membrane.app.TenantRegistry;
import org.apache.metamodel.membrane.swagger.model.DeleteTenantResponse;
import org.apache.metamodel.membrane.swagger.model.GetTenantResponse;
import org.apache.metamodel.membrane.swagger.model.GetTenantResponseDatasources;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/{tenant}", produces = MediaType.APPLICATION_JSON_VALUE)
public class TenantController {

    private final TenantRegistry tenantRegistry;

    @Autowired
    public TenantController(TenantRegistry tenantRegistry) {
        this.tenantRegistry = tenantRegistry;
    }

    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    public GetTenantResponse getTenant(@PathVariable("tenant") String tenantName) {
        final TenantContext tenantContext = tenantRegistry.getTenantContext(tenantName);
        final String tenantNameNormalized = tenantContext.getTenantName();

        final UriBuilder uriBuilder = UriBuilder.fromPath("/{tenant}/{datasource}");

        final List<String> dataContextIdentifiers = tenantContext.getDataSourceRegistry().getDataSourceNames();
        final List<GetTenantResponseDatasources> dataSourceLinks = dataContextIdentifiers.stream().map(s -> {
            final String uri = uriBuilder.build(tenantNameNormalized, s).toString();
            return new GetTenantResponseDatasources().name(s).uri(uri);
        }).collect(Collectors.toList());

        final GetTenantResponse resp = new GetTenantResponse();
        resp.type("tenant");
        resp.name(tenantNameNormalized);
        resp.datasources(dataSourceLinks);
        return resp;
    }

    @RequestMapping(method = RequestMethod.PUT)
    @ResponseBody
    public GetTenantResponse putTenant(@PathVariable("tenant") String tenantName) {
        final TenantContext tenantContext = tenantRegistry.createTenantContext(tenantName);
        final String tenantIdentifier = tenantContext.getTenantName();

        final GetTenantResponse resp = new GetTenantResponse();
        resp.type("tenant");
        resp.name(tenantIdentifier);
        resp.datasources(Collections.emptyList());
        return resp;
    }

    @RequestMapping(method = RequestMethod.DELETE)
    @ResponseBody
    public DeleteTenantResponse deleteTenant(@PathVariable("tenant") String tenantName) {
        tenantRegistry.deleteTenantContext(tenantName);

        final DeleteTenantResponse resp = new DeleteTenantResponse();
        resp.type("tenant");
        resp.name(tenantName);
        resp.deleted(true);

        return resp;
    }
}
