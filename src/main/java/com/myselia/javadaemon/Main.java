package com.myselia.javadaemon;

import com.myselia.javacommon.constants.opcode.ComponentType;

public class Main {

	public static DaemonServer ds;
	public static DaemonBroadcaster bcast;

	public static void main(String[] args) {

		try {
			bcast = new DaemonBroadcaster(DaemonServer.DaemonServer_BCAST,
					ComponentType.SANDBOXSLAVE);
			ds = new DaemonServer(bcast);
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("Daemon Server initialization error");
		}

	}
}
