package net.alternating.network;

public class Test {
    
    /**
     * @param args
     * @throws InterruptedException 
     */
    public static void main(String[] args) throws InterruptedException {
        Server s = new Server(55665);
        s.join();
        
        
    }
    
}
