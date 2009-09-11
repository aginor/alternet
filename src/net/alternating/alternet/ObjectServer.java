package net.alternating.alternet;

import processing.core.PApplet;

public class ObjectServer extends Server {

	public ObjectServer(PApplet parent, int port) {
		super(parent, port);
	}

	public void sendTo(RemoteAddress address, byte[] data) {
		byte[] size = Utils.intToByteArray(data.length);
		super.sendTo(address,size);
		super.sendTo(address, data);
	}
	
	

}
