package com.fusesource.poc.integration;

import org.apache.felix.service.command.CommandProcessor;
import org.apache.felix.service.command.CommandSession;
import org.apache.karaf.testing.AbstractIntegrationTest;
import org.apache.karaf.testing.Helper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.Configuration;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;
import org.osgi.service.blueprint.container.BlueprintContainer;

import static org.junit.Assert.assertNotNull;
import static org.ops4j.pax.exam.CoreOptions.*;
import static org.ops4j.pax.exam.OptionUtils.combine;
import static org.ops4j.pax.exam.container.def.PaxRunnerOptions.scanFeatures;
import static org.ops4j.pax.exam.container.def.PaxRunnerOptions.workingDirectory;

@RunWith(JUnit4TestRunner.class)
public class RouteTest extends AbstractIntegrationTest {

    @Test
    public void testFeatures() throws Exception {
        // Make sure the command services are available
        assertNotNull(getOsgiService(BlueprintContainer.class, "osgi.blueprint.container.symbolicname=org.apache.karaf.shell.wrapper", 20000));
        // Run some commands to make sure they are installed properly
        CommandProcessor cp = getOsgiService(CommandProcessor.class);
        CommandSession cs = cp.createSession(System.in, System.out, System.err);
        cs.execute("wrapper:install --help");
        cs.close();
    }

    @Configuration
    public static Option[] configuration() throws Exception{
        return combine(
            // Default karaf environment
            Helper.getDefaultOptions(
                // this is how you set the default log level when using pax logging (logProfile)
                Helper.setLogLevel("INFO")),

            // add two features
            // Helper.loadKarafFeatures("obr", "wrapper", "http", "camel"),

            scanFeatures(
                    maven().groupId("org.apache.karaf.assemblies.features").artifactId("standard").type("xml").classifier("features").versionAsInProject(),
                    "wrapper","spring/3.0.5.RELEASE"),

            scanFeatures(
                    maven().groupId("org.apache.camel.karaf").artifactId("apache-camel").type("xml").classifier("features").version("2.7.1-fuse-00-43"),
                    "camel"),

            workingDirectory("target/paxrunner/features/"),

            waitForFrameworkStartup(),

            felix()

            // felixProvisionalApis()
        );
    }

}
