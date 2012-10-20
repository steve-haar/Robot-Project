package org.opencv.samples.tutorial1;



import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.util.Log;
import android.view.Display;
import android.*;

class Sample1View extends SampleViewBase {
	private static final String TAG = "Sample1View";
	public  int ScreenWidth;
	public  int ScreenHeight;
	private Template Cmin , template , ObjectLocation, candidate0 = null,candidate1= null,candidate2 = null,candidate3 = null,candidate4=null ; 
	private Template CminS1, CminS2, templateS1, templateS2; 
    private Mat mYuv;
    private Mat mRgba;
    private Mat mGraySubmat;
    private Mat mIntermediateMat;
	private Bitmap mBitmap;
	double r , min = Double.MAX_VALUE;
	int i , index;

    public static final int     VIEW_MODE_RGBA_TEMPLATE  = 0;
    public static final int     VIEW_MODE_RGBA_TRACKING  = 1;
	 private int mViewMode;
    public Sample1View(Context context , Display e) {
        super(context);
       Template.ScreenWidth =  ScreenWidth =  e.getWidth();
        Template.ScreenHeight =  ScreenHeight = e.getHeight();
        mViewMode = VIEW_MODE_RGBA_TEMPLATE;
        
    }

	@Override
	protected void onPreviewStarted(int previewWidth, int previewHeight) {
	    synchronized (this) {
        	// initialize Mats before usage
        	mYuv = new Mat(getFrameHeight() + getFrameHeight() / 2, getFrameWidth(), CvType.CV_8UC1);
        	mGraySubmat = mYuv.submat(0, getFrameHeight(), 0, getFrameWidth());
        	mRgba = new Mat();
        	mIntermediateMat = new Mat();
        	mBitmap = Bitmap.createBitmap(previewWidth, previewHeight, Bitmap.Config.ARGB_8888); 
    	    }
	}

	@Override
	protected void onPreviewStopped() {
		if(mBitmap != null) {
			mBitmap.recycle();
		}

		synchronized (this) {
            // Explicitly deallocate Mats
            if (mYuv != null)
                mYuv.release();
            if (mRgba != null)
                mRgba.release();
            if (mGraySubmat != null)
                mGraySubmat.release();
            if (mIntermediateMat != null)
                mIntermediateMat.release();

            mYuv = null;
            mRgba = null;
            mGraySubmat = null;
            mIntermediateMat = null;
        }
    }

