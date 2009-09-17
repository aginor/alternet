/*
 * This is an alternative server/client library for Processing. 
 * Copyright (C)2009 Andreas Löf 
 * Email: andreas@alternating.net
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */


package net.alternating.alternet;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousCloseException;
import java.nio.channels.SocketChannel;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;

import processing.core.PApplet;


/**
 * This class acts as a TCP client. It can be used to communicate with any TCP server, although it does 
 * not contain support for any specific protocol.
 * <p>
 * The client is asynchronous and will notify the encapsulating processing applet when data is received over the network
 * or when the network connection is broken.
 * <p>
 * The methods that the encapsulating processing applet can implement are:<br>
 * <code>
 * void clientReceiveEvent(RemoteAddress serverAddress, String data);<br>
 * void clientReceiveEvent(RemoteAddress serverAddress, byte[] data);<br>
 * void clientDisconnectedEvent(RemoteAddress serverAddress); <br>
 * </code>
 * 
 * 
 * @author Andreas L&ouml;f
 * @see RemoteAddress
 * @see Server
 */
public class Client extends Thread implements Deliverer{
	
	public static int bufferSize = 256*1024;//*1024; 
	
	private PApplet parent;
	private RemoteAddress remoteAddress;

	protected SocketChannel channel;
	
	private Charset charset = Charset.forName("UTF-8");
    private CharsetDecoder decoder = charset.newDecoder();
    
    private Method clientReceiveEventString;
    private Method clientReceiveEventByteArray;
	private Method clientDisconnectedEvent;
	private boolean run = true;
        
	/**
	 * This constructs a new client and initializes it. The client is however not connected and need
	 * to explicitly be connected.
	 * 
	 * @param parent the encapsulating processing applet
	 * @param ip the ip or address to connect to
	 * @param port the port to connect to
	 */
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
			clientDisconnectedEvent = parent.getClass().getMethod("clientDisconnectEvent", new Class[]{RemoteAddress.class} );
		} catch (Exception e) {
			//not declared, fine.
			//so we won't invoke this method.
			clientDisconnectedEvent = null;
		}
		
	}
	
	/**
	 * This method perform the asynchronous reception of data.
	 */
	public void run() {
		try {
		    //FIXME this should be the same as in the server and not hardcoded like this
			ByteBuffer bf = ByteBuffer.allocate(bufferSize);
			while(run) {
				
			    //read data
				//System.out.println(channel.isBlocking());
				int read = channel.read(bf);
				//have we lost the connection to the server
				if(read == -1) {
					throwDisconnectedEvent();
					break;
				}
				//nope, thus we proceed.

				//if(read > 0) {
					//System.out.println("read : " + read);
					decodeData(bf, read);
					bf.clear();
				//}
			}
		} catch (AsynchronousCloseException ace) {
			// someone closed the socket, we do nothing more than exit the
			// thread gracefully.
		} catch (IOException e) {
			throwDisconnectedEvent();
			e.printStackTrace();
		}
		
	}

	protected void decodeData(ByteBuffer bf, int length) throws CharacterCodingException {
		bf.flip();
		//deliver data
		throwReceiveEventString(bf);
		throwReceiveEventByteArray(bf);	
		bf.clear();
	}
	/**
     * This is a helper method used to notify the encapsulating processing
     * applet that we have received data. The data will be delivered to the
     * processing applet as a byte[].
     * 
     * @param bf
     *            the ByteBuffer containing the data
     */
    public void throwReceiveEventByteArray(ByteBuffer bf) {
        if(clientReceiveEventByteArray != null) {
        	//byte[] data = (byte[]) bf.array().clone();
        	int size = bf.limit();
        	byte[] data = new byte[size];
        	System.arraycopy(bf.array(), 0, data, 0, size);
        	Object[] args = {remoteAddress,data};
        	try {
        		clientReceiveEventByteArray.invoke(parent, args);
        	} catch (Exception e) {
        		e.printStackTrace();
        	}
        }
    }

    /**
     * This is a helper method used to notify the encapsulating processing
     * applet that we have received data. The data will be delivered to the
     * processing applet as a String.
     * 
     * @param bf
     *            the ByteBuffer containing the data
     */
    public void throwReceiveEventString(ByteBuffer bf)
            throws CharacterCodingException {
        if(clientReceiveEventString != null) {
        	String data = decoder.decode(bf).toString();
        	Object[] args = {remoteAddress,data};
        	try {
        		clientReceiveEventString.invoke(parent, args);
        	} catch (Exception e) {
        		e.printStackTrace();
        	}
        }
    }
	
    /**
     * This method establishes the connection and starts the thread that listens to the data.
     * @return true if the connection was successfully established, false otherwise
     */
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
	
	
	
	/**
	 * This client sends data to the server.
	 * <p>
	 * The method returns -1 and throws a disconnectedEvent if the connection has been broken.
	 * 
	 * @param data the data to be sent
	 * @return the number of bytes sent to the server.
	 */
	public int send(String data) {
		return send(data.getBytes());
	}
	
	/**
	 * Helper function used to tell the encapsulating processing applet when we have lost the connection
	 * to the server.
	 */
	protected void throwDisconnectedEvent() {
	    if(clientDisconnectedEvent != null) {
            try {
                clientDisconnectedEvent.invoke(parent, new Object[]{remoteAddress});
            } catch (Exception e) {
                e.printStackTrace();
            }
        } 
    }
	/**
     * This client sends data to the server.
     * <p>
     * The method returns -1 and throws a disconnectedEvent if the connection has been broken.
     * 
     * @param data the data to be sent
     * @return the number of bytes sent to the server.
     */
    public int send(int data){
		return send(Integer.toString(data));
	}
    /**
     * This client sends data to the server.
     * <p>
     * The method returns -1 and throws a disconnectedEvent if the connection has been broken.
     * 
     * @param data the data to be sent
     * @return the number of bytes sent to the server.
     */
	public int send(double data) {
		return send(Double.toString(data));
	}
	/**
     * This client sends data to the server.
     * <p>
     * The method returns -1 and throws a disconnectedEvent if the connection has been broken.
     * 
     * @param data the data to be sent
     * @return the number of bytes sent to the server.
     */
	public int send(byte data) {
		return send(Byte.toString(data));
	}
	/**
     * This client sends data to the server.
     * <p>
     * The method returns -1 and throws a disconnectedEvent if the connection has been broken.
     * 
     * @param data the data to be sent
     * @return the number of bytes sent to the server.
     */
	public int send(byte[] data) {
	    ByteBuffer bf = ByteBuffer.wrap(data);
        try {
            return channel.write(bf);
        } catch (IOException e) {
            e.printStackTrace();
            disconnect();
            throwDisconnectedEvent();
            return -1;
        }
	}
	
	/**
	 * This method tears down the connection to the server and stops the worker thread.
	 */
	public void disconnect() {
		try {
			channel.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		this.run  = false;
	}
	
	/**
	 * This is called by the encapsulating processing applet.
	 * It closes the connection if it is not already closed.
	 */
	public void dispose() {
		if(channel.isOpen())
			disconnect();
	}

}
