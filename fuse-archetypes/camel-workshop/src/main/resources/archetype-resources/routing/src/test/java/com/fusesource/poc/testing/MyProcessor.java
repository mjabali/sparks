package com.fusesource.poc.testing;

import com.fusesource.service.DocumentResponse;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;


/**
 * Created by IntelliJ IDEA.
 * User: charlesmoulliard
 * Date: 28/01/11
 * Time: 16:34
 * To change this template use File | Settings | File Templates.
 */
public class MyProcessor implements Processor {

    @Override
    public void process(Exchange exchange) throws Exception {

        String text = "<html><body>Message received !!</body></html>";
        DocumentResponse response = new DocumentResponse();
        response.setEmail("cmoulliard@gmail.com");
        response.setFamilyName("Moulliard");
        response.setGivenName("Charles");
        response.setIncidentId("123");
        response.setPhone("111 222 333");
        response.setDetails(text);

        exchange.getOut().setBody(response);
    }
}
