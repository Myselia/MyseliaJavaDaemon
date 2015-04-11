package com.myselia.javadaemon;

import com.mycelia.common.communication.ComponentCommunicator;
import com.mycelia.common.communication.MailService;
import com.mycelia.common.communication.distributors.DistributorType;
import com.mycelia.common.constants.opcode.ComponentType;

public class Main {
	
	public static MailService ms = new MailService(DistributorType.FORWARDER, ComponentType.DAEMON);
	public static ComponentCommunicator cc = new ComponentCommunicator(ComponentType.DAEMON);
	public static DaemonServer ds;
	public static DaemonBroadcaster bcast;

	public static void main(String[] args) {
		try{
			bcast = new DaemonBroadcaster(DaemonServer.DaemonServer_BCAST, ComponentType.SANDBOXSLAVE);
			ds = new DaemonServer(bcast);
		}catch (Exception e){
			System.err.println("Daemon Server initialization error");
		}
		
		MailService.registerAddressable(cc);
		MailService.registerAddressable(ds);
		
		Thread ms_thread = new Thread(ms);
		ms_thread.start();
		
		Thread cc_thread = new Thread(cc);
		cc_thread.start();
		
		Thread bcast_thread = new Thread(bcast);
		bcast_thread.start();
		bcast.on();
		
		Thread ds_thread = new Thread(ds);
		ds_thread.start();
		
		
	}
}
