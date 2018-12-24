package fay.betacat.dev.qqmusic.utils;

import com.google.gson.Gson;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import fay.betacat.dev.qqmusic.dto.Song;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class QQMusicUtils {

    /**
     * 分页查询歌曲
     *
     * @param name          查询内容
     * @param pagesize      每页歌曲数量
     * @param pageindex       第几页
     * @return
     */
    public static List searchMusic(String name, int pagesize, int pageindex){

        try {
            name = URLEncoder.encode(name, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        String url = "http://c.y.qq.com/soso/fcgi-bin/client_search_cp?new_json=1&cr=1&format=json&inCharset=utf8&outCharset=utf-8" +
                "&p=" + pageindex +
                "&n=" + pagesize +
                "&w=" + name;

        OkHttpClient http = new OkHttpClient();
        Request request = new Request.Builder()
                .get()
                .url(url)
                .build();
        http.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
//                forff = false;
            }

            @Override
            public void onResponse(Response response) throws IOException {
                if (response.isSuccessful()) {
                    String data = response.body().string();

                    dealSongs(data);
                }
            }
        });


        return null;
    }


    private static List<Song> dealSongs(String str){
        List<Song> res = new LinkedList<>();

        Map map = new Gson().fromJson(str, Map.class);


        return res;
    }


    public static File downloadMusic(){



        return null;
    }

}
