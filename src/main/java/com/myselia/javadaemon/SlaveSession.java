package com.myselia.javadaemon;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.Iterator;

import javax.management.MBeanAttributeInfo;

import com.myselia.javacommon.communication.codecs.StringToTransmissionDecoder;
import com.myselia.javacommon.communication.codecs.TransmissionToStringEncoder;
import com.myselia.javacommon.communication.mail.Addressable;
import com.myselia.javacommon.communication.mail.MailBox;
import com.myselia.javacommon.communication.mail.MailService;
import com.myselia.javacommon.communication.units.Atom;
import com.myselia.javacommon.communication.units.Transmission;

public class SlaveSession extends SimpleChannelInboundHandler<Transmission>
		implements Addressable {

	private Channel clientChannel;
	private MailBox<Transmission> mailbox;
	private boolean handshakeComplete = false;

	public SlaveSession(Channel clientChannel) {
		this.mailbox = new MailBox<Transmission>();
		MailService.registerAddressable(this);
		this.clientChannel = clientChannel;
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
			System.out.println("\t!!!!!!!!!!!!!!!RECV!!!!!!!!!!!!!!!!!!\n"
					+ "\t|----> " + s);
			printRecvMsg(s);

			handshakeComplete = true;
		} catch (Exception e) {
			System.out.println("\tSetup packet from component is malformed!");
			e.printStackTrace();
		}
		System.out.println("[HANDSHAKE COMPLETE]");
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
