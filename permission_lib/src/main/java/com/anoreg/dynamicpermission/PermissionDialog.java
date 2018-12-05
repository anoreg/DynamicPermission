package com.anoreg.dynamicpermission;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

@TargetApi(23)
public class PermissionDialog extends DialogFragment {

    private IPermissionListener mListener;

    private static final int REQUEST_BASIC = 0x1000;
    private static String[] mPermissions;
    private static final String TAG = "permission_dialog";

    public static PermissionDialog newInstance(String[] permissions) {
        Bundle bundle = new Bundle();
        bundle.putStringArray("permissions", permissions);
        PermissionDialog fragment = new PermissionDialog();
        fragment.setArguments(bundle);
        return fragment;
    }

    public PermissionDialog() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE, 0);
        mPermissions = getArguments().getStringArray("permissions");
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.setCanceledOnTouchOutside(false);
        return dialog;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        setCancelable(false);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        PermissionUtil.requestPermissions(PermissionDialog.this, mPermissions, REQUEST_BASIC);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length == 0) {
            onGrant(false);
            dismissDialog();
            return;
        }
        switch (requestCode) {
            case REQUEST_BASIC:
                if (PermissionUtil.verifyPermissions(grantResults)) {
                    onGrant(true);
                    dismissDialog();
                } else {
                    final Activity activity = getActivity();
                    final String[] filterPermissions = PermissionUtil.filterPermissions(activity, permissions);
                    boolean isPermissionDeined = false;
                    for (String permission : filterPermissions) {
                       if(!PermissionUtil.shouldShowPermissionRational(activity, permission)) {
                           isPermissionDeined = true;
                           break;
                       }
                    }
                    final boolean isAlwaysDenied = isPermissionDeined;
                    String contentTips = getString(R.string.permission_alert,
                            getPermissionTips(filterPermissions));
                    if (isAlwaysDenied) {
                        contentTips += "\n" + getString(R.string.permission_set);
                    }
                    AlertDialog dialog = new AlertDialog.Builder(activity, android.R.style.Theme_DeviceDefault_Light_Dialog_Alert)
                            .setMessage(contentTips)
                            .setCancelable(false)
                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if (isAlwaysDenied) {
                                        dismissDialog();
                                        startAppDetailSetting(activity);
                                    } else {
                                        PermissionUtil.requestPermissions(PermissionDialog.this,
                                                filterPermissions, REQUEST_BASIC);
                                    }
                                }
                            })
                            .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    onGrant(false);
                                    dismissDialog();
                                }
                            })
                            .create();
                    dialog.setCancelable(false);
                    dialog.setCanceledOnTouchOutside(false);
                    dialog.show();
                }
                break;
            default:
                break;
        }
    }

    private String getPermissionTips(String[] permissions) {
        SparseArray<String> sparseArray = new SparseArray<>();
        for (String permission : permissions) {
            if (permission.contains("CAMERA")) {
                sparseArray.put(CAMERA, getString(R.string.permission_camera));
            } else if (permission.contains("PHONE_STATE")) {
                sparseArray.put(PHONE_STATE, getString(R.string.permission_phone));
            } else if (permission.contains("STORAGE")) {
                sparseArray.put(STORAGE, getString(R.string.permission_storage));
            } else if (permission.contains("LOCATION")) {
                sparseArray.put(LOCATION, getString(R.string.permission_location));
            } else if (permission.contains("AUDIO")) {
                sparseArray.put(AUDIO, getString(R.string.permission_audio));
            } else if (permission.contains("CONTACTS")) {
                sparseArray.put(CONTACT, getString(R.string.permission_contact));
            }
        }
        int length = sparseArray.size();
        String permissionTips = "";
        for(int i = 0; i < length; i++) {
            permissionTips += sparseArray.valueAt(i) + ", ";
        }
        int index = permissionTips.lastIndexOf(",");
        if (index > 0)
            permissionTips = permissionTips.substring(0, index);
        return permissionTips.trim();
    }

    public static final int PHONE_STATE = 0;
    public static final int CAMERA = 1;
    public static final int STORAGE = 2;
    public static final int LOCATION = 3;
    public static final int AUDIO = 4;
    public static final int CONTACT = 5;

    public interface IPermissionListener {
        void onGrant(boolean isGranted);
    }


    @Override
    public void onDetach() {
        super.onDetach();
        if (mListener != null)
            mListener = null;
    }

    public void onGrant(boolean isGranted) {
        if (mListener != null)
            mListener.onGrant(isGranted);
    }

    /**
     * listener must be set before show dialog
     *
     * @param listener
     */
    public void setPermissionListener(IPermissionListener listener) {
        mListener = listener;
    }

    /**
     * 显示Dialog
     * @param context
     */
    public void showDialog(FragmentActivity context) {
        FragmentManager manager;
        if (context != null && (manager = context.getSupportFragmentManager()) != null) {
            show(manager, TAG);
        }
    }

    /**
     * 关闭Dialog
     */
    public void dismissDialog() {
        Dialog dialog = getDialog();
        if (dialog != null && dialog.isShowing()) {
            dismissAllowingStateLoss();
        }
    }

    private void startAppDetailSetting(Context context) {
        try {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.setData(Uri.parse("package:" + context.getPackageName()));
            context.startActivity(intent);
        } catch (Exception e) {

        }
    }
}
