
/**
 * Class PeerLogger for logging purpose
 *
 * @author Saksham Goel
 * @author Fnu Ojasvi
 * @author Destinee Gant
 */

import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class peerLogger {

  /**
   * The logger instance
   */
  public static Logger logger;

  /**
   * The log record for a peer
   */
  private static peerLogger logRecord = new peerLogger();

  /**
   * The suffix of the log
   */
  static String logSuffix;

  /**
   * The Peer ID this log is for
   */
  private int peerid;

  /**
   * The logger constructor
   */
  private peerLogger() {
    Logger logger = Logger.getLogger(peerLogger.class.getName());
    this.logger = logger;
  }

  /**
   * Setter to set the logger for a peer
   *
   * @param peerId The Peer Id
   * @return void
   */
  public void setLoggerForPeer(int peerId) {
    this.peerid = peerId;
    this.logSuffix = ": Peer" + " " + Integer.toString(peerId);
    String filename = "logs_peer_" + Integer.toString(peerId) + ".log";

    try {
      Handler loggerHandler = new FileHandler(filename);
      Formatter formatter = (Formatter) Class.forName("java.util.logging.SimpleFormatter").newInstance();
      loggerHandler.setFormatter(formatter);
      loggerHandler.setLevel(Level.parse("INFO"));
      this.logger.addHandler(loggerHandler);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Getter for getting log records for the peer
   *
   * @return PeerLogger The peer logger
   */
  public static peerLogger getLog() {
    return logRecord;
  }
}