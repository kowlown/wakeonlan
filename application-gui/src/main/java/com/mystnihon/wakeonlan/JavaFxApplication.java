package com.mystnihon.wakeonlan;

import com.mystnihon.wakeonlan.controller.MainController;
import com.mystnihon.wakeonlan.utils.IconUtil;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.ResourceBundle;

public class JavaFxApplication extends Application {
    private ConfigurableApplicationContext springContext;

    @Override
    public void init() throws Exception {
        super.init();
        springContext = new SpringApplicationBuilder(WakeOnLanGui.class)
                .headless(false).run();
        springContext.getAutowireCapableBeanFactory().autowireBean(this);
    }

    @Override
    public void start(Stage primaryStage) {
        MainController mainController = springContext.getBean(MainController.class);
        IconUtil.setIcon(primaryStage);
        primaryStage.setOnCloseRequest(event -> stop());
        Scene scene = new Scene(mainController);
        primaryStage.setScene(scene);
        primaryStage.setTitle(ResourceBundle.getBundle("strings").getString("title.application"));
        primaryStage.show();
        primaryStage.setOnShown(event -> {

        });
    }

    @Override
    public void stop() {
        springContext.close();
        Platform.exit();
    }
}
