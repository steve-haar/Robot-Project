package com.stevehaar.robot;

import com.stevehaar.adkutils.UsbEvent;
import com.stevehaar.adkutils.UsbListener;

public abstract class RobotListener extends UsbListener {
	public RobotListener(RobotEventType eventToListenFor) {
		super(eventToListenFor.id);
	}

	public RobotListener(RobotEventType[] eventsToListenFor) {
		byte[] signals = new byte[eventsToListenFor.length];
		for (int i = 0; i < eventsToListenFor.length; i++) {
			signals[i] = eventsToListenFor[i].id;
		}
		this.setSignalsToListenFor(signals);
	}
	
	@Override
	public void usbSignalReceived(UsbEvent e) {
		robotSignalReceived(new RobotEvent(e));		
	}
	
	public abstract void robotSignalReceived(RobotEvent e);
}
