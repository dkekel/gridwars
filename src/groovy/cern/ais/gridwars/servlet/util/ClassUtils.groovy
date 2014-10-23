package cern.ais.gridwars.servlet.util

import cern.ais.gridwars.GameConstants

/**
 * Copyright (C) 2013 CERN - European Laboratory for Particle Physics
 * All Rights Reserved.
 */
class ClassUtils
{
  public static Class loadClassFromJarFile(String agentFQCN, String jarAbsoluteURL, ClassLoader parentClassLoader)
  {
    Class classToLoad = null
    URL jarURL;
    try
    {
      jarURL = new URL(jarAbsoluteURL);
    }
    catch (MalformedURLException e)
    {
      println "Stacktrace should follow"
      e.printStackTrace()
      return null
    }

    URL[] urlArray = [jarURL]
    URLClassLoader urlClassLoader = new URLClassLoader(urlArray, parentClassLoader)

    Thread loadThread = new Thread() {
      @Override
      public void run()
      {
        classToLoad = Class.forName(agentFQCN, true, urlClassLoader)
      }
    };

    loadThread.start();
    try
    {
      loadThread.join(GameConstants.CLASS_LOAD_TIMEOUT_DURATION_MS)
    }
    catch (InterruptedException e)
    {
      // Bad stuff!
      e.printStackTrace()
    }

    if (loadThread.isAlive())
    {
      loadThread.stop()
    }

    return classToLoad
  }
}
