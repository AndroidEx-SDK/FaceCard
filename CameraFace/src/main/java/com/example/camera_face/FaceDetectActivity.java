package com.example.camera_face;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.media.FaceDetector;
import android.media.FaceDetector.Face;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import java.io.File;

import util.ImageUtil;
import util.MyToast;

public class FaceDetectActivity extends Activity {
	static final String tag="zhang";
	ImageView imgView=null;

	FaceDetector faceDetector=null;
	FaceDetector.Face[] face;
	final int N_MAX=10;

	Button gopreview_button=null;

	ProgressBar progressBar=null;

	Bitmap srcImg=null;
	Bitmap detectFaceImage=null;

	Thread checkFaceThread = new Thread() {
		@Override
		public void run() {
			Bitmap faceBitmap = detectFace();
			mainHandler.sendEmptyMessage(2);
			Message m = new Message();
			m.what=0;
			m.obj=faceBitmap;
			mainHandler.sendMessage(m);
		}
	};

	Handler mainHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case 0:
					Bitmap b =(Bitmap)msg.obj;
					imgView.setImageBitmap(b);
					MyToast.showToast(getApplicationContext(),"Done Face Detection");
					int checkFace = faceDetector.findFaces(detectFaceImage, face);
					if (checkFace <1) {
						MyToast.showToast(getApplicationContext(),"No face detected!Try again! ");
					}
					break;
				case 1:
					showProcessBar();
					break;
				case 2:
					progressBar.setVisibility(View.GONE);
					gopreview_button.setClickable(true);
					break;
				default:
					break;
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//readPictureDegree("/sdcard/pic.jpg");
		setContentView(R.layout.activity_face_detect);
		initUI();
		initFaceDetect();
		mainHandler.sendEmptyMessage(1);
		checkFaceThread.start();

		gopreview_button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

				Intent intent = new Intent(FaceDetectActivity.this, MainActivity.class);
				finish();
				startActivity(intent);
			}
		});
	}

	@Override
	public void onStop() {
		super.onStop();
		File pic = new File("/sdcard/pic.jpg");
		if(pic.exists())
		{
			pic.delete();
		}
	}

	public Bitmap rotate(Bitmap ori,int degree){
		Matrix matrix = new Matrix();
		matrix.postRotate(degree);
		Bitmap rotateBitmap = Bitmap.createBitmap(ori, 0, 0,
				ori.getWidth(), ori.getHeight(), matrix, true);

		if(rotateBitmap != ori)
		{
			ori.recycle();
			ori = rotateBitmap;
		}

		return ori;
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	public void initUI() {
		gopreview_button=(Button)findViewById(R.id.gopreview_button);
		imgView=(ImageView)findViewById(R.id.imgview);
		LayoutParams params = imgView.getLayoutParams();
		DisplayMetrics dm= getResources().getDisplayMetrics();
		int w_screen=dm.widthPixels;

		//int degree = readPictureDegree("/sdcard/pic.jpg");
		srcImg=BitmapFactory.decodeFile("/sdcard/pic.jpg");
		srcImg=rotate(srcImg,0);
		int h=srcImg.getHeight();
		int w=srcImg.getWidth();
		float r=(float)h/(float)w;
		params.width=(int)(params.width*r);
		imgView.setLayoutParams(params);
		imgView.setImageBitmap(srcImg);
	}

	public void initFaceDetect(){
		this.detectFaceImage = srcImg.copy(Config.RGB_565, true);
		int w = detectFaceImage.getWidth();
		int h = detectFaceImage.getHeight();
		Log.i(tag, "width of image = " + w + "h = " + h);
		faceDetector = new FaceDetector(w, h, N_MAX);
		face = new FaceDetector.Face[N_MAX];
	}
	public boolean checkFace(Rect rect){
		int w = rect.width();
		int h = rect.height();
		int s = w*h;
		Log.i(tag, "width of face = " + w + "高h = " + h + "area of face = " + s);
		if(s < 10000){
			Log.i(tag, "Unusefal face, give up!");
			//MyToast.showToast(getApplicationContext(),"Face Detect Failure!");
			return false;
		} else{
			Log.i(tag, "Usefal face, keep it!");
			return true;
		}
	}
	public Bitmap detectFace(){
		//		Drawable d = getResources().getDrawable(R.drawable.face_2);
		//		Log.i(tag, "Drawable尺寸 w = " + d.getIntrinsicWidth() + "h = " + d.getIntrinsicHeight());
		//		BitmapDrawable bd = (BitmapDrawable)d;
		//		Bitmap detectFaceImage = bd.getBitmap();

		int nFace = faceDetector.findFaces(detectFaceImage, face);
		Log.i(tag, "detect number of faces：n = " + nFace);
		if (nFace<1) {
			Log.i(tag, "there is no face in the image");
			//MyToast.showToast(getApplicationContext(),"Failure");
		} else {
			for(int i=0; i<nFace; i++){
				Face f  = face[i];
				PointF midPoint = new PointF();
				float dis = f.eyesDistance();
				f.getMidPoint(midPoint);
				int dd = (int)(dis);
				Point eyeLeft = new Point((int)(midPoint.x - dis/2), (int)midPoint.y);
				Point eyeRight = new Point((int)(midPoint.x + dis/2), (int)midPoint.y);
				Rect faceRect = new Rect((int)(midPoint.x - dd), (int)(midPoint.y - dd), (int)(midPoint.x + dd), (int)(midPoint.y + dd));
				Log.i(tag, "x = " + eyeLeft.x + "y = " + eyeLeft.y);
				if(checkFace(faceRect)){
					Canvas canvas = new Canvas(detectFaceImage);
					Paint paint = new Paint();
					paint.setAntiAlias(true);
					paint.setStrokeWidth(20);
					paint.setStyle(Paint.Style.STROKE);
					paint.setColor(Color.RED);
					canvas.drawCircle(eyeLeft.x, eyeLeft.y, 20, paint);
					canvas.drawCircle(eyeRight.x, eyeRight.y, 20, paint);
					canvas.drawRect(faceRect, paint);
				}
			}
		}
		ImageUtil.saveJpeg(detectFaceImage);
		Log.i(tag, "done");

		return detectFaceImage;

	}
	public void showProcessBar(){
		RelativeLayout mainLayout = (RelativeLayout)findViewById(R.id.layout_main);
		progressBar = new ProgressBar(FaceDetectActivity.this, null, android.R.attr.progressBarStyleLargeInverse); //ViewGroup.LayoutParams.WRAP_CONTENT
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		params.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
		params.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
		progressBar.setVisibility(View.VISIBLE);
		//progressBar.setLayoutParams(params);
		mainLayout.addView(progressBar, params);

	}

}