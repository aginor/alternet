import net.alternating.alternet.*;



int connectedCount = 0;
Server server;
void setup() {
 PFont metaBold = loadFont("TimesNewRomanPSMT-48.vlw");
 textFont(metaBold, 44); 
 size(80,80); 
 server = new Server(this,34567);
}

void draw() {
  fill(color(255,255,255));
  rect(0,0,80,80);
  fill(color(0,0,0));
  text(connectedCount,40,40);
}

//void serverReceiveEvent(Server receivingServer, RemoteAddress client, byte[] data) {
//}

void serverReceiveEvent(Server receivingServer, RemoteAddress client, String data) {
  if(connectedCount > 0) {
   server.sendToAll(data); 
  }
}

void serverConnectEvent(Server connectingServer, RemoteAddress client) {
  connectedCount++;
}

void serverDisconnectEvent(Server connectingServer, RemoteAddress client) {
  connectedCount--;
}
