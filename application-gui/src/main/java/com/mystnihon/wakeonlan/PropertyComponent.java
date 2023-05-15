package com.mystnihon.wakeonlan;

import lombok.extern.slf4j.Slf4j;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Properties;
import java.util.function.Function;

@SuppressWarnings("unused")
@Slf4j
public class PropertyComponent {
    public static final String FILE_PROPERTIES = "application.properties";
    public static final String COMMENTS = "Properties saved for application. Edit at your own risk";
    private final String fileName;
    private Properties properties;

    public PropertyComponent() {
        this(FILE_PROPERTIES);
    }

    public PropertyComponent(String fileName) {
        this.fileName = fileName;
    }

    public Optional<File> getLastFileFor(String key) {
        open(false);
        return Optional.ofNullable(properties.getProperty(key)).map(File::new);
    }

    public void setLastFileFor(String keyImage, File file) {
        open(true);
        properties.put(keyImage, file.getAbsolutePath());
        write();

    }

    public Optional<String> getValueFor(String key) {
        open(false);
        return Optional.ofNullable(properties.getProperty(key));
    }

    public <U> Optional<U> getValueFor(String key, Function<String, U> converter) {
        open(false);
        return Optional.ofNullable(properties.getProperty(key)).map(converter);
    }

    public <T> void setValueFor(String key, T value) {
        open(true);
        properties.put(key, String.valueOf(value));
        write();
    }

    @PostConstruct
    public void init() {
        open(false);
    }

    @PreDestroy
    public void terminate() {
        write();
    }

    public void open(boolean force) {
        if (properties == null || force) {
            properties = new Properties();
            if (Files.exists(Path.of(fileName))) {
                try (FileInputStream fileInputStream = new FileInputStream(fileName)) {
                    properties.load(fileInputStream);
                } catch (IOException e) {
                    log.warn("Could not read properties", e);
                }
            }
        }
    }

    private void write() {
        try (FileOutputStream fileOutputStream = new FileOutputStream(fileName)) {
            properties.store(fileOutputStream, PropertyComponent.COMMENTS);
        } catch (IOException e) {
            log.error("IO read/write properties", e);
        }
    }
}
