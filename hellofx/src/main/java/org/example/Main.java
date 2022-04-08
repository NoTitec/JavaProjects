package org.example;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import com.lynden.gmapsfx.MainApp;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebEvent;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

/**
 *
 * @web http://java-buddy.blogspot.com/
 */
public class Main extends Application {

    private Scene scene;//지도 scene
    MyBrowser myBrowser;//지도 class 생성자


    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("국토지도");

        myBrowser = new MyBrowser();
        myBrowser.setPrefSize(1800,1200);
        myBrowser.webEngine.setJavaScriptEnabled(true);//javascript true 추가
        myBrowser.prefHeightProperty().bind(primaryStage.heightProperty());
        myBrowser.prefWidthProperty().bind(primaryStage.widthProperty());
        scene = new Scene(myBrowser, 1200, 800);

        primaryStage.setScene(scene);
        primaryStage.show();
    }


}