DESCRIPTION:
==============
A demo that uses servicemix-wsn. Tested with SMX 4.3.1.
This demo is based on the SMX 3.4 demo examples/wsn-http-binding but unlike
the original demo this one is in Maven format. 
Additional demo descriptions can be found at
http://servicemix.apache.org/ws-notification-http-binding.html, and
http://servicemix.apache.org/example-scenario.html

The only interesting file is wsn-demo-su/src/main/resources/xbean.xml, which 
configures a http endpoint for wsn. 

There is a SOAP-UI test suite provided with the demo, that makes it easy to test.
Alternatively the client.html page can be used as well, but does not offer to 
unregister a pull point. 

In order to fully test the notification part, an external HTTP server is needed, 
which can receive the notification. Its http endpoint address needs to be passed 
along with the CreatePullPoint() operation. 
There is such HTTP server provided in the HTTP-Server subdirectory. 
Use run.bat to start it. It listens on http://0.0.0.0:9080/DEV-3002


COMPILING:
===========
mvn install


RUNNING:
==========
- deploy into SMX
- start external HTTP server, notice its endpoint address
- load soap-ui project
- change test suite property pullpoint to http servers address
- invoke the test suite operations


OUTPUT:
==========
If all goes fine, then the call to Notify should trigger an invocation on the 
external HTTP server.
Request onto HTTP server should read:

<?xml version='1.0' encoding='UTF-8'?>
<ns2:Notify xmlns="http://www.w3.org/2005/08/addressing" 
  xmlns:ns2="http://docs.oasis-open.org/wsn/b-2" 
  xmlns:ns3="http://docs.oasis-open.org/wsrf/bf-2" 
  xmlns:ns4="http://docs.oasis-open.org/wsrf/rp-2" 
  xmlns:ns5="http://docs.oasis-open.org/wsn/t-1">
  <ns2:NotificationMessage>
    <ns2:Topic Dialect="http://docs.oasis-open.org/wsn/t-1/TopicExpression/Simple">
              myTopic
    </ns2:Topic>
    <ns2:Message>
      <hello xmlns="" xmlns:add="http://www.w3.org/2005/08/addressing" 
        xmlns:b="http://docs.oasis-open.org/wsn/b-2" 
        xmlns:ns6="http://www.w3.org/2005/08/addressing"
        xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/">world</hello>
     </ns2:Message>
   </ns2:NotificationMessage>
</ns2:Notify>
