package net.alternating.network;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.Iterator;
import java.util.Set;

import javax.net.ServerSocketFactory;

public class Server extends Thread {
    
    private int port;
    private ServerSocketChannel serverChannel;
    private ServerSocket serverSocket;
    
    private Selector selector;
    
    //private ServerSocket serverSocket;
    
    ReadThread readThread;
    
    public Server(int port) {
        this.port = port;
        
        start();
    }
    
    public int getPort() {
        return port;
    }
    
    public void run(){
    	SelectionKey serverKey;
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
    				System.out.println(key);
    				if(key.equals(serverKey) && key.isAcceptable()){
    					System.out.println(key.readyOps());
    					SocketChannel newConnection = serverChannel.accept();
    					
    					newConnection.configureBlocking(false);
    					newConnection.register(selector, newConnection.validOps());
    					
    				}
    				else {
    					SocketChannel clientChannel = (SocketChannel) key.channel();
    					if(key.isReadable()) {
    						System.out.println("read data");
    					} else if(key.isWritable()) {
    						System.out.println("ready for writing");
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

    private void handleNewClient(SocketChannel newConnection) throws IOException {
    	
        readThread.addClientChannel(newConnection);
    }
    
    
    
    
    
    
}
