package com.myselia.javadaemon;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import com.google.gson.Gson;
import com.mycelia.common.communication.Addressable;
import com.mycelia.common.communication.structures.MailBox;
import com.mycelia.common.communication.units.Transmission;
import com.myselia.javadaemon.extraction.DaemonComponentInfoExtract;

public class DaemonServer implements Runnable, Addressable{
	
	public static int DaemonServer_BCAST = 42067;
	public static int DaemonServer_INTERNAL_COMMUNICATE = 42066;
	public static int Slave_Listen_Port = 42065;
	
	private boolean RUNNING = false;
	private DaemonBroadcaster bcastHandle;
	private ServerSocket serverSocket;
	private Socket clientConnectionSocket;
	private Thread clientThread;
	private Gson jsonParser;
	
	private MailBox<Transmission> mb = new MailBox<Transmission>();
	
	public DaemonServer(DaemonBroadcaster bcast) throws IOException{
		bcastHandle = bcast;
		jsonParser = new Gson();
		
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
	
	public void tick() {
		try {
			if (serverSocket == null) {
				openServerSocket(DaemonServer_INTERNAL_COMMUNICATE);
			}

			while (RUNNING) {
				clientConnectionSocket = null;
				try {
					clientConnectionSocket = this.serverSocket.accept();
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!--------------------!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
				System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!ACCEPTING CONNECTION!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
				System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!--------------------!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");

				clientThread = new Thread(new ClientSession(clientConnectionSocket));
				bcastHandle.off();
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private void openServerSocket(int port) {
		try {
			serverSocket = new ServerSocket(port);
			System.out.println("Created Server \n" 
					+ "\t->Port: " + port 
					+ "\n\t->Server Socket at: " + serverSocket);
		} catch (Exception e) {
			System.err.println("FATAL: Cannot open Stem Server port on " + port);
		}
	}

	@Override
	public MailBox<?> getMailBox() {
		return mb;
	}

	/*
	 * THINGS THAT ARE TO BE DONE WITH THE SANDBOX SLAVE ARE DONE HERE!
	 */
	class ClientSession implements Runnable {
		PrintWriter output;
		BufferedReader input;
		Socket connection;
		boolean RUNNING = true;
		boolean CONNECTED = true;
		boolean HANDSHAKEOK = false;
		
		String infoSet[];
		
		public ClientSession(Socket clientConnectionSocket) throws IOException {
			connection = clientConnectionSocket;
			input = new BufferedReader(new InputStreamReader(clientConnectionSocket.getInputStream()));
			output = new PrintWriter(clientConnectionSocket.getOutputStream(), true);
		}
		
		@Override
		public void run() {
			while (RUNNING) {
				if (CONNECTED) {
					String inputS = "";
					try {
						if (!HANDSHAKEOK) {
							// IP, TYPE, MAC, HASH
							while ((inputS = input.readLine()) != null) {
								System.out.println("RECV: " + inputS);
								infoSet = handleSetupPacket(inputS);
								HANDSHAKEOK = true;
								break;
							}
						} else {

							if (!output.checkError()) {
								/*
								 * PROCESSING GOES HERE!
								 */
								System.out.println("!PROCESSING CONNECTION WITH SANDBOX SLAVE!!!");
							} else {
								System.err.println("DEADSESSION!!!!!!!!!!!!!!!!!!!!");
								clientConnectionSocket.close();
								throw new IOException();
							}
						}

					} catch (IOException e) {
						System.err.println("Session at " + this.toString() + " has broken stream!");
						e.printStackTrace();
					}
				}
			}
		}
		
		public void killThread() {
			RUNNING = false;
			try {
				Thread.currentThread().join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		public void stopConnection() {
			CONNECTED = false;
		}
		
		public void startConnection() {
			CONNECTED = true;
		}
		
		private String[] handleSetupPacket(String s) {
			String[] infoSet = null;
			System.out.print("Setting up received packet...");
			try {
				Transmission setupTransmission = jsonParser.fromJson(s, Transmission.class);
				infoSet = DaemonComponentInfoExtract.createHandler(setupTransmission);
				
				for (String str: infoSet) {
					System.out.println("Found: " + str);
				}
			} catch (Exception e) {
				System.out.println("Setup packet from component is malformed!");
				e.printStackTrace();
			}
			System.out.println("....done");
			
			return infoSet;
		}
	}
}
