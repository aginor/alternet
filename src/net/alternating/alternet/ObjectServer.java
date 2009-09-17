package net.alternating.alternet;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.nio.charset.CharacterCodingException;
import java.util.HashMap;
import java.util.Map;

import net.alternating.alternet.decoder.ObjectDecoder;

import processing.core.PApplet;

public class ObjectServer extends Server {

    private ObjectDecoder od;
    private Map decoders; 
    
	public ObjectServer(PApplet parent, int port) {
		super(parent, port);
		decoders = new HashMap();
		
	}

	protected void clientDisconnected(SelectionKey key) {
		decoders.remove(super.clientChannel);
		super.clientDisconnected(key);
	}

	protected void newClientConnected(SocketChannel newConnection,
			RemoteAddress remoteSide) {
		decoders.put(newConnection, new ObjectDecoder(this));
		super.newClientConnected(newConnection, remoteSide);
	}

	public void sendTo(RemoteAddress address, byte[] data) {
		byte[] size = Utils.intToByteArray(data.length);
		super.sendTo(address,size);
		super.sendTo(address, data);
	}

	protected void decodeData(ByteBuffer bf, int read)
			throws CharacterCodingException {
		ObjectDecoder od = (ObjectDecoder) decoders.get(clientChannel);
		bf.flip();
		od.decode(bf, read);
	}
	
	
	
}
