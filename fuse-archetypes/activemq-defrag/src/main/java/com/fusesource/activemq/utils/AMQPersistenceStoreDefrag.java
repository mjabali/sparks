package com.fusesource.activemq.utils;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.apache.activemq.command.ActiveMQBlobMessage;
import org.apache.activemq.command.ActiveMQBytesMessage;
import org.apache.activemq.command.ActiveMQDestination;
import org.apache.activemq.command.ActiveMQMapMessage;
import org.apache.activemq.command.ActiveMQMessage;
import org.apache.activemq.command.ActiveMQObjectMessage;
import org.apache.activemq.command.ActiveMQStreamMessage;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.apache.activemq.command.DataStructure;
import org.apache.activemq.command.JournalQueueAck;
import org.apache.activemq.command.JournalTopicAck;
import org.apache.activemq.command.JournalTrace;
import org.apache.activemq.command.JournalTransaction;
import org.apache.activemq.kaha.impl.async.ReadOnlyAsyncDataManager;
import org.apache.activemq.kaha.impl.async.Location;
import org.apache.activemq.util.ByteSequence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AMQPersistenceStoreDefrag extends Defragger {

  private static final Logger log = LoggerFactory.getLogger(AMQPersistenceStoreDefrag.class);

  private ReadOnlyAsyncDataManager manager = null;

  public void run() throws Exception {
    try {
      manager = new ReadOnlyAsyncDataManager(journalDirs);
      manager.start();

      broker.start();

      scanReporter.started();

      Set<String> undeliveredMessages = findUnacknowledgedMessages();
      
      scanReporter.done();
      
      moveReporter.started(undeliveredMessages.size());
      moveMessages(undeliveredMessages);

      broker.shutdown();

      manager.close();

      moveReporter.done();
    } catch (Exception ex) {
      log.error("Unable to create target broker; details: ", ex);
      throw new Exception("Unable to create target broker; details: " + ex.getMessage());
    }
  }

  private void moveMessages(Set<String> messageIds)
          throws Exception {
    
    int totalMsgs = messageIds.size();

    Map<String, Location> processedMsgs = new HashMap<String, Location>();

    int nextPercentile = 0;

    int numMessages = 0;

    Location curr = manager.getFirstLocation();

    while (curr != null) {

      ByteSequence data = manager.read(curr);
      DataStructure record = (DataStructure) wireFormat.unmarshal(data);

      switch (record.getDataStructureType()) {
        case ActiveMQBytesMessage.DATA_STRUCTURE_TYPE:
          log.warn("Bytes message not supported.");
          break;
        case ActiveMQBlobMessage.DATA_STRUCTURE_TYPE:
          log.warn("Blob message not supported.");
          break;
        case ActiveMQMapMessage.DATA_STRUCTURE_TYPE:
          log.warn("Map message not supported.");
          break;
        case ActiveMQObjectMessage.DATA_STRUCTURE_TYPE:
          log.warn("Object message not supported.");
          break;
        case ActiveMQStreamMessage.DATA_STRUCTURE_TYPE:
          log.warn("Stream message not supported.");
          break;
        case ActiveMQTextMessage.DATA_STRUCTURE_TYPE:
          String messageId = ((ActiveMQMessage) record).getMessageId().toString();

          if (messageIds.contains(messageId)) {

            if (processedMsgs.keySet().contains(messageId)) {
              log.warn("Duplicate messageId detected; already processed message " + messageId);
              ByteSequence originalData = manager.read(processedMsgs.get(messageId));
              DataStructure originalRecord = (DataStructure) wireFormat.unmarshal(originalData);

              ActiveMQTextMessage textMessage = (ActiveMQTextMessage) record;
              ActiveMQTextMessage originalTextMessage = (ActiveMQTextMessage) originalRecord;

              if (textMessage.getText().equals(originalTextMessage.getText())) {
                log.warn("The content for duplicate entry " + messageId + " is identical; ignoring the duplicate.");
              } else {
                log.warn("The content for duplicate entry " + messageId + " is different; ignoring the duplicate but this needs further investigation.");
              }
            } else {

              ActiveMQTextMessage textMessage = (ActiveMQTextMessage) record;
              ActiveMQDestination dest = textMessage.getDestination();
              long timestamp = textMessage.getJMSTimestamp();
              String queueName = dest.getPhysicalName().toString();
              
              logMessage(log, (ActiveMQMessage) record, curr.getSize());

              moveReporter.movedMessage(queueName, timestamp);

              broker.getProducer().send(textMessage.getDestination(), textMessage);

              processedMsgs.put(messageId, curr);
            }
          }
          break;

        case JournalQueueAck.DATA_STRUCTURE_TYPE:
          log.debug("Ignoring Queue ACK for "
                  + ((JournalQueueAck) record).getMessageAck().getFirstMessageId());
          break;
        case JournalTopicAck.DATA_STRUCTURE_TYPE:
          log.debug("Ignoring Topic ACK");
          break;
        case JournalTransaction.DATA_STRUCTURE_TYPE:
          log.debug("Ignoring Journal Transaction");
          break;
        case JournalTrace.DATA_STRUCTURE_TYPE:
          log.debug("Ignoring Journal Trace");
          break;

        default:
          break;
      }

      curr = manager.getNextLocation(curr);
    }

    moveReporter.summarize(broker.getStoreDirectory().getAbsolutePath());
  }

  private Set<String> findUnacknowledgedMessages() throws Exception {
    Set<String> msgIds = new HashSet<String>();

    int numCommands = 0;
    int currentFileId = 0;

    Location curr = manager.getFirstLocation();
    while (curr != null) {
      // If we've begun processing a new file.
      if (currentFileId != curr.getDataFileId()) {
        scanReporter.newJournalFile(currentFileId + "");
      }

      ByteSequence data = manager.read(curr);
      DataStructure record = (DataStructure) wireFormat.unmarshal(data);

      switch (record.getDataStructureType()) {
        case ActiveMQBytesMessage.DATA_STRUCTURE_TYPE:
          log.debug("Bytes message");
          break;
        case ActiveMQBlobMessage.DATA_STRUCTURE_TYPE:
          log.debug("Blob message");
          break;
        case ActiveMQMapMessage.DATA_STRUCTURE_TYPE:
          log.debug("Map message");
          break;
        case ActiveMQObjectMessage.DATA_STRUCTURE_TYPE:
          log.debug("Object message");
          break;
        case ActiveMQStreamMessage.DATA_STRUCTURE_TYPE:
          log.debug("Stream message");
          break;
        case ActiveMQTextMessage.DATA_STRUCTURE_TYPE:
          log.debug("Text message, id " + ((ActiveMQMessage) record).getMessageId());
          msgIds.add(((ActiveMQMessage) record).getMessageId().toString());
          scanReporter.addMessage();
          break;
        case JournalQueueAck.DATA_STRUCTURE_TYPE:
          
          log.debug("Queue ACK from "
                  + ((JournalQueueAck) record).getMessageAck().getFirstMessageId()
                  + " to " + ((JournalQueueAck) record).getMessageAck().getLastMessageId());

          String firstMessageId = ((JournalQueueAck) record).getMessageAck().getFirstMessageId().toString();
          String lastMessageId = ((JournalQueueAck) record).getMessageAck().getLastMessageId().toString();
          if (firstMessageId.equals(lastMessageId)) {
            msgIds.remove(lastMessageId);
          } else {
            log.warn("Found JournalQueueACK for range of message Ids; this is not supported by this tool. "
                    + "Results may vary; however, it is likely that all messages except the last message for the "
                    + "session will be stored in the new clean store.");
          }

          scanReporter.removeMessage();
          break;
        case JournalTopicAck.DATA_STRUCTURE_TYPE:
          log.debug("Topic ACK");
          break;
        case JournalTransaction.DATA_STRUCTURE_TYPE:
          log.debug("Journal Transaction");
          break;
        case JournalTrace.DATA_STRUCTURE_TYPE:
          log.debug("Journal Trace");
          break;
        default:
          break;
      }

      numCommands++;

      curr = manager.getNextLocation(curr);

      scanReporter.endOfJournal(msgIds.size());

    }

    scanReporter.endOfScan(numCommands, msgIds.size());
    
    return msgIds;
  }

}
