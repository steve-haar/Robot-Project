Robot-Project
=============

Southern Illinois University 2012 Robot Project

We have built a four wheeled robot with an Arduino microcontroller, specifically the Arduino Mega 2650. We have written Arduino (C++) and Android (Java) libraries to allow an Android device to control the robot through a USB connection. The robot is designed to track objects by spinning left and right to keep the object in sight and driving forward and backward to maintain a constant distance between the robot and the object. Images are acquired through the camera of an Android device which is attached to the robot. The camera is attached to servos on the robot which allow the camera to pan and tilt. Several image processing techniques are used to detect the location of the object being tracked in the images. 
Two different kernel based trackers are implemented as Android applications. One of them uses a color based tracking method and the other uses a template based tracking method. Both applications use Android's OpenCV library to help with the image processing. The experimental results of the robot using both methods show robust tracking of a variety of objects undergoing significant appearance changes, with a low computational complexity.
