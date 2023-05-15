package com.mystnihon.wakeonlan;

import org.springframework.boot.autoconfigure.SpringBootApplication;

import static javafx.application.Application.launch;

@SpringBootApplication
public class WakeOnLanGui {
    public static void main(String[] args) {
        launch(JavaFxApplication.class, args);
    }
}
