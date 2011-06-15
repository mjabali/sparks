package com.fusesource.activemq.utils;

import java.io.DataInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.jms.Message;
import org.apache.activemq.command.ActiveMQDestination;
import org.apache.activemq.command.ActiveMQMessage;
import org.apache.activemq.store.kahadb.JournalCommand;
import org.apache.activemq.store.kahadb.MessageDatabase;
import org.apache.activemq.store.kahadb.data.KahaAddMessageCommand;
import org.apache.activemq.store.kahadb.data.KahaCommitCommand;
import org.apache.activemq.store.kahadb.data.KahaRemoveMessageCommand;
import org.apache.activemq.store.kahadb.data.KahaRollbackCommand;
import org.apache.activemq.store.kahadb.data.KahaTransactionInfo;
import org.apache.kahadb.journal.Location;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KahaDBStoreDefrag extends Defragger {

  private static final Logger log = LoggerFactory.getLogger(KahaDBStoreDefrag.class);
  private MessageDatabase manager = null;

  public void run() throws Exception {
    try {
      manager = new MessageDatabase();
      manager.setDirectory(journalDirs.get(0));
      manager.getJournal().setFilePrefix("db-");
      manager.getJournal().start();

      broker.start();

      scanReporter.started();
      Map<String, Set<String>> undelivered = findUnacknowledgedMessages();
      scanReporter.done();

      moveReporter.started(undelivered.size());
      moveMessages(undelivered);



      broker.shutdown();

      manager.close();

      moveReporter.done();
    } catch (Exception ex) {
      log.error("Unable to create target broker; details: ", ex);
      throw new Exception("Unable to create target broker; details: " + ex.getMessage());
    }
  }

  private void moveMessages(Map<String, Set<String>> msgs)
          throws Exception {
    Map<String, Location> processedMsgs = new HashMap<String, Location>();

    Location curr = manager.getJournal().getNextLocation(null);

    while (curr != null) {

      JournalCommand cmd = (JournalCommand) manager.load(curr);

      if (cmd instanceof KahaAddMessageCommand) {
        KahaAddMessageCommand addMsg = (KahaAddMessageCommand) cmd;
        String q = addMsg.getDestination().getName();
        String msgId = addMsg.getMessageId();

        if (msgs.containsKey(msgId) && msgs.get(msgId).contains(q)) {
          Message msg = (Message) wireFormat.unmarshal(new DataInputStream(addMsg.getMessage().newInput()));
          long ts = msg.getJMSTimestamp();
          ActiveMQDestination dst = ((ActiveMQMessage) msg).getDestination();
          String queue = dst.getPhysicalName().toString();

          logMessage(log, msg, curr.getSize());

          moveReporter.movedMessage(queue, ts);

          broker.getProducer().send(dst, msg);

          processedMsgs.put(msgId, curr);
        }
      }

      curr = manager.getJournal().getNextLocation(curr);
    }

    moveReporter.summarize(broker.getStoreDirectory().getAbsolutePath());
  }

  private boolean isNotTransacted(KahaTransactionInfo tx) {
    return tx.getLocalTransacitonId().toString().isEmpty()
            && tx.getXaTransacitonId().toString().isEmpty();
  }

  private String getTransactionKey(KahaTransactionInfo tx) {
    return tx.getLocalTransacitonId().toString()
            + tx.getXaTransacitonId().toString();
  }

  private Map<String, Set<String>> findUnacknowledgedMessages() throws Exception {
    // Set<String> msgIds = new HashSet<String>();
    Map<String, List<JournalCommand>> transactedCmds = new HashMap<String, List<JournalCommand>>();
    Map<String, Set<String>> msgs = new HashMap<String, Set<String>>();

    int numCommands = 0;
    int currentFileId = 0;

    Location curr = manager.getJournal().getNextLocation(null);
    while (curr != null) {
      // If we've begun processing a new file.
      if (currentFileId != curr.getDataFileId()) {
        currentFileId = curr.getDataFileId();
        scanReporter.newJournalFile(currentFileId + "");
      }

      JournalCommand cmd = (JournalCommand) manager.load(curr);
      if (cmd instanceof KahaAddMessageCommand) {
        KahaAddMessageCommand addMsg = (KahaAddMessageCommand) cmd;

        if (isNotTransacted(addMsg.getTransactionInfo())) {
          // not transacted.
          log.info("AddMessage " + addMsg.getMessageId());
          addMessageForQueue(msgs, addMsg.getMessageId(), addMsg.getDestination().getName());
        } else {
          // transacted. Wait till a commit or rollback before adding.
          String tx = getTransactionKey(addMsg.getTransactionInfo());
          log.info("AddMessage " + addMsg.getMessageId() + ", txnId = " + tx);
          List<JournalCommand> txCmds = transactedCmds.get(tx);
          if (txCmds == null) {
            txCmds = new ArrayList<JournalCommand>();
            transactedCmds.put(tx, txCmds);
          }
          txCmds.add(addMsg);
        }

        scanReporter.addMessage();
      } else if (cmd instanceof KahaRemoveMessageCommand) {
        KahaRemoveMessageCommand rmMsg = (KahaRemoveMessageCommand) cmd;

        if (isNotTransacted(rmMsg.getTransactionInfo())) {
          log.info("RemoveMessage " + rmMsg.getMessageId());
          removeMessageForQueue(msgs, rmMsg.getMessageId(), rmMsg.getDestination().getName());
        } else {
          // transacted. 
          String tx = getTransactionKey(rmMsg.getTransactionInfo());
          log.info("Transactional RemoveMessage " + rmMsg.getMessageId() + ", txnId = " + tx);

          List<JournalCommand> txCmds = transactedCmds.get(tx);
          if (txCmds == null) {
            txCmds = new ArrayList<JournalCommand>();
            transactedCmds.put(tx, txCmds);
          }
          txCmds.add(rmMsg);
        }

        scanReporter.removeMessage();
      } else if (cmd instanceof KahaRollbackCommand) {
        KahaRollbackCommand rb = (KahaRollbackCommand) cmd;
        String tx = getTransactionKey(rb.getTransactionInfo());
        log.info("Rollback, id " + tx);
        transactedCmds.remove(tx);
        scanReporter.rollbackCommand();
      } else if (cmd instanceof KahaCommitCommand) {
        KahaCommitCommand c = (KahaCommitCommand) cmd;
        String tx = getTransactionKey(c.getTransactionInfo());
        log.info("Commit, id " + tx);
        if (transactedCmds.containsKey(tx)) {
          for (JournalCommand jc : transactedCmds.get(tx)) {
            if (jc instanceof KahaAddMessageCommand) {
              KahaAddMessageCommand addMsg = (KahaAddMessageCommand) jc;
              addMessageForQueue(msgs, addMsg.getMessageId(), addMsg.getDestination().getName());
            } else if (jc instanceof KahaRemoveMessageCommand) {
              KahaRemoveMessageCommand rmMsg = (KahaRemoveMessageCommand) jc;
              removeMessageForQueue(msgs, rmMsg.getMessageId(), rmMsg.getDestination().getName());
            }
          }
        }
        scanReporter.commitCommand();
      } else {
        log.info("Command " + cmd.getClass().getSimpleName());
      }

      numCommands++;

      curr = manager.getJournal().getNextLocation(curr);

      if (curr == null || currentFileId != curr.getDataFileId()) {
        scanReporter.endOfJournal(msgs.size());
      }

    }

    scanReporter.endOfScan(numCommands, msgs.size());

    return msgs;
  }

  public void addMessageForQueue(Map<String, Set<String>> msgs, String msgId, String q) {
    if (msgs.containsKey(msgId)) {
      msgs.get(msgId).add(q);
    } else {
      Set<String> queues = new HashSet<String>();
      queues.add(q);
      msgs.put(msgId, queues);
    }
  }

  public void removeMessageForQueue(Map<String, Set<String>> msgs, String msgId, String q) {
    if (msgs.containsKey(msgId)) {
      msgs.get(msgId).remove(q);

      if (msgs.get(msgId).isEmpty()) {
        msgs.remove(msgId);
      }
    }
  }
}
