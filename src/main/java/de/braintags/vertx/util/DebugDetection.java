package de.braintags.vertx.util;

/**
 * Copyright: Copyright (c) 13.02.2017 <br>
 * Company: Braintags GmbH <br>
 * 
 * @author mpluecker
 * 
 */
public class DebugDetection {

  // TODO: only temporary, replace this by some environment detection
  public static boolean isLaunchedByEclipse() {
    String develop = System.getProperty("develop");
    if (develop != null)
      return "true".equals(develop);
    else
      return java.lang.management.ManagementFactory.getRuntimeMXBean().getInputArguments().toString()
          .indexOf("-agentlib:jdwp") >= 0;
  }

  public static boolean isTest() {
    String test = System.getProperty("test");
    if (test != null)
      return "true".equals(test);
    else
      return isLaunchedByEclipse();
  }

  public static boolean isFileCachingDisabled() {
    return "true".equals(System.getProperties().getProperty("vertx.disableFileCaching"));
  }

}
