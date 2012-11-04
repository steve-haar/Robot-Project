package com.robotProject.adkutils;

import java.util.*;

public class UsbEvent extends EventObject {
	private static final long serialVersionUID = -2033802394247439771L;
	private int value;
	public int getValue(){
		return this.value;
	}
	
	public UsbEvent(Object source, int value) {
		super(source);
		this.value = value;
	}	
}
