/**
 * 
 */
package net.alternating.alternet;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.charset.CharacterCodingException;

import processing.core.PApplet;

/**
 * @author Andreas Löf
 *
 */
public class ObjectClient extends Client {

	
	protected static final int STATE_SIZE = 0;
	protected static final int STATE_DATA = 1;
	
	//protected static final byte TYPE_STRING = 0;
	//protected static final byte TYPE_JPG = 1;
	//protected static final byte TYPE_PNG = 2;
	
	private int state;
	private int read;
	private int size;
	
	protected ByteBuffer data;
	
	/**
	 * @param parent
	 * @param ip
	 * @param port
	 */
	public ObjectClient(PApplet parent, String ip, int port) {
		super(parent, ip, port);
		state = ObjectClient.STATE_SIZE;
		read = 0;
		size = 0;
		data = ByteBuffer.allocate(2000);
	}

	public int send(byte[] data) {
		int length = data.length;
		byte[] lengthAsbytes = Utils.intToByteArray(length);
		super.send(lengthAsbytes);
		return super.send(data);
	}

	protected void decodeData(ByteBuffer bf, int length) throws CharacterCodingException {
		bf.flip();
		decodeData2(bf, length);
	}
	
	private void decodeData2(ByteBuffer bf, int length) throws CharacterCodingException {

		switch(state){
		case STATE_SIZE:
			for(int i = 0; i < length && data.hasRemaining(); i++)
				data.put(bf.get());
			
			if(data.position() >= 4) {
				state = STATE_DATA;
				//FIXME use getInt() instead
				byte[] sizeBytes = new byte[4];
				data.rewind();
				data.get(sizeBytes);
				size = Utils.byteArrayToInt(sizeBytes);
				ByteBuffer newData = ByteBuffer.allocate(size);
				ByteBuffer oldData = data;
				data = newData;
				/*while(newData.hasRemaining())
					newData.put(data.get());
				
				for(int i = newData.capacity(); i < length && newData.hasRemaining(); i++)
					newData.put(bf.get());*/

				//data = newData;
				//read += length - 4;
				int newLength = length - 4;
				decodeData2(oldData, newLength);
				if(length > oldData.capacity())
					decodeData2(bf,oldData.capacity());
			}
			else {
				read += length;
			}
			break;
		case STATE_DATA:
			for(int i = 0; i < length && data.hasRemaining(); i++)
				data.put(bf.get());
			read += length;
			if(read == size) {
				throwClientRecieveEventString(data);
				throwClientReceiveEventByteArray(data);
				read = 0;
				state = STATE_SIZE;
				data = ByteBuffer.allocate(20000);
				if(size < length) {
					decodeData2(bf, length - size);
				}
			}
			break;
		}

		//super.decodeData(bf, length);
	}
	
	

}
