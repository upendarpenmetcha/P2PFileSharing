
/**
 * Class fileManager. This class is responsible for managing the file.
 */

import java.util.BitSet;
import java.util.HashMap;
import java.util.List;

public class fileManager {

  /**
   * Id of the peer
   */
  int peerId;

  /**
   * Peer
   */
  private Peer peer;

  /**
   * List of all the peers
   */
  List<Peer> peerList;

  /**
   * Configuration map
   */
  HashMap<String, String> configMap;

  /**
   * Available parts in bitset
   */
  BitSet availableParts;

  /**
   * The count file was split into
   */
  public int splitsCount;

  /**
   * The peer handler instance
   */
  peerHandler peerHandler;

  /**
   * The constructor
   *
   * @return void
   */
  public fileManager(int peerId, HashMap<String, String> configMap, List<Peer> peerList) {
    this.peerId = peerId;
    this.peerList = peerList;
    this.configMap = configMap;
    this.availableParts = new BitSet();
    this.peerHandler = peerHandler.getInstance();
  }

  /**
   * The Init method to initialize the file manager
   */
  public void init() {
    double inputFileLength = Double.parseDouble(configMap.get("FileSize"));
    double splitPieceSize = Double.parseDouble(configMap.get("PieceSize"));
    this.splitsCount = (int) Math.ceil(inputFileLength / splitPieceSize);
    for (Peer p : peerList) {
      if (p.id == peerId) {
        this.peer = p;
        break;
      }
    }
  }


  /**
   * Bitset of currently available parts
   * @return BitSet
   */
  public BitSet getCurrentAvailableParts() {
    return this.availableParts;
  }
}
