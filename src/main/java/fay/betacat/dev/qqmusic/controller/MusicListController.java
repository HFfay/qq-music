package fay.betacat.dev.qqmusic.controller;

import com.google.gson.Gson;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import fay.betacat.dev.qqmusic.dto.Song;
import fay.betacat.dev.qqmusic.utils.QQMusicUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Pagination;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import org.apache.commons.collections.MapUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.*;


public class MusicListController implements Initializable {

    @FXML
    private TextField name;

    @FXML
    private TableView<Song> tableView;

    @FXML
    private Pagination page;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

//        page.setPageFactory(new Callback<Integer, Node>() {
//            @Override
//            public Node call(Integer param) {
//
//                search(null);
//
//                return null;
//            }
//
//        });
    }


    @FXML
    public void search(ActionEvent event) {

        String text = name.getText();
        if (text != null && !text.trim().equalsIgnoreCase("")){
//            tableView.setItems(null);

            int pageindex = page.getCurrentPageIndex();
            int pagesize = 20;
            int sum = 35;

//            List<Song> list = new LinkedList<>();
//            for(int i = 1 + pageindex*pagesize; i <= (pageindex+1)*pagesize && i<=sum; i++){
//                Song song = new Song();
//                song.setName(text + "_No." + i);
//                song.setIndex("1");
//                list.add(song);
//            }

//            ObservableList<Song> data = FXCollections.observableArrayList(list);
//            tableView.setItems(data);
//            page.setPageCount((sum / pagesize) + 1);
//            page.setMaxPageIndicatorCount((sum / pagesize) + 1);

            searchMusic(text, pagesize, pageindex);
        }

    }


    /**
     * 分页查询歌曲
     *
     * @param name          查询内容
     * @param pagesize      每页歌曲数量
     * @param pageindex       第几页
     * @return
     */
    public void searchMusic(String name, int pagesize, int pageindex){

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
//                flag[0] = true;
            }

            @Override
            public void onResponse(Response response) throws IOException {
                try {
                    if (response.isSuccessful()) {
                        String data = response.body().string();

                        dealSongs(data);
                    }
                } catch (Exception e){
                    e.printStackTrace();
                } finally {
//                    flag[0] = true;
                }
            }
        });

    }



    private void dealSongs(String str){
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

        tableView.setItems(FXCollections.observableArrayList(res));
        page.setPageCount((totalnum / curnum) + 1);

    }

}
