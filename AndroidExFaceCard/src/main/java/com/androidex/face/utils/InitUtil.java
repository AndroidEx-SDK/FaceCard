package com.androidex.face.utils;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.Toast;

import com.androidex.face.PermissionsManager;
import com.synjones.idcard.IDCard;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.content.ContentValues.TAG;

/**
 * Created by cts on 17/5/26.
 */

public class InitUtil {
    // 要校验的权限
    public static String[] PERMISSIONS = new String[]{Manifest.permission.CAMERA};

    /**
     * 初始化权限管理类
     */
    public static void initPermissionManager(final Activity context) {
        PermissionsManager mPermissionsManager = new PermissionsManager(context) {
            @Override
            public void authorized(int requestCode) {
                Toast.makeText(context, "权限通过！", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void noAuthorization(int requestCode, String[] lacksPermissions) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("提示");
                builder.setMessage("缺少相机权限！");
                builder.setPositiveButton("设置权限", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        PermissionsManager.startAppSettings(context);
                    }
                });
                builder.create().show();
            }

            @Override
            public void ignore() {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("提示");
                builder.setMessage("Android 6.0 以下系统不做权限的动态检查\n如果运行异常\n请优先检查是否安装了 OpenCV Manager\n并且打开了 CAMERA 权限");
                builder.setPositiveButton("确认", null);
                builder.setNeutralButton("设置权限", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        PermissionsManager.startAppSettings(context);
                    }
                });
                //builder.create().show();
            }
        };
        mPermissionsManager.checkPermissions(0, PERMISSIONS);// 检查权限
    }

    /**
     * 保存方法
     */
    public static void saveBitmap(String path, String picName, Bitmap bmp) {
        Log.e(TAG, "保存图片");
        File f = new File(path, picName);
        if (f.exists()) {
            f.delete();
        }
        try {
            FileOutputStream out = new FileOutputStream(f);
            bmp.compress(Bitmap.CompressFormat.PNG, 90, out);
            out.flush();
            out.close();
            Log.i(TAG, "已经保存");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 用JSON文件保存数组
     */
    public static void saveJsonStringArray(String[] data) {

        JSONObject allData = new JSONObject();//建立最外面的节点对象
        JSONArray sing = new JSONArray();//定义数组
        for (int x = 0; x < data.length; x++) {//将数组内容配置到相应的节点
            JSONObject temp = new JSONObject();//JSONObject包装数据,而JSONArray包含多个JSONObject
            try {
                temp.put("myurl", data[x]); //JSONObject是按照key:value形式保存

            } catch (JSONException e) {
                e.printStackTrace();
            }
            sing.put(temp);//保存多个JSONObject
        }
        try {
            allData.put("urldata", sing);//把JSONArray用最外面JSONObject包装起来
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (!Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {//SD卡不存在则不操作
            return;//返回到程序的被调用处
        }
        File file = new File(Environment.getExternalStorageDirectory()
                + File.separator + "mldndata" + File.separator
                + "json.txt");//要输出的文件路径
        if (!file.getParentFile().exists()) {//文件不存在
            file.getParentFile().mkdirs();//创建文件夹
        }
        PrintStream out = null;
        try {
            out = new PrintStream(new FileOutputStream(file));
            out.print(allData.toString());//将数据变为字符串后保存
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (out != null) {
                out.close();//关闭输出
            }
        }
    }


    /**
     * 解析JSON文件的简单数组
     */
    public static List<Map<String, String>> parseJson() throws Exception {
        String data=getString2Txt();
        List<Map<String, String>> all = new ArrayList<Map<String, String>>();
        JSONArray jsonArr = new JSONArray(data);    //是数组
        for (int x = 0; x < jsonArr.length(); x++) {
            Map<String, String> map = new HashMap<String, String>();
            JSONObject jsonobj = jsonArr.getJSONObject(x);
            map.put("name", jsonobj.getString("head"));
            map.put("photo", jsonobj.getString("photo"));
            map.put("sex", jsonobj.getString("sex"));
            map.put("nation", jsonobj.getString("nation"));
            map.put("birthday", jsonobj.getString("birthday"));
            map.put("address", jsonobj.getString("address"));
            map.put("idnum", jsonobj.getString("idnum"));
            map.put("head", jsonobj.getString("head"));
            all.add(map);
        }
        return all;
    }

    public static String getString2Txt() {
        String str = null;
        try {
            File urlFile = new File(Environment.getExternalStorageDirectory()
                    + File.separator + "mldndata" + File.separator
                    + "json.txt");
            InputStreamReader isr = new InputStreamReader(new FileInputStream(urlFile), "UTF-8");
            BufferedReader br = new BufferedReader(isr);

            String mimeTypeLine = null;
            while ((mimeTypeLine = br.readLine()) != null) {
                str = str + mimeTypeLine;
            }
            return str;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (str != null && str != "") {
                return str;
            }
        }
        Log.e(TAG, "文件为空");
        return null;
    }

    /**
     * 用JSON文件保存复杂数据
     */
    public void saveJson() {
        JSONObject allData = new JSONObject();//建立最外面的节点对象
        JSONArray sing = new JSONArray();//定义数组

//        for(int x = 0; x < this.nameData.length; x++){//将数组内容配置到相应的节点
//            JSONObject temp = new JSONObject();//JSONObject包装数据,而JSONArray包含多个JSONObject
//            try {
//                //每个JSONObject中，后加入的数据显示在前面
//                temp.put("name", this.nameData[x]);
//                temp.put("age", this.ageData[x]);
//                temp.put("married", this.isMarriedData[x]);
//                temp.put("salary", this.salaryData[x]);
//                temp.put("birthday", this.birthdayData[x]);
//                //JSONObject是按照key:value形式保存
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//            sing.put(temp);//保存多个JSONObject
//        }
//        try {
//            allData.put("persondata", sing);//把JSONArray用最外面JSONObject包装起来
//            allData.put("company",this.companyName);
//            allData.put("address",this.companyAddr);
//            allData.put("telephone",this.companyTel);
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
        if (!Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {//SD卡不存在则不操作
            return;//返回到程序的被调用处
        }
        File file = new File(Environment.getExternalStorageDirectory()
                + File.separator + "mldndata" + File.separator
                + "json.txt");//要输出的文件路径
        if (!file.getParentFile().exists()) {//文件不存在
            file.getParentFile().mkdirs();//创建文件夹
        }
        PrintStream out = null;
        try {
            out = new PrintStream(new FileOutputStream(file));
            out.print(allData.toString());//将数据变为字符串后保存
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (out != null) {
                out.close();//关闭输出
            }
        }


    }

    public static JSONObject getJsonSimple(IDCard idCard, Bitmap bmp) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("name", idCard.getName());
            jsonObject.put("photo", idCard.getPhoto());
            jsonObject.put("sex", idCard.getSex());
            jsonObject.put("nation", idCard.getNation());
            jsonObject.put("birthday", idCard.getBirthday());
            jsonObject.put("address", idCard.getAddress());
            jsonObject.put("idnum", idCard.getIDCardNo());
            jsonObject.put("head", bmp);

        } catch (JSONException e) {
            throw new RuntimeException("getJsonSimple JSONException:" + e);
        }
        Log.d(TAG, "getJsonSimple jsonObject:" + jsonObject.toString());
        return jsonObject;
    }
}
