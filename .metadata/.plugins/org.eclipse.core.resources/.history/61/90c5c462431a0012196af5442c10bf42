package com.stevehaar.robot;

import android.content.Context;

public class Robot {
	private Context context;
	private RobotBroadcastReceiver robotReceiver;
	private int CameraVertPosition = 90;
	private int CameraHorPosition = 90;
	private int MaxCameraVertPosition = 130;
	private int MaxCameraHorPosition = 180;
	private int MinCameraVertPosition = 50;
	private int MinCameraHorPosition = 0;
	
	public Robot () { }
	
	public Robot(Context context) {
		this.context = context;
		robotReceiver = new RobotBroadcastReceiver(this.context);
		robotReceiver.open();
	}
	
    public void resume() {
        robotReceiver.resume();
    }
    
    public void pause() {
        robotReceiver.suspend();
    }
    
    public void destroy() {
        robotReceiver.close();
    }
    
    public void addEventListener(RobotListener listener) {
    	robotReceiver.addUsbEventListener(listener);
    }
    
    public void removeEventListener(RobotListener listener) {
    	robotReceiver.removeUsbEventListener(listener);
    }
    
    public int getCameraVertPosition() {
    	return CameraVertPosition;
    }
    
    public int getCameraHorPosition() {
    	return CameraHorPosition;
    }
    
    public int getMaxCameraHorPosition() {
    	return MaxCameraHorPosition;
    }
    
    public int getMaxCameraVertPosition() {
    	return MaxCameraVertPosition;
    }
    
    public int getMinCameraHorPosition() {
    	return MinCameraHorPosition;
    }
    
    public int getMinCameraVertPosition() {
    	return MinCameraVertPosition;
    }
    
    public void MoveForward(int speed) {
    	robotReceiver.sendMessage(RobotCommand.CmdMoveForward, speed);
    }
    
    public void MoveForward(int speed, int centimeters) {
    	robotReceiver.sendMessage(RobotCommand.CmdMoveForwardDistance, speed, centimeters);
    }
    
    public void MoveBackward(int speed) {
    	robotReceiver.sendMessage(RobotCommand.CmdMoveBackward, speed);
    }
    
    public void MoveBackward(int speed, int centimeters) {
    	robotReceiver.sendMessage(RobotCommand.CmdMoveBackwardDistance, speed, centimeters);
    }    
    
    public void SpinLeft(int speed) {
    	robotReceiver.sendMessage(RobotCommand.CmdSpinLeft, speed);
    }
    
    public void SpinLeft(int speed, int degrees) {
    	robotReceiver.sendMessage(RobotCommand.CmdSpinLeftDistance, speed, degrees);
    }
    
    public void SpinRight(int speed) {
    	robotReceiver.sendMessage(RobotCommand.CmdSpinRight, speed);
    }
    
    public void SpinRight(int speed, int degrees) {
    	robotReceiver.sendMessage(RobotCommand.CmdSpinRightDistance, speed, degrees);
    }
    
    public void Stop() {
    	robotReceiver.sendMessage(RobotCommand.CmdStop);
    }
    
    public void MoveCameraVert(int degrees) {
    	robotReceiver.sendMessage(RobotCommand.CmdMoveForward, degrees);
    	CameraVertPosition = degrees;
    }
    
    public void MoveCameraHor(int degrees) {
    	robotReceiver.sendMessage(RobotCommand.CmdMoveForward, degrees);
    	CameraHorPosition = degrees;
    }

}
