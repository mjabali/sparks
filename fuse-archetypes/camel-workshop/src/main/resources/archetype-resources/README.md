# FUSESOURCE POC WORKSHOP PROJECT - V1.0

Author : Charles Moulliard
Created : 12/09/2011

## Introduction

The goal of this project is to provide in a maven project structure the archetypes required to build camel routes, test them and deploy the project
in Fuse ESB or simply runs the camel routes locally. The project exposes 4 Camel routes :

FILE

fromFileToQueue
fromQueueToPoJo

WEB SERVICE

fromWebServiceToWsQueue
fromWsQueueToPoJo

where the 2 first can be use to copy a file in a directory and consulting the result into the log of the console. The other group of camel routes
exposes a WebService that we can reach using SOAPUI.

The routes have been defined using Java DSL but the definition of the camel endpoints/URI and context is defined in the file META-INF/spring/camel-context.xml.
The project can be extended using Spring DSL and Transactional Camel routes can be developed. The camel-context-tx.xml will be used for that purpose (remove the comments
and extend it).

## Pre-requisite

* Maven 3.0.x
* JDK 6, 7
* SOAP UI

## Maven Modules

* parent : contain dependencies, properties definitions and common maven plugins used by the other maven module
* routes : camel routes with unit test.
* features : features file
* tests : integration tests (OSGI)

## Build

mvn clean install

## Launch Camel

a) Standalone mode

* Execute the following command within routes module
    mvn camel:run

b) Deploy in Fuse ESB 4.4-fuse-00-43

* Change the content of the /etc/org.apache.karaf.features.cfg file like defined hereafter

````
    #
    # Comma separated list of features repositories to register by default
    #
    featuresRepositories=mvn:org.apache.karaf.assemblies.features/standard/2.2.0-fuse-00-43/xml/features,mvn:org.apache.karaf.assemblies.features/enterprise/2.2.0-fuse-00-43/xml/features,mvn:org.apache.servicemix.nmr/apache-servicemix-nmr/1.5.0-fuse-00-43/xml/features,mvn:org.apache.servicemix/apache-servicemix/4.4.0-fuse-00-43/xml/features,mvn:org.apache.camel.karaf/apache-camel/2.7.1-fuse-00-43/xml/features,mvn:org.apache.activemq/activemq-karaf/5.5.0-fuse-00-43/xml/features

    #
    # Comma separated list of features to install at startup
    #
    featuresBoot=karaf-framework,config
````

* Install features file

    features:install mvn:com.fusesource.workshop/features/1.0/xml/features

* Install Poc camel routes

    features:install poc-camel-routes

## Test it

FILE

* Copy the file test/data/incidentId.txt into the directory fusesource/data

    e.g. cp /Users/charlesmoulliard/Fuse/sparks/fuse-archetypes/camel-workshop/src/main/resources/archetype-resources/routing/src/test/data/incidentId.txt /Users/charlesmoulliard/tmp/poc/routing/fusesource/data/

* Check the result in the log

````
    14:03:49,794 | INFO  | fusesource/data/ | fromFileToQueue                  | ?                                   ? | 54 - org.apache.camel.camel-core - 2.7.1.fuse-00-43 | >>> File received : 999
    14:03:50,000 | INFO  | fusesource/data/ | fromFileToQueue                  | ?                                   ? | 54 - org.apache.camel.camel-core - 2.7.1.fuse-00-43 | >>> DocumentId created
    14:03:50,187 | INFO  | usesource-input] | fromQueueToLog                   | ?                                   ? | 54 - org.apache.camel.camel-core - 2.7.1.fuse-00-43 | >>> DocumentResponse created
    14:03:50,205 | INFO  | usesource-input] | fromQueueToLog                   | ?                                   ? | 54 - org.apache.camel.camel-core - 2.7.1.fuse-00-43 | >>> Incident created : <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
    <ns2:documentResponse xmlns:ns2="http://service.fusesource.com">
        <incidentId>999</incidentId>
        <givenName>Fuse</givenName>
        <familyName>Source</familyName>
        <details>This is a big incident</details>
        <email>info@fusesource.com</email>
    </ns2:documentResponse>
````

WEBSERVICE

* Create a project in SOAPUI pointing to the following wsdl --> http://localhost:9090/cxf/service?wsdl
* Send the following request

````xml
    <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:ser="http://service.fusesource.com">
       <soapenv:Header/>
       <soapenv:Body>
          <ser:documentId>
             <id>999</id>
          </ser:documentId>
       </soapenv:Body>
    </soapenv:Envelope>
````

* Get this response

````xml
    <soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
       <soap:Body>
          <ns2:documentResponse xmlns:ns2="http://service.fusesource.com">
             <incidentId>999</incidentId>
             <givenName>Fuse</givenName>
             <familyName>Source</familyName>
             <details>This is a big incident</details>
             <email>info@fusesource.com</email>
          </ns2:documentResponse>
       </soap:Body>
    </soap:Envelope>
````
* Check the log

````
    14:05:17,454 | INFO  | er[fusesource-ws | fromQueueToPoJo                  | ?                                   ? | 54 - org.apache.camel.camel-core - 2.7.1.fuse-00-43 | >>> Web Service Message : <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
    <ns2:documentId xmlns:ns2="http://service.fusesource.com">
        <id>999</id>
    </ns2:documentId>
    14:05:18,474 | INFO  | tenerContainer-1 | fromWebServiceToQueue            | ?                                   ? | 54 - org.apache.camel.camel-core - 2.7.1.fuse-00-43 | >>> WebService called and incident created : <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
    <ns2:documentResponse xmlns:ns2="http://service.fusesource.com">
        <incidentId>999</incidentId>
        <givenName>Fuse</givenName>
        <familyName>Source</familyName>
        <details>This is a big incident</details>
        <email>info@fusesource.com</email>
    </ns2:documentResponse>
````

