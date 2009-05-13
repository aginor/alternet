package net.alternating.network;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.Iterator;
import java.util.Set;

public class ReadThread extends Thread{
    
    private Selector selector;
    private ByteBuffer readBuffer;
    public ReadThread() {
        try {
            selector = SelectorProvider.provider().openSelector();
            readBuffer = ByteBuffer.allocate(5000);
        } catch (IOException e) {
            System.err.println("Could not create selector");
            e.printStackTrace();
        }
    }
    
    public synchronized void addClientChannel(SocketChannel clientChannel) throws IOException {
        clientChannel.configureBlocking(false);
        clientChannel.register(selector, clientChannel.validOps());
    }
    
    public synchronized void run() {
        try {
            while(selector.select() > 0) {
                Set keys = selector.selectedKeys();
                Iterator it = keys.iterator();
                while(it.hasNext()) {
                    SelectionKey key = (SelectionKey) it.next();
                    SocketChannel socketChan = (SocketChannel) key.channel();
                    readBuffer.clear();
                    socketChan.read(readBuffer);
                    performCallback(readBuffer);
                }
            }
            System.out.println("exited reading loop");
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void performCallback(ByteBuffer readBuffer) {
        System.out.println(readBuffer);
    }
    
}
