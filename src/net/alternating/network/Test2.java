package net.alternating.network;

import processing.core.PApplet;

public class Test2 extends PApplet {

	Client c;
	
	public void setup() {
		size(400,400);
		int color = color(0,255,0);
        background(color);

		 c = new Client(this,"localhost",55665);
		 boolean success = c.connect();
		 System.out.println(success);
		 if(!success) {
		     exit();
		 }
	}
	
	public void clientReceiveEvent(RemoteAddress address,String data) {
		this.print(data);
		c.writeData("meep");
	}
	
	
	public void draw() {
        stroke(0);
        if (mousePressed) {
                line(mouseX,mouseY,pmouseX,pmouseY);
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
    	PApplet.main(new String[] {"--present","net.alternating.network.Test"});
        
    }
}
