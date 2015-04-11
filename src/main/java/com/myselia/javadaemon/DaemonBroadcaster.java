package com.myselia.javadaemon;

public class DaemonBroadcaster implements Runnable{
	
	public DaemonBroadcaster(){
		
	}
	
	public void on(){
		
	}
	
	public void off(){
		
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
	
	public void tick(){
		
	}

}
