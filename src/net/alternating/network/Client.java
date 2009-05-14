package net.alternating.network;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;

import processing.core.PApplet;

public class Client extends Thread{
	
	private PApplet parent;
	private RemoteAddress remoteAddress;

	private SocketChannel channel;
	
	private Charset charset = Charset.forName("UTF-8");
    private CharsetDecoder decoder = charset.newDecoder();
    
    private Method clientReceiveEventString;
    private Method clientReceiveEventByteArray;
	private Method clientDisconnectedEvent;
	private boolean run = true;
        
	public Client(PApplet parent,String ip, int port) {
		this.parent = parent;
		this.remoteAddress = new RemoteAddress(ip,port);
		parent.registerDispose(this);
		
		try {
			channel = SocketChannel.open();
		} catch (IOException e) {
			e.printStackTrace();
		}

        try {
			clientReceiveEventString = parent.getClass().getMethod("clientReceiveEvent", new Class[]{RemoteAddress.class,String.class} );
		} catch (Exception e) {
			//not declared, fine.
			//so we won't invoke this method.
			clientReceiveEventString = null;
		}
	    try {
			clientReceiveEventByteArray = parent.getClass().getMethod("clientReceiveEvent", new Class[]{RemoteAddress.class,byte[].class} );
		} catch (Exception e) {
			//not declared, fine.
			//so we won't invoke this method.
			clientReceiveEventByteArray = null;
		}
	    try {
			clientDisconnectedEvent = parent.getClass().getMethod("clientDisconnectedEvent", new Class[]{RemoteAddress.class} );
		} catch (Exception e) {
			//not declared, fine.
			//so we won't invoke this method.
			clientDisconnectedEvent = null;
		}
		
	}
	
	public void run() {
		try {
			ByteBuffer bf = ByteBuffer.allocate(5000);
			while(run) {
				
				int read = channel.read(bf);
				if(read == -1) {
					if(clientDisconnectedEvent != null) {
						try {
							clientDisconnectedEvent.invoke(parent, new Object[]{remoteAddress});
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					disconnect();
					break;
				}
				bf.flip();
				if(clientReceiveEventString != null) {
					String data = decoder.decode(bf).toString();
					Object[] args = {remoteAddress,data};
					try {
						clientReceiveEventString.invoke(parent, args);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				if(clientReceiveEventByteArray != null) {
					byte[] data = (byte[]) bf.array().clone();
					Object[] args = {remoteAddress,data};
					try {
						clientReceiveEventByteArray.invoke(parent, args);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}	
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	public boolean connect() {
		SocketAddress addr;
		try {
			addr = new InetSocketAddress(InetAddress.getByName(remoteAddress.getIp()),remoteAddress.getPort());
		} catch (UnknownHostException e) {
			e.printStackTrace();
			return false;
		}
		try {
			boolean success = channel.connect(addr);
			if(success) {
				start();
			}
			return success;
			
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		
	}
	
	
	
	
	public int writeData(String data) {
		ByteBuffer bf = ByteBuffer.wrap(data.getBytes());
		try {
			return channel.write(bf);
		} catch (IOException e) {
			e.printStackTrace();
			disconnect();
			return -1;
		}
	}
	
	public int writeData(int data){
		return writeData(Integer.toString(data));
	}
	
	public int writeData(double data) {
		return writeData(Double.toString(data));
	}
	
	public int writeData(byte data) {
		return writeData(Byte.toString(data));
	}
	
	public int writeData(byte[] data) {
		return writeData(new String(data));
	}
	
	public void disconnect() {
		try {
			channel.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		this.run  = false;
		dispose();
	}
	
	public void dispose() {
		if(channel.isOpen())
			try {
				channel.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
	}

}
