package net.alternating.alternet;

import processing.core.PApplet;

public class ServerTest extends PApplet {

	ObjectServer s;

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
		String path = selectInput("save");
		println("server:" + path);
		saveBytes(path, data);
	}

	public void draw() {
		stroke(0);
		if (mousePressed) {
			String loadPath = selectInput();  // Opens file chooser
        	if (loadPath == null) {
        	  // If a file was not selected
        	  println("No file was selected...");
        	} else {
        	  // If a file was selected, print path to file
        	  byte[] data = loadBytes(loadPath);
        	  s.sendToAll(data);
        	}
			//line(mouseX, mouseY, pmouseX, pmouseY);
		}
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
