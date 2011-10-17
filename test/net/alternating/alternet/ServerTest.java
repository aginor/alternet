package net.alternating.alternet;

import processing.core.PApplet;

public class ServerTest extends PApplet {

	ObjectServer s;
	
	String[] umlauts = {"audio0*Mit mei-nem schö-nen un-be-kann-ten Mäd-chen will ich nun bald zum Ziel,*1*17","audio15*Die-se, je-ne, sie gilt mir nicht mehr als an-dre Schö-ne,*1*14"};

	public void setup() {
		size(400, 400);
		int color = color(0, 255, 0);
		background(color);

		s = new ObjectServer(this, 55665);
	}

	public void serverReceiveEvent(Server server, RemoteAddress address,
			byte[] data) {
		System.out.println("server received:" + data.length);
		//for (int i = 0; i < 300 && i < data.length; i++) {
			//System.out.print(data[i] + ",");
		//}
		//System.out.println();
		//byte[] data2 = new byte[data.length - 4];
		//System.arraycopy(data, 4, data2, 0, data2.length);
		//String received = new String(data);
		//println("server received:" + received);
		//s.sendToAll(received.toUpperCase());
		//String path = selectInput("save");
		//println("server:" + path);
		//saveBytes(path, data);
	}

	int num = 0;
	public void draw() {
		stroke(0);
		
		
	}

	public void mouseClicked() {
		
			//num++;
			String loadPath = selectInput();  // Opens file chooser
        	if (loadPath == null) {
        	  // If a file was not selected
        	  println("No file was selected...");
        	} else {
        		println("Sending");
        		// If a file was selected, print path to file
        		byte[] data = loadBytes(loadPath);
        		s.sendToAll(data);
        	}
			//line(mouseX, mouseY, pmouseX, pmouseY);
			 
			 
			//println("sending: " + umlauts[num%2]);
			//s.sendToAll(umlauts[num%2]);

	}
	
	/**
	 * @param args
	 * @throws InterruptedException
	 */
	public static void main(String[] args) {
		PApplet.main(new String[] { "--present",
				"net.alternating.alternet.ServerTest" });

	}

}
