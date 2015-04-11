package com.myselia.javadaemon;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import com.mycelia.common.communication.Addressable;
import com.mycelia.common.communication.structures.MailBox;
import com.mycelia.common.communication.units.Transmission;

public class DaemonServer implements Runnable, Addressable{
	
	DaemonBroadcaster broadcaster = new DaemonBroadcaster();
	private int portNumber = 42068;
	
	private MailBox<Transmission> mb = new MailBox<Transmission>();
	ServerSocket serverSocket = new ServerSocket(portNumber);
	
	boolean CONNECTED = false;
	Socket clientSocket;
	PrintWriter out;
	BufferedReader in;

	public DaemonServer() throws IOException{
		Thread broadcaster_thread = new Thread(broadcaster);
		broadcaster_thread.start();
		
		
	}

	@Override
	public void run() {
		try {
			while (!Thread.currentThread().isInterrupted()) {
				tick();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	public void tick(){
		if(!CONNECTED){
			seekOn();
			connect();
		} else {
			//read incoming stream from the buffered reader
			
			//put the transmission in the out-going mailbox
			
			//check the incoming mail-box
			
			//put any transmission in the print writer
		}
		
	}
	
	private void connect(){
		try{
			clientSocket = serverSocket.accept();
			out = new PrintWriter(clientSocket.getOutputStream(), true);
			in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
		} catch(Exception e) {
			System.err.println("Error accepting client connections");
		}
		
		seekOff();
	}
	
	private void seekOn(){
		broadcaster.on();
	}
	
	private void seekOff(){
		broadcaster.off();
	}

	@Override
	public MailBox<?> getMailBox() {
		return mb;
	}

}
