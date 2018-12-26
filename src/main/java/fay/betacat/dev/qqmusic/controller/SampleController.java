package fay.betacat.dev.qqmusic.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class SampleController {

    @FXML
    public void handlerBtnClick(ActionEvent event) {
        Button btnSource = (Button) event.getSource();
        btnSource.setText("I am clicked!");
    }


    @FXML
    public void search(ActionEvent event) {
        Button btnSource = (Button) event.getSource();
        btnSource.setText("I am clicked!");
    }
}
