Usage：

	 PermissionUtil.requestStorage(this, new PermissionDialog.IPermissionListener() {
            @Override
            public void onGrant(boolean isGranted) {
                if (isGranted) {
                    // TODO: 18-12-5 do your things
                }
            }
        });
        
There are many methods, like requestLocation, requestCamera, and also you can pass an permission array to the PermissionUtil.

All you need to do is add PermissionUtil.request* to your code and waiting for the callback

If the user deny the permission, this util will again request the permission. If user check the always deny button, this util will lead the user to app detail setting to manually check the permission button