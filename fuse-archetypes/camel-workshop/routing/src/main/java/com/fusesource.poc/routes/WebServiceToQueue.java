package com.fusesource.poc.routes;

import org.apache.camel.Endpoint;
import org.apache.camel.LoggingLevel;
import org.apache.camel.EndpointInject;
import org.apache.camel.builder.RouteBuilder;

/**
 * Created by IntelliJ IDEA.
 * User: charlesmoulliard
 * Date: 07/02/11
 * Time: 09:16
 * To change this template use File | Settings | File Templates.
 */
public class WebServiceToQueue extends RouteBuilder {

    @EndpointInject(ref = "cxfUri")
    Endpoint cxfEndpoint;


    @EndpointInject(ref = "activeMqUri")
    Endpoint activeMqEndpoint;

    @Override
    public void configure() throws Exception {

        // From WebService to Queue and reply to client
        from(cxfEndpoint)
        .log(LoggingLevel.INFO, ">>> WebService called : ${body}")
        .inOnly(activeMqEndpoint)
        .beanRef("feedback");

    }


}