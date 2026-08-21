// LoginController.java
package com.fbi.criminal_justice_system.controllers;

import com.fbi.cjs.shared.dto.UserDTO;
import com.fbi.criminal_justice_system.services.AuthService;
import com.fbi.criminal_justice_system.utils.ApiException;
import com.fbi.criminal_justice_system.utils.BackgroundTask;
import com.fbi.criminal_justice_system.utils.FlowController;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;

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
	private Button btnHepl;
	@FXML
	private Button btnEnter;
	@FXML
	private Label lblError;
	@FXML
	private ImageView imgFbiImage;

	private final AuthService authService = new AuthService();

	@Override
	public void initialize() {
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
			openDashboard(user);
		}, error -> {
			setLoading(false);
			lblError.setText(describe(error));
		});
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

	/**
	 * Traduce el error a algo que el usuario entienda: no es lo mismo "clave
	 * incorrecta" que "el servidor está apagado", y el segundo caso es el más común
	 * al trabajar con un WS local.
	 */
	private String describe(Throwable error) {
		if (error instanceof ApiException apiException) {
			if (apiException.isConnectionProblem()) {
				return "No se pudo conectar con el servidor del FBI.\nVerificá que Payara esté corriendo.";
			}
			if (apiException.getStatus() == 401) {
				return "Usuario o contraseña incorrectos.";
			}
			return apiException.getDisplayMessage();
		}
		return "Error inesperado: " + error.getMessage();
	}

	private void openDashboard(UserDTO user) {
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
