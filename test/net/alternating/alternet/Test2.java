package net.alternating.alternet;

import processing.core.PApplet;

public class Test2 extends PApplet {

	ObjectClient c;
	
	public void setup() {
		size(400,400);
		int color = color(0,0,255);
        background(color);

		 c = new ObjectClient(this,"localhost",55665);
		 boolean success = c.connect();
		 //System.out.println(success);
		 if(!success) {
		     exit();
		 }
		 c.send("meep");
	}
	
	public void clientReceiveEvent(RemoteAddress address,byte[] data) {
		String path = selectInput("save");
		println(path);
		saveBytes(path, data);

	}
	
	
	public void draw() {
        stroke(0);
        if (mousePressed) {
        	
        }
	}
	
	public void disconnectedEvent(RemoteAddress address) {
	    System.out.println("disconnected");
	    exit();
	}
	
    /**
     * @param args
     * @throws InterruptedException 
     */
    public static void main(String[] args)  {
    	PApplet.main(new String[] {"--present","net.alternating.alternet.Test2"});
        
    }
}
