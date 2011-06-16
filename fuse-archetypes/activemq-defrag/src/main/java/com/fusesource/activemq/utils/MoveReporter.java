package com.fusesource.activemq.utils;

public interface MoveReporter {
  
  public void started(int totalMsgs);
  
  public void done();
  
  public void movedMessage(String queueName, long timestamp);
  
  public void summarize(String journalLocation);
}
