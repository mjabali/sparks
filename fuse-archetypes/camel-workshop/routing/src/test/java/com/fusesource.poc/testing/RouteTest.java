package com.fusesource.poc.testing;

import org.apache.camel.*;
import org.apache.camel.component.mock.MockEndpoint;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;


/**
 * Created by IntelliJ IDEA.
 * User: charlesmoulliard
 * Date: 11/01/11
 * Time: 11:36
 * To change this template use File | Settings | File Templates.
 */
@ContextConfiguration
public class RouteTest extends AbstractJUnit4SpringContextTests {

    @Autowired
    protected CamelContext camelContext;

    @EndpointInject(uri = "mock:result")
    public MockEndpoint result;

    @Produce(uri = "direct:start")
    private ProducerTemplate template;

    @Test
    public void testMocksAreValid() throws Exception {
        MockEndpoint.assertIsSatisfied(camelContext);
    }

    @Test
    @DirtiesContext
    public void testWebService() throws Exception {

        result.expectedBodiesReceived(response);
        template.sendBody(payload);

        result.assertIsSatisfied();


    }

    private final static String payload = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ser=\"http://service.fusesource.com\">\n" +
            "   <soapenv:Header/>\n" +
            "   <soapenv:Body>\n" +
            "      <ser:documentId>\n" +
            "         <id>123</id>\n" +
            "      </ser:documentId>\n" +
            "   </soapenv:Body>\n" +
            "</soapenv:Envelope>";

    private final static String response = "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\"><soap:Body><ns2:documentResponse xmlns:ns2=\"http://service.fusesource.com\"><incidentId>123</incidentId><givenName>Charles</givenName><familyName>Moulliard</familyName><details>&lt;html>&lt;body>Message received !!&lt;/body>&lt;/html></details><email>cmoulliard@gmail.com</email><phone>111 222 333</phone></ns2:documentResponse></soap:Body></soap:Envelope>";

}