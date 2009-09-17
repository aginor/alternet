package net.alternating.alternet;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.CharacterCodingException;

import net.alternating.alternet.decoder.ObjectDecoder;

import processing.core.PApplet;

public class ObjectServer extends Server {

    private ObjectDecoder od;
    
	public ObjectServer(PApplet parent, int port) {
		super(parent, port);
		ObjectDecoder od = new ObjectDecoder(this);
		
	}

	public void sendTo(RemoteAddress address, byte[] data) {
		byte[] size = Utils.intToByteArray(data.length);
		super.sendTo(address,size);
		super.sendTo(address, data);
	}
	
	

	private void decodeData(ByteBuffer bf, int read, SocketChannel clientChannel)
    throws CharacterCodingException {
	    //	    throw a received event for String
	    // data
	    	    
	    // throw a received event for a byte[]
	    
	}
	
}
