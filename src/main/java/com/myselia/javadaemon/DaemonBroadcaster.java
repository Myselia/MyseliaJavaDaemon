package com.myselia.javadaemon;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

import com.google.gson.Gson;
import com.mycelia.common.communication.units.Transmission;
import com.mycelia.common.communication.units.TransmissionBuilder;
import com.mycelia.common.constants.opcode.ActionType;
import com.mycelia.common.constants.opcode.ComponentType;
import com.mycelia.common.constants.opcode.OpcodeAccessor;
import com.mycelia.common.constants.opcode.operations.StemOperation;

public class DaemonBroadcaster implements Runnable{
	
	private boolean RUNNING = true;
	private boolean SEEKING = false;
	private int BROADCAST_SLEEP = 1500;
	private DatagramSocket socket = null;
	public ComponentType type;
	
	public DaemonBroadcaster(int port, ComponentType type) {
		this.type = type;
		try {
			socket = new DatagramSocket(port);
			System.out.println("Created broadcaster \n" 
					+ "\t->Port: " + port 
					+ "\n\t->Socket at: " + socket);
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}
	
	public void on(){
		SEEKING = true;
	}
	
	public void off(){
		SEEKING = false;
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
		while (RUNNING) {
			try {
				if (SEEKING) {
					byte[] infoPacket = buildInfoPacket();
					DatagramPacket networkPacket = new DatagramPacket(infoPacket, 
							infoPacket.length, InetAddress.getByName("127.0.0.1"), 
							DaemonServer.Slave_Listen_Port);
					System.out.println("Sending: " + networkPacket.toString());
					socket.send(networkPacket);
					
					Thread.sleep(BROADCAST_SLEEP);
				}
			} catch (IOException e) {
				System.err.println("Failed to broadcast on socket: " + socket);
				e.printStackTrace();
			} catch (InterruptedException e) {
				System.err.println("Failed to sleep on socket: " + socket);
				e.printStackTrace();
			}

		}
	}

	/**
	 * Used to build the JSON that will be sent in the UDP seek packets that components
	 * will interpret internally.
	 * @return A packet to be sent by the seeker accepting a particular type of component connection
	 */
	private byte[] buildInfoPacket() {
		String seekPacketString = null;

		switch (type) {
		case DAEMON:
			break;
		case LENS:
			break;
		case SANDBOXMASTER:
			break;
		case DATABASE:
			break;
		case SANDBOXSLAVE:
			seekPacketString = seekPacket(ComponentType.SANDBOXSLAVE);
			break;
		case STEM:
			break;
		default:
			break;
		}
		
		return seekPacketString.getBytes();
	}
	
	private String seekPacket(ComponentType type) {
		TransmissionBuilder tb = new TransmissionBuilder();
		Gson g = new Gson();
		String from = OpcodeAccessor.make(ComponentType.DAEMON, ActionType.SETUP, StemOperation.BROADCAST);
		String to = OpcodeAccessor.make(type, ActionType.SETUP, StemOperation.BROADCAST);
		tb.newTransmission(from, to);
		tb.addAtom("ip", "String", "127.0.0.1");
		tb.addAtom("port", "int", Integer.toString(DaemonServer.DaemonServer_INTERNAL_COMMUNICATE));
		tb.addAtom("type", "String", type.toString());
		Transmission t = tb.getTransmission();

		return g.toJson(t);
	}

}
