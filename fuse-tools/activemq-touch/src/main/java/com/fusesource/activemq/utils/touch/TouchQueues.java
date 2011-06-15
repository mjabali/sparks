package com.fusesource.activemq.utils.touch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;
import org.apache.log4j.Logger;

public class TouchQueues implements Runnable {

    private List<String> queues = new ArrayList<String>();
    private Session session;
    private static final Logger log = Logger.getLogger(TouchQueues.class);
    private ConnectionFactory cf;
    private Connection connection;
    private String brokerUrl;
    private String username; 
    private String password;
    private long sleepTime = 5000;
    private long timeToLive = 0;
    private boolean finished = false;
    private boolean loop = true;
    private Map<String, MessageProducer> producers = new HashMap<String, MessageProducer>();
    private Map<String, MessageConsumer> consumers = new HashMap<String, MessageConsumer>();
    private Thread t;

    public static void main(String args[]) throws Exception {

        CommandLine cli = new PosixParser().parse(options, args);

        if (cli.hasOption("help")) {
            printUsageAndExit();
        }

        String sleepTimeString = cli.getOptionValue("s");
        long sleepTime = 5000l;
        if (sleepTimeString != null) {
            try {
                sleepTime = Long.parseLong(cli.getOptionValue("s"));
            } catch (NumberFormatException ex) {
                printUsageAndExit("Invalid value for sleepTime '" + sleepTimeString + "'");
            }
        }

        long timeToLive = 0l;
        String ttlString = cli.getOptionValue("t");
        if (ttlString != null) {
            try {
                timeToLive = Long.parseLong(ttlString);
            } catch (NumberFormatException ex) {
                printUsageAndExit("Invalid value for timeToLive '" + ttlString + "'");
            }
        }

        String queues[] = cli.getOptionValues("queue");
        if (queues == null || queues.length == 0) {
            printUsageAndExit("You must specify at least one queue!");
        }

        String brokerUrl = cli.getOptionValue("brokerUrl");
        if (brokerUrl == null) {
            brokerUrl = "tcp://localhost:61616";
        }
        
        String username = cli.getOptionValue('n');
        String password = cli.getOptionValue('p');

        boolean loop = cli.hasOption("l");

        TouchQueues touch = new TouchQueues();
        touch.setBrokerUrl(brokerUrl);
        touch.setUsername(username);
        touch.setPassword(password);
        touch.setQueues(Arrays.asList(queues));
        touch.setLoop(loop);
        touch.setSleepTime(sleepTime);
        touch.setTimeToLive(timeToLive);

        touch.start();
        // touch.stop();
    }

    public void run() {

        while (!finished) {

            try {
                Thread.sleep(sleepTime);
            } catch (InterruptedException ex) {
                if (finished) {
                    break;
                }
            }

            for (String queue : queues) {
                String touchQueue = "ZZZ-touch." + queue;

                try {
                    boolean done = false;
                    int msgs = 0;
                    log.info("Moving all messages from " + queue + " to " + touchQueue + "...");
                    while (!done) {
                        Message m = getConsumer(queue).receive(1000);
                        if (m != null) {
                            getProducer(touchQueue).send(m);
                            if (msgs % 500 == 0) {
                                log.info("Still working; moved " + msgs + " messages.");
                            }
                            m.acknowledge();
                            msgs++;
                        } else {
                            done = true;
                        }
                    }

                    log.info("Moving " + msgs + " messages from " + touchQueue + " to " + queue + "...");
                    msgs = 0;
                    done = false;
                    while (!done) {
                        Message m = getConsumer(touchQueue).receive(1000);
                        if (m != null) {
                            getProducer(queue).send(m);
                            if (msgs % 500 == 0) {
                                log.info("Still working; moved " + msgs + " messages.");
                            }
                            m.acknowledge();
                            msgs++;
                        } else {
                            done = true;
                        }
                    }

                    log.info("Touched " + msgs + " messages from queue " + queue + " via " + touchQueue);
                } catch (JMSException ex) {
                    log.warn("Caught exception while performing touch operations; will retry again later. Details: " + ex.getMessage());
                }
            }

            try {
                shutdownProducersAndConsumers();
            } catch (JMSException ex) {
                log.warn("Problem while shutting down producers / consumers; details: " + ex.getMessage());
            }

            if (!loop) {
                finished = true;
            }
        }

        shutdown();
    }

