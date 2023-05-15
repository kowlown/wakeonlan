package com.mystnihon.wakeonlan.controller;

import com.mystnihon.wakeonlan.PropertyComponent;
import com.mystnihon.wakeonlan.WakeOnLan;
import com.mystnihon.wakeonlan.annotations.SceneController;
import com.mystnihon.wakeonlan.controller.listcells.HistoryEntryCell;
import com.mystnihon.wakeonlan.controller.listcells.HistoryEntryCellActionListener;
import com.mystnihon.wakeonlan.data.collections.WakeupHistoryCollection;
import com.mystnihon.wakeonlan.service.HistoryService;
import com.mystnihon.wakeonlan.utils.IconUtil;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionModel;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import lombok.extern.slf4j.Slf4j;
import net.synedra.validatorfx.Validator;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.DomainValidator;
import org.apache.commons.validator.routines.InetAddressValidator;
import org.apache.commons.validator.routines.RegexValidator;
import org.controlsfx.control.StatusBar;

import java.io.IOException;
import java.net.URL;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Slf4j
@SceneController
public class MainController extends VBox implements Initializable, HistoryEntryCellActionListener {
    public static final String HOST = "hostname";
    private static final String PREF_KEY_HOST = "host";
    private static final String PREF_KEY_MAC = "mac";
    private static final String PREF_KEY_PORT = "port";
    private static final int DELAY_BEFORE_LABEL_CLEANING = 10;
    public static final String RESOURCE_FILENAME = "strings";
    private final Validator validator = new Validator();
    private final PropertyComponent propertyComponent;
    private final HistoryService historyService;
    private final WakeOnLan wakeOnLan;
    private final InetAddressValidator inetAddressValidator = InetAddressValidator.getInstance();
    private final DomainValidator domainValidator = DomainValidator.getInstance();
    private final RegexValidator macValidator = new RegexValidator("^([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})|([0-9a-fA-F]{4}\\\\.[0-9a-fA-F]{4}\\\\.[0-9a-fA-F]{4})$", false);
    private final ScheduledExecutorService executor;
    private final ObservableList<WakeupHistoryCollection> observableList = FXCollections.observableArrayList();
    @FXML
    private ListView<WakeupHistoryCollection> listViewHistory;
    @FXML
    private TextField hostOrIpAddress;
    @FXML
    private TextField macAddress;
    @FXML
    private TextField port;
    private ScheduledFuture<?> scheduledFuture;
    @FXML
    private StatusBar labelStatus;

    public MainController(PropertyComponent propertyComponent, HistoryService historyService) {
        this.propertyComponent = propertyComponent;
        this.historyService = historyService;
        executor = Executors.newScheduledThreadPool(10);
        wakeOnLan = new WakeOnLan();
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/scenes/main.fxml"), ResourceBundle.getBundle(RESOURCE_FILENAME));
        fxmlLoader.setControllerFactory(param -> this);
        fxmlLoader.setRoot(this);
        try {
            fxmlLoader.load();
        } catch (IOException e) {
            log.error("Error", e);
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        validator.createCheck()
            .dependsOn(HOST, hostOrIpAddress.textProperty())
            .withMethod(c -> {
                String hostname = c.get(HOST);
                if (!(inetAddressValidator.isValid(hostname) || domainValidator.isValid(hostname))) {
                    c.error(getText("message.error.invalid.host"));
                }
            })
            .decorates(hostOrIpAddress)
            .immediate();
        validator.createCheck()
            .dependsOn("mac", macAddress.textProperty())
            .withMethod(c -> {
                String mac = c.get("mac");
                if (!(macValidator.isValid(mac))) {
                    c.error(getText("message.error.invalid.mac"));
                }
            })
            .decorates(macAddress)
            .immediate();
        readPreferenceValues();

        listViewHistory.setItems(observableList);
        listViewHistory.setCellFactory(param -> new HistoryEntryCell(this));
        refresh();
    }

    @Override
    public void onDelete(WakeupHistoryCollection wakeupHistoryCollection) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        IconUtil.setIcon(alert);
        alert.setHeaderText(null);
        alert.setContentText(ResourceBundle.getBundle(RESOURCE_FILENAME, Locale.getDefault()).getString("dialog.deletion"));
        alert.showAndWait().ifPresent(buttonType -> {
            if (buttonType.equals(ButtonType.OK)) {
                delete(wakeupHistoryCollection);
            }
        });

    }

    private void delete(WakeupHistoryCollection wakeupHistoryCollection) {
        log.info("Deleting this : {}", wakeupHistoryCollection);
        if (historyService.delete(wakeupHistoryCollection)) {
            refresh();
            listViewHistory.requestLayout();
            listViewHistory.autosize();
        } else {
            log.error("Deletion error");
        }
    }

    @FXML
    public void handleWake(ActionEvent ignored) {
        String hostOrIpAddressText = hostOrIpAddress.getText();
        String macAddressText = macAddress.getText();
        String portText = port.getText();

        if (validator.validate()) {
            int portIfBlank = defaultPortIfBlank(portText);

            if (wakeOnLan.sendPacket(hostOrIpAddressText, macAddressText, portIfBlank)) {
                savePreferenceValues(hostOrIpAddressText, macAddressText, portIfBlank);
                log.debug("Successfully sent packet");
                refresh();
                displayOnStatus(getText("message.packet.sent"));
            }

        } else {
            log.error("Blank host or mac");
        }
    }

    @FXML
    public void handleListHistorySelection(MouseEvent ignored) {
        Optional.ofNullable(listViewHistory.getSelectionModel()).map(SelectionModel::getSelectedItem).ifPresent(wakeupHistoryCollection -> {
            hostOrIpAddress.setText(wakeupHistoryCollection.getHost());
            macAddress.setText(wakeupHistoryCollection.getMacAddress());
            port.setText(String.valueOf(wakeupHistoryCollection.getPort()));
        });
    }

    private String getText(String key) {
        return ResourceBundle.getBundle(RESOURCE_FILENAME).getString(key);
    }

    private void refresh() {
        observableList.clear();
        observableList.addAll(historyService.getLastRecentWakeUp(10));
    }

    private void displayOnStatus(String text) {
        if (scheduledFuture != null) {
            scheduledFuture.cancel(true);
        }
        labelStatus.setText(text);
        scheduledFuture = executor.schedule(() -> Platform.runLater(() -> labelStatus.setText("")), DELAY_BEFORE_LABEL_CLEANING, TimeUnit.SECONDS);

    }

    private void savePreferenceValues(String hostOrIpAddressText, String macAddressTextext, int portIfBlank) {
        propertyComponent.setValueFor(PREF_KEY_HOST, hostOrIpAddressText);
        propertyComponent.setValueFor(PREF_KEY_MAC, macAddressTextext);
        propertyComponent.setValueFor(PREF_KEY_PORT, portIfBlank);
        historyService.insert(new WakeupHistoryCollection(hostOrIpAddressText, macAddressTextext, portIfBlank));
    }

    private void readPreferenceValues() {
        this.hostOrIpAddress.setText(propertyComponent.getValueFor(PREF_KEY_HOST).orElse(null));
        this.macAddress.setText(propertyComponent.getValueFor(PREF_KEY_MAC).orElse(null));
        this.port.setText(propertyComponent.getValueFor(PREF_KEY_PORT).orElse(null));
    }

    private int defaultPortIfBlank(String portText) {
        if (StringUtils.isNotBlank(portText)) {
            try {
                return Integer.parseInt(portText);
            } catch (NumberFormatException exception) {
                log.error("Conversion port error", exception);
            }
        }
        return 9;
    }
}
