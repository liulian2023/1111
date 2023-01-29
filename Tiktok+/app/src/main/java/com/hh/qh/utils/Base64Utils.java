package com.hh.qh.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;
import android.util.Base64;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Base64Utils {
    /**
     * encodeBase64File:(将文件转成base64 字符串). <br/>
     *
     * @param path 文件路径
     * @return
     * @throws Exception
     * @since JDK 1.6
     */
    public static String encodeBase64File(String path) throws Exception {
        File file = new File(path);
        FileInputStream inputFile = new FileInputStream(file);
        byte[] buffer = new byte[(int) file.length()];
        inputFile.read(buffer);
        inputFile.close();
        return Base64.encodeToString(buffer, Base64.DEFAULT);
    }

    /**
     * decoderBase64File:(将base64字符解码保存文件). <br/>
     *
     * @param base64Code 编码后的字串
     * @throws Exception
     * @since JDK 1.6
     */
    public static File decoderBase64File(String base64Code, Context context) {
        File file = null;
        try{
            String fileName = System.currentTimeMillis() + ".jpg";

            File image = new File(Environment.getExternalStorageDirectory(),"DCIM/Camera/"+fileName+".jpg");

//            File root = new File(Environment.getExternalStorageDirectory(), context.getPackageName());
            File dir = new File(image.getAbsolutePath());
            if (dir.mkdirs() || dir.isDirectory()) {
                file = new File(dir, fileName);
            }else{
                file.delete();
            }
            byte[] buffer = Base64.decode(base64Code, Base64.DEFAULT);
            FileOutputStream out = new FileOutputStream(file);
            out.write(buffer);
            out.close();
        }catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file;
    }

    /* Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

}
