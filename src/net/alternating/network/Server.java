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
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeMap;

import processing.core.PApplet;

/**
 * 
 * This class acts as an asynchronous TCP server. It listens for new connections
 * on the port specified in the constructor and adds the new connections to the
 * list of connections to monitor.
 * <p>
 * A callback is done every time a new connection is established, a connection
 * is torn down or data is received if the PApplet invoking this server has the
 * correct methods defined.
 * <p>
 * The appropriate methods in processing are:<br>
 * <code>
 * void serverConnectEvent(Server serverInstance, RemoteAddress clientAddress);<br>
 * void serverDisconnectEvent(Server serverInstance, RemoteAddress clientAddress);<br>
 * void serverReceiveEvent(Server serverInstance, RemoteAddress clientAddress, String data);<br>
 * void serverReceiveEvent(Server serverInstance, RemoteAddress clientAddress, byte[] data);<br>
 * </code>
 * <p>
 * The <code>serverReceiveEvent(...)</code> method with the <code>String</code>
 * argument will receive the data as an UTF-8 string. The version with a
 * <code>byte[]</code> will receive the data without any conversions applied to
 * it.
 * <p>
 * The server itself runs in a separate thread.
 * 
 * @author Andreas Löf
 * @see RemoteAddress
 * @see Client
 */
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
    
    private boolean run = true;
    
    /**
     * This constructs a new Server. The server will immediately start a new
     * thread and start listening on the specified port on all of the network
     * interfaces present in the computer.
     * 
     * 
     * 
     * @param parent
     *            the processing applet that contains the server instance.
     * @param port
     *            the port that the server will listen for new connections on
     */
    public Server(PApplet parent, int port) {
        this.parent = parent;
        this.port = port;
        
        parent.registerDispose(this);
        
        connectedClients = new TreeMap();
        
        try {
            connectEvent = parent.getClass().getMethod("serverConnectEvent",
                    new Class[] { Server.class, RemoteAddress.class });
        } catch (Exception e) {
            // not declared, fine.
            // so we won't invoke this method.
            connectEvent = null;
        }
        
        try {
            disconnectEvent = parent.getClass().getMethod(
                    "serverDisconnectEvent",
                    new Class[] { Server.class, RemoteAddress.class });
        } catch (Exception e) {
            // not declared, fine.
            // so we won't invoke this method.
            disconnectEvent = null;
        }
        try {
            receiveEventString = parent.getClass().getMethod(
                    "serverReceiveEvent",
                    new Class[] { Server.class, RemoteAddress.class,
                            String.class });
        } catch (Exception e) {
            // not declared, fine.
            // so we won't invoke this method.
            receiveEventString = null;
        }
        try {
            receiveEventByteArray = parent.getClass().getMethod(
                    "serverReceiveEvent",
                    new Class[] { Server.class, RemoteAddress.class,
                            byte[].class });
        } catch (Exception e) {
            // not declared, fine.
            // so we won't invoke this method.
            receiveEventByteArray = null;
        }
        
        start();
    }
    
    /**
     * Returns the port that this server instance is listening for new
     * connections on
     * 
     * @return the port that the server is bound to
     */
    public int getPort() {
        return port;
    }
    
    /**
     * This method is doing the actual work of the server. It deals with all of
     * the incoming connections and data.
     * 
     */
    public void run() {
        // used to identify the server
        SelectionKey serverKey;
        // FIXME this is probably not big enough in the long run
        ByteBuffer bf = ByteBuffer.allocate(20000);
        try {
            // open a new selector
            selector = Selector.open();
            
            // initiate the server socket and register it with the selector
            serverChannel = ServerSocketChannel.open();
            serverSocket = serverChannel.socket();
            serverSocket.bind(new InetSocketAddress(port));
            serverChannel.configureBlocking(false);
            serverKey = serverChannel
                    .register(selector, SelectionKey.OP_ACCEPT);
            
            // run until we are told not to
            while (run) {
                // select the channels that have data coming in on them or new
                // connections
                while (selector.select() > 0) {
                    
                    // find the channels with activity and iterate through them
                    Set keys = selector.selectedKeys();
                    SelectionKey key = null;
                    for (Iterator it = keys.iterator(); it.hasNext();) {
                        key = (SelectionKey) it.next();
                        // remove the key so we don't process it twice
                        it.remove();
                        
                        // did we get a new connection?
                        if (key.equals(serverKey) && key.isAcceptable()) {
                            
                            // accept the connection and add it to the selector
                            SocketChannel newConnection = serverChannel
                                    .accept();
                            
                            newConnection.configureBlocking(false);
                            newConnection.register(selector,
                                    SelectionKey.OP_READ);
                            
                            RemoteAddress remoteSide = new RemoteAddress(
                                    newConnection.socket().getInetAddress()
                                            .toString(), newConnection.socket()
                                            .getPort());
                            
                            // add it to our list of active connections
                            connectedClients.put(remoteSide, newConnection);
                            
                            // notify the processing applet that we have a new
                            // connection
                            throwConnectionEvent(remoteSide);
                            
                        }
                        // it wasn't the server, thus it must be one of the
                        // clients
                        else {
                            
                            SocketChannel clientChannel = (SocketChannel) key
                                    .channel();
                            
                            // have we gotten new data?
                            if (key.isReadable()) {
                                // clear the receiving buffer
                                bf.clear();
                                
                                // read data from the channel
                                int bytesRead = clientChannel.read(bf);
                                // is the socket closed?
                                if (bytesRead == -1) {
                                    
                                    RemoteAddress remoteSide = new RemoteAddress(
                                            clientChannel.socket()
                                                    .getInetAddress()
                                                    .toString(), clientChannel
                                                    .socket().getPort());
                                    
                                    // clean up this socket
                                    key.cancel();
                                    connectedClients.remove(remoteSide);
                                    clientChannel.close();
                                    
                                    // notify the processing applet
                                    throwDisconnectedEvent(remoteSide);
                                }
                                // we got data
                                else {
                                    
                                    // throw a received event for String data
                                    throwReceivedEventString(bf, clientChannel);
                                    
                                    // throw a received event for a byte[]
                                    throwReceivedEventByteArray(bf,
                                            clientChannel);
                                }
                            } else if (key.isWritable()) {
                                // if we cared about the channel being ready for
                                // writing, this is where
                                // we'd handle it.
                                // however, listening for the write status would
                                // lead to us doing busy wating
                                
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        
    }
    
    /**
     * This is a helper method used to notify the encapsulating processing
     * applet that we have received data. The data will be delivered to the
     * processing applet as a byte[].
     * 
     * @param bf
     *            the ByteBuffer containing the data
     * @param clientChannel
     *            the SocketChannel the data came from
     */
    private void throwReceivedEventByteArray(ByteBuffer bf,
            SocketChannel clientChannel) {
        if (receiveEventByteArray != null) {
            bf.flip();
            byte[] data = (byte[]) bf.array().clone();
            RemoteAddress remoteSide = new RemoteAddress(clientChannel.socket()
                    .getInetAddress().toString(), clientChannel.socket()
                    .getPort());
            Object[] args = { this, remoteSide, data };
            try {
                receiveEventByteArray.invoke(parent, args);
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
     * @param clientChannel
     *            the SocketChannel the data came from
     */
    private void throwReceivedEventString(ByteBuffer bf,
            SocketChannel clientChannel) throws CharacterCodingException {
        if (receiveEventString != null) {
            bf.flip();
            String data = decoder.decode(bf).toString();
            RemoteAddress remoteSide = new RemoteAddress(clientChannel.socket()
                    .getInetAddress().toString(), clientChannel.socket()
                    .getPort());
            Object[] args = { this, remoteSide, data };
            try {
                receiveEventString.invoke(parent, args);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    /**
     * This is a a helper method that will tell the encapsulating processing
     * applet that a client has disconnected
     * 
     * @param remoteSide
     *            the address and port of the disconnected client
     */
    private void throwDisconnectedEvent(RemoteAddress remoteSide) {
        if (disconnectEvent != null) {
            Object[] args = { this, remoteSide };
            try {
                disconnectEvent.invoke(parent, args);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    /**
     * This is a helper method that tells the encapsulating processing applet
     * that a new client has connected.
     * 
     * @param remoteSide
     *            the port and address of the new client
     */
    private void throwConnectionEvent(RemoteAddress remoteSide) {
        if (connectEvent != null) {
            Object[] args = { this, remoteSide };
            try {
                connectEvent.invoke(parent, args);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    /**
     * This method sends data to all connected clients.
     * 
     * @param data
     *            the data to be sent
     */
    public synchronized void sendToAll(String data) {
        Iterator keys = connectedClients.keySet().iterator();
        while (keys.hasNext()) {
            RemoteAddress address = (RemoteAddress) keys.next();
            this.sendTo(address, data);
        }
    }
    
    /**
     * This method sends data to all connected clients.
     * 
     * @param data
     *            the data to be sent
     */
    public synchronized void sendToAll(int data) {
        this.sendToAll(Integer.toString(data));
    }
    
    /**
     * This method sends data to all connected clients.
     * 
     * @param data
     *            the data to be sent
     */
    public synchronized void sendToAll(double data) {
        this.sendToAll(Double.toString(data));
    }
    
    /**
     * This method sends data to all connected clients.
     * 
     * @param data
     *            the data to be sent
     */
    public synchronized void sendToAll(byte data) {
        this.sendToAll(Byte.toString(data));
    }
    
    /**
     * This method sends data to all connected clients.
     * 
     * @param data
     *            the data to be sent
     */
    public synchronized void sendToAll(byte[] data) {
        this.sendToAll(new String(data));
    }
    
    /**
     * This method sends data to a specified client identified by its
     * RemoteAddress.
     * 
     * @param address
     *            the connected client's remote address
     * @param data
     *            the data to be sent
     * @see RemoteAddress
     */
    public synchronized void sendTo(RemoteAddress address, String data) {
        SocketChannel clientChannel = (SocketChannel) connectedClients
                .get(address);
        
        ByteBuffer bf = ByteBuffer.wrap(data.getBytes());
        
        try {
            clientChannel.write(bf);
        } catch (IOException e) {
            e.printStackTrace();
        }
        
    }
    
    /**
     * This method sends data to a specified client identified by its
     * RemoteAddress.
     * 
     * @param address
     *            the connected client's remote address
     * @param data
     *            the data to be sent
     * @see RemoteAddress
     */
    public synchronized void sendTo(RemoteAddress address, int data) {
        this.sendTo(address, Integer.toString(data));
    }
    
    /**
     * This method sends data to a specified client identified by its
     * RemoteAddress.
     * 
     * @param address
     *            the connected client's remote address
     * @param data
     *            the data to be sent
     * @see RemoteAddress
     */
    public synchronized void sendTo(RemoteAddress address, double data) {
        this.sendTo(address, Double.toString(data));
    }
    
    /**
     * This method sends data to a specified client identified by its
     * RemoteAddress.
     * 
     * @param address
     *            the connected client's remote address
     * @param data
     *            the data to be sent
     * @see RemoteAddress
     */
    public synchronized void sendTo(RemoteAddress address, byte data) {
        this.sendTo(address, Byte.toString(data));
    }
    
    /**
     * This method sends data to a specified client identified by its
     * RemoteAddress.
     * 
     * @param address
     *            the connected client's remote address
     * @param data
     *            the data to be sent
     * @see RemoteAddress
     */
    public synchronized void sendTo(RemoteAddress address, byte[] data) {
        this.sendTo(address, new String(data));
    }
    
    /**
     * This method is called by the processing applet that contains this server
     * instance. All of the open network sockets are closed and the worker
     * thread is told to terminate.
     * 
     */
    public void dispose() {
        run = false;
        try {
            serverChannel.close();
            Iterator keys = connectedClients.keySet().iterator();
            while (keys.hasNext()) {
                RemoteAddress address = (RemoteAddress) keys.next();
                SocketChannel clientChannel = (SocketChannel) connectedClients
                        .get(address);
                clientChannel.close();
            }
            selector.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
    }
    
}
