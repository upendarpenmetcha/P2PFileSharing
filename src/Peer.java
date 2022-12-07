/**
 * This class defines a peer and its related operations.
 *
 * @author Saksham Goel
 * @author Fnu Ojasvi
 * @author Destinee Gant
 */

import java.util.BitSet;
import java.util.concurrent.atomic.AtomicInteger;

public class Peer {

	/**
	 * Id of the peer
	 *
	 * @var int
	 */
	public int id;

	/**
	 * Port number at which this peer listens to
	 *
	 * @var int
	 */
	public int port;

	/**
	 * Host name where this peer is connected to
	 *
	 * @var string
	 */
	public String host;

	/**
	 * Whether the peer has file or not
	 *
	 * @var bool
	 */
	public boolean isFile;

	/**
	 * Bitset array of available parts
	 *
	 * @var BitSet
	 */
	public BitSet availableParts;

	/**
	 * Number of peices of original file
	 *
	 * @var int
	 */
	public int noOfParts;

	/**
	 * Host name where this peer is connected to
	 *
	 * @var string
	 */
	public int rate;

	/**
	 * Boolean whether this peer is chocked or not
	 *
	 * @var bool
	 */
	private boolean isChoked;

	/**
	 * Boolean whether this peer is optimistically chocked or not
	 *
	 * @var bool
	 */
	private boolean isOptimisticallyChoked;

	/**
	 * The download rate for this peer
	 *
	 * @var AtomicInteger
	 */
	private AtomicInteger downloadRate;

	public boolean remotechoke;

	/**
	 * Constructor of Peer
	 * 
	 * @param string host Host name where this peer is connected to
	 * @param bool isfile Whether the peer has file or not
	 * @param int id The Id of the peer
	 * @param int port Port number at which this peer listens to
	 * @return void
	 */
	public Peer(boolean isfile, String host, int id, int port) {
		this.id = id;
		this.host = host;
		this.port = port;
		this.isFile = isfile;
		this.availableParts = new BitSet();
		this.downloadRate = new AtomicInteger(0);
	}

	/**
	 * Sets the available parts and isfile flag
	 *
	 * @param BitSet
	 *
	 * @return void
	 */
	public void setParts(BitSet b) {
		this.availableParts.or(b);
		if (this.availableParts.cardinality() == this.noOfParts) {
			isFile = true;
		} else {
			isFile = false;
		}
	}

	/**
	 * Sets total number of parts and bitset stream of bit for available parts
	 *
	 * @param int parts Number of parts
	 */
	public void setPartsCount(int parts) {
		this.noOfParts = parts;
		this.availableParts = new BitSet(this.noOfParts);
	}

	/**
	 * Choke this peer
	 */
	public void Choke() {
		this.isChoked = true;
	}

	/**
	 * Un-choke this peer
	 */
	public void Unchoke() {
		this.isChoked = false;
	}

}
