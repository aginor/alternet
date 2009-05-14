package net.alternating.network;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeMap;

import processing.core.PApplet;

public class Server extends Thread {
    
    private int port;
    private ServerSocketChannel serverChannel;
    private ServerSocket serverSocket;
    
    private Selector selector;
    
    private Charset charset = Charset.forName("UTF-8");
    private CharsetDecoder decoder = charset.newDecoder();
	private PApplet parent;
    
	
	private Method connectEvent;
	private Method disconnectEvent;
	private Method receiveEventString;
	private Method receiveEventByteArray;
	
    private TreeMap connectedClients;
    
    public Server(PApplet parent, int port) {
    	this.parent = parent;
        this.port = port;
        
        connectedClients = new TreeMap();
    
        try {
			connectEvent = parent.getClass().getMethod("connectEvent", new Class[]{Server.class,RemoteAddress.class} );
		} catch (Exception e) {
			//not declared, fine.
			//so we won't invoke this method.
			connectEvent = null;
		}
		
        try {
			disconnectEvent = parent.getClass().getMethod("disconnectEvent", new Class[]{Server.class,RemoteAddress.class} );
		} catch (Exception e) {
			//not declared, fine.
			//so we won't invoke this method.
			disconnectEvent = null;
		}
        try {
			receiveEventString = parent.getClass().getMethod("receiveEventString", new Class[]{Server.class,RemoteAddress.class,String.class} );
		} catch (Exception e) {
			//not declared, fine.
			//so we won't invoke this method.
			receiveEventString = null;
		}
		try {
			receiveEventByteArray = parent.getClass().getMethod("receiveEventByteArray", new Class[]{Server.class,RemoteAddress.class,byte[].class} );
		} catch (Exception e) {
			//not declared, fine.
			//so we won't invoke this method.
			receiveEventByteArray = null;
		}
        
        start();
    }
    
    public int getPort() {
        return port;
    }
    
    public void run(){
    	SelectionKey serverKey;
    	ByteBuffer bf = ByteBuffer.allocate(5000);
    	try {
    		selector = SelectorProvider.provider().openSelector();
    		this.serverChannel = ServerSocketChannel.open();
    		this.serverSocket = serverChannel.socket();
    		serverSocket.bind(new InetSocketAddress(port));
    		serverChannel.configureBlocking(false);
    		serverKey = serverChannel.register(selector, SelectionKey.OP_ACCEPT);
    		while(true) {
    		while(selector.select() > 0) {
    			Set keys = selector.selectedKeys();
    			SelectionKey key = null;
    			for(Iterator it = keys.iterator(); it.hasNext(); ) {
    				key=(SelectionKey)it.next();
    				it.remove();
    				
    				if(key.equals(serverKey) && key.isAcceptable()){
    					
    					SocketChannel newConnection = serverChannel.accept();
    					
    					newConnection.configureBlocking(false);
    					newConnection.register(selector, SelectionKey.OP_READ);
    					
    					RemoteAddress remoteSide = new RemoteAddress(newConnection.socket().getInetAddress().toString(),newConnection.socket().getPort());
    					
    					connectedClients.put(remoteSide, newConnection);
    					
    					if(connectEvent != null){
    						Object[] args = {this,remoteSide};
							try {
								connectEvent.invoke(parent, args);
							} catch (Exception e) {
								e.printStackTrace();
							}
    					}
    					
    					
    				}
    				
    				else {
    					SocketChannel clientChannel = (SocketChannel) key.channel();
    					if(key.isReadable()) {
    						bf.clear();
    						int bytesRead = clientChannel.read(bf);
    						//socket is closed
    						if(bytesRead == -1) {
    					
    							RemoteAddress remoteSide = new RemoteAddress(clientChannel.socket().getInetAddress().toString(),clientChannel.socket().getPort());
    							
    							
    							if(disconnectEvent != null) {
    								Object[] args = {this,remoteSide};
    								try {
										disconnectEvent.invoke(parent, args);
									} catch (Exception e) {
										e.printStackTrace();
									}
    							}
    							
    							// clean up this socket
    							key.cancel();
    							connectedClients.remove(remoteSide);
    							clientChannel.close();
    							
    							
    							
    						}
    						else {
    							if(receiveEventString != null) {
        							bf.flip();
    								String data = decoder.decode(bf).toString();
    								RemoteAddress remoteSide = new RemoteAddress(clientChannel.socket().getInetAddress().toString(),clientChannel.socket().getPort());
    								Object[] args = {this,remoteSide,data};
    								try {
										receiveEventString.invoke(parent, args);
									} catch (Exception e) {
										e.printStackTrace();
									}
    							}
    							if(receiveEventByteArray != null) {
        							bf.flip();
    								byte[] data = bf.array();
    								RemoteAddress remoteSide = new RemoteAddress(clientChannel.socket().getInetAddress().toString(),clientChannel.socket().getPort());
    								Object[] args = {this,remoteSide,data};
    								try {
										receiveEventByteArray.invoke(parent, args);
									} catch (Exception e) {
										e.printStackTrace();
									}
    							}
    						}
    					} else if(key.isWritable()) {
    						
    						//System.out.println("ready for writing");
    					}
    					else {
    						
    						//System.out.println("something else");
    					}
    				}
    				
    			}
    		}
    		}
		} catch (IOException e) {
			e.printStackTrace();
		}
        
        
        
    }
    
    public synchronized void sendToAll(String data) {
    	Iterator keys = connectedClients.keySet().iterator();
    	while(keys.hasNext()) {
    		RemoteAddress address = (RemoteAddress) keys.next();
    		this.sendTo(address,data);
    	}
    }
    public synchronized void sendToAll(int data) {
    	this.sendToAll(Integer.toString(data));
    }
    public synchronized void sendToAll(double data) {
    	this.sendToAll(Double.toString(data));
    }
    public synchronized void sendToAll(byte data) {
    	this.sendToAll(Byte.toString(data));
    }
    public synchronized void sendToAll(byte[] data) {
    	this.sendToAll(new String(data));
    }
    
    
    
    
    public synchronized void sendTo(RemoteAddress address,String data) {
    	SocketChannel clientChannel = (SocketChannel) connectedClients.get(address);
    
    	ByteBuffer bf = ByteBuffer.wrap(data.getBytes());
    	
    	try {
			clientChannel.write(bf);
		} catch (IOException e) {
			e.printStackTrace();
		}
    	
    	
    	
    }
    public synchronized void sendTo(RemoteAddress address,int data) {
    	this.sendTo(address, Integer.toString(data));
    }
    public synchronized void sendTo(RemoteAddress address,double data) {
    	this.sendTo(address, Double.toString(data));
    }
    public synchronized void sendTo(RemoteAddress address,byte data) {
    	this.sendTo(address, Byte.toString(data));
    }
    public synchronized void sendTo(RemoteAddress address,byte[] data) {
    	this.sendTo(address, new String(data));
    }
    
    

    
    
        
    
}
