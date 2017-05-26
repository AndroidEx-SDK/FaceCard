package util;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class ImageUtil {
	private static final String TAG = "yan";
	static public String initSavePath(){
		File dateDir = Environment.getExternalStorageDirectory();
		String path = dateDir.getAbsolutePath() + "/RectPhoto/";
		File folder = new File(path);
		if(!folder.exists()) 		{
			folder.mkdir();
		}
		return path;
	}
	//保存图片
	static  public void saveJpeg(Bitmap bm){

		long dataTake = System.currentTimeMillis();
		String jpegName = initSavePath() + dataTake +".jpg";
		Log.i(TAG, "路径 " + jpegName);

		//File jpegFile = new File(jpegName);
		try {
			FileOutputStream fout = new FileOutputStream(jpegName);
			BufferedOutputStream bos = new BufferedOutputStream(fout);

			//Bitmap newBM = bm.createScaledBitmap(bm, 600, 800, false);

			bm.compress(Bitmap.CompressFormat.JPEG, 100, bos);
			bos.flush();
			bos.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block

			e.printStackTrace();
		}
	}

	public static Bitmap rotateBitamp(Bitmap bmp, float degree) {
		// 获得Bitmap的高和宽
		int bmpWidth = bmp.getWidth();
		int bmpHeight = bmp.getHeight();
		// 产生resize后的Bitmap对象
		Matrix matrix = new Matrix();
		matrix.postRotate(degree);
		Bitmap resizeBmp = Bitmap.createBitmap(bmp, 0, 0, bmpWidth, bmpHeight, matrix, true);
		return resizeBmp;

	}
}
