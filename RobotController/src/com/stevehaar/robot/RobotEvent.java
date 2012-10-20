package com.stevehaar.robot;

import com.stevehaar.adkutils.UsbEvent;

public class RobotEvent extends UsbEvent {
	private static final long serialVersionUID = 1L;

	public RobotEvent(UsbEvent e) {
		super(e.getSource(), e.getValue());
	}
	
}