    private void init() {
        try {
            cf = new ActiveMQConnectionFactory(username, password, brokerUrl);
            connection = cf.createConnection();
            session = connection.createSession(false, Session.CLIENT_ACKNOWLEDGE);
            connection.start();
        } catch (JMSException ex) {
            log.fatal("Unable to connect to broker at '" + brokerUrl + "'; details: " + ex.getMessage());
            System.exit(1);
        }
    }

    public void start() {
        init();
        t = new Thread(this, "ActiveMQ Touch Utility");
        t.start();
    }

    public void stop() {
        finished = true;
    }

    private void shutdown() {
        try {
            session.close();
            connection.close();
        } catch (JMSException ex) {
        }
    }

    public void setBrokerUrl(String brokerUrl) {
        this.brokerUrl = brokerUrl;
    }

    public void setQueues(List<String> queues) {
        this.queues = queues;
    }

    public void setTimeToLive(long timeToLive) {
        this.timeToLive = timeToLive;
    }

    public void setSleepTime(long sleepTime) {
        this.sleepTime = sleepTime;
    }

    public void setLoop(boolean loop) {
        this.loop = loop;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    
    
    private MessageProducer getProducer(String queue) throws JMSException {
        if (producers.containsKey(queue)) {
            return producers.get(queue);
        } else {
            Destination queueDest = session.createQueue(queue);
            MessageProducer p = session.createProducer(queueDest);
            p.setTimeToLive(timeToLive);
            producers.put(queue, p);
            return p;
        }
    }

    private MessageConsumer getConsumer(String queue) throws JMSException {
        if (consumers.containsKey(queue)) {
            return consumers.get(queue);
        } else {
            Destination queueDest = session.createQueue(queue);
            MessageConsumer c = session.createConsumer(queueDest);
            consumers.put(queue, c);
            return c;
        }
    }

    private static void printUsageAndExit(String message) {
        if (message != null) {
            System.out.println(message);
        }

        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("activemq-touch", options);

        System.exit(0);
    }

    private static void printUsageAndExit() {
        printUsageAndExit(null);
    }

    private void shutdownProducersAndConsumers() throws JMSException {
        for (String queue : producers.keySet()) {
            log.info("Closed producer for queue " + queue);
            producers.get(queue).close();
        }
        producers.clear();

        for (String queue : consumers.keySet()) {
            log.info("Closed consumer for queue " + queue);
            consumers.get(queue).close();
        }
        consumers.clear();
    }
    private static final Options options = new Options();

    static {
        Option sleepTime = OptionBuilder.withArgName("sleepTime").
                hasOptionalArg().
                withDescription("Sleep interval in ms (if looping).").withLongOpt("sleepTime").create("s");

        Option brokerUrl = OptionBuilder.withArgName("brokerUrl").
                hasOptionalArg().
                withDescription("The broker URL (defaults to tcp://localhost:61616)").withLongOpt("brokerUrl").create("u");

        Option queues = OptionBuilder.withArgName("queue").
                hasArgs().
                withDescription("Queues to touch; use multiple -q args to specify one or more queues. ").withLongOpt("queue").create("q");

        Option loop = OptionBuilder.hasArg(false).
                withDescription("Keep looping using sleepTime as an interval.").withLongOpt("loop").create("l");

        Option timeToLive = OptionBuilder.withArgName("milliseconds").
                hasArgs().
                withDescription("Add a time-to-live on outgoing messages.").withLongOpt("timeToLive").create("t");


        Option username = OptionBuilder.withArgName("userName").
                hasOptionalArg().
                withDescription("Username for ActiveMQ connection").withLongOpt("username").create("n");

        Option password = OptionBuilder.withArgName("password").
                hasOptionalArg().
                withDescription("Password for ActiveMQ connection").withLongOpt("password").create("p");


        Option help = OptionBuilder.withDescription("Prints this message.").withLongOpt("help").create("h");

        options.addOption(sleepTime);
        options.addOption(queues);
        options.addOption(timeToLive);
        options.addOption(username);
        options.addOption(password);
        options.addOption(brokerUrl);
        options.addOption(loop);
        options.addOption(help);

    }
}
