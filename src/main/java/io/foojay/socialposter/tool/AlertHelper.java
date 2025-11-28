package io.foojay.socialposter.tool;

import javafx.scene.control.Alert;

public class AlertHelper {
    private AlertHelper() {
        // Hide constructor
    }

    public static void showAlert(String title, String content) {
        var alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
