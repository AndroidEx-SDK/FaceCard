package com.androidex.face;

import android.Manifest;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.androidex.face.db.FaceDao;
import com.androidex.face.db.UserInfo;
import com.androidex.face.idcard.util.IdCardUtil;
import com.kongqw.interfaces.OnFaceDetectorListener;
import com.kongqw.interfaces.OnOpenCVInitListener;
import com.kongqw.util.FaceUtil;
import com.kongqw.view.CameraFaceDetectionView;
import com.synjones.idcard.IDCard;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements OnFaceDetectorListener ,IdCardUtil.BitmapCallBack{

    private static final String TAG = "MainActivity";
    private static final String FACE1 = "face1";
    private static final String FACE2 = "face2";
    private static final int MINCMP = 80;
    private static boolean isGettingFace = true;
    private static boolean isSaveFace = false;
    private Bitmap mBitmapFace1;
    private Bitmap mBitmapFace2;
    private Bitmap mBitmapFace3;
    private ImageView mImageViewFace1;
    private ImageView mImageViewFace2;
    private TextView mCmpPic;
    private double cmp ;
    private double cmp1 = 0.0;
    private double cmpTemp;
    private CameraFaceDetectionView mCameraFaceDetectionView;
    private PermissionsManager mPermissionsManager;

    private IdCardUtil mIdCardUtil;

    public  Mat matFace ;
    public Mat matFace1;

    public FaceDao faceDao;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        faceDao = FaceDao.getInstance(this);
        // 检测人脸的View
        mCameraFaceDetectionView = (CameraFaceDetectionView) findViewById(R.id.cameraFaceDetectionView);
        if (mCameraFaceDetectionView != null) {
            mCameraFaceDetectionView.setOnFaceDetectorListener(this);
            mCameraFaceDetectionView.setOnOpenCVInitListener(new OnOpenCVInitListener() {
                @Override
                public void onLoadSuccess() {
                    Log.i(TAG, "onLoadSuccess: ");
                }

                @Override
                public void onLoadFail() {
                    Log.i(TAG, "onLoadFail: ");
                }

                @Override
                public void onMarketError() {
                    Log.i(TAG, "onMarketError: ");
                }

                @Override
                public void onInstallCanceled() {
                    Log.i(TAG, "onInstallCanceled: ");
                }

                @Override
                public void onIncompatibleManagerVersion() {
                    Log.i(TAG, "onIncompatibleManagerVersion: ");
                }

                @Override
                public void onOtherError() {
                    Log.i(TAG, "onOtherError: ");
                }
            });
            mCameraFaceDetectionView.loadOpenCV(getApplicationContext());
        }
        // 显示的View
        mImageViewFace1 = (ImageView) findViewById(R.id.face1);
        mImageViewFace2 = (ImageView) findViewById(R.id.face2);
        mCmpPic = (TextView) findViewById(R.id.text_view);
        Button bn_get_face = (Button) findViewById(R.id.bn_get_face);
        // 存入人脸信息
        bn_get_face.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isSaveFace = true;
            }
        });
        Button switch_camera = (Button) findViewById(R.id.switch_camera);
        // 切换摄像头（如果有多个）
        switch_camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 切换摄像头
                boolean isSwitched = mCameraFaceDetectionView.switchCamera();
                Toast.makeText(getApplicationContext(), isSwitched ? "摄像头切换成功" : "摄像头切换失败", Toast.LENGTH_SHORT).show();
            }
        });

        // 动态权限检查器
        mPermissionsManager = new PermissionsManager(this) {
            @Override
            public void authorized(int requestCode) {
                Toast.makeText(getApplicationContext(), "权限通过！", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void noAuthorization(int requestCode, String[] lacksPermissions) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("提示");
                builder.setMessage("缺少相机权限！");
                builder.setPositiveButton("设置权限", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        PermissionsManager.startAppSettings(getApplicationContext());
                    }
                });
                builder.create().show();
            }

            @Override
            public void ignore() {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("提示");
                builder.setMessage("Android 6.0 以下系统不做权限的动态检查\n如果运行异常\n请优先检查是否安装了 OpenCV Manager\n并且打开了 CAMERA 权限");
                builder.setPositiveButton("确认", null);
                builder.setNeutralButton("设置权限", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        PermissionsManager.startAppSettings(getApplicationContext());
                    }
                });
                builder.create().show();
            }
        };
    }


    private IDCard idCard;
    @Override
    protected void onResume() {
        super.onResume();
        // 要校验的权限
        String[] PERMISSIONS = new String[]{Manifest.permission.CAMERA};
        // 检查权限
        mPermissionsManager.checkPermissions(0, PERMISSIONS);
        //打开阅读器
        if(mIdCardUtil==null){
            mIdCardUtil = new IdCardUtil(this,this);
        }
        mIdCardUtil.openIdCard();
        mIdCardUtil.readIdCard();

    }

    /**
     * 设置应用权限
     *
     * @param view view
     */
    public void setPermissions(View view) {
       PermissionsManager.startAppSettings(getApplicationContext());
    }

    /**
     * 检测到人脸
     *
     * @param mat  Mat
     * @param rect Rect
     */
    @Override
    public void onFace(final Mat mat, final Rect rect) {
        //是否录入人脸判断
        Mat m = FaceUtil.grayChange(mat,rect);
        if (isSaveFace){
            isSaveFace = false;
            //和文件里面已经存入的人脸做对比，有相同的则不存储，
            //查询数据库
            ArrayList<UserInfo> userInfoArrayList = new ArrayList<UserInfo>();
            userInfoArrayList = faceDao.getUserinfo();
            if (userInfoArrayList!=null&&userInfoArrayList.size()>0){
                for (int i= 0;i<userInfoArrayList.size();i++){
                    UserInfo users = new UserInfo();
                    users = userInfoArrayList.get(i);
                    Bitmap bitmap = BitmapFactory.decodeFile(users.getFacepath());
                    if (bitmap!=null){
                        Mat ma = new Mat();
                        Mat ma1 = new Mat();
                        Utils.bitmapToMat(bitmap,ma);
                        Imgproc.cvtColor(ma,ma1,Imgproc.COLOR_BGR2GRAY);
                        cmp1 = FaceUtil.comPareHist(m,ma1);
                        Log.d(TAG, "onFace: cmp1="+cmp1);
                        if (cmp1>MINCMP){//有相同的，不存入
                            Log.d(TAG, "onFace: 已经存入过");
                            return;
                        }
                    }
                }
            }
            if (cmp1<MINCMP){
                Log.d(TAG, "onFace: 没有存入过");
                String time = System.currentTimeMillis()+"";
                //存入数据
                FaceUtil.saveImage(this,mat,rect,time);
                faceDao.insertUserinfo(null,getApplicationContext().getFilesDir().getPath() + time + ".jpg");
            }
        }
        if (isGettingFace){
            mBitmapFace1 = null;
            cmp = 0.0;
            cmpTemp = 0.0;
            // 保存人脸信息并显示
           // FaceUtil.saveImage(this, mat, rect, FACE1);
           // mBitmapFace1 = FaceUtil.getImage(this, FACE1);
            //Utils.matToBitmap(mat,mBitmapFace1);
            //计算相似度
            if (idCard!=null){//读取到身份证照片才做对比
                //Mat mat11 = new Mat();
                Mat mat2 = new Mat();
                Mat mat22 = new Mat();
                Utils.bitmapToMat(mBitmapFace3,mat2);
                //Imgproc.cvtColor(mat,mat11,Imgproc.COLOR_BGR2GRAY);
                Imgproc.cvtColor(mat2,mat22, Imgproc.COLOR_BGR2GRAY);
                //Mat mat1 = FaceUtil.extractSIFT(m);
               // Mat mat11 = FaceUtil.extractSIFT(mat22);
                cmp = FaceUtil.comPareHist(m,mat22);
                Log.d(TAG, "onFace: cmp="+cmp);
            }else{
                
            }
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (null == mat) {
                    mImageViewFace1.setImageResource(R.mipmap.ic_contact_picture);
                } else {
                   //当cmp相似度大于数值时，设置图片
                    if (cmp>MINCMP){
                        cmpTemp = cmp;
                        FaceUtil.saveImage(MainActivity.this, mat, rect, FACE1);
                        mBitmapFace1 = FaceUtil.getImage(MainActivity.this, FACE1);
                        mImageViewFace1.setImageBitmap(mBitmapFace1);
                        isGettingFace = false;
                        cmp =0.0;
                    }else{
                        if (mBitmapFace1!=null){
                            mImageViewFace1.setImageBitmap(mBitmapFace1);
                        }else {
                            mImageViewFace1.setImageResource(R.mipmap.ic_contact_picture);
                        }
                    }
                }
                mCmpPic.setText(String.format("相似度 :  %.2f", cmpTemp) + "%");
            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        mPermissionsManager.recheckPermissions(requestCode, permissions, grantResults);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mIdCardUtil.close();
    }

    //身份证回调
    @Override
    public void callBack(int a) {
        if (a ==IdCardUtil.READ){
            idCard = mIdCardUtil.getIdCard();
            if (idCard!=null){
                if(matFace == null)matFace = new Mat();
                mBitmapFace2 = idCard.getPhoto();
                mBitmapFace3 = FaceUtil.getSizeBmp(FaceUtil.grey(mBitmapFace2));
                //保存bitmap位图，用于比较
                //FaceUtil.saveImage(MainActivity.this,FaceUtil.grey(FaceUtil.getSizeBmp(mBitmapFace2)),FACE2);
            }else{
                isGettingFace = true;
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (null == mBitmapFace2||idCard==null) {

                        mImageViewFace2.setImageResource(R.mipmap.ic_contact_picture);
                    } else {
                        mImageViewFace2.setImageBitmap(mBitmapFace3);
                    }

                }
            });
        }
    }
}
