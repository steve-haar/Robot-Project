package com.robotProject.video.processors.template;

import org.opencv.core.*;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.imgproc.*;

import com.robotProject.video.events.template.TemplateFrameProcessedEvent;
import com.robotProject.video.listeners.template.ITemplateFrameProcessedListener;
import com.robotProject.video.processors.FrameProcessor;

import android.graphics.*;
import android.hardware.*;
import android.hardware.Camera;
import android.hardware.Camera.*;
import android.util.*;

@SuppressWarnings("unused")
public class TemplateFrameProcessor extends FrameProcessor {
	private static final String TAG = "TemplatePreviewProcessing";
	private long timeStamp = 0;
	private ITemplateFrameProcessedListener mListener;	
	private android.graphics.Rect mRect = null;
	private Object mRectLocker = new Object();
	private boolean mTracking = false;

	/* template localization variables */
	private Template Cmin , template , OldLocation, candidate0 = null,candidate1= null,candidate2 = null,candidate3 = null,candidate4=null ; 
	/* Scaling variables */
	private Template CminS1, CminS2, templateS1, templateS2;
   
	/* color space variables */
	private Mat mYuv;
    private Mat mRgba;
	
	/* general variables */
	private double r , min = Double.MAX_VALUE;
	private int i , index;

	/* two modes for the application (Select an object , or start tracking the selected object) */
    public static final int     VIEW_MODE_RGBA_TEMPLATE  = 0;
    public static final int     VIEW_MODE_RGBA_TRACKING  = 1;
	private int mViewMode;
	
	public TemplateFrameProcessor(ITemplateFrameProcessedListener listener) {
		this.mListener = listener;
	}
	
	@Override
	public void ready() {
		mViewMode = VIEW_MODE_RGBA_TEMPLATE;
		Template.screenWidth = this.mFrameSize.width;
		Template.screenHeight = this.mFrameSize.height;
		mYuv = new Mat(mFrameSize.height + mFrameSize.height / 2, mFrameSize.width, CvType.CV_8UC1);
    	mRgba = new Mat();
	}
	
