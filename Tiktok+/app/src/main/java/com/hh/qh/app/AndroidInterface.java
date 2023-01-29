package com.hh.qh.app;

        import android.Manifest;
        import android.app.Activity;
        import android.content.ContentResolver;
        import android.content.ContentValues;
        import android.content.Context;
        import android.content.Intent;
        import android.content.pm.ApplicationInfo;
        import android.content.pm.PackageManager;
        import android.media.MediaScannerConnection;
        import android.net.Uri;
        import android.os.Build;
        import android.os.Handler;
        import android.os.Looper;
        import android.provider.MediaStore;
        import android.text.TextUtils;
        import android.util.Log;
        import android.webkit.JavascriptInterface;
        import android.widget.Toast;

        import androidx.fragment.app.FragmentActivity;

        import com.hh.qh.utils.Base64Utils;
        import com.just.agentweb.AgentWeb;
        import com.tbruyelle.rxpermissions3.RxPermissions;

        import java.io.BufferedInputStream;
        import java.io.File;
        import java.io.FileInputStream;
        import java.io.FileNotFoundException;
        import java.io.IOException;
        import java.io.OutputStream;

        import io.reactivex.rxjava3.functions.Action;
        import io.reactivex.rxjava3.functions.Consumer;


/**
 * Created by cenxiaozhong on 2017/5/14.
 *  source code  https://github.com/Justson/AgentWeb
 */

public class AndroidInterface {
    private static final String TAG="AndroidInterface";
    private Handler deliver = new Handler(Looper.getMainLooper());
    private AgentWeb agent;
    private Activity context;

    public AndroidInterface(AgentWeb agent, Activity context) {
        this.agent = agent;
        this.context = context;
    }

    @JavascriptInterface
    public void _androidDownloadFile(String msg) {
//        Log.i(TAG, "_androidDownloadFile" + msg);
        context.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                getRxPermissions(msg);
            }
        });
    }


    @JavascriptInterface
    public void _openApp(String msg) {
        Log.i(TAG, "_openApp" + msg);
        if(TextUtils.isEmpty(msg)){
            return;
        }
        context.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(isAppExist(msg)){ //如果安装了App跳转
                    PackageManager packageManager = context.getPackageManager();
                    Intent it = packageManager.getLaunchIntentForPackage(msg);
                    context.startActivity(it);
                }else if(isAppExist("com.google.android.apps")){ //如果没有安装了App跳转Google商店
                    PackageManager packageManager = context.getPackageManager();
                    Intent it = packageManager.getLaunchIntentForPackage("com.google.android.apps");
                    context.startActivity(it);
                }else if("com.google.android.youtube".equals(msg)){ //如果没有Youtube  App，没有应用商店跳转到网页版
                    Uri uri = Uri.parse("https://m.youtube.com/");
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    context.startActivity(intent);
                }else if("com.facebook.katana".equals(msg)){ //如果没有 facebook App，没有应用商店跳转到网页版
                    Uri uri = Uri.parse("https://m.facebook.com/");
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    context.startActivity(intent);
                }else if(!TextUtils.isEmpty(msg)){
                    try {
                        Uri uri = Uri.parse(msg);
                        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                        intent.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
                        context.startActivity(intent);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }else{
                    Toast.makeText(context,"APP này chưa được cài đặt, vui lòng thử lại sau",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    protected boolean isAppExist(String pkgName) {
        ApplicationInfo info;
        try {
            info = context.getPackageManager().getApplicationInfo(pkgName, 0);
        }
        catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            info = null;
        }
        return info != null;
    }

    private void getRxPermissions(final String msg){
        RxPermissions rxPermissions = new RxPermissions((FragmentActivity) context);
        rxPermissions
                .request(Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .subscribe(new Consumer<Boolean>() {
                               @Override
                               public void accept(Boolean aBoolean) {
                                   Log.i(TAG, "Permission result " + aBoolean);
                                   if (aBoolean) {
                                       File file=Base64Utils.decoderBase64File(msg,context);
                                       updata(file);
                                   } else {
                                       // Denied permission with ask never again
                                       // Need to go to the settings
                                       Toast.makeText(context,
                                               "Permission denied, can't enable the camera",
                                               Toast.LENGTH_SHORT).show();
                                   }
                               }
                           },
                        new Consumer<Throwable>() {
                            @Override
                            public void accept(Throwable t) {
                                Log.e(TAG, "onError", t);
                            }
                        },
                        new Action() {
                            @Override
                            public void run() {
                                Log.i(TAG, "OnComplete");
                            }
                        });
    }


    private void updata(File file){
        if(!file.exists()){
            return;
        }
        //安卓系统环境版本在29以下时：
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
//            try{
//                MediaStore.Images.Media.insertImage(context.getContentResolver(),
//                        file.getAbsolutePath(), file.getName(), null);
//            } catch (FileNotFoundException e) {
//                e.printStackTrace();
//            }
            // 通知图库更新
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                MediaScannerConnection.scanFile(context, new String[]{file.getAbsolutePath()}, null,
                        new MediaScannerConnection.OnScanCompletedListener() {
                            public void onScanCompleted(String path, Uri uri) {
                                Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                                mediaScanIntent.setData(uri);
                                context.sendBroadcast(mediaScanIntent);
                            }
                        });
            } else {
                String relationDir = file.getParent();
                File file1 = new File(relationDir);
                context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.fromFile(file1.getAbsoluteFile())));
            }
        } else {//Android Q把文件插入到系统图库
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.DESCRIPTION, "This is an qr image");
            values.put(MediaStore.Images.Media.DISPLAY_NAME, file.getName());
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
            values.put(MediaStore.Images.Media.TITLE, System.currentTimeMillis()+".png");
            values.put(MediaStore.Images.Media.RELATIVE_PATH, "DCIM/Camera/");

            Uri external = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            ContentResolver resolver = context.getContentResolver();
            Uri insertUri = resolver.insert(external, values);
            BufferedInputStream inputStream = null;
            OutputStream os = null;
            try {
                inputStream = new BufferedInputStream(new FileInputStream(file));
                if (insertUri != null) {
                    os = resolver.openOutputStream(insertUri);
                }
                if (os != null) {
                    byte[] buffer = new byte[1024 * 4];
                    int len;
                    while ((len = inputStream.read(buffer)) != -1) {
                        os.write(buffer, 0, len);
                    }
                    os.flush();
                }
                if (os != null) os.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Toast.makeText(context, "đã lưu thành công", Toast.LENGTH_SHORT).show();
    }
}
