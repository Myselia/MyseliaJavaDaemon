package com.myselia.javadaemon;


public class SlaveEndpoint {

	private int port;
	
	/**
	 * Handles communication with a slave by being its daemon (server)
	 */
	public SlaveEndpoint(int port) {
		System.out.println("Starting Daemon Server with Port: " + port);
		this.port = port;
		
		DaemonServerBootstrapper bootstrap = new DaemonServerBootstrapper(this.port);
	}
	
}
