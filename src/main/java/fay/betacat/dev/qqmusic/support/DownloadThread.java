package fay.betacat.dev.qqmusic.support;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.concurrent.atomic.AtomicInteger;

import static fay.betacat.dev.qqmusic.support.DownloadConfig.DOWNLOAD_PATH;
import static fay.betacat.dev.qqmusic.support.DownloadConfig.TIME_OUT;

public class DownloadThread extends Thread {

    int id;
    URL url;
    int start;
    int end;

    public DownloadThread(int id, URL url, int start, int end) {
        super("DownloadThread - " + id);
        this.id = id;
        this.url = url;
        this.start = start;
        this.end = end;
    }

    public void run2() {
        try {
            //下载进度文件保存的路径和文件名
            File progressFile = new File("d://文件测试",(id + ".txt"));
            //判断保存下载进度的临时文件是否存在，以便确定下载的开始位置
            if (progressFile.exists()) {
                FileInputStream fis = new FileInputStream(progressFile);
                BufferedReader bReader = new BufferedReader(new InputStreamReader(fis));
                //拿到临时文件中保存的数据，并把此数据设置为新的开始位置
                int text = Integer.parseInt(bReader.readLine());
                start = text;
                fis.close();
            }
            System.out.println("线程"+ id +"的最终开始下载位置是："+ start);

            URL url = new URL(MultiDownload2.path);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            //设置请求数据的范围
            conn.setRequestProperty("Range", "bytes="+ index +"-"+ end);
            //建立连接，状态码206表示请求部分数据成功，此时开始下载任务
            if (conn.getResponseCode()==206) {
                InputStream is = conn.getInputStream();
                //指定文件名和文件路径
                File file = new File(MultiDownload2.getFileName(MultiDownload2.path) );
                int len = 0;
                byte [] b = new byte[1024];
                //三个线程各自创建自己的随机存储文件
                RandomAccessFile raf = new RandomAccessFile(file, "rwd");
                //设置数据从哪个位置开始写入数据到临时文件
                raf.seek(index);
                //设置当前线程下载的总字节数
                int total = 0;
                long start = System.currentTimeMillis();

                //当下载意外停止时，记录当前下载进度
                int currentPosition = index;

                while ((len=is.read(b))!=-1) {
                    raf.write(b,0,len);
                    //打印当前线程下载的总字节数
                    total += len;
                    /**
                     * 实现断点续传的功能
                     */
                    //RandomAccessFile主要用来存放下载的临时文件，可以用FileOutputStream代替
                    RandomAccessFile rafProgress = new RandomAccessFile(progressFile, "rwd");
                    //再次下载时的开始位置
                    currentPosition = start + total;
                    //把下载进度写进rafProgress临时文件，下一次下载时，就以这个值作为新的startIndex
                    rafProgress.write((currentPosition + "").getBytes());
                    //关流
                    rafProgress.close();
                    System.out.println("线程"+ id +"已经下载了"+total);
                }
                raf.close();
                long end = System.currentTimeMillis();
                //打印线程下载文件用时
                System.out.println("线程"+ id +"下载文件用时"+(end-start)+"ms");
                //打印线程的结束
                System.out.println("线程："+ id +" 下载结束了 !!!");
                //下载结束后，删除所有的临时文件
                MultiDownload2.threadFinished ++;
                //使用同步语句块，保证线程的安全性
                synchronized (MultiDownload2.path) {
                    //如果这个条件成立，说明所有的线程下载结束
                    if (MultiDownload2.threadFinished == MultiDownload2.threadCount) {
                        for (int i = 0; i < MultiDownload2.threadCount; i++) {
                            //删除三个线程产生的临时文件
                            File temp = new File("d://文件测试", i + ".txt");
                            temp.delete();
                        }
                        //保证三个线程的临时文件同时被删除
                        MultiDownload2.threadFinished = 0;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void run() {
        boolean success = false;
        int i = 1;
        while (true && i <= 3) {
            success = download();
            if (success) {
                System.out.println("* Downloaded part " + (id + 1));
                break;
            } else {
                System.out.println("[" + i++ + "] Retry to download part " + (id + 1));
            }
        }
        aliveThreads.decrementAndGet();
    }

    public boolean download() {
        AtomicInteger downloadedBytes = new AtomicInteger(0);

        File localFile = new File(DOWNLOAD_PATH + File.separator + url.getFile());
        try (
                OutputStream out = new FileOutputStream(localFile.getAbsolutePath() + "." + id + ".tmp");
                ){
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestProperty("Range", String.format("bytes=%d-%d", start, end));
            con.setConnectTimeout(TIME_OUT);
            con.setReadTimeout(TIME_OUT);
            con.connect();
            int partSize = con.getHeaderFieldInt("Content-Length", -1);
            if (partSize != end - start + 1) return false;

            try (InputStream in = con.getInputStream()) {
                byte[] buffer = new byte[1024];
                int size;
                while (start <= end && (size = in.read(buffer)) > 0) {
                    start += size;
                    downloadedBytes.addAndGet(size);
                    out.write(buffer, 0, size);
                    out.flush();
                }
                con.disconnect();
                if (start <= end) return false;
                else out.close();
            }
        } catch(SocketTimeoutException e) {
            System.out.println("Part " + (id + 1) + " Reading timeout.");
            return false;
        } catch (IOException e) {
            System.out.println("Part " + (id + 1) + " encountered error.");
            return false;
        }
        return true;
    }
}
