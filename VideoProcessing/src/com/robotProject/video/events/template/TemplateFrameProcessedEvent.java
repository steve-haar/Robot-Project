package com.robotProject.video.events.template;

import android.graphics.Rect;

public class TemplateFrameProcessedEvent {
	public long time;
	public Rect rect;
	public boolean tracking;
	
	public TemplateFrameProcessedEvent(long time, Rect rect, boolean tracking) {
		this.time = time;
		this.rect = rect;
		this.tracking = tracking;
	}

}
