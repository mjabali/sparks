/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.fusesource.activemq.utils;

/**
 *
 * @author ade
 */
public interface ScanReporter {
  
  public void started();
  
  public void newJournalFile(String fileId);
  
  public void addMessage();
  
  public void removeMessage();
  
  public void rollbackCommand();
  
  public void commitCommand();
  
  public void done();
  
  public void endOfJournal(int undelivered);
  
  public void endOfScan(int numCommands, int undelivered);

}
