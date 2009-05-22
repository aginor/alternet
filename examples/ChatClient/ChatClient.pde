
/*
 * This is an alternative server/client library for Processing.
 *
 * This example demonstrates a very simple chat client. The client 
 * connects to port 34567 on localhost by default.
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


Client client;

String displayString = "";

PFont displayFont;

void setup() {
  displayFont = loadFont("SansSerif.plain-16.vlw");
  textFont(displayFont,16);
  size(640,480); 

  //instanciate a new server that will connect to localhost on port 34567
  client = new Client(this,"127.0.0.1",34567);

  //connect to the server
  boolean success = client.connect();

  //check if we failed to establish a connection
  if(!success) {
    // if we failed, exit.
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

// Only implement this method if you want to receive byte[]s from the client.
//void clientReceiveEvent(RemoteAddress serverAddress, byte[] data) {
//}

// Add the string data that we receive to string we display
void clientReceiveEvent(RemoteAddress serverAddress, String data) {
  displayString += data;
}

void draw() {
  
  text(displayString,0,0,640,480);
  
}

//send all of the keypresses we receive to the server
void keyPressed() {
  client.send("" + key);
}

