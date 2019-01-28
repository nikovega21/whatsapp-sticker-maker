package com.unusualapps.whatsappstickers.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

public class RequestPermissionsHelper {

    private static final int CODE_REQUEST_WRITE_READ_EXTERNAL_STORAGE = 0;

    public static boolean verifyPermissions(Context context) {
        int permissionToWriteCheck = ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int permissionToReadCheck = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE);

        if (permissionToWriteCheck == PackageManager.PERMISSION_DENIED && permissionToReadCheck == PackageManager.PERMISSION_DENIED) {
            return false;
        } else return true;
    }

    public static void requestPermissions(Activity context) {
        ActivityCompat.requestPermissions(context, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, CODE_REQUEST_WRITE_READ_EXTERNAL_STORAGE);
    }
}
