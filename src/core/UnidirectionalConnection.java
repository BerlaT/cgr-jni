package core;

public class UnidirectionalConnection extends CBRConnection{

	public UnidirectionalConnection(DTNHost fromNode, NetworkInterface fromInterface, DTNHost toNode, NetworkInterface toInterface,
			int connectionSpeed) {
		super(fromNode, fromInterface, toNode, toInterface, connectionSpeed);
		// TODO Auto-generated constructor stub
	}


	/**
	 * Returns the node in the other end of the connection
	 * @param node The node in this end of the connection
	 * @return The requested node
	 */
	@Override
	public DTNHost getOtherNode(DTNHost node) {
		
		return this.toNode;
		
	}

	/**
	 * Returns the interface in the other end of the connection
	 * @param i The interface in this end of the connection
	 * @return The requested interface
	 */
	@Override
	public NetworkInterface getOtherInterface(NetworkInterface i) {
		
		return this.toInterface;
		
	}
	@Override
	public String toString() {
		return fromNode + "->" + toNode + " (" + getSpeed()/1000 + " kBps) is " +
		(isUp() ? "up":"down") +
		(isTransferring() ? " transferring " + this.msgOnFly  +
				" from " + this.msgFromNode : "");
	}
}
