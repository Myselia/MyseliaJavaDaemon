package com.myselia.javadaemon;

import java.io.IOException;

import com.google.gson.Gson;
import com.myselia.javacommon.communication.mail.Addressable;
import com.myselia.javacommon.communication.mail.MailBox;
import com.myselia.javacommon.communication.mail.MailService;
import com.myselia.javacommon.communication.units.Transmission;
import com.myselia.javacommon.constants.opcode.ComponentType;
import com.myselia.javacommon.topology.ComponentCertificate;

public class DaemonServer {

	public static int DaemonServer_BCAST = 42067;
	public static int DaemonServer_INTERNAL_COMMUNICATE = 42066;
	public static int Slave_Listen_Port = 42065;

	public static MailService ms = new MailService(ComponentType.DAEMON);
	private static Gson jsonParser = new Gson();

	public static ComponentCertificate daemonCertificate;
	private MailBox<Transmission> mb = new MailBox<Transmission>();
	private DaemonBroadcaster bcast;
	private StemEndpoint stemConnector;
	private SlaveEndpoint slaveConnector;


	public DaemonServer(DaemonBroadcaster bcast) throws IOException {
		//Start the stem endpoint (client)
		this.stemConnector = new StemEndpoint(this);
		
		daemonCertificate = this.stemConnector.getComponentCertificate();
		
		//Start the slave endpoint (server)
		this.slaveConnector = new SlaveEndpoint(DaemonServer_INTERNAL_COMMUNICATE);
		
		//Register the addressable
		/* MailService.registerAddressable(this); */

		// Start Mail Service
		Thread ms_thread = new Thread(ms);
		ms_thread.start();

		// Start Component Broadcasting Module
		this.bcast = bcast;
		Thread bcast_thread = new Thread(this.bcast);
		bcast_thread.start();
		this.bcast.on();
	}


	public ComponentCertificate getDaemonCertificate() {
		return daemonCertificate;
	}

}
