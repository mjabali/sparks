/*
 * Copyright 2011 FuseSource.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.fusesource.customer.rest;

import com.fusesource.demo.customer.Customer;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/CustomerService/")
public class CustomerService {

    private static final Logger log = LoggerFactory.getLogger(CustomerService.class);

    @GET
    @Path("/customers/{id}/")
    @Produces("application/xml")
    public Customer getCustomer(@PathParam("id") long id) {
        log.info("Getting customer; id is: " + id);
        Customer c = new Customer();

        c.setFirstName("Ade");
        c.setLastName("Trenaman");
        c.setId("" + id);
        c.setPhoneNumber("+353-1-01234567");

        return c;
    }
}
