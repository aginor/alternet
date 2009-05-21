import net.alternating.alternet.*;


Client client;

String displayString = "";

PFont displayFont;

void setup() {
  displayFont = loadFont("SansSerif.plain-16.vlw");
  textFont(displayFont,16);
  size(640,480); 
  client = new Client(this,"127.0.0.1",34567);
  boolean success = client.connect();
  if(!success) {
    print("failed to connect");
    exit(); 
  }
  background(0,0,0);
}



void clientDisconnectEvent(RemoteAddress ra) {
  fill(255,0,0);
  textFont(displayFont,50);
  displayString = "Server Aborted Connection";

}

//void clientReceiveEvent(RemoteAddress serverAddress, byte[] data) {
//}

void clientReceiveEvent(RemoteAddress serverAddress, String data) {
  displayString += data;
}

void draw() {
  
  text(displayString,0,0,640,480);
  
}

void keyPressed() {
  client.send("" + key);
}

