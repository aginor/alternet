package net.alternating.alternet;

import processing.core.PApplet;

public class ClientTest extends PApplet {

	ObjectClient c;
	

	
	public void setup() {
		size(400,400);
		int color = color(0,0,255);
		//int fg = color(0);
        background(color);
        //setForeground(fg);
		 c = new ObjectClient(this,"localhost",55665);
		 boolean success = c.connect();
		 //System.out.println(success);
		 if(!success) {
		     exit();
		 }
		 //c.send("meep");
	}
	
	String paintme = "";
	
	public void clientReceiveEvent(RemoteAddress address,byte[] data) {
		String path = selectInput("save");
		println("client: " + path);
		saveBytes(path, data);
		c.send(data);
		//println("received: "+ data);
		//paintme = data;
	}
	
	
	public void draw() {
        stroke(0);
        rect(0,0,400,400);
        stroke(255);
        text(paintme);
        //if (mousePressed) {
        	
        //}
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
    	PApplet.main(new String[] {"--present","net.alternating.alternet.ClientTest"});
        
    }
}
