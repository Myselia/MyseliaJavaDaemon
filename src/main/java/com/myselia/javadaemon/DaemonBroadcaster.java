package com.myselia.javadaemon;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

import com.google.gson.Gson;
import com.myselia.javacommon.communication.units.Transmission;
import com.myselia.javacommon.communication.units.TransmissionBuilder;
import com.myselia.javacommon.constants.opcode.ActionType;
import com.myselia.javacommon.constants.opcode.ComponentType;
import com.myselia.javacommon.constants.opcode.OpcodeBroker;
import com.myselia.javacommon.constants.opcode.operations.DaemonOperation;
import com.myselia.javacommon.topology.ComponentCertificate;

public class DaemonBroadcaster implements Runnable {

	private boolean RUNNING = true;
	private boolean SEEKING = false;
	private int BROADCAST_SLEEP = 1500;
	private DatagramSocket socket = null;
	private Gson jsonInterpreter;
	public ComponentType type;

	public DaemonBroadcaster(int port, ComponentType type) {
		this.jsonInterpreter = new Gson();
		this.type = type;
		try {
			socket = new DatagramSocket(port);
			System.out.println("Created broadcaster \n" + "\t->Port: " + port + "\n\t->Socket at: " + socket);
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}

	public void on() {
		SEEKING = true;
	}

	public void off() {
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
					DatagramPacket networkPacket = new DatagramPacket(infoPacket, infoPacket.length, InetAddress.getByName("127.0.0.1"), DaemonServer.Slave_Listen_Port);
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
	 * Used to build the JSON that will be sent in the UDP seek packets that
	 * components will interpret internally.
	 * 
	 * @return A packet to be sent by the seeker accepting a particular type of
	 *         component connection
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
		ComponentCertificate daemonCert = DaemonServer.daemonCertificate;
		TransmissionBuilder tb = new TransmissionBuilder();
		String from = OpcodeBroker.make(ComponentType.DAEMON, daemonCert.getUUID(), ActionType.SETUP, DaemonOperation.BROADCAST);
		String to = OpcodeBroker.make(type, null, ActionType.SETUP, DaemonOperation.BROADCAST);
		tb.newTransmission(from, to);
		tb.addAtom("daemonCertificate", "componentCertificate", jsonInterpreter.toJson(daemonCert));
		tb.addAtom("port", "int", Integer.toString(DaemonServer.DaemonServer_INTERNAL_COMMUNICATE));
		tb.addAtom("type", "String", type.name());
		Transmission t = tb.getTransmission();

		return jsonInterpreter.toJson(t);
	}

}
