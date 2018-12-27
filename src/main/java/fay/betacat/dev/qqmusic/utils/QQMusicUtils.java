package fay.betacat.dev.qqmusic.utils;

import com.google.gson.Gson;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import fay.betacat.dev.qqmusic.dto.Song;
import org.apache.commons.collections.MapUtils;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
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

        final List<Song> res = new LinkedList<>();
        final boolean[] flag = {false};

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
                flag[0] = true;
            }

            @Override
            public void onResponse(Response response) throws IOException {
                try {
                    if (response.isSuccessful()) {
                        String data = response.body().string();

                        res.addAll(dealSongs(data));
                    }
                } catch (Exception e){
                    e.printStackTrace();
                } finally {
                    flag[0] = true;
                }
            }
        });

//        int i = 0;
        while (!flag[0]){
//            if(i / 50 == 0){
                System.out.println("waiting for the result [" + flag[0] + "]......");
//            }
//            i++;
        }

        return res;
    }


    private static List<Song> dealSongs(String str){
        List<Song> res = new LinkedList<>();

        Map map = new Gson().fromJson(str, Map.class);

        Map data = MapUtils.getMap(map, "data");
        Map songInfo = MapUtils.getMap(data, "song");
        int curnum = MapUtils.getIntValue(songInfo, "curnum", 0);
        int curpage = MapUtils.getIntValue(songInfo, "curpage", 0);
        int totalnum = MapUtils.getIntValue(songInfo, "totalnum", 0);
        List<Map> list = (List<Map>) MapUtils.getObject(songInfo, "list", new LinkedList<>());
        for (Map s : list){
            String mid = MapUtils.getString(s, "mid", "");
            String name = MapUtils.getString(s, "name", "");
            String ptime = MapUtils.getString(s, "time_public", "");
            List<Map> singers = (List<Map>) s.get("singer");
            String singername = "";
            for(Map singer : singers){
                String t = MapUtils.getString(singer, "name", "");
                singername = singername.equals("") ? "" : "," + t;
            }
            int index = MapUtils.getIntValue(s, "index_album", 0);
            String albummid = MapUtils.getString(((Map)s.get("album")), "mid", "");
            String albumname = MapUtils.getString(((Map)s.get("album")), "name", "");

            Map file = MapUtils.getMap(s, "file", new HashMap());
            String type_128 = MapUtils.getString(file, "size_128", "");
            String type_320 = MapUtils.getString(file, "size_320", "");
            String type_aac = MapUtils.getString(file, "size_aac", "");
            String type_ape = MapUtils.getString(file, "size_ape", "");
            String type_flac = MapUtils.getString(file, "size_flac", "");

            Song song = new Song();
            song.setMid(mid);
            song.setName(name);
            song.setIndex(index);
            song.setPublicTime(ptime);
            song.setSingerName(singername);
            song.setAlbummid(albummid);
            song.setAlbumName(albumname);
            song.setType_128(type_128);
            song.setType_320(type_320);
            song.setType_aac(type_aac);
            song.setType_ape(type_ape);
            song.setType_flac(type_flac);
            res.add(song);
        }
        return res;
    }


    public static File downloadMusic(){



        return null;
    }

}
