package fay.betacat.dev.qqmusic.controller;

import com.google.gson.Gson;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import fay.betacat.dev.qqmusic.dto.Song;
import fay.betacat.dev.qqmusic.utils.DownloadUtils;
import fay.betacat.dev.qqmusic.utils.QQMusicUtils;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import org.apache.commons.collections.MapUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.*;


public class MusicListController implements Initializable {

    final String vkey = "A2BD417485219FA8339DA04A0F169E0761DAE657A01E72D079E2CEE018214692F0BE34D9BEFCB4DF71928769F809095A2D671015182F56D2";
    final String guid = "5931742855";

    private static final int PAGE_SIZE = 20;

    @FXML
    private TextField name;
    @FXML
    private TableView<Song> tableView;
    @FXML
    private Pagination page;
    @FXML
    private TableColumn<Song, String> idCol;


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {


        idCol.setCellFactory((col) -> {
            TableCell<Song, String> cell = new TableCell<Song, String>() {
                @Override
                public void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    this.setText(null);
                    this.setGraphic(null);

                    if (!empty) {
                        int pageindex = page.getCurrentPageIndex();
                        int rowIndex = this.getIndex() + 1 + (pageindex * PAGE_SIZE);
                        this.setText(String.valueOf(rowIndex));

                        CheckBox checkBox = new CheckBox();
                        Song song = this.getTableView().getItems().get(this.getIndex());
                        checkBox.setSelected(song.getSelected().get());
                        checkBox.selectedProperty().bindBidirectional(song.getSelected());
                        this.setGraphic(checkBox);
                        checkBox.selectedProperty().addListener((obVal, oldVal, newVal) -> {
                            if (newVal) {
                                // 添加选中时执行的代码
                                // 获取当前单元格的对象
                                Song t = this.getTableView().getItems().get(this.getIndex());
                                // this.getItem();
                                System.out.println("第" + this.getIndex() + "行被选中！: " + t.toString());
                            }

                        });
                    }
                }
            };
            return cell;
        });



        long t = System.currentTimeMillis();
        long guid = Math.round(2147483647 * Math.random()) * t % 10000000000L;
        System.out.println("guid = " + guid);

//        Song song = new Song();
//        song.setMid("000drj9r0TOUGx");
//        song.setName("超快感");
//        song.setIndex(1);
//        song.setPublicTime("2000-06-08");
//        song.setSingerName("孙燕姿");
//        song.setAlbummid("002UZ9ob4Ecg0S");
//        song.setAlbumName("孙燕姿 同名专辑");
//        song.setType_128("3531963");
//        song.setType_320("8829595");
//        song.setType_aac("5416054");
//        song.setType_ape("27488184");
//        song.setType_flac("27858174");
//
//        downloadMusic("320", song);
    }


    @FXML
    public void search(ActionEvent event) {

        String text = name.getText();
        if (text != null && !text.trim().equalsIgnoreCase("")){

            int pageindex = page.getCurrentPageIndex();

            searchMusic(text, PAGE_SIZE, pageindex);
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
                "&p=" + (pageindex + 1) +
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


    public void downloadMusic(String type, Song... songs){

        for(Song song : songs){
            String url = prepareDownUrl(type, song.getMid());
            DownloadUtils.download(url, song);
        }

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
                singername += singername.equals("") ? t : "," + t;
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

        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                tableView.setItems(FXCollections.observableArrayList(res));
                int pagecount = totalnum == 0 ? 1 : (totalnum / curnum) + (totalnum % curnum == 0 ? 0 : 1);
                page.setPageCount(pagecount);
            }
        });
    }

    private String prepareDownUrl(String fileType, String mid) {
        String url = "";

        switch (fileType) {
            case "flac": {
                url = "http://dl.stream.qqmusic.qq.com/F000" + mid + ".flac?vkey=" + vkey + "&guid=" + guid + "&uin=3051522991&fromtag=64";
            }
            break;
            case "ape": {
                url = "http://dl.stream.qqmusic.qq.com/A000" + mid + ".ape?vkey=" + vkey + "&guid=" + guid + "&uin=3051522991&fromtag=64";
            }
            break;
            case "aac": {
                url = "http://dl.stream.qqmusic.qq.com/C600" + mid + ".m4a?vkey=" + vkey + "&guid=" + guid + "&uin=3051522991&fromtag=64";
            }
            break;
            case "320": {
                url = "http://dl.stream.qqmusic.qq.com/M800" + mid + ".mp3?vkey=" + vkey + "&guid=" + guid + "&uin=3051522991&fromtag=64";
            }
            break;
            case "128": {
                url = "http://dl.stream.qqmusic.qq.com/M500" + mid + ".mp3?vkey=" + vkey + "&guid=" + guid + "&uin=3051522991&fromtag=64";
            }
            break;
        }
        return url;
    }

}
