package fay.betacat.dev.qqmusic.utils;

import fay.betacat.dev.qqmusic.dto.Song;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class DownloadUtils {

    public static final String DOWNLOAD_PATH = "/Users/betacat/DEV/TEMP";


    public static void download(String link, Song song){
        try {
            URL url = new URL(link);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            //设置超时间为3秒
            conn.setConnectTimeout(30 * 1000);
            //防止屏蔽程序抓取而返回403错误
            conn.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");

            //得到输入流
            InputStream inputStream = conn.getInputStream();
            byte[] buffer = new byte[1024];
            int len = 0;
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            while ((len = inputStream.read(buffer)) != -1) {
                bos.write(buffer, 0, len);
            }
            bos.close();
            byte[] musicData = bos.toByteArray();

            //文件保存位置
            File saveDir = new File(DOWNLOAD_PATH);
            if (!saveDir.exists()) {
                saveDir.mkdirs();
            }
            File file = new File(saveDir + File.separator + song.getName() + ".mp3");
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(musicData);
            if (fos != null) {
                fos.close();
            }
            if (inputStream != null) {
                inputStream.close();
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        System.out.println("info:" + link + " , download success");

    }




}
