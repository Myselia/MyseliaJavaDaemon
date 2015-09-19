package com.myselia.javadaemon;

import com.google.gson.Gson;
import com.myselia.javacommon.communication.ComponentCommunicator;
import com.myselia.javacommon.communication.mail.Addressable;
import com.myselia.javacommon.communication.mail.MailBox;
import com.myselia.javacommon.communication.mail.MailService;
import com.myselia.javacommon.communication.units.Transmission;
import com.myselia.javacommon.communication.units.TransmissionBuilder;
import com.myselia.javacommon.constants.opcode.ActionType;
import com.myselia.javacommon.constants.opcode.ComponentType;
import com.myselia.javacommon.constants.opcode.OpcodeBroker;
import com.myselia.javacommon.constants.opcode.operations.DaemonOperation;
import com.myselia.javacommon.constants.opcode.operations.StemOperation;
import com.myselia.javacommon.topology.ComponentCertificate;

public class StemEndpoint implements Addressable {

	public static ComponentCommunicator cc;
	
	private static Gson jsonInterpreter = new Gson();
	
	private MailBox<Transmission> mb = new MailBox<Transmission>();
	private DaemonServer s;
	
	/**
	 * Handles communication with a stem by being its client 
	 */
	public StemEndpoint(DaemonServer s) {
		this.s = s;
		
		cc = new ComponentCommunicator(ComponentType.DAEMON);
		this.mb = cc.getMailBox();
		
		cc.start();
		
		//Register for relevant opcodes
		MailService.registerAddressable(this);
		MailService.register("DATA_RESULTCONTAINER", this);
		MailService.register("CONFIG_TABLEBROADCAST", this);
	}
	
	public ComponentCertificate getComponentCertificate() {
		return cc.getComponentCertificate();
	}

	@Override
	public void in(Transmission trans) {
		cc.in(trans);
	}

	@Override
	public Transmission out() {
		return mb.dequeueOut();
	}

}
