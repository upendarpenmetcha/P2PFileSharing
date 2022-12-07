
/**
 * Class PeerHandler. Responsible for handling peers.
 *
 * @author Saksham Goel
 * @author Fnu Ojasvi
 * @author Destinee Gant
 */

import java.util.BitSet;
import java.util.HashMap;
import java.util.List;

public class peerHandler {

  /**
   * The peer handler instance
   *
   * @var PeerHandler
   */
  private static peerHandler handler;

  /**
   * The Peer table - key value Map of peer Id and the peer
   *
   * @var HashMap<Integer, Peer>
   */
  private HashMap<Integer, Peer> peerTable;

  /**
   * The constructor
   *
   * @var void
   */
  public HashMap<Integer, peerToPeerSocket> connectionTable;

  /**
   * The constructor
   *
   * @return void
   */
  public peerHandler() {
    this.peerTable = new HashMap<Integer, Peer>();
    this.connectionTable = new HashMap<Integer, peerToPeerSocket>();
  }

  /**
   * Get an instance of peer handler
   *
   * @return PeerHandler
   */
  public static peerHandler getInstance() {
    if (handler == null) {
      handler = new peerHandler();
    }
    return handler;
  }

  /**
   * Initialize the peer handler
   *
   * @param List<Peer> peerList The list of peers
   * @param HashMap<String,String> configMap Key value map of common configuration file
   * @return void
   */
  public void init(List<Peer> peerList, HashMap<String, String> configMap) {
    double inputFileLength = (double) Double.parseDouble(configMap.get("FileSize"));
    double splitPieceSize = (double) Double.parseDouble(configMap.get("PieceSize"));
    int nofSplits = (int) Math.ceil(inputFileLength / splitPieceSize);
    for (Peer peer : peerList) {
      peer.availableParts = new BitSet(nofSplits);
      peerTable.put(peer.id, peer);
      connectionTable.put(peer.id, null);
    }
  }

  /**
   * Get the peer from the peer table
   *
   * @param int peerId Id of the peer
   * @return Peer
   */
  public Peer getPeer(int peerId) {
    return this.peerTable.get(peerId);
  }
}
