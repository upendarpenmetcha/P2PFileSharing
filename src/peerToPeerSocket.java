
/**
 * Class peerToPeerSocket to handles all the socket level operations
 *
 * @author Saksham Goel
 * @author Fnu Ojasvi
 * @author Destinee Gant
 */

import java.io.IOException;
import java.net.Socket;

public class peerToPeerSocket {

  /**
   * Java standard client sockets A java socket is an used for communication
   * between two machines.
   */
  Socket socket;

  /**
   * Creates a stream socket and connects it to the specified port number on the
   * named host
   *
   * @param string host Peer host
   * @param int port Peer port
   */
  public peerToPeerSocket(int port, String host) throws IOException {
    this.socket = new Socket(host, port);
  }

}
