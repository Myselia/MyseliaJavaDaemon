package com.myselia.javadaemon;

import java.io.IOException;
import java.net.ServerSocket;

import com.mycelia.common.communication.Addressable;
import com.mycelia.common.communication.structures.MailBox;
import com.mycelia.common.communication.units.Transmission;

public class DaemonServer implements Runnable, Addressable{
	
	private MailBox<Transmission> mb = new MailBox<Transmission>();

	public DaemonServer() throws IOException{
		super();
		
	}

	@Override
	public void run() {
		
	}
	
	public void tick(){
		
	}

	@Override
	public MailBox<?> getMailBox() {
		return mb;
	}

}
