FUSESOURCE POC WORKSHOP PROJECT - V1.0

Author : Charles Moulliard
Created : 31/08/2011

Pre-requisite
- Maven 3.0.x

Modules

- parent : dependencies 
- routes : camel routes with unit test. Can be run with camel:run. Provide example using activemq + webservices. WebServices is available on the following 
address --> http://localhost:9090/cxf/service?wsdl
- features : features file
- tests : integration tests

Build

mvn clean install

Run localy

Execute the following command within routes module
mvn camel:run

Deploy on ESB

Install features file
features:install mvn:com.fusesource/features/1.0/xml/features

Install Poc camel routes
features:install poc-camel-routes