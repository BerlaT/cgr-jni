package interfaces;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import core.Connection;
import core.NetworkInterface;
import core.Settings;
import core.UnidirectionalConnection;

public class SimpleAsymmetricInterface extends NetworkInterface{
	protected List<Connection> forcedConnections; // connected hosts
	public SimpleAsymmetricInterface(Settings s) {
		super(s);
		forcedConnections = new ArrayList<Connection>();
	}
	
	public SimpleAsymmetricInterface(SimpleAsymmetricInterface ni) {
		super(ni);
		forcedConnections = new ArrayList<Connection>();
	}

	@Override
	public void connect(NetworkInterface anotherInterface) {
		if(!(anotherInterface instanceof SimpleAsymmetricInterface))
			throw new IllegalArgumentException("Impossible to connect to a different NetworkInterface type");
		if (isScanning()
				&& anotherInterface.getHost().isRadioActive()
				&& isWithinRange(anotherInterface)
				&& !isConnected(anotherInterface)
				&& (this != anotherInterface)) {

			Connection con = new UnidirectionalConnection(this.host, this,
					anotherInterface.getHost(), anotherInterface, this.transmitSpeed);
			connect(con, anotherInterface);
			//anotherInterface.connect(this);
		}
	}
	
	@Override
	public void createConnection(NetworkInterface anotherInterface) {
		if(!(anotherInterface instanceof SimpleAsymmetricInterface))
			throw new IllegalArgumentException("Impossible to connect to a different NetworkInterface type");
		if (!isConnected(anotherInterface) && (this != anotherInterface)) {
			Connection con = new UnidirectionalConnection(this.host, this,
					anotherInterface.getHost(), anotherInterface, this.transmitSpeed);
			forcedConnections.add(con);
			connect(con, anotherInterface);
			//anotherInterface.connect(this);
		}
	}
	@Override
	public NetworkInterface replicate()	{
		return new SimpleAsymmetricInterface(this);
	}
	
	@Override
	public String toString() {
		return "IONSimpleFullDuplexInterface " + super.toString();
	}

	@Override
	public void update() {
		if (optimizer == null) {
			return; /* nothing to do */
		}

		// First break the old ones
		optimizer.updateLocation(this);
		for (int i=0; i<this.connections.size(); ) {
			Connection con = this.connections.get(i);
			NetworkInterface anotherInterface = con.getOtherInterface(this);

			// all connections should be up at this stage
			assert con.isUp() : "Connection " + con + " was down!";

			if (!isWithinRange(anotherInterface) && !forcedConnections.contains(con)) {
				disconnect(con,anotherInterface);
				connections.remove(i);
			}
			else {
				i++;
			}
		}
		// Then find new possible connections
		Collection<NetworkInterface> interfaces =
			optimizer.getNearInterfaces(this);
		for (NetworkInterface i : interfaces) {
			connect(i);
			i.connect(this);
		}
	}
	@Override
	public void destroyConnection(NetworkInterface anotherInterface) {
		if(!(anotherInterface instanceof SimpleAsymmetricInterface))
			throw new IllegalArgumentException("Impossible to connect to a different NetworkInterface type");
		if (isConnected(anotherInterface) && (this != anotherInterface)) {
			super.destroyConnection(anotherInterface);
		}
	}
}