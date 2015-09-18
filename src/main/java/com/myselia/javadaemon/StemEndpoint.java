package com.myselia.javadaemon;

import com.myselia.javacommon.communication.ComponentCommunicator;
import com.myselia.javacommon.communication.mail.Addressable;
import com.myselia.javacommon.communication.mail.MailBox;
import com.myselia.javacommon.communication.mail.MailService;
import com.myselia.javacommon.communication.units.Transmission;
import com.myselia.javacommon.constants.opcode.ComponentType;
import com.myselia.javacommon.topology.ComponentCertificate;

public class StemEndpoint implements Addressable {

	public static ComponentCommunicator cc;
	private MailBox<Transmission> mb = new MailBox<Transmission>();
	private DaemonServer s;
	
	/**
	 * Handles communication with a stem by being its client 
	 */
	public StemEndpoint(DaemonServer s) {
		this.s = s;
		this.cc = new ComponentCommunicator(ComponentType.DAEMON);
		this.mb = cc.getMailBox();
		cc.start();
		
		MailService.registerAddressable(this);
		MailService.register("DATA_RESULTCONTAINER", this);
		MailService.register("CONFIG_TABLEBROADCAST", this);
	}
	
	public ComponentCertificate getComponentCertificate() {
		return cc.getComponentCertificate();
	}

	@Override
	public void in(Transmission trans) {
		System.out.println("GOT FOR STEM: " + trans);
		cc.in(trans);
	}

	@Override
	public Transmission out() {
		return mb.dequeueOut();
	}

}
