
/*
 * This is an alternative server/client library for Processing.
 *
 * This example demonstrates a very simple chat server. The server 
 * listens on port 34567 by default.
 *
 * Please see the documentation for the different methods implemented by Server.
 *
 * Copyright (C)2009 Andreas Löf 
 * Email: andreas@alternating.net
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */


import net.alternating.alternet.*;


// This variable is used to track the number of connected clients we have
int connectedCount = 0;

Server server;

void setup() {
 PFont metaBold = loadFont("TimesNewRomanPSMT-48.vlw");
 textFont(metaBold, 44); 
 size(80,80); 
 // create a new server instance, listening to port 34567
 server = new Server(this,34567);
}

void draw() {
  fill(color(255,255,255));
  rect(0,0,80,80);
  fill(color(0,0,0));
  text(connectedCount,40,40);
}

// This will only need to be added if you want to receive byte[] data.
//void serverReceiveEvent(Server receivingServer, RemoteAddress client, byte[] data) {
//}

// We want to receive string data from our clients.
void serverReceiveEvent(Server receivingServer, RemoteAddress client, String data) {
  if(connectedCount > 0) {
   // Forward the received data to all the clients
   server.sendToAll(data); 
  }
}


void serverConnectEvent(Server connectingServer, RemoteAddress client) {
  connectedCount++;
}

void serverDisconnectEvent(Server connectingServer, RemoteAddress client) {
  connectedCount--;
}
