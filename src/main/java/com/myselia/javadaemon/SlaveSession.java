package com.myselia.javadaemon;

import java.util.Iterator;

import com.google.gson.Gson;
import com.myselia.javacommon.communication.ComponentCommunicator;
import com.myselia.javacommon.communication.mail.Addressable;
import com.myselia.javacommon.communication.mail.MailBox;
import com.myselia.javacommon.communication.mail.MailService;
import com.myselia.javacommon.communication.units.Atom;
import com.myselia.javacommon.communication.units.Transmission;
import com.myselia.javacommon.topology.ComponentCertificate;
import com.myselia.javacommon.topology.MyseliaUUID;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class SlaveSession extends SimpleChannelInboundHandler<Transmission>
		implements Addressable {

	private static Gson jsonInterpreter = new Gson();
	private Channel clientChannel;
	private MailBox<Transmission> mailbox;
	private boolean handshakeComplete = false;
	private ComponentCertificate slaveCert;

	public SlaveSession(Channel clientChannel) {
		this.clientChannel = clientChannel;
		this.mailbox = new MailBox<Transmission>();
		MailService.registerAddressable(this);
		
		setupFutures(clientChannel);
	}
	
	@Override
	protected void messageReceived(ChannelHandlerContext ctx, Transmission msg)
			throws Exception {
		if (!handshakeComplete) {
			handleSetupPacket(msg);
		} else {
			printRecvMsg(msg);
			in(msg);	
		}
	}

	private void handleSetupPacket(Transmission s) {
		System.out.print("[HANDSHAKE BEGIN]");
		try {
			printRecvMsg(s);
			
			//Get the certificate
			extractCertificate(s);
			
			//Register into routing table
			MyseliaUUID slaveUUID = slaveCert.getUUID();
			MailService.routingTable.setNext(slaveUUID.toString(), slaveUUID.toString());
			
			handshakeComplete = true;
			
			//Report the routing table to the stem on creation
			in(ComponentCommunicator.routingTableUpdateTransmission());
		} catch (Exception e) {
			System.out.println("\t[SlaveSession Error] : Setup packet from component is malformed!");
			e.printStackTrace();
		}
		System.out.println("[HANDSHAKE COMPLETE]");
	}
	
	private void extractCertificate(Transmission s) {
		Iterator<Atom> it = s.get_atoms().iterator();
		Atom a = it.next();
		String atomValue = a.get_value();
		slaveCert = jsonInterpreter.fromJson(atomValue, ComponentCertificate.class);
	}
	
	public void setupFutures(Channel channel) {
		ChannelFuture closeFuture = channel.closeFuture();

		closeFuture.addListener(new ChannelFutureListener() {
			@Override
			public void operationComplete(ChannelFuture future) throws Exception {
				System.out.println("[Disconnected]");
				onRemove();
			}
		});
	}

	private void onRemove() {
		System.out.println("\t->{Removing " + slaveCert.getUUID() + " from routing table");
		// Remove the route from the routing table and alert your parent component
		MailService.routingTable.removeDestination(slaveCert.getUUID().toString());

		System.out.println("\t->{Notifying stem of routing table change}");

		System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
		System.out.println(MailService.routingTable);

		in(ComponentCommunicator.routingTableUpdateTransmission());
	}

	private void printRecvMsg(Transmission t) {
		System.out.println();
		System.out.println("[Message Received]");

		// Header
		System.out.println("\t->Header: ");
		System.out.println("\t\t->ID: " + t.get_header().get_id());
		System.out.println("\t\t->From: " + t.get_header().get_from());
		System.out.println("\t\t->To: " + t.get_header().get_to());
		// Atoms
		System.out.println("\t->Atoms:");
		Iterator<Atom> it = t.get_atoms().iterator();
		while (it.hasNext()) {
			Atom a = it.next();
			System.out.println("\t\t->Field: " + a.get_field());
			System.out.println("\t\t\t->Type: " + a.get_type());
			System.out.println("\t\t\t->Value: " + a.get_value());
		}
		System.out.println();
	}

	@Override
	public void in(Transmission trans) {
		mailbox.enqueueOut(trans);
		MailService.notify(this);
	}

	@Override
	public Transmission out() {
		return mailbox.dequeueOut();
	}

}
