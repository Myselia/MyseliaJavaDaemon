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
import com.mycelia.common.communication.units.TransmissionBuilder;
import com.mycelia.common.constants.opcode.ActionType;
import com.mycelia.common.constants.opcode.ComponentType;
import com.mycelia.common.constants.opcode.OpcodeAccessor;
import com.mycelia.common.constants.opcode.operations.LensOperation;
import com.mycelia.common.constants.opcode.operations.StemOperation;
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
		RUNNING = true;
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
				System.out.println("RUNNING");
				clientConnectionSocket = null;
				try {
					clientConnectionSocket = this.serverSocket.accept();
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!--------------------!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
				System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!ACCEPTING CONNECTION!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
				System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!--------------------!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");

				clientThread = new Thread(new ClientSession(clientConnectionSocket, getMailBox()));
				clientThread.start();
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
		MailBox<Transmission> mb;
		boolean RUNNING = true;
		boolean CONNECTED = true;
		boolean HANDSHAKEOK = false;
		
		String infoSet[];
		
		public ClientSession(Socket clientConnectionSocket, MailBox<?> mb) throws IOException {
			this.mb = (MailBox<Transmission>) mb;
			System.out.println("MB IS::::::::::::::::::::::::::::::::"+mb);
			connection = clientConnectionSocket;
			input = new BufferedReader(new InputStreamReader(clientConnectionSocket.getInputStream()));
			output = new PrintWriter(clientConnectionSocket.getOutputStream(), true);
		}
		
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
			while (RUNNING) {
				String inputToken = "";
				try {
					if (!HANDSHAKEOK) {
						// IP, TYPE, MAC, HASH
						System.out.println("WAITING FOR RESPONSE");
						while ((inputToken = input.readLine()) != null) {
							System.out.println("RECV: " + inputToken);
							infoSet = handleSetupPacket(inputToken);
							HANDSHAKEOK = true;
							System.out.println("HANDSHAKE OK!!!!! " + infoSet[0]);
							break;
						}
					} else {
						/*
						 * ONCE THE HANDSHAKE IS DONE, ALL COMMUNICATION HANDLING HAPPENS HERE
						 */
						String outputToken = "";
						if (!output.checkError()) {
							buildTestPacket();
							if (input.ready()) {
								if ((inputToken = input.readLine()) != null) {
									mb.putInOutQueue(jsonParser.fromJson(inputToken, Transmission.class));
									System.out.println("Received: " + mb.peekOutQueue());
								}
							}
							if (mb.getInQueueSize() > 0) {
								outputToken = jsonParser.toJson(mb.getFromInQueue());
								System.out.println("Sending: " + outputToken);
								output.println(outputToken);
							} 
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
		
		private void buildTestPacket() {
			TransmissionBuilder tb = new TransmissionBuilder();
			String from = OpcodeAccessor.make(ComponentType.STEM, ActionType.DATA, StemOperation.TEST);
			String to = OpcodeAccessor.make(ComponentType.LENS, ActionType.DATA, LensOperation.TEST);
			tb.newTransmission(from, to);
			tb.addAtom("someNumber", "int", Integer.toString(5));
			Transmission t = tb.getTransmission();
			mb.putInInQueue(t);
		}
		
	}
}
