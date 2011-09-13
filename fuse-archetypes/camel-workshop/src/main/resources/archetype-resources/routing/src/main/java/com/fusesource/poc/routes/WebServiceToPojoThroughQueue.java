package com.fusesource.poc.routes;

import org.apache.camel.Endpoint;
import org.apache.camel.EndpointInject;
import org.apache.camel.builder.RouteBuilder;

/**
 * Created by IntelliJ IDEA.
 * User: charlesmoulliard
 * Date: 07/02/11
 * Time: 09:16
 * To change this template use File | Settings | File Templates.
 */
public class WebServiceToPojoThroughQueue extends RouteBuilder {

    @EndpointInject(ref = "cxfUri")
    Endpoint cxfEndpoint;


    @EndpointInject(ref = "activeMqWSQueueUri")
    Endpoint activeMqWSQueueEndpoint;

    @Override
    public void configure() throws Exception {

        // From WebService to PoJo
        from(cxfEndpoint)
        .id("fromWebServiceToQueue")
        .convertBodyTo(com.fusesource.service.DocumentId.class)
        .inOut(activeMqWSQueueEndpoint)
        .log(">>> WebService called and incident created : ${body}");

        // Consume message from WS queue for Web Service
        from(activeMqWSQueueEndpoint)
        .id("fromQueueToPoJo")
        .log(">>> Web Service Message : ${body}")
        .transform()
                .method("feedback","clientReply");

    }


}