package org.example;

import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

import java.net.URL;

public class MyBrowser extends StackPane {
    HBox toolbar;

    WebView webView = new WebView();
    WebEngine webEngine = webView.getEngine();

    public MyBrowser(){

        final URL urlMaps = getClass().getResource("index.html");
        //webEngine.load(urlGoogleMaps.toExternalForm());
        webEngine.load(urlMaps.toString());

        getChildren().add(webView);

    }

}
