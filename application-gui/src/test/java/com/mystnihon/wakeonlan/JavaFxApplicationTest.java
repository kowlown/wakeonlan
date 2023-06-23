package com.mystnihon.wakeonlan;

import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseButton;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testfx.framework.junit.ApplicationTest;
import org.testfx.util.WaitForAsyncUtils;

@ExtendWith(SpringExtension.class)
public class JavaFxApplicationTest extends ApplicationTest {
    @MockBean
    IWakeOnLan wakeOnLan;

    @BeforeAll
    public static void setUpClass() throws Exception {
        ApplicationTest.launch(JavaFxApplication.class);
//
    }

    @Test
    void canSelectSendForValidIp() {
        TextField textField = lookup("#hostOrIpAddress").query();
        Platform.runLater(()-> textField.setText("192.168.1.1"));
        WaitForAsyncUtils.waitForFxEvents();
        Button button = lookup("#btnValidation").queryButton();
        clickOn(button, MouseButton.PRIMARY);

        Assertions.assertThat(textField.getText()).isEqualTo("192.168.1.1");

    }
}
