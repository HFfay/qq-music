package fay.betacat.dev.qqmusic.controller;

import fay.betacat.dev.qqmusic.dto.Song;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Pagination;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

import java.util.LinkedList;
import java.util.List;


public class MusicListController {

    @FXML
    private TextField name;

    @FXML
    private TableView<Song> tableView;

    @FXML
    private Pagination page;



    @FXML
    public void search(ActionEvent event) {

        String text = name.getText();
        if (text != null && !text.trim().equalsIgnoreCase("")){
//            tableView.setItems(null);

            int pageindex = page.getCurrentPageIndex();
            int pagesize = 20;

            List<Song> list = new LinkedList<>();
            for(int i = 1 + pageindex*pagesize; i <= (pageindex+1)*pagesize && i<=35; i++){
                Song song = new Song();
                song.setName(text + "_No." + i);
                song.setIndex("1");
                list.add(song);
            }

            ObservableList<Song> data = FXCollections.observableArrayList(list);
            tableView.setItems(data);
            page.setPageCount(35);
        }

    }



}
