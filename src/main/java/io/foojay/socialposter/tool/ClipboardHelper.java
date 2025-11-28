package io.foojay.socialposter.tool;

import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;

public class ClipboardHelper {
    private ClipboardHelper() {
        // Hide constructor
    }

    public static void copyToClipboard(String text) {
        var clipboard = Clipboard.getSystemClipboard();
        var content = new ClipboardContent();
        content.putString(text);
        clipboard.setContent(content);
    }
}
