/*
 * Copyright (C) 2018 Waldemar Sojka, All Rights Reserved
 */
package com.wsojka.hammerui.ui;

import javafx.application.Preloader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class UIPreloader extends Preloader {

    private ProgressBar bar;

    private Stage stage;

    private Scene createPreloaderScene() {
        bar = new ProgressBar();
        bar.setMinWidth(600);
        bar.setMaxHeight(2);
        VBox vb = new VBox();
        vb.setMaxWidth(Double.MAX_VALUE);
        vb.setMaxHeight(Double.MAX_VALUE);
        vb.setAlignment(Pos.BOTTOM_CENTER);
        vb.setPadding(new Insets(0, 0, 50, 0));
        vb.getChildren().add(bar);
        BorderPane p = new BorderPane();
        p.setCenter(vb);
        Scene scene = new Scene(p, 600, 365);
        Image img = new Image(this.getClass().getResource("/css/background.png").toExternalForm(), 600, 365, true, true);
        BackgroundImage background = new BackgroundImage(
                img,
                BackgroundRepeat.REPEAT,
                BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.DEFAULT,
                BackgroundSize.DEFAULT);
        p.setBackground(new Background(background));
        return scene;
    }

    public void start(Stage stage) {
        this.stage = stage;
        stage.initStyle(StageStyle.UNDECORATED);
        stage.setScene(createPreloaderScene());
        stage.show();
    }

    @Override
    public void handleProgressNotification(ProgressNotification pn) {
        bar.setProgress(pn.getProgress());
    }

    @Override
    public void handleStateChangeNotification(StateChangeNotification evt) {
        if (evt.getType() == Preloader.StateChangeNotification.Type.BEFORE_START) {
            stage.hide();
        }
    }
}
