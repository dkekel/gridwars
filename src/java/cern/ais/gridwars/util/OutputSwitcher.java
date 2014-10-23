package cern.ais.gridwars.util;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;

/**
 * Copyright (C) 2013 CERN - European Laboratory for Particle Physics
 * All Rights Reserved.
 */
public class OutputSwitcher
{
  private static final OutputSwitcher instance = new OutputSwitcher();
  private PrintStream oldOut = System.out;
  private PrintStream oldErr = System.err;

  private OutputSwitcher()
  {
  }

  public void switchToFile(FileOutputStream newOut)
  {
    System.setOut(new PrintStream(newOut));
    System.setErr(new PrintStream(newOut));
  }

  public void restoreInitial()
  {
    System.setOut(oldOut);
    System.setErr(oldErr);
  }

  public static OutputSwitcher getInstance()
  {
    return instance;
  }
}
