package com.fusesource.activemq.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Level;
import javax.jms.JMSException;
import javax.jms.Message;
import org.apache.activemq.command.ActiveMQBlobMessage;
import org.apache.activemq.command.ActiveMQBytesMessage;
import org.apache.activemq.command.ActiveMQDestination;
import org.apache.activemq.command.ActiveMQMessage;
import org.apache.activemq.command.ActiveMQObjectMessage;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.apache.activemq.openwire.OpenWireFormat;
import org.apache.activemq.wireformat.WireFormat;
import org.apache.commons.lang.time.DateUtils;
import org.slf4j.Logger;

public abstract class Defragger {

  protected ScanReporter scanReporter;
  protected MoveReporter moveReporter;
  protected TargetBroker broker;
  protected ArrayList<File> journalDirs;
  protected final WireFormat wireFormat = new OpenWireFormat();
  public static final long CURRENT_THRESHOLD_WEEKS = 1l;
  public static final long RECENT_THRESHOLD_WEEKS = 8l;
  public static final long OLD_THRESHOLD_WEEKS = 16l;
  public static final long CURRENT_THRESHOLD_MS = CURRENT_THRESHOLD_WEEKS * 7 * DateUtils.MILLIS_PER_DAY; // 7d weeks
  public static final long RECENT_THRESHOLD_MS = RECENT_THRESHOLD_WEEKS * 7 * DateUtils.MILLIS_PER_DAY; // 8 weeks
  public static final long OLD_THRESHOLD_MS = OLD_THRESHOLD_WEEKS * 7 * DateUtils.MILLIS_PER_DAY; // 16 weeks

  public void setScanReporter(ScanReporter scanReporting) {
    this.scanReporter = scanReporting;
  }

  public void setBroker(TargetBroker broker) {
    this.broker = broker;
  }

  public void setJournalDirs(ArrayList<File> journalDirs) {
    this.journalDirs = journalDirs;
  }

  public abstract void run() throws Exception;

  protected String determineAge(long timestamp) {
    if (isAncient(timestamp)) {
      return "ANCIENT ( > " + OLD_THRESHOLD_WEEKS + " weeks)";
    } else if (isOld(timestamp)) {
      return "OLD ( <= " + OLD_THRESHOLD_WEEKS + " weeks)";
    } else if (isRecent(timestamp)) {
      return "RECENT ( <= " + RECENT_THRESHOLD_WEEKS + " weeks)";
    } else {
      return "CURRENT ( <= " + CURRENT_THRESHOLD_WEEKS + " weeks)";
    }
  }

  public static boolean isAncient(long timestamp) {
    long age = System.currentTimeMillis() - timestamp;
    return age > OLD_THRESHOLD_MS;
  }

  public static boolean isOld(long timestamp) {
    long age = System.currentTimeMillis() - timestamp;
    return age > RECENT_THRESHOLD_MS && age <= OLD_THRESHOLD_MS;
  }

  public static boolean isRecent(long timestamp) {
    long age = System.currentTimeMillis() - timestamp;
    return age > CURRENT_THRESHOLD_MS && age <= RECENT_THRESHOLD_MS;
  }

  public static boolean isCurrent(long timestamp) {
    long age = System.currentTimeMillis() - timestamp;
    return age <= CURRENT_THRESHOLD_MS;
  }

  public void setMoveReporter(MoveReporter moveReporter) {
    this.moveReporter = moveReporter;
  }

  protected void logMessage(Logger log, Message msg, long size) {
    
    try {
      long ts = msg.getJMSTimestamp();

      ActiveMQDestination dst = ((ActiveMQMessage) msg).getDestination();
      String queue = dst.getPhysicalName().toString();

      log.info("======");
      log.info("MessageId: " + msg.getJMSMessageID());
      log.info("Queue: " + queue);
      log.info("Maturity: " + determineAge(ts));
      log.info("JMSTimestamp: " + ts + "(" + new Date(ts) + ")");
      log.info("Payload size: " + size);
      log.info("Payload: \n" + payloadAsString(msg));
      log.info("======");
    } catch (Exception ex) {

      log.warn("Exception while trying to log message contents; details: " + ex.getMessage());
    }
  }

  public String payloadAsString(Message msg) throws Exception {
    if (msg instanceof ActiveMQTextMessage) {
      return ((ActiveMQTextMessage) msg).getText();
    } else if (msg instanceof ActiveMQBytesMessage) { 
      ActiveMQBytesMessage bytes = (ActiveMQBytesMessage) msg;
      return "BytesMessage, length " + bytes.getBodyLength();
    } else if (msg instanceof ActiveMQObjectMessage) {
      ActiveMQObjectMessage obj = (ActiveMQObjectMessage) msg;
      return obj.getClass().getSimpleName();
    }
    else {
      return msg.getClass().getSimpleName();
    }
  }
}