    @Override
    protected Bitmap processFrame(byte[] data , int frameNum) {
        mYuv.put(0, 0, data);
        Bitmap bmp = mBitmap;
        int stepsize; double NCCration;
        // get the RGBA
        Imgproc.cvtColor(mYuv, mRgba, Imgproc.COLOR_YUV420sp2RGB, 4);
        final int view_mode = mViewMode;
    	Core.putText(mRgba, "frameNum: " + Integer.toString(frameNum) , new Point(10, 70), 3/* CV_FONT_HERSHEY_COMPLEX */, 1, new Scalar(255, 0, 0, 255), 2);
        if(view_mode == VIEW_MODE_RGBA_TEMPLATE)
        {
        	template = new Template(ScreenWidth/2,ScreenHeight/2, 50, 50, mRgba);
        	ObjectLocation = new Template(template);
	        drawRect(template.rect);
	        //template.templateimage.copyTo(mRgba.submat(template.rect));
	    }
        else
        {
        	
        	// all other frames except frame number 1
        	stepsize = 3;
        	
        	while(stepsize > 0)
        	{

            	//  at the same location where the template was in the last frame.
            	candidate0 = new Template(ObjectLocation.rect.x, ObjectLocation.rect.y, ObjectLocation.rect.width, ObjectLocation.rect.height, mRgba);
            	// located right to the template by stepsize pixels. 
            	if(ObjectLocation.rect.x + stepsize < ScreenWidth - ObjectLocation.rect.width)
            		candidate1 = new Template(ObjectLocation.rect.x + stepsize, ObjectLocation.rect.y, ObjectLocation.rect.width, ObjectLocation.rect.height, mRgba);
            	// located left to the template by stepsize pixels.
            	if(ObjectLocation.rect.x - stepsize >= 0)
            		candidate2 = new Template(ObjectLocation.rect.x - stepsize, ObjectLocation.rect.y, ObjectLocation.rect.width, ObjectLocation.rect.height, mRgba);
            	//located down to the template by stepsize pixels.
            	if(ObjectLocation.rect.y + stepsize < ScreenHeight - ObjectLocation.rect.height)
            		candidate3 = new Template(ObjectLocation.rect.x , ObjectLocation.rect.y + stepsize , ObjectLocation.rect.width, ObjectLocation.rect.height, mRgba);
            	//located up to the template by stepsize pixels.
            	if(ObjectLocation.rect.y - stepsize >= 0)
            		candidate4 = new Template(ObjectLocation.rect.x , ObjectLocation.rect.y - stepsize , ObjectLocation.rect.width, ObjectLocation.rect.height, mRgba);
            	
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
            	Cmin = new Template(candidates[index]);
            	
            	if((Cmin.rect.x == ObjectLocation.rect.x) && (Cmin.rect.y == ObjectLocation.rect.y))	// at the same location 
            	{
            		if(stepsize > 0)
            			stepsize--;
            	}
            	else
            	{	// at a different location

            		ObjectLocation = new Template(Cmin);
            	}
        	}
        	
        	// Calculate Normalized Cross Correlation NCC between Original template and selected candidate region (ObjectLocation)
        	NCCration = NCC(template , Cmin);
        	if(NCCration >= 0.8)
        	{
        		// Object scale handling
 /*
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
        		// get the minimum MAD among all scaled and resize templates
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
        		template = new Template(ResizedTemplates[index]);
        		Cmin = new Template(ScaledCandidates[index]);
        		ObjectLocation = new Template(Cmin);
        		
        		*/
             	// Template Adaptation
        		Core.putText(mRgba, "Template Adaptation" , new Point(10, 120), 3, 1, new Scalar(255, 0, 0, 255), 2);
        		for(int r = 0 ; r < template.templateimage.height() ; r++)
        			for(int c = 0 ; c < template.templateimage.width()  ; c++)
        			{
        				template.templateimage.put(r, c, (0.9 * template.templateimage.get(r, c)[0]) + (0.1 * Cmin.templateimage.get(r, c)[0]));
        			}
        		
        		
        		
        		
        		
        	}

        	// draw the new location of the object 
        	drawRect(Cmin.rect);
        
        	candidate0 = null; candidate1 = null; candidate2 = null; candidate3 = null; candidate4 = null;
        }
         
        try{
        	 Utils.matToBitmap(mRgba, bmp);
        }catch(Exception e) {
            Log.e("org.opencv.samples.tutorial1", "Utils.matToBitmap() throws an exception: " + e.getMessage());
            bmp.recycle();
            bmp = null;
        }
        
        return bmp;
    }
    
    private void drawRect(Rect r)
    {
    	Core.rectangle(mRgba, new Point(r.x, r.y) , new Point(r.x + r.width - 1 , r.y + r.height -1), new Scalar(255, 0, 0));
    }
    
    private double ComputeMAD(Mat t1 , Mat t2)
    {
    	Mat AD = new Mat();
    	Core.absdiff(t1, t2, AD);
    	return Core.mean(AD).val[0];
    }
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
    	
    	for(int r =0 ; r < rows; r++)
    		for(int c= 0 ; c < colums; c++)
    		{
    			TintensitySubmean = TintensitySubmean + Math.pow( T.templateimage.get(r, c)[0] - TMIV , 2);
    			CintensitySubmean = CintensitySubmean + Math.pow(C.templateimage.get(r, c)[0] - CMIV, 2);
    			productOfTintAndCint = productOfTintAndCint + ((T.templateimage.get(r, c)[0] - TMIV) * (C.templateimage.get(r, c)[0] - CMIV));
    		}
    	
    	
    	// Now we have all the items for computing NCC
    	
    	NCCration = (productOfTintAndCint / (Math.sqrt(TintensitySubmean/T.templateimage.total()) * Math.sqrt(CintensitySubmean/C.templateimage.total()))) / T.templateimage.total() ;
    	return NCCration;
    }
    public void setViewMode(int viewMode) {
		Log.i(TAG, "setViewMode("+viewMode+")");
		mViewMode = viewMode;
	}
}
