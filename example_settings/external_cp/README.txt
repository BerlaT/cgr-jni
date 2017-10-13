You have to do this before run:

1. class input.ExternalEventsQueue.java

	protected void init(String eeFilePath) {
		this.eventsFile = new File(eeFilePath);

		if (BinaryEventsReader.isBinaryEeFile(eventsFile)) {
			this.reader = new BinaryEventsReader(eventsFile);
		}
		else {
			//WRITE THIS
			this.reader = new CPEventsReader(eventsFile);
			
			//COMMENT THIS
			//this.reader = new StandardEventsReader(eventsFile);
	}

	Ctrl+S to save.

2. class core.NetworkInterface.java

	public void setTransmitSpeed(int transmitSpeed) {
		this.transmitSpeed = transmitSpeed;
	}
	
	Ctrl+S to save.

	You can add this in every place in code (e.g. after line 200) 

3. class core.DTNHost.java
	3.1 Remove DTNHost.java class in core package in theONE project (open core package, right click on DTNHost.java, select Delete)
	
	3.2 *COPY* DTNHost.java class that you find in this folder

	3.3 *PASTE* DTNHost.java class in core package in theONE project (Right click on core package>Paste)






 