	@Override
    public void onPreviewFrame(byte[] data, Camera camera) {
    	final int view_mode = mViewMode;
    	int stepsize; double NCCration;
        
    	mYuv.put(0, 0, data);
        
        // get the RGBA
        Imgproc.cvtColor(mYuv, mRgba, Imgproc.COLOR_YUV420sp2RGB, 4);
        
        
        // show the frame number on the screen
    	//Core.putText(mRgba, "FrameN: " + Integer.toString(frameNum) , new Point(5, 50), 3, 1, new Scalar(255, 0, 0, 255), 2);
    	
        if(view_mode == VIEW_MODE_RGBA_TEMPLATE)			// To select a template 
        {
        	mTracking = false;
        	template = new Template(mFrameSize.width / 2, mFrameSize.height / 2, Configuration.TEMPLATE_SIZE, Configuration.TEMPLATE_SIZE, mRgba);
        	OldLocation = new Template(template);
        	setRectangle(template.rect);
	    }
        else	// Start Tracking		
        {
        	stepsize = 3;
        	
        	while(stepsize > 0)
        	{

            	//  at the same location where the template was in the last frame.
            	candidate0 = new Template(OldLocation.rect.x, OldLocation.rect.y, OldLocation.rect.width, OldLocation.rect.height, mRgba);
            	// located right to the template by stepsize pixels. 
            	if(OldLocation.rect.x + stepsize < mFrameSize.width - OldLocation.rect.width)
            		candidate1 = new Template(OldLocation.rect.x + stepsize, OldLocation.rect.y, OldLocation.rect.width, OldLocation.rect.height, mRgba);
            	// located left to the template by stepsize pixels.
            	if(OldLocation.rect.x - stepsize >= 0)
            		candidate2 = new Template(OldLocation.rect.x - stepsize, OldLocation.rect.y, OldLocation.rect.width, OldLocation.rect.height, mRgba);
            	//located down to the template by stepsize pixels.
            	if(OldLocation.rect.y + stepsize < mFrameSize.height - OldLocation.rect.height)
            		candidate3 = new Template(OldLocation.rect.x , OldLocation.rect.y + stepsize , OldLocation.rect.width, OldLocation.rect.height, mRgba);
            	//located up to the template by stepsize pixels.
            	if(OldLocation.rect.y - stepsize >= 0)
            		candidate4 = new Template(OldLocation.rect.x , OldLocation.rect.y - stepsize , OldLocation.rect.width, OldLocation.rect.height, mRgba);
            	
            	
            	// Calculate the mean absolute difference values between the original template and the candidate regions
            	Template  [] candidates  = {candidate0,candidate1,candidate2,candidate3,candidate4};
            	for(min = Double.MAX_VALUE, index = 0, r = 0 , i =0; i < candidates.length; i++)
            	{
            		if(candidates[i] != null)
            		{
	            		r = ComputeMAD(candidates[i].templateimage , template.templateimage); 
	            		if(r < min)
	            		{
	            			min = r; index = i;
	            		}
            		}
            	}
            	
            	Cmin = new Template(candidates[index]);		// the one who minimizes the MAD value will be selected as the new location for the object
            	
            	
            	// if the selected candidate region is at the same location as the one in previous frame, then go back and check the closest neighbors 
            	if((Cmin.rect.x == OldLocation.rect.x) && (Cmin.rect.y == OldLocation.rect.y))	
            	{
            		if(stepsize > 0)
            			stepsize--;
            	}
            	else
            	{	// if else, change the object location using the selected candidate region's location. 

            		OldLocation = new Template(Cmin);
            	}
        	}
        	
        	// Calculate Normalized Cross Correlation NCC between Original template and selected candidate region (OldLocation) to be 
        	// used to determine the need of scaling, template adaptation and if object is occluded. 
        	NCCration = NCC(template , Cmin);
        	
        	// 0.8 is a threshold for scaling and adaptation 
        	if(NCCration >= 0.8)
        	{
        		mTracking = true;
        		// draw a green circle: the rect is on the template 
        		//Core.circle(mRgba, new Point(mFrameSize.width - 150, 50)  ,  10, new Scalar(0, 255, 0, 255) , -2);
        		/*
        		 * Object scale handling
        		 */
 
        		// amount of scaling the candidate region
        		int scaleH = (int)Math.round(Cmin.rect.width * 0.05);
        		int scaleV = (int) Math.round(Cmin.rect.height * 0.05);
            	
        		// scale the candidate region by +-5%
        		CminS1 = new Template(Cmin.rect.x - (scaleH/2) , Cmin.rect.y - (scaleV/2) , Cmin.rect.width + scaleH , Cmin.rect.height + scaleV, mRgba); // 5% scaling
        		CminS2 = new Template(Cmin.rect.x + (scaleH/2) , Cmin.rect.y + (scaleV/2) , Cmin.rect.width - scaleH , Cmin.rect.height - scaleV, mRgba); // 5% scaling
        		
        		// resize the original template to match rescaled candidate regions
        		Mat t1 = new Mat(),t2 = new Mat();
        		Imgproc.resize(template.templateimage, t1, CminS1.templateimage.size(),  0 , 0 , Imgproc.INTER_CUBIC);
        		Imgproc.resize(template.templateimage, t2, CminS2.templateimage.size(),  0 , 0 , Imgproc.INTER_AREA);
        		templateS1 = new Template(template, t1);
        		templateS2 = new Template(template , t2);
        		
        		// get the minimum MAD among all scaled and resized templates
        		Template ScaledCandidates [] = {Cmin , CminS1, CminS2};
        		Template ResizedTemplates [] = {template,templateS1, templateS2};
        		for(index = 0 , min = Double.MAX_VALUE, r = 0, i = 0 ; i < 3 ; i++)
        		{
        			r = ComputeMAD(ScaledCandidates[i].templateimage, ResizedTemplates[i].templateimage);
        			if(r < min)
        			{
        				min = r; index = i;
        			}
        		}
        		
        		// Updating the original template, Candidate region and the Object Location to be used in the next frame.  
        		template = new Template(ResizedTemplates[index]);
        		Cmin = new Template(ScaledCandidates[index]);        		

             	/*
             	 * Template Adaptation
             	 * Updating the original template due to the change in its appearance 
             	 * 90% of the original template and 10% of the selected candidate region
             	 */
        		//Core.putText(mRgba, "Template Adaptation" , new Point(5, 90), 3, 1, new Scalar(255, 0, 0, 255), 2);
        		for(int r = 0 ; r < template.templateimage.height() ; r++) {
        			for(int c = 0 ; c < template.templateimage.width()  ; c++) {
        				template.templateimage.put(r, c, (0.9 * template.templateimage.get(r, c)[0]) + (0.1 * Cmin.templateimage.get(r, c)[0]));
        			}
        		}
        	}
        	else
        	{
        		mTracking = false;
        		// draw a red circle: the rect isn't on the template
        		//Core.circle(mRgba, new Point(mFrameSize.width - 150, 50)  ,  10, new Scalar(255, 0, 0, 255) , -2);
        	}

        	
        	/*
        	 *  Calculating the threshold that determines the occlusion  
        	 */
        	
        	// to be done soon
        	
        	
        	
        	//Core.putText(mRgba, "Template Size(" + Integer.toString(template.templateimage.cols()) + "," + 
        	//Integer.toString(template.templateimage.rows()) + ")" , new Point(5, 130), 3, 1, new Scalar(255, 0, 0, 255), 2);
        	// draw the new location of the object
        	setRectangle(Cmin.rect);
        	candidate0 = null; candidate1 = null; candidate2 = null; candidate3 = null; candidate4 = null;
        	//MoveControlRobot(OldLocation, Cmin);
        }
        
        long newTimeStamp = System.currentTimeMillis();
		mCamera.addCallbackBuffer(data);
		mListener.onFrameProcessed(new TemplateFrameProcessedEvent(newTimeStamp - timeStamp, mRect, mTracking));
		timeStamp = newTimeStamp;
    }
    
