package com.fusesource.activemq.utils;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

public class ConsoleMoveReporter implements MoveReporter {

  private static final PrintStream out = System.out;
  private int numMessages = 0;
  private int ancientMsgs = 0;
  private int oldMsgs = 0;
  private int recentMsgs = 0;
  private int currentMsgs = 0;
  private int duplicateMsgs = 0;
  int nextPercentile = 0;
  int totalMsgs;
  private Map<String, Integer> msgsPerQueue = new HashMap<String, Integer>();

  public void started(int totalMsgs) {
    out.println("\nPHASE 2: Moving unacknowledged messages to new journal \n");
    this.totalMsgs = totalMsgs;
  }

  public void done() {
    out.println("Done!");
  }

  public void ancient() {
    ancientMsgs++;
    numMessages++;
  }

  public void current() {
    currentMsgs++;
    numMessages++;
  }

  public void old() {
    oldMsgs++;
    numMessages++;

  }

  public void recent() {
    recentMsgs++;
    numMessages++;

  }

  private void reportStatus() {
    int percentage = (int) (((float) numMessages / totalMsgs) * 100);

    if (percentage >= nextPercentile) {
      out.print(percentage + "%... ");
      nextPercentile = percentage + 10;
    }
  }

  public void movedMessage(String queueName, long timestamp) {
    numMessages++;

    if (Defragger.isAncient(timestamp)) {
      ancientMsgs++;
    } else if (Defragger.isRecent(timestamp)) {
      recentMsgs++;
    } else if (Defragger.isOld(timestamp)) {
      oldMsgs++;
    } else if (Defragger.isCurrent(timestamp)) {
      currentMsgs++;
    }

    int queueCount = 0;
    if (msgsPerQueue.containsKey(queueName)) {
      queueCount = msgsPerQueue.get(queueName);
    }
    msgsPerQueue.put(queueName, queueCount + 1);

    reportStatus();

  }

  public void summarize(String journalLocation) {

    out.printf("\n\nSummary:\n\n");
    out.printf("%-40s %7d\n", "Ancient messages ( > " + Defragger.OLD_THRESHOLD_WEEKS + " weeks)", ancientMsgs);
    out.printf("%-40s %7d\n", "Old messages ( <= " + Defragger.OLD_THRESHOLD_WEEKS + " weeks)", oldMsgs);
    out.printf("%-40s %7d\n", "Recent messages ( <= " + Defragger.RECENT_THRESHOLD_WEEKS + " weeks)", recentMsgs);
    out.printf("%-40s %7d\n", "Current messages ( <= " + Defragger.CURRENT_THRESHOLD_WEEKS + " weeks)", currentMsgs);
    out.printf("%-40s %7d\n", "Duplicate (ignored!) messages ", duplicateMsgs);
    out.printf("%-40s %7s\n", "", "-----");
    out.printf("%-40s %7d\n", "Total", ancientMsgs + oldMsgs + recentMsgs + currentMsgs);

    out.println("\nMoved " + numMessages + " messages to new journal at " + journalLocation);

    out.printf("\n%-40s %7s\n", "QUEUE", "#MSGS");
    out.printf("%-40s %7s\n", "-----", "-----");
    for (String queue : msgsPerQueue.keySet()) {
      out.printf("%-40s %7d\n", queue, msgsPerQueue.get(queue));
    }
    out.println();  }
}
