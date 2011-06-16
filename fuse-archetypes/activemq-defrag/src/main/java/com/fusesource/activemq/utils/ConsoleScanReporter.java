/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.fusesource.activemq.utils;

import java.io.PrintStream;

/**
 *
 * @author ade
 */
public class ConsoleScanReporter implements ScanReporter {

  private static final PrintStream out = System.out;
  private static final int REPORTING_BATCH_SIZE = 1000;
  int messagesAdded = 0;
  int messagesRemoved = 0;
  int rollbacks = 0;
  int commits = 0;

  public void started() {
    out.println("\nScanning journal logs for messages and acknowledgements.\n");
    out.println("Progress indicator; each '.' represents " + REPORTING_BATCH_SIZE + " messages read from journal.");
  }

  public void done() {
    out.println("\n Scan completed.");
  }

  public void newJournalFile(String fileId) {
    out.println();
    out.print("Journal log file " + fileId + " ");
    out.flush();
  }

  public void addMessage() {
    messagesAdded++;

    if ((messagesAdded % REPORTING_BATCH_SIZE) == 0) {
      out.print(".");
      out.flush();
    }
  }

  public void removeMessage() {
    messagesRemoved++;
  }

  public void rollbackCommand() {
    rollbacks++;
  }

  public void commitCommand() {
    commits++;
  }

  public void endOfJournal(int undelivered) {
    out.print(" (" + messagesAdded + " msgs, " + undelivered + " undelivered so far, " + commits + " commits, " + rollbacks + " rollbacks)");
    commits = 0;
    rollbacks = 0;
    messagesAdded = 0;
    messagesRemoved = 0;
  }

  public void endOfScan(int numCommands, int undelivered) {
    out.println("\n\nProcessed " + numCommands
            + " kahadb commands in KahaDB journals; found " + undelivered
            + " undelivered messages.");
  }
}
