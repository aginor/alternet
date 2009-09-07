package net.alternating.alternet;

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
		 c.send("meep");
	}
	
	public void clientReceiveEvent(RemoteAddress address,byte[] data) {
		for(int i = 0; i < 300 && i < data.length; i++) {
			System.out.print(data[i]+",");
		}
		this.print(data);

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
    	PApplet.main(new String[] {"--present","net.alternating.alternet.Test2"});
        
    }
}
