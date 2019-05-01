package com.example.android.tflitecamerademo.util;

import android.content.Context;
import android.content.res.AssetManager;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

public class AppUtils {

    public static long getFileSize(String path) {
        try {
            return new File(path).length();
        } catch (Exception e) {
            return 0;
        }
    }
    private static final String SEPARATOR = File.separator;//路径分隔符

    public static void copyFilesFromRaw(Context context, int id, String fileName, String storagePath){
        InputStream inputStream=context.getResources().openRawResource(id);
        File file = new File(storagePath);
        if (!file.exists()) {//如果文件夹不存在，则创建新的文件夹
            file.mkdirs();
        }
        readInputStream(storagePath + SEPARATOR + fileName, inputStream);
    }

    /**
     * 读取输入流中的数据写入输出流
     *
     * @param storagePath 目标文件路径
     * @param inputStream 输入流
     */
    public static void readInputStream(String storagePath, InputStream inputStream) {
        File file = new File(storagePath);
        try {
            if (!file.exists()) {
                // 1.建立通道对象
                FileOutputStream fos = new FileOutputStream(file);
                // 2.定义存储空间
                byte[] buffer = new byte[inputStream.available()];
                // 3.开始读文件
                int lenght = 0;
                while ((lenght = inputStream.read(buffer)) != -1) {// 循环从输入流读取buffer字节
                    // 将Buffer中的数据写到outputStream对象中
                    fos.write(buffer, 0, lenght);
                }
                fos.flush();// 刷新缓冲区
                // 4.关闭流
                fos.close();
                inputStream.close();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     *  执行拷贝任务
     *  @param asset 需要拷贝的assets文件路径
     *  @return 拷贝成功后的目标文件句柄
     *  @throws IOException
     */
    public static File copy(Context mContext,String videoFilePath,String asset){
        File destinationFile = null;
        try {
            AssetManager mAssetManager = mContext.getAssets();
//            File mAppDirectory = mContext.getExternalFilesDir(null);
            InputStream source = mAssetManager.open(new File(asset).getPath());
            destinationFile = new File(videoFilePath, asset);
            destinationFile.getParentFile().mkdirs();
            OutputStream destination = new FileOutputStream(destinationFile);
            byte[] buffer = new byte[1024];
            int nread;

            while ((nread = source.read(buffer)) != -1) {
                if (nread == 0) {
                    nread = source.read();
                    if (nread < 0)
                        break;
                    destination.write(nread);
                    continue;
                }
                destination.write(buffer, 0, nread);
            }
            destination.close();
        } catch (Exception e) {

        }
        return destinationFile;
    }

    public static String getResultString(List<String> results) {
        if (results == null || results.isEmpty()) {
            return "";
        }
        String resultString = "";
        for (String resutl : results) {
            if (resultString.equals("")) {
                resultString = resutl;
            } else {
                resultString += "&"+resutl;
            }
        }
        return resultString;
    }
}
