package com.mystnihon.wakeonlan.controller.listcells;

import com.mystnihon.wakeonlan.data.collections.WakeupHistoryCollection;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.ListCell;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.ResourceBundle;

@Slf4j
public class HistoryEntryCell extends ListCell<WakeupHistoryCollection> {

    private HistoryEntryController rendererController;

    private Node renderer;

    public HistoryEntryCell(HistoryEntryCellActionListener actionListener) {
        super();
        // Chargement du FXML.
        try {
            final FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/scenes/listcells/history_entry.fxml"), ResourceBundle.getBundle("strings"));
            renderer = fxmlLoader.load();
            rendererController = fxmlLoader.getController();
            rendererController.setListener(actionListener);
        } catch (IOException ex) {
            log.error("Loading error", ex);
        }
        setText(null);
    }

    @Override
    protected void updateItem(WakeupHistoryCollection item, boolean empty) {
        super.updateItem(item, empty);
        setEditable(false);
        if (empty || item == null) {
            setGraphic(null);
        } else {
            rendererController.setValue(item);
            setGraphic(renderer);
        }
    }
}
