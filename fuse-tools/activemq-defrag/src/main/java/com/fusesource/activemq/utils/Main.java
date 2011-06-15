package com.fusesource.activemq.utils;

import java.io.File;
import java.text.ParseException;
import java.util.ArrayList;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.lang.time.DurationFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {

  public static final String DEFAULT_SCRIPT_NAME = "activemq-defrag";
  private static final Logger logger = LoggerFactory.getLogger(AMQPersistenceStoreDefrag.class);
  
  public static final String AMQ = "amq";
  public static final String KAHADB = "kahadb";
  
  public static final File storeDirectory = new File(TargetBroker.BROKERNAME);


  public static void main(String[] args) {
    long start = System.currentTimeMillis();
    String journalDir;
    String archiveDir;
    int maxFileLength = 32;
    String targetStoreType;
    String storeType;
    ArrayList<File> dirs = new ArrayList<File>();

    try {
      CommandLine cli = new PosixParser().parse(options, args);

      if (cli.hasOption("help")) {
        printUsageAndExit();
      }

      journalDir = cli.getOptionValue('j');
      if (journalDir == null) { 
        printUsageAndExit("You must specify a value for journalDir with the -j option.");
      }
      
      archiveDir = cli.getOptionValue('a');
      targetStoreType = cli.getOptionValue('t', KAHADB);
      storeType = cli.getOptionValue('s', KAHADB);

      if (cli.getOptionValue('m') != null) {
        try {
          maxFileLength = Integer.parseInt(cli.getOptionValue('m'));
        } catch (NumberFormatException ex) {
          printUsageAndExit("Invalid value '" + cli.getOptionValue('m') + "' for journalMaxFileLength.");
        }
      }

      dirs.add(new File(journalDir));
      if (archiveDir != null) {
        dirs.add(new File(archiveDir));
      }
      
      TargetBroker broker = new TargetBroker();
      broker.setMaxFileLength(maxFileLength);
      broker.setName(TargetBroker.BROKERNAME);
      broker.setStoreDirectory(new File(TargetBroker.BROKERNAME));
      broker.setTargetStoreType(targetStoreType);

      Defragger defragger = null;
      
      if (storeType.equalsIgnoreCase(KAHADB)) { 
        defragger = new KahaDBStoreDefrag();
      } 
      else if (storeType.equalsIgnoreCase(AMQ)) { 
        defragger = new AMQPersistenceStoreDefrag();
      } 
      else { 
        printUsageAndExit("Unsupported store type " + storeType);
      }

      defragger.setJournalDirs(dirs);
      defragger.setBroker(broker);
      defragger.setScanReporter(new ConsoleScanReporter());
      defragger.setMoveReporter(new ConsoleMoveReporter());
      defragger.run();

    } catch (ParseException ex) {
      printUsageAndExit();
    } catch (java.lang.Exception ex) {
      System.err.println("Unexpected error; details: " + ex.getMessage());
    }

    System.out.printf("Elapsed time %s.",
            DurationFormatUtils.formatDuration(
            System.currentTimeMillis() - start, "HH:mm:ss:SSS"));
  }

  private static void printUsageAndExit(String message) {
    if (message != null) {
      System.out.println(message);
    }

    HelpFormatter formatter = new HelpFormatter();
    formatter.printHelp(DEFAULT_SCRIPT_NAME, options);

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

        Option archiveDir = OptionBuilder.withArgName("dir").
                hasArg().
                withDescription("The directory containing the broker's AMQ Journal archives.").withLongOpt("archiveDir").create("a");

        Option maxFileLength = OptionBuilder.withArgName("size").
                hasArg().
                withDescription("The max length of journal files for the target data store.").withLongOpt("maxFileLength").create("m");

        Option targetStoreType = OptionBuilder.withArgName(KAHADB + " | " + AMQ).hasArg().withDescription("The target store type, either 'kahadb' or 'amq' (default kahadb).").withLongOpt("targetStoreType").create("t");

        Option storeType = OptionBuilder.withArgName(KAHADB + " | " + AMQ).hasArg().withDescription("The source store type, either 'kahadb' or 'amq' (default kahadb).").withLongOpt("storeType").create("s");

        Option help = OptionBuilder.withDescription("Prints this message.").withLongOpt("help").create("h");

        options.addOption(journalDir);
        options.addOption(archiveDir);
        options.addOption(maxFileLength);
        options.addOption(storeType);
        options.addOption(targetStoreType);
        options.addOption(help);

    }  
}
