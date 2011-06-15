These demos have been built and tested using Fuse ESB (ServiceMix) 4.3.1-fuse-00-00

Installing the 'features'
-------------------------
The samples for the cxf-webinars have all been modeled as ServiceMix 'features', that 
is, sets of bundles. To install each feature, you must first register the features
file with ServiceMix. In the ServiceMix shell, try this: 

    karaf@root> features:addUrl mvn:org.fusesource.sparks.fuse-webinars.cxf-webinars/customer-features/<version>/xml

Now do 'features:list | grep customer'. You should see the relevant features listed. Pick
the feature you're interested in, and install it using 'features:install'.

Installing the customer-ws-osgi-bundle
--------------------------------------
The 'customer-ws-osgi-bundle' deploys a simple web service listening on ServiceMix's
HTTP port (by default, this is port 8181). To install, just do 

    karaf@root> features:install customer-ws

You will now find that the server is listening on 'http://localhost:8181/cxf/Customers' 
- you can verify this quickly by pointing your browser at 'http://localhost:8181/cxf/Customers?wsdl'.
You can test the service by using a tool such as SoapUI from http://www.soapui.org. 

Alternatively, you can install a bundle that creates a CXF client to this web 
service. 

    karaf@root> features:install customer-ws-client

The bundle creates a thread that invokes on the web service once a second. It logs 
the response it gets; you can view the log using 

    karaf@root> log:display -n 10


Installing the customer-ws-secure web service.
----------------------------------------------
The customer-ws-secure web service provides a secure implementation of the web service. 
To make this work, First, you've got copy the certificates to the <servicemix-install>/etc 
directory. You can do this with something like: 

	cp certs/*jks <servicemix-install>/etc

Then, you've got to copy the customer.cfg file to the <servicemix-install>/etc directory.

	cp etc/customer.cfg <servicemix-install>/etc

You'll also want to copy the org.ops4j.pax.web.cfg file to the <servicemix-install>/etc directory.

        cp etc/org.ops4j.pax.web.cfg <servicemix-install>/etc

Finally, you might want to configure a user called 'Alice', with password 'ecilA' and roles 'user' 
and 'admin'. You can do this by simply copying the file etc/users.properties to the 
<servicemix-install>/etc directory.

	cp etc/users.properties <servicemix-install>/etc

Now, start up your Fuse ESB (ServiceMix) instance - it will pick up the 'customer' configuration
from customer.cfg, and use this to pick up the correct certificates. To install the secure custoemr 
web service, try: 

	features:install customer-ws-secure

If you like, you can later edit the <servicemix-install>/etc/customer.cfg file, for example, you 
can change the WS-Security settings. If you do so, you must update the customer-ws-secure bundle
to get the changes. To do this, do an 'osgi:list  | grep customer' in the the ServiceMix shell, to 
get the bundle id, and then do 'osgi:restart <bundle-id>'. 

Installing the camel-cxf routes
-------------------------------
To install the camel-cxf demos into Fuse ESB, you need install the relevant feature.

For example, to install the camel-cxf POJO route, do 

    karaf@root> features:uninstall customer-ws-camel-cxf-pojo

To install the camel-cxf Payload route, do 

    karaf@root> features:uninstall customer-ws-camel-cxf-payload

Finally, to install the camel-cxf Provider route, do 

    karaf@root> features:uninstall customer-ws-camel-cxf-pojo



Running the secure customer WS client
-------------------------------------
The secure customer client has been designed to be very, very configurable, allowing you to 
specify diffent WS-Security actions (e.g. UsernameToken Timestamp Signature Encrypt). Run the 
client using 'mvn exec:java' to get a full list of the options available. Then, use the '-Dexec.args' 
option to specify the arguments you want to use. For example, to perform the full gamut, you might 
use (from the customer-ws-security-client dir):

	mvn exec:java -Dexec.args="-u -t -user Alice -pw ecilA -sign -sa scott -sk ../certs/scott.jks -skpw scott123 -spw ttocs123 -encrypt -ea fuse-esb -ek ../certs/fuse-esb.jks -ekpw fuse-esb"

If you want to do something simpler like UsernameToken with Timestamp, do: 

	mvn exec:java -Dexec.args="-u -t -user Alice -pw ecilA"
