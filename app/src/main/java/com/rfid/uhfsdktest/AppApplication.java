package com.rfid.uhfsdktest;

import android.app.Application;
import android.content.Context;

import com.rfid.common.CrashHandler;


public class AppApplication extends Application
{

    @Override
    public void onCreate()
    {
        super.onCreate();
        // 异常处理，不需要处理时注释掉这两句即可！
        CrashHandler crashHandler = CrashHandler.getInstance();
        // 注册crashHandler,//初始化全局异常管理
        crashHandler.init(getApplicationContext());
    }


    @Override
    protected void attachBaseContext(Context base)
    {
        // 在这里调用Context的方法会崩溃
        super.attachBaseContext(base);
        // 在这里可以正常调用Context的方法
    }

}
