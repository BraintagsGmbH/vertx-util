package de.braintags.vertx.util;

/**
 * Copyright: Copyright (c) 13.02.2017 <br>
 * Company: Braintags GmbH <br>
 * 
 * @author mpluecker
 * 
 */
public class DebugDetection {

  private static final boolean develop = detectDevelopMode();
  private static final boolean profiling = detectProfilingMode();
  private static final boolean test = detectTestMode();
  private static final boolean fileCachingDisabled = detectFileCahcingDisabled();

  public static boolean isLaunchedByEclipse() {
    return develop;
  }

  public static boolean isTest() {
    return test;
  }

  public static boolean isProfiling() {
    return profiling;
  }

  public static boolean isFileCachingDisabled() {
    return fileCachingDisabled;
  }

  private static boolean detectTestMode() {
    String test = System.getProperty("test");
    if (test != null)
      return "true".equals(test);
    else
      return detectDevelopMode();
  }

  private static boolean detectFileCahcingDisabled() {
    return "true".equals(System.getProperties().getProperty("vertx.disableFileCaching"));
  }

  private static boolean detectProfilingMode() {
    return "true".equals(System.getProperties().getProperty("profiling"));
  }

  private static boolean detectDevelopMode() {
    String develop = System.getProperty("develop");
    if (develop != null)
      return "true".equals(develop);
    else
      return java.lang.management.ManagementFactory.getRuntimeMXBean().getInputArguments().toString()
          .indexOf("-agentlib:jdwp") >= 0;
  }

}
