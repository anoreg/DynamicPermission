package com.anoreg.dynamicpermission;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Utility class that wraps access to the runtime permissions API in M and provides basic helper
 * methods.
 */
public class PermissionUtil {

    public static void requestPermissions(final FragmentActivity context, String[] permissions,
                                          final int errResId, final PermissionDialog.IPermissionListener permissionListener) {
        if(PermissionUtil.hasPermission(context, permissions, new PermissionDialog.IPermissionListener() {
            @Override
            public void onGrant(boolean isGranted) {
                if (!isGranted) {
                    context.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(context, errResId, Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                permissionListener.onGrant(isGranted);
            }
        })) {
            permissionListener.onGrant(true);
        }
    }

    public static void requestCamera(final FragmentActivity context, final PermissionDialog.IPermissionListener callback) {
        requestPermissions(context, new String[] {Manifest.permission.CAMERA,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE}, R.string.permission_camera_null, callback);
    }

    public static void requestStorage(final FragmentActivity context, final PermissionDialog.IPermissionListener callback) {
        requestPermissions(context, new String[] {Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE}, R.string.permission_storage_null, callback);
    }

    public static boolean hasLocationPermission(Context context) {
        return PermissionUtil.hasSelfPermission(context, new String[] {Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION});
    }

    public static void requestLocation(final FragmentActivity context, final PermissionDialog.IPermissionListener callback) {
        requestPermissions(context, new String[] {
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION}, R.string.permission_location_null, callback);
    }

    public static void requestContact(final FragmentActivity context, final PermissionDialog.IPermissionListener callback) {
        requestPermissions(context, new String[] {Manifest.permission.READ_CONTACTS},
                R.string.permission_contact_null, callback);
    }

    public static boolean hasPermission(FragmentActivity context, String[] permissions,
                                        PermissionDialog.IPermissionListener permissionListener) {
        if (!isMNC()) return true;
        String[] notGrantedPermission = filterPermissions(context, permissions);
        if (notGrantedPermission.length == 0) return true;

        PermissionDialog dialog = PermissionDialog.newInstance(notGrantedPermission);
        dialog.setPermissionListener(permissionListener);
        dialog.showDialog(context);
        return false;

    }

    public static String[] filterPermissions(Context context, String[] permissions) {
        ArrayList<String> notGrantedPermissionList = new ArrayList<String>();
        for (String permission : permissions) {
            if(!verifyPermission(context, permission)) {
                notGrantedPermissionList.add(permission);
            }
        }
        return notGrantedPermissionList.toArray(new String[notGrantedPermissionList.size()]);
    }

    /**
     * Check that all given permissions have been granted by verifying that each entry in the
     * given array is of the value {@link PackageManager#PERMISSION_GRANTED}.
     *
     * @see Activity#onRequestPermissionsResult(int, String[], int[])
     */
    public static boolean verifyPermissions(int[] grantResults) {
        // Verify that each required permission has been granted, otherwise return false.
        for (int result : grantResults) {
            if (!verifyPermission(result)) {
                return false;
            }
        }
        return true;
    }

    public static boolean verifyPermission(int result) {
        return result == PackageManager.PERMISSION_GRANTED;
    }

    private static boolean verifyPermission(Context context, String permission) {
        return verifyPermission(ContextCompat.checkSelfPermission(context, permission));
    }

    public static boolean hasSelfPermission(Context context, String[] permissions) {
        // Below Android M all permissions are granted at install time and are already available.
        if (!isMNC()) {
            return true;
        }

        // Verify that all required permissions have been granted
        for (String permission : permissions) {
            if (!verifyPermission(context, permission)) {
                return false;
            }
        }
        return true;
    }

    public static boolean hasSelfPermission(Context context, String permission) {
        // Below Android M all permissions are granted at install time and are already available.
        return !isMNC() || verifyPermission(context, permission);

    }

    public static boolean shouldShowPermissionRational(Activity activity, String permission) {
        return ActivityCompat.shouldShowRequestPermissionRationale(activity, permission);
    }

    public static void requestPermissions(Activity activity, String[] permissions, int requestCode) {
        ActivityCompat.requestPermissions(activity, permissions, requestCode);
    }

    public static void requestPermissions(Fragment fragment, String[] permissions, int requestCode) {
        fragment.requestPermissions(permissions, requestCode);
    }

    private static boolean isMNC() {
        return Build.VERSION_CODES.M <= Build.VERSION.SDK_INT;
    }

}
