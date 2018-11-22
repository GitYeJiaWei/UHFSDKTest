package com.rfid.common;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**

 * @Author: YeJW

 * @Date: 2018/6/11 10:54

 * @Description: 全局异常捕捉类

 *

 */
public class CrashHandler implements Thread.UncaughtExceptionHandler {

    private Thread.UncaughtExceptionHandler mDefaultHandler;//系统默认UncaughtExceptionHandler

    private Context mcontext;

    private SimpleDateFormat format = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");//格式化时间

    private String TAG = this.getClass().getSimpleName();

    private HashMap<String, String> hashMap = new HashMap<>();//存储异常和参数信息

    private static CrashHandler mInstance;

    private CrashHandler() {

    }

    /**
     * 获取CrashHandler 实例
     *
     * @return
     */
    public static synchronized CrashHandler getInstance() {
        if (mInstance == null) {
            mInstance = new CrashHandler();
        }
        return mInstance;
    }

    public void init(Context context) {
        mcontext = context;
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        //设置该CrashHandler为系统默认的
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    /**
     * uncaughtException 回调函数
     */
    @Override
    public void uncaughtException(Thread t, Throwable ex) {
        if (!handleException(ex) && mDefaultHandler != null) {//如果自己没处理交给系统处理
            mDefaultHandler.uncaughtException(t, ex);
        } else {//自己处理
            try {//延迟3秒杀进程
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                Log.e(TAG, "error : ", e);
            }
            //退出程序
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(0);
        }

    }

    /**
     * 收集错误信息.发送到服务器
     * @return 处理了该异常返回true,否则false
     */
    private boolean handleException(Throwable throwable){
        if (throwable == null) {
            return false;
        }
        //收集设备参数信息
        collectDeviceInfo(mcontext);
        //添加自定义信息
        addCustomInfo();
        //使用Toast来显示异常信息
        new Thread() {
            @Override
            public void run() {
                Looper.prepare();
                Toast.makeText(mcontext, "程序开小差了呢..", Toast.LENGTH_SHORT).show();
                Looper.loop();
            }
        }.start();
        //保存日志文件
        saveCrashInfo2File(throwable);
        return true;
    }

    /**
     * 收集设备参数信息
     * @param ctx
     */
    public void collectDeviceInfo(Context ctx) {
        //获取versionName,versionCode
        try {
            PackageManager pm = ctx.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(ctx.getPackageName(), PackageManager.GET_ACTIVITIES);
            if (pi != null) {
                String versionName = pi.versionName == null ? "null" : pi.versionName;
                //条件语句判断，如果为null则输出null 否则为pi.versionName
                String versionCode = pi.versionCode + "";
                hashMap.put("versionName", versionName);
                hashMap.put("versionCode", versionCode);
            }
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "an error occured when collect package info", e);
        }
        //获取所有系统信息
        Field[] fields = Build.class.getDeclaredFields();
        for (Field field : fields) {
            try {
                field.setAccessible(true);
                hashMap.put(field.getName(), field.get(null).toString());
            } catch (Exception e) {
                Log.e(TAG, "an error occured when collect crash info", e);
            }
        }
    }

    /**
     * 添加自定义参数
     */
    private void addCustomInfo(){

    }

    /**
     * 保存错误信息到文件中
     * @param throwable
     * @return
     */
    private String saveCrashInfo2File(Throwable throwable){
        StringBuilder stringBuilder =new StringBuilder();
        for (Map.Entry<String,String> entry:hashMap.entrySet()
             ) {
            String Key=entry.getKey();
            String Value =entry.getValue();
            stringBuilder.append(Key + Value);
        }

        //Writer将包含堆栈信息
        Writer writer =new StringWriter();
        //必须将Writer封装成PrintWriter对象，以满足printStackTrace的要求
        PrintWriter printWriter =new PrintWriter(writer);
        //获取堆栈信息
        throwable.printStackTrace(printWriter);
        Throwable cause = throwable.getCause();
        while (cause != null) {
            cause.printStackTrace(printWriter);
            cause = cause.getCause();
        }
        printWriter.close();
        String result = writer.toString();
        stringBuilder.append(result);

        try {
            long timeamp = System.currentTimeMillis();
            String time =format.format(new Date());
            String fileName = "crash-" + time + "-" + timeamp + ".log";
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/crash/";
                File dir = new File(path);
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                FileOutputStream fos = new FileOutputStream(path + fileName);
                fos.write(stringBuilder.toString().getBytes());
                fos.close();
            }
            return fileName;
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

}
