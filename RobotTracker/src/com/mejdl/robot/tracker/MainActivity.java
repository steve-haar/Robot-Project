package com.mejdl.robot.tracker;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;

public class MainActivity extends Activity {
    private static final String TAG = "Main::Activity";

    public static final int     VIEW_MODE_RGBA_TEMPLATE  = 0;
    public static final int     VIEW_MODE_RGBA_TRACKING  = 1;
    public static final int     VIEW_MODE_TEMPLATE_SIZE  = 2;
    private MenuItem            mItemPreviewRGBATEMPLATE;
    private MenuItem            mItemPreviewRGBATRACKING;
    private MenuItem            mItemChangeTemplateSize;

    public static int           viewMode        = VIEW_MODE_RGBA_TEMPLATE;
    private AppView         mView;

    private BaseLoaderCallback  mOpenCVCallBack = new BaseLoaderCallback(this) {
    	@SuppressWarnings("deprecation")
		@Override
    	public void onManagerConnected(int status) {
    		switch (status) {
				case LoaderCallbackInterface.SUCCESS:
				{
					Log.i(TAG, "OpenCV loaded successfully");
					// Create and set View
					Display display = getWindowManager().getDefaultDisplay();
					Point size = new Point();
					display.getSize(size);
					GlobalVar.ScreenHeight = size.x;
					GlobalVar.ScreenWidth = size.y;
					mView = new AppView(mAppContext);
					setContentView(mView);
					// Check native OpenCV camera
					if( !mView.openCamera() ) {
						AlertDialog ad = new AlertDialog.Builder(mAppContext).create();
						ad.setCancelable(false); // This blocks the 'BACK' button
						ad.setMessage("Fatal error: can't open camera!");
						ad.setButton("OK", new DialogInterface.OnClickListener() {
						    public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
							finish();
						    }
						});
						ad.show();
					}
				} break;
				default:
				{
					super.onManagerConnected(status);
				} break;
			}
    	}
	};
    
    public MainActivity() {
        Log.i(TAG, "Instantiated new " + this.getClass());
    }

    @Override
	protected void onPause() {
        Log.i(TAG, "onPause");
		super.onPause();
		if (null != mView)
			mView.releaseCamera();
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void onResume() {
        Log.i(TAG, "onResume");
		super.onResume();
		if( (null != mView) && !mView.openCamera() ) {
			AlertDialog ad = new AlertDialog.Builder(this).create();  
			ad.setCancelable(false); // This blocks the 'BACK' button  
			ad.setMessage("Fatal error: can't open camera!");  
			ad.setButton("OK", new DialogInterface.OnClickListener() {  
			    public void onClick(DialogInterface dialog, int which) {  
				dialog.dismiss();
				finish();
			    }  
			});  
			ad.show();
		}
	}

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        Log.i(TAG, "Trying to load OpenCV library");
        if (!OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_2, this, mOpenCVCallBack))
        {
        	Log.e(TAG, "Cannot connect to OpenCV Manager");
        }
        
        /* This code together with the one in onDestroy() 
         * will make the screen be always on until this Activity gets destroyed. */
       

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.i(TAG, "onCreateOptionsMenu");
        mItemChangeTemplateSize = menu.add("Change Template Size");
        mItemPreviewRGBATEMPLATE = menu.add("To Select Object");
        mItemPreviewRGBATRACKING = menu.add("To Strat Tracking");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.i(TAG, "Menu Item selected " + item);
        if (item == mItemPreviewRGBATEMPLATE)
            mView.setViewMode(AppView.VIEW_MODE_RGBA_TEMPLATE);
        else if (item == mItemPreviewRGBATRACKING)
        	mView.setViewMode(AppView.VIEW_MODE_RGBA_TRACKING);
        else
        	if(item == mItemChangeTemplateSize)
        	{
        		mView.setViewMode(AppView.VIEW_MODE_RGBA_TEMPLATE);
        		Intent intent = new Intent(this, UserInputActivity.class);
        		startActivity(intent);
        	}
        return true;
    }


}
