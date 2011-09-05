package com.fusesource.poc.routes;

import org.apache.camel.Endpoint;
import org.apache.camel.LoggingLevel;
import org.apache.camel.EndpointInject;
import org.apache.camel.builder.RouteBuilder;

/**
 * Created by IntelliJ IDEA.
 * User: charlesmoulliard
 * Date: 11/01/11
 * Time: 10:35
 * To change this template use File | Settings | File Templates.
 */
public class FileToQueue extends RouteBuilder {


    @EndpointInject(ref = "fileUri")
    Endpoint fileEndpoint;


    @EndpointInject(ref = "activeMqUri")
    Endpoint activeMqEndpoint;

    @Override
    public void configure() throws Exception {

        // Consume file and sending it to a Queue
        from(fileEndpoint)
        .id("fromFileToQueue")
        .convertBodyTo(String.class)
        .log(LoggingLevel.INFO, ">>> File received : ${body}")
        .to(activeMqEndpoint);

        // Consume message from queue
        from(activeMqEndpoint)
        .id("fromQueueToLog")
        .convertBodyTo(String.class)
        .log(LoggingLevel.INFO, ">>> Message : ${body}");


    }

}
