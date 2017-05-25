package com.example.camera_face;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;

import java.io.FileOutputStream;
import java.io.IOException;

public class MainActivity extends Activity implements OnClickListener, PictureCallback {

    private CameraSurfacePreview mCameraSurPreview = null;
    private Button mCaptureButton = null;
    private String TAG = "zhang";
    private SurfaceHolder mHolder;
    private Camera mCamera;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
        mCameraSurPreview = new CameraSurfacePreview(this);
        preview.addView(mCameraSurPreview);

        mCaptureButton = (Button) findViewById(R.id.button_capture);
        mCaptureButton.setOnClickListener(this);
    }

    @Override
    public void onPictureTaken(byte[] data, Camera camera) {
        FileOutputStream outStream = null;
        try {
            outStream = new FileOutputStream(String.format("/sdcard/pic.jpg"));
            outStream.write(data);
            outStream.close();
            Log.i("onPictureTaken", "Picture is taken and saved.");
        } catch (Exception e) {
            Log.d(TAG, "Error starting camera preview:" + e.getMessage());
        }
        //restart the preview
        //camera.startPreview();
        //see if we need to enable or not
        //mCaptureButton.setEnabled(true);

        //this.mCameraSurPreview.releaseCamera();
//		finish();
//		Log.d(TAG, "camera is released");
        Intent intent = new Intent(MainActivity.this, FaceDetectActivity.class);
        startActivity(intent);
        Log.d(TAG, "go to the face detect activity");
        finish();
        Log.d(TAG, "camera is released");

    }

    @Override
    public void onClick(View v) {
        //mCaptureButton.setEnabled(false);
        //get an image from camera
        mCameraSurPreview.takePicture(this);
        Log.d(TAG, "mCameraSurPreview.takePicture() is called");

//		try {
//		Thread.sleep(3000);
//		}catch (Exception e) {
//			Log.d(TAG, "sleep");
//		}
//		
//		Intent intent = new Intent (MainActivity.this,FaceDetectActivity.class);
//		startActivity(intent);
//		Log.d(TAG, "go to the face detect activity");
//		finish();
//		Log.d(TAG, "camera is released");

    }

    @Override
    public void onStop() {
        super.onStop();
        releaseCamera();
    }

    public void releaseCamera() {
        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
        } else {
            return;
        }
    }

    public class CameraSurfacePreview extends SurfaceView implements SurfaceHolder.Callback {
//		private SurfaceHolder mHolder;
//		private Camera mCamera;

//		public void releaseCamera() {
//			if(mCamera!=null)	{
//				mCamera.release();
//				mCamera=null;
//			}
//			else {
//				return;
//			}
//		}

        public CameraSurfacePreview(Context context) {
            super(context);
            mHolder = getHolder();
            mHolder.addCallback(this);
            mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        }

        public void surfaceCreated(SurfaceHolder holder) {
            Log.d("zhang", "surfaceCreated()is called");
            try {//open camera in preview mode
                mCamera = Camera.open();
                mCamera.setDisplayOrientation(0);
                mCamera.setPreviewDisplay(holder);
            } catch (IOException e) {
                Log.d("zhang", "Error when setting camera preview:" + e.getMessage());
            }
        }

        public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
            Log.d(TAG, "surfaceChanged()is called");
            try {
                mCamera.startPreview();
            } catch (Exception e) {
                Log.d(TAG, "Error when starting camera preview:" + e.getMessage());
            }
        }

        public void surfaceDestroyed(SurfaceHolder holder) {
            if (mCamera != null) {
                mCamera.stopPreview();
                //mCamera.release();
                //mCamera = null;
            }
            Log.d(TAG, "surfaceDestroyed() is called");
        }

        public void takePicture(PictureCallback imageCallback) {
            mCamera.takePicture(null, null, imageCallback);
            Log.d(TAG, "take picture() is called");
        }
    }
}
