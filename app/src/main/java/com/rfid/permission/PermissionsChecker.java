package com.rfid.permission;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.content.ContextCompat;


public class PermissionsChecker {
    private final Context mContext;

    // 把需要动态获取权限的列表放入
   public static final String[] PERMISSIONS = new String[]{
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    public PermissionsChecker(Context context) {
        mContext = context.getApplicationContext();
    }
    public boolean lacksPermissions()
    {
        return  lacksPermissions(PERMISSIONS);
    }

    // 判断权限集合
    private boolean lacksPermissions(String... permissions) {
        for (String permission : permissions) {
            if (lacksPermission(permission)) {
                return true;
            }
        }
        return false;
    }

    // 判断是否缺少权限
    private boolean lacksPermission(String permission) {
        return ContextCompat.checkSelfPermission(mContext, permission) ==
                PackageManager.PERMISSION_DENIED;
    }
}
