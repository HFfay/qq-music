package fay.betacat.dev.qqmusic.support;

import fay.betacat.dev.qqmusic.dto.Task;

import java.io.IOException;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import static fay.betacat.dev.qqmusic.support.DownloadConfig.THREAD_NUM;

public class DownloadManager {

    private static final List<Task> predownList = new LinkedList();

    private static final List<Task> downingList = new LinkedList();

    public static Task popPreDownTask(){
        if(predownList.size() > 0) {
            Task task = predownList.get(0);
            predownList.remove(0);
            return task;
        }
        return null;
    }

    public static void pushPreDownTask(Task task) {
        predownList.add(task);
    }

    public static Task popDowningTask(){
        if(downingList.size() > 0) {
            Task task = downingList.get(0);
            downingList.remove(0);
            return task;
        }
        return null;
    }

    public static void pushDowningTask(Task task) {
        downingList.add(task);
    }

    public static void delTask(String uuid) {
        for (int i = predownList.size() - 1; i >= 0; i--) {
            if (predownList.get(i).getUuid().equalsIgnoreCase(uuid)) {
                predownList.remove(i);
                break;
            }
        }
        for (int i = downingList.size() - 1; i >= 0; i--) {
            if (downingList.get(i).getUuid().equalsIgnoreCase(uuid)) {
                downingList.remove(i);
                // TODO 中断下载线程，删除临时文件
                break;
            }
        }
    }

    public void excute() {

        while (true){
            Task task = popPreDownTask();
            if(task != null) {
                long startTime = System.currentTimeMillis();

                boolean resumable = supportResumeDownload(task.getUrl());
                if (!resumable || THREAD_NUM == 1|| fileSize < MIN_SIZE) multithreaded = false;
                if (!multithreaded) {
                    new DownloadThread(0, 0, fileSize - 1).start();
                } else {
                    endPoint = new int[THREAD_NUM + 1];
                    int block = fileSize / THREAD_NUM;
                    for (int i = 0; i < THREAD_NUM; i++) {
                        endPoint[i] = block * i;
                    }
                    endPoint[THREAD_NUM] = fileSize;
                    for (int i = 0; i < THREAD_NUM; i++) {
                        new DownloadThread(i, endPoint[i], endPoint[i + 1] - 1).start();
                    }
                }

                startDownloadMonitor();

                //等待 downloadMonitor 通知下载完成
                try {
                    synchronized(waiting) {
                        waiting.wait();
                    }
                } catch (InterruptedException e) {
                    System.err.println("Download interrupted.");
                }

                cleanTempFile();

                long endTime = System.currentTimeMillis();
                System.out.println("* File successfully downloaded.");
                System.out.println(String.format("* Time used: %.3f s, Average speed: %d KB/s", (endTime - startTime) / 1000.0, downloadedBytes.get() / (endTime - startTime)));
            }
        }

    }

    //检测目标文件是否支持断点续传，以决定是否开启多线程下载文件的不同部分
    public boolean supportResumeDownload(String url){
        try {
            URL _url = new URL(url);
            HttpURLConnection con = (HttpURLConnection) _url.openConnection();
            con.setRequestProperty("Range", "bytes=0-");
            int resCode;
            while (true) {
                try {
                    con.connect();
                    fileSize = con.getContentLength();
                    resCode = con.getResponseCode();
                    con.disconnect();
                    break;
                } catch (ConnectException e) {
                    System.out.println("Retry to connect due to connection problem.");
                }
            }
            if (resCode == 206) {
                System.out.println("* Support resume download");
                return true;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        System.out.println("* Doesn't support resume download");
        return false;
    }

    //监测下载速度及下载状态，下载完成时通知主线程
    public void startDownloadMonitor() {
        Thread downloadMonitor = new Thread(() -> {
            int prev = 0;
            int curr = 0;
            while (true) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {}

                curr = downloadedBytes.get();
                System.out.println(String.format("Speed: %d KB/s, Downloaded: %d KB (%.2f%%), Threads: %d", (curr - prev) >> 10, curr >> 10, curr / (float) fileSize * 100, aliveThreads.get()));
                prev = curr;

                if (aliveThreads.get() == 0) {
                    synchronized (waiting) {
                        waiting.notifyAll();
                    }
                }
            }
        });

        downloadMonitor.setDaemon(true);
        downloadMonitor.start();
    }


    //对临时文件进行合并或重命名
    public void cleanTempFile() throws IOException {
        if (multithreaded) {
            merge();
            System.out.println("* Temp file merged.");
        } else {
            Files.move(Paths.get(localFile.getAbsolutePath() + ".0.tmp"),
                    Paths.get(localFile.getAbsolutePath()), StandardCopyOption.REPLACE_EXISTING);
        }
    }


    //合并多线程下载产生的多个临时文件
    public void merge() {
        try (OutputStream out = new FileOutputStream(localFile)) {
            byte[] buffer = new byte[1024];
            int size;
            for (int i = 0; i < THREAD_NUM; i++) {
                String tmpFile = localFile.getAbsolutePath() + "." + i + ".tmp";
                InputStream in = new FileInputStream(tmpFile);
                while ((size = in.read(buffer)) != -1) {
                    out.write(buffer, 0, size);
                }
                in.close();
                Files.delete(Paths.get(tmpFile));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
