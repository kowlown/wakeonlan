package com.mystnihon.wakeonlan.controller.listcells;

import com.mystnihon.wakeonlan.data.collections.WakeupHistoryCollection;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.net.URL;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.ResourceBundle;

@Slf4j
public class HistoryEntryController implements Initializable {
    @FXML
    private Label ipAddress;
    @FXML
    private Label macAddress;
    @FXML
    private Label date;
    private HistoryEntryCellActionListener historyEntryCellActionListener;
    private WakeupHistoryCollection item;

    public void setValue(WakeupHistoryCollection item) {
        this.item = item;
        String ipInfo = item.getHost().concat(" : ").concat(String.valueOf(item.getPort()));

        if (StringUtils.isNotBlank(item.getLabel())) {
            ipAddress.setText(item.getLabel() + " (" + ipInfo + ")");
        } else {
            ipAddress.setText(ipInfo);
        }
        macAddress.setText(item.getMacAddress());
        date.setText(item.getOffsetDateTime().atZoneSameInstant(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("EEE dd MM yyyy HH:mm")));
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ipAddress.setText(null);
        macAddress.setText(null);
        date.setText(null);
    }

    public void setListener(HistoryEntryCellActionListener listener) {
        this.historyEntryCellActionListener = listener;
    }

    @FXML
    protected void handleDeleteEntry(ActionEvent ignored) {
        log.info("Clickouille!");
        Optional.ofNullable(this.historyEntryCellActionListener).ifPresentOrElse(actionListener -> actionListener.onDelete(item), () -> log.error("Listener non présent"));
    }

}
