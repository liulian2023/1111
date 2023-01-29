package com.hh.qh.app;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.hh.qh.utils.PermissionUtils;
import com.hjq.permissions.OnPermissionCallback;
import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;

import java.util.List;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!isTaskRoot()) { // 判断当前activity是不是所在任务栈的根
            Intent intent = getIntent();
            if(intent!=null){
                String intentAction = intent.getAction();
                if (intent.hasCategory(Intent.CATEGORY_LAUNCHER) && intentAction != null &&
                        intentAction.equals(Intent.ACTION_MAIN)) {
                    //通过启动页启动直接finish
                    finish();
                    return;
                }
            }
        }
        doPermissionWork();
    }

    private void doPermissionWork() {
        XXPermissions.with(SplashActivity.this)
                // InstallPackages Permission
                //.permission(Permission.REQUEST_INSTALL_PACKAGES)
                // R/W STORAGE Permission
                .permission(Permission.Group.STORAGE)
                .request(new OnPermissionCallback() {

                    @Override
                    public void onGranted(List<String> permissions, boolean all) {
                        if (all) {
                            gotoMain();
                        } else {
                            toast(getString(R.string.authorization_not_completely_succeeded));
                        }
                    }

                    @Override
                    public void onDenied(List<String> permissions, boolean never) {
                        if (never) {
                            toast(getString(R.string.authorization_denied));
                            // 如果是被永久拒绝就跳转到应用权限系统设置页面
                            XXPermissions.startPermissionActivity(SplashActivity.this, permissions);
                        } else {
                            toast(getString(R.string.authorization_denied_failed_to_get_permission));
                        }
                    }
                });
    }


    private void gotoMain(){
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent=new Intent(SplashActivity.this,MainActivity.class);
                startActivity(intent);
                finish();
            }
        },1000); // 延时1秒
    }

    private void toast(String str){
        Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == XXPermissions.REQUEST_CODE) {
            if (XXPermissions.isGranted(this, Permission.Group.STORAGE)) {
                gotoMain();
            } else {
                toast(getString(R.string.permission_not_granted));
            }
        }
    }
}
