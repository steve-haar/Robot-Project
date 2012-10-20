package org.opencv.samples.tutorial1;
import org.opencv.core.Rect;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;
public class Template {

	public static int ScreenWidth;
	public static int ScreenHeight;
	public Rect rect;
	public Mat templateimage;
	public double area; 

	public Template ()
	{}
	public Template (int x, int y, int w , int h , Mat image)
	{
		if(x >= 0 && y >= 0 && x + w < ScreenWidth && y + h < ScreenHeight)
		{
			rect = new Rect(x, y ,w , h);
			templateimage = new Mat(image, rect);
			area = w * h;
			Imgproc.cvtColor(templateimage, templateimage, Imgproc.COLOR_RGBA2GRAY, 1);	// to gray scale
		}
			  
	}
	
	public Template (Template temp)
	{
		rect = new Rect(temp.rect.x, temp.rect.y,temp.rect.width, temp.rect.height);
		templateimage = new Mat();
		temp.templateimage.copyTo(templateimage);
		area = temp.area;
	}
	
	public Template(Template temp , Mat image)
	{
		rect = new Rect(temp.rect.x, temp.rect.y,image.width(), image.height());
		templateimage = new Mat();
		image.copyTo(templateimage);
		
	}

}
