package com.fusesource.activemq.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.jms.Connection;
import javax.jms.MessageProducer;
import javax.jms.Session;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.broker.region.policy.PolicyEntry;
import org.apache.activemq.broker.region.policy.PolicyMap;
import org.apache.activemq.broker.region.policy.StorePendingQueueMessageStoragePolicy;
import org.apache.activemq.store.PersistenceAdapter;
import org.apache.activemq.store.amq.AMQPersistenceAdapter;
import org.apache.activemq.store.kahadb.KahaDBPersistenceAdapter;
import org.apache.activemq.usage.MemoryUsage;
import org.apache.activemq.usage.SystemUsage;

public class TargetBroker {

  private Connection jmsConn;
  private Session jmsSess;
  private MessageProducer jms;
  private BrokerService broker;
  public static final String BROKERNAME = "clean";
  private int maxFileLength = 32;
  private String targetStoreType = Main.KAHADB;
  private String name = BROKERNAME;
  private File storeDirectory = new File(BROKERNAME);

  public void start() throws Exception {
    broker = new BrokerService();
    PersistenceAdapter p = null;
    int maxFileLengthBytes = maxFileLength * 1024 * 1024;

    if (Main.AMQ.equalsIgnoreCase(targetStoreType)) {
      AMQPersistenceAdapter amq = new AMQPersistenceAdapter();
      amq.setDirectory(storeDirectory);
      amq.setMaxFileLength(maxFileLengthBytes);
      p = amq;
    } else if (Main.KAHADB.equalsIgnoreCase(targetStoreType)) {
      KahaDBPersistenceAdapter kahadb = new KahaDBPersistenceAdapter();
      kahadb.setDirectory(storeDirectory);
      kahadb.setJournalMaxFileLength(maxFileLengthBytes);
      p = kahadb;
    }

    broker.setBrokerName(name);
    broker.setPersistenceAdapter(p);
    broker.deleteAllMessages();
    broker.setUseJmx(true);
        
    PolicyMap policyMap = new PolicyMap();
    List<PolicyEntry> policies = new ArrayList<PolicyEntry>();
    policyMap.setPolicyEntries(policies);
    PolicyEntry policy = new PolicyEntry();
    policy.setQueue(">");
    policy.setProducerFlowControl(false);
    policy.setPendingQueuePolicy(new StorePendingQueueMessageStoragePolicy());
    policies.add(policy);
    broker.setDestinationPolicy(policyMap);

    
    SystemUsage su = new SystemUsage();
    MemoryUsage mu = new MemoryUsage();
    mu.setLimit(1000l * 1024l * 1024l);
    su.setMemoryUsage(mu);
    broker.setSystemUsage(su);

    broker.start();

    

    jmsConn = new ActiveMQConnectionFactory("vm://" + name).createConnection();
    jmsConn.start();
    jmsSess = jmsConn.createSession(false, Session.AUTO_ACKNOWLEDGE);
    jms = jmsSess.createProducer(null);
  }

  public void shutdown() throws Exception {
    jmsSess.close();
    jmsConn.stop();
    broker.stop();
  }

  public MessageProducer getProducer() {
    return jms;
  }
  
  public Session getSession() { 
    return jmsSess;
  }

  public void setMaxFileLength(int maxFileLength) {
    this.maxFileLength = maxFileLength;
  }

  public void setTargetStoreType(String targetStoreType) {
    this.targetStoreType = targetStoreType;
  }

  public void setName(String name) {
    this.name = name;
  }

  public File getStoreDirectory() {
    return storeDirectory;
  }

  public void setStoreDirectory(File storeDirectory) {
    this.storeDirectory = storeDirectory;
  }
  
  
}
