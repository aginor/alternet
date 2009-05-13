package net.alternating.network;

import java.io.IOException;
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

public class Server extends Thread {
    
    private int port;
    private ServerSocketChannel serverChannel;
    private ServerSocket serverSocket;
    
    private Selector selector;
    
    private Charset charset = Charset.forName("UTF-8");
    private CharsetDecoder decoder = charset.newDecoder();
    
    
    
    public Server(int port) {
        this.port = port;
        
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
    				//System.out.println(key);
    				
    				if(key.equals(serverKey) && key.isAcceptable()){
    					
    					SocketChannel newConnection = serverChannel.accept();
    					
    					newConnection.configureBlocking(false);
    					newConnection.register(selector, SelectionKey.OP_READ);
    					
    				}
    				
    				else {
    					SocketChannel clientChannel = (SocketChannel) key.channel();
    					if(key.isReadable()) {
    						bf.clear();
    						int bytesRead = clientChannel.read(bf);
    						//socket is closed
    						if(bytesRead == -1) {
    							key.cancel();
    							clientChannel.close();
    							System.out.println("disconnected");
    							//TODO throw disconnected event
    						}
    						else {
    							bf.flip();
    							String data = decoder.decode(bf).toString();
    							System.out.println(data.trim());
    							//TODO throw data event
    						}
    					} else if(key.isWritable()) {
    						
    						//System.out.println("ready for writing");
    					}
    					else {
    						System.out.println("something else");
    					}
    				}
    				
    			}
    		}
    		}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        
        
    }

        
    
}
