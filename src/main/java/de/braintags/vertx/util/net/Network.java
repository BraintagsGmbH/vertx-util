package de.braintags.vertx.util.net;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;

import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

/**
 * Helper class for networking related functions
 *
 * @author jkerkenhoff
 *
 */
public final class Network {

  private static final Logger logger = LoggerFactory.getLogger(Network.class);

  private Network() {
    // noop
  }
  
  /**
   * Get a free port at the local host.
   * 
   * @return the free port
   * @throws IOException
   *           if an I/O error occurs when opening a socket to determine the port.
   */
  public static int getFreeServerPort() throws IOException {
    return getFreeServerPort(getLocalHost());
  }

  private static int getFreeServerPort(InetAddress hostAdress) throws IOException {
    int ret;
    ServerSocket socket = new ServerSocket(0, 0, hostAdress);
    ret = socket.getLocalPort();
    socket.close();
    return ret;
  }

  /**
   * Get the localhost address
   * 
   * @return the localhost {@link InetAddress}
   * @throws UnknownHostException
   *           if the local host name could not be resolved into an address.
   */
  public static InetAddress getLocalHost() throws UnknownHostException {
    InetAddress ret = InetAddress.getLocalHost();
    if (!ret.isLoopbackAddress()) {
      ret = InetAddress.getByName("localhost");
      if (!ret.isLoopbackAddress()) {
        logger.error("{} is not a loopback address", ret.getHostAddress());
      }
    }
    return ret;
  }
}
