package com.mystnihon.wakeonlan.utils;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.control.Dialog;
import javafx.stage.Stage;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import net.sf.image4j.codec.ico.ICODecoder;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@UtilityClass
public class IconUtil {
    public static void setIcon(Stage stage) {
        IconUtil.getIcons().map(bufferedImages -> bufferedImages.stream().map(bufferedImage -> SwingFXUtils.toFXImage(bufferedImage, null)).collect(Collectors.toList()))
                .ifPresent(writableImages -> stage.getIcons().addAll(writableImages));
    }

    public static void setIcon(Dialog<?> dialog) {
        Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
        setIcon(stage);
    }

    private static Optional<List<BufferedImage>> getIcons() {
        try {
            return Optional.of(ICODecoder.read(IconUtil.class.getClassLoader().getResourceAsStream("wakeonlan.ico")));
        } catch (IOException e) {
            log.error("Error when getting icons", e);
            return Optional.empty();
        }
    }
}
