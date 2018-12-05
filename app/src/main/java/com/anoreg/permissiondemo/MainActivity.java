package com.anoreg.permissiondemo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.anoreg.dynamicpermission.PermissionDialog;
import com.anoreg.dynamicpermission.PermissionUtil;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.permission_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestPermission();
            }
        });
    }

    private void requestPermission() {
        PermissionUtil.requestStorage(this, new PermissionDialog.IPermissionListener() {
            @Override
            public void onGrant(boolean isGranted) {
                if (isGranted) {
                    // TODO: 18-12-5 do your things
                }
            }
        });
    }
}
