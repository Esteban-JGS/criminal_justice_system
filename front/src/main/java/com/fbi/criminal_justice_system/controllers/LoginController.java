// LoginController.java
package com.fbi.criminal_justice_system.controllers;

import com.fbi.criminal_justice_system.services.AuthService;
import com.fbi.criminal_justice_system.utils.ApiClient;
import com.fbi.criminal_justice_system.utils.BackgroundTask;
import com.fbi.criminal_justice_system.utils.FlowController;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

/**
 * Pantalla de login.
 *
 * <p>
 * Las credenciales las valida el web service. El front solo revisa que los
 * campos no estén vacíos, para no gastar una llamada de red en algo evidente.
 */
public class LoginController extends Controller {

	@FXML
	private TextField txtUsername;
	@FXML
	private PasswordField txtPassword;
	@FXML
	private Button btnHelp;
	@FXML
	private Button btnEnter;
	@FXML
	private Label lblError;

	private final AuthService authService = new AuthService();

	@Override
	public void onViewShown() {
		clearFields();
	}

	@Override
	public String getNombreVista() {
		return "Login - FBI System";
	}

	@FXML
	private void onActionBtnEnter(ActionEvent event) {
		String username = txtUsername.getText().trim();
		String password = txtPassword.getText().trim();

		if (username.isEmpty() || password.isEmpty()) {
			lblError.setText("Por favor completá todos los campos.");
			return;
		}

		setLoading(true);

		// El login viaja por red: si se hiciera en el hilo de JavaFX, la ventana se
		// quedaría congelada hasta que Payara conteste (o hasta que expire el timeout).
		BackgroundTask.run(() -> authService.login(username, password), user -> {
			setLoading(false);
			openDashboard();
		}, error -> {
			setLoading(false);
			lblError.setText(describeError(error));
		});
	}

	/**
	 * Ayuda para diagnosticar el problema más común: el servidor no está levantado
	 * o la URL apunta a otro lado.
	 */
	@FXML
	private void onActionBtnHelp(ActionEvent event) {
		mensaje.showModal(AlertType.INFORMATION, "Ayuda", stage,
				"Las credenciales las administra el jefe del departamento.\n\n"
						+ "Si el sistema no responde, verificá que el servidor esté activo.\n"
						+ "Servidor configurado: " + ApiClient.getInstance().getBaseUrl());
	}

	/**
	 * Mientras se consulta al servidor, el botón se bloquea para evitar dobles
	 * envíos.
	 */
	private void setLoading(boolean loading) {
		btnEnter.setDisable(loading);
		btnEnter.setText(loading ? "Verificando..." : "Entrar");
		if (loading) {
			lblError.setText("");
		}
	}

	private void openDashboard() {
		FlowController.getInstance().goMain();
		stage.close();
		clearFields();
	}

	private void clearFields() {
		txtUsername.clear();
		txtPassword.clear();
		lblError.setText("");
	}
}
