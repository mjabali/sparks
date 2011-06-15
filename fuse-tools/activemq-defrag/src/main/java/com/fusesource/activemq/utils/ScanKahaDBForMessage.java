package com.fusesource.activemq.utils;

import java.io.File;
import org.apache.activemq.store.kahadb.JournalCommand;
import org.apache.activemq.store.kahadb.MessageDatabase;
import org.apache.activemq.store.kahadb.data.KahaAddMessageCommand;
import org.apache.activemq.store.kahadb.data.KahaCommitCommand;
import org.apache.activemq.store.kahadb.data.KahaRemoveMessageCommand;
import org.apache.activemq.store.kahadb.data.KahaRollbackCommand;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;
import org.apache.kahadb.journal.Location;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ScanKahaDBForMessage {

  private static final Logger log = LoggerFactory.getLogger(ScanKahaDBForMessage.class);
  private MessageDatabase manager = null;
  private String journalDir;
  private String messageId;

  public void setJournalDir(String journalDir) {
    this.journalDir = journalDir;
  }

  public void setMessageId(String messageId) {
    this.messageId = messageId;
  }

  public static void main(String args[]) {
    try {

      CommandLine cli = new PosixParser().parse(options, args);

      if (cli.hasOption("help")) {
        printUsageAndExit();
      }

      String journalDir = cli.getOptionValue('j');
      if (journalDir == null) {
        printUsageAndExit("You must specify a value for journalDir with the -j option.");
      }
      
      String messageId = cli.getOptionValue('m');
      if (messageId == null) {
        printUsageAndExit("You must specify a value for messageId with the -m option.");
      }

      ScanKahaDBForMessage scanner = new ScanKahaDBForMessage();
      scanner.setJournalDir(journalDir);
      scanner.setMessageId(messageId);
      scanner.init();
      scanner.run();
    } catch (Exception ex) {
      log.error("Unable to proceed; details: " + ex.getMessage());
    }

  }

  private void init() throws Exception {
    manager = new MessageDatabase();
    manager.setDirectory(new File(journalDir));
    manager.getJournal().setFilePrefix("db-");
    manager.getJournal().start();
  }

  private void run() throws Exception {
    Location curr = manager.getJournal().getNextLocation(null);
    int currentFileId = 0;
    
    if (curr != null) { 
      currentFileId = curr.getDataFileId();
      System.out.println("Log file " + currentFileId);
    }
    
    while (curr != null) {
      
      JournalCommand cmd = (JournalCommand) manager.load(curr);
      if (cmd instanceof KahaAddMessageCommand)  {
        KahaAddMessageCommand addMsg = (KahaAddMessageCommand) cmd;
        if (addMsg.getMessageId().equalsIgnoreCase(messageId)) { 
          System.out.println("AddMsg " + messageId + addMsg.getDestination().getName());
        } 
      } else if (cmd instanceof KahaRemoveMessageCommand)   { 
        KahaRemoveMessageCommand rm = (KahaRemoveMessageCommand) cmd;
        if (rm.getMessageId().equalsIgnoreCase(messageId))  {
          System.out.println("RemoveMessage.. " + messageId + rm.getDestination().getName());
        } 
      } else if (cmd instanceof KahaCommitCommand) { 
        KahaCommitCommand commit = (KahaCommitCommand) cmd;
        System.out.println("Commit... txn: " + commit.getTransactionInfo().getLocalTransacitonId().toString());
      } else if (cmd instanceof KahaRollbackCommand) { 
        KahaRollbackCommand rollback = (KahaRollbackCommand) cmd;
        System.out.println("Rollback... txn: " + rollback.getTransactionInfo().getLocalTransacitonId().toString());        
      }
      curr = manager.getJournal().getNextLocation(curr);
      
      if (curr == null || currentFileId != curr.getDataFileId()) {
        currentFileId = curr.getDataFileId();
        System.out.println("Log file " + currentFileId);
      }
    }
  }
  
    private static void printUsageAndExit(String message) {
    if (message != null) {
      System.out.println(message);
    }

    HelpFormatter formatter = new HelpFormatter();
    formatter.printHelp("scanForMessage", options);

    System.exit(0);
  }

  private static void printUsageAndExit() {
    printUsageAndExit(null);
  }
  
  private static final Options options = new Options();

  static {
    Option journalDir = OptionBuilder.withArgName("dir").
            hasArg().
            withDescription("The directory containing the broker's AMQ Journal").withLongOpt("journalDir").create("j");

    Option messageId = OptionBuilder.withArgName("message-id").
            hasArg().
            withDescription("The messageId to scan for.").withLongOpt("messageId").create("m");


    Option help = OptionBuilder.withDescription("Prints this message.").withLongOpt("help").create("h");

    options.addOption(journalDir);
    options.addOption(messageId);
    options.addOption(help);

  }
}
