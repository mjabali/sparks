package com.fusesource.poc.routes;

import org.apache.camel.Endpoint;
import org.apache.camel.EndpointInject;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.converter.jaxb.JaxbDataFormat;

/**
 * Created by IntelliJ IDEA.
 * User: charlesmoulliard
 * Date: 11/01/11
 * Time: 10:35
 * To change this template use File | Settings | File Templates.
 */
public class FileToPoJoThroughQueue extends RouteBuilder {


    @EndpointInject(ref = "fileUri")
    Endpoint fileEndpoint;

    @EndpointInject(ref = "activeMqQueueUri")
    Endpoint activeMqQueueEndpoint;

    @Override
    public void configure() throws Exception {

        JaxbDataFormat jaxb = new JaxbDataFormat("com.fusesource.service");

        // Consume file and sending it to a Queue
        from(fileEndpoint)
        .id("fromFileToQueue")
        .convertBodyTo(String.class)
        .log(">>> File received : ${body}")
        .beanRef("feedback", "createRequest")
        .log(">>> DocumentId created")
        .to(activeMqQueueEndpoint);

        // Consume message from queue for file and log info
        from(activeMqQueueEndpoint)
        .id("fromQueueToLog")
        .beanRef("feedback", "clientReply")
        .log(">>> DocumentResponse created")
        .marshal(jaxb)
        .convertBodyTo(String.class)
        .log(">>> Incident created : ${body}");


    }

}