	@Override
	public void destroy() {
        if (mYuv != null) {
            mYuv.release();
        }
        if (mRgba != null) {
            mRgba.release();
        }
        mYuv = null;
        mRgba = null;
    }
	
	public void setViewMode(int viewMode) {
		Log.i(TAG, "setViewMode("+viewMode+")");
		mViewMode = viewMode;
	}
    
    private void setRectangle(Rect r) {
    	mRect = new android.graphics.Rect(r.x, r.y, r.x + r.width, r.y + r.height);
    }
    
    /* compute the mean absolute differences between two mats */
    private double ComputeMAD(Mat t1 , Mat t2)
    {
    	Mat AD = new Mat();
    	Core.absdiff(t1, t2, AD);
    	return Core.mean(AD).val[0];
    }
    
    /* compute the normalized cross correlation between template and candidate region */
    private double NCC(Template T, Template C)
    {
    	// first calculate the mean intensity values of original T and candidate C
    	double NCCration;
    	double TMIV = Core.mean(T.templateimage).val[0];
    	double CMIV = Core.mean(C.templateimage).val[0];
    	
    	// then calculate Cross correlation
    	double TintensitySubmean = 0;
    	double CintensitySubmean = 0;
    	double productOfTintAndCint = 0;
    	
    	Size size = T.templateimage.size();
    	double rows = size.height;
    	double colums = size.width;
    	
    	for(int r =0 ; r < rows; r++) {
    		for(int c= 0 ; c < colums; c++)
    		{
    			TintensitySubmean = TintensitySubmean + Math.pow( T.templateimage.get(r, c)[0] - TMIV , 2);
    			CintensitySubmean = CintensitySubmean + Math.pow(C.templateimage.get(r, c)[0] - CMIV, 2);
    			productOfTintAndCint = productOfTintAndCint + ((T.templateimage.get(r, c)[0] - TMIV) * (C.templateimage.get(r, c)[0] - CMIV));
    		}
    	}
    	
    	// Now we have all the items for computing NCC
    	NCCration = (productOfTintAndCint / (Math.sqrt(TintensitySubmean/T.templateimage.total()) * Math.sqrt(CintensitySubmean/C.templateimage.total()))) / T.templateimage.total() ;
    	return NCCration;
    }
}
