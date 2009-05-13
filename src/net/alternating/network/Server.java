package net.alternating.network;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import javax.net.ServerSocketFactory;

public class Server extends Thread {
    
    private int port;
    private ServerSocketChannel serverChannel;
    private ServerSocket serverSocket;
    
    //private ServerSocket serverSocket;
    
    ReadThread readThread;
    
    public Server(int port) {
        this.port = port;
        readThread = new ReadThread();
        WriteThread writeThread = new WriteThread();
        start();
        readThread.start();
        writeThread.start();
    }
    
    public int getPort() {
        return port;
    }
    
    public void run(){
        if(this.serverSocket == null || this.serverSocket.isClosed()) {
            try {
                this.serverChannel = ServerSocketChannel.open();
                this.serverSocket = serverChannel.socket();
                serverSocket.bind(new InetSocketAddress(port));
                
            } catch (IOException e) {
                System.err.println("Couldn't create server socket. Do you have sufficient priviligies and is the port unused?");
                e.printStackTrace();
            }
        }
        
        
        while(true) {
            try {
                SocketChannel newConnection = serverChannel.accept();
                handleNewClient(newConnection);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        
        
        
    }

    private void handleNewClient(SocketChannel newConnection) throws ClosedChannelException {
        readThread.addClientChannel(newConnection);
    }
    
    
    
    
    
    
}
