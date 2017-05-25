package com.example.asm;

import org.opencv.android.Utils;
import org.opencv.core.Mat;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.ImageView;

public class FaceDetectActivity extends Activity implements PreviewCallback,
		SurfaceHolder.Callback {
	private final String TAG = "com.example.asm.FaceDetectActivity";

	private Camera mCamera;
	private ImageView face_detect_view;
	private SurfaceView preview;
	private SurfaceHolder mHolder;

	private Size cameraPreviewSize;
	private ImageUtils imageUtils;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.face_detect_layout);
		face_detect_view = (ImageView) findViewById(R.id.face_view_face_detect_activity);
		preview = (SurfaceView) findViewById(R.id.face_detect_preview);
		mHolder = preview.getHolder();
		mHolder.addCallback(this);
		imageUtils = new ImageUtils(this);
	}

	@Override
	protected void onResume() {
		super.onResume();

		mCamera = CameraUtils.getCameraInstance(this,
				Camera.CameraInfo.CAMERA_FACING_BACK);
	}

	@Override
	protected void onPause() {
		super.onPause();
		Log.d(TAG, "on pause");

		if (mCamera != null) {
			try {
				mCamera.setPreviewCallback(null);
				mCamera.setPreviewDisplay(null);
			} catch (Exception e2) {
				e2.printStackTrace();
			} finally {
				mCamera.stopPreview();
				mCamera.release();
				mCamera = null;
			}
		}
	}

	@Override
	protected void onStop() {
		super.onPause();
		Log.d(TAG, "on stop");
	}

	private int getScreenWidth() {
		if (Integer.valueOf(android.os.Build.VERSION.SDK_INT) < 13) {
			Display display = getWindowManager().getDefaultDisplay();
			int width = display.getWidth();
			return width;

		} else {
			Display display = getWindowManager().getDefaultDisplay();
			Point size = new Point();
			display.getSize(size);
			int width = size.x;
			return width;
		}
	}

	@Override
	public void onPreviewFrame(byte[] data, Camera camera) {
		// TODO Auto-generated method stub
		Log.d(TAG, "onPreviewFrame");

		Bitmap bitmap = ImageUtils.yuv2bitmap(data, cameraPreviewSize.width,
				cameraPreviewSize.height);
		Mat src = new Mat();
		Utils.bitmapToMat(bitmap, src);

		// do face detection
		Mat face = new Mat();
		Mat detected = imageUtils.detectFacesAndExtractFace(src, face);
		Bitmap detected_bitmap = ImageUtils.mat2Bitmap(detected);
		face_detect_view.setImageBitmap(detected_bitmap);
	}

	@Override
	public void surfaceChanged(SurfaceHolder mHolder, int format, int w, int h) {
		// TODO Auto-generated method stub
		// If your preview can change or rotate, take care of those events here.
		// Make sure to stop the preview before resizing or reformatting it.
		Log.i(TAG, "SurfaceView Changed!");
		if (mHolder.getSurface() == null) {
			// preview surface does not exist
			return;
		}

		// stop preview before making changes
		try {
			mCamera.stopPreview();
		} catch (Exception e) {
			// ignore: tried to stop a non-existent preview
			Log.d(TAG, "Error stop camera preview: " + e.getMessage());
		}

		// set preview size and make any resize, rotate or
		// reformatting changes here
		int screenWidth = getScreenWidth();
		CameraUtils.setOptimalCameraPreviewSize(mCamera, screenWidth, screenWidth);
		cameraPreviewSize = mCamera.getParameters().getPreviewSize();

		// start preview with new settings
		try {
			mCamera.setPreviewDisplay(mHolder);
			mCamera.setDisplayOrientation(Params.CameraPreview.PREVIEW_DEGREE);
			// set preview callback
			mCamera.setPreviewCallback(this);
			mCamera.startPreview();

		} catch (Exception e) {
			Log.d(TAG, "Error starting camera preview: " + e.getMessage());
			if (mCamera != null) {
				try {
					mCamera.setPreviewDisplay(null);
				} catch (Exception e2) {
					e2.printStackTrace();
				}
				mCamera.setPreviewCallback(null);
				mCamera.stopPreview();
				mCamera.release();
				mCamera = null;
			}
		}
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		// TODO Auto-generated method stub
		Log.d(TAG, "SurfaceView Created!");
		try {
			mCamera.setPreviewDisplay(holder);
			mCamera.setDisplayOrientation(Params.CameraPreview.PREVIEW_DEGREE);
			mCamera.startPreview();
		} catch (Exception e) {
			Log.d(TAG, "Error setting camera preview: " + e.getMessage());
			if (mCamera != null) {
				mCamera.setPreviewCallback(null);
				mCamera.stopPreview();
				mCamera.release();
				mCamera = null;
			}
		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		// TODO Auto-generated method stub
		Log.d(TAG, "SurfaceView Destroy!");
	}
}
