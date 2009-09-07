package net.alternating.alternet;

import processing.core.PApplet;

public class Test extends PApplet{
    
	Server s;
	
	public void setup() {
		size(400,400);
		int color = color(0,255,0);
        background(color);

		 s = new Server(this,55665);
	}
	
	public void serverReceiveEvent(Server server, RemoteAddress address,byte[] data) {
		System.out.println("server received:" + data.length);
		for(int i = 0; i < 300 && i < data.length; i++) {
			System.out.print(data[i]+",");
		}
		System.out.println();
			
		String received = new String(data);
		s.sendToAll(received.toUpperCase());
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
    	PApplet.main(new String[] {"--present","net.alternating.alternet.Test"});
        
    }
    
    
    
}
