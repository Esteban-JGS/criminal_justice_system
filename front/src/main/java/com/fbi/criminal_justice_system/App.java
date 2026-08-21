// App.java

package com.fbi.criminal_justice_system;

import com.fbi.criminal_justice_system.utils.FlowController;
import javafx.application.Application;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * JavaFX App
 */
public class App extends Application {

	@Override
	public void start(Stage stage) throws IOException {
		FlowController.getInstance().InitializeFlow(stage, null);
		FlowController.getInstance().goViewInWindow("LoginView");
		stage.setTitle("Login - FBI System");
	}

	public static void main(String[] args) {
		launch();
	}

}