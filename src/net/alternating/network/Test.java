package net.alternating.network;

import processing.core.PApplet;

public class Test extends PApplet{
    
	Server s;
	
	public void setup() {
		size(400,400);
		int color = color(0,255,0);
        background(color);

		 s = new Server(this,55665);
	}
	
	public void receiveEvent(Server server, RemoteAddress address,String data) {
		this.print(data);
		s.sendToAll(data.toUpperCase());
	}
	
	
	public void draw() {
        stroke(0);
        if (mousePressed) {
                line(mouseX,mouseY,pmouseX,pmouseY);
        }
}
	
    /**
     * @param args
     * @throws InterruptedException 
     */
    public static void main(String[] args)  {
    	PApplet.main(new String[] {"--present","net.alternating.network.Test"});
        
    }
    
    
    
}
