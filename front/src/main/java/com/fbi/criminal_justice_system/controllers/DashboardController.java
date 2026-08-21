// DashboardController.java
package com.fbi.criminal_justice_system.controllers;

import com.fbi.cjs.shared.dto.UserDTO;
import com.fbi.cjs.shared.enums.Role;
import com.fbi.criminal_justice_system.services.AuthService;
import com.fbi.criminal_justice_system.utils.BackgroundTask;
import com.fbi.criminal_justice_system.utils.FlowController;
import com.fbi.criminal_justice_system.utils.Session;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 * Pantalla principal.
 *
 * <p>
 * Toma el usuario de {@link Session}. Antes dependía de que alguien llamara
 * {@code initData(user)}, pero {@code FlowController.goMain()} carga el FXML
 * sin pasar por el controlador, así que los labels se quedaban con el texto de
 * ejemplo.
 */
public class DashboardController extends Controller {

	@FXML
	private ImageView imgFbiImage;
	@FXML
	private Label lblAgent;
	@FXML
	private Label lblRol;
	@FXML
	private Label lblWelcome;
	@FXML
	private Button btnCriminals;
	@FXML
	private Button btnSearch;
	@FXML
	private Button btnLogOut;

	private final AuthService authService = new AuthService();

	/** JavaFX llama este método al cargar el FXML, con los @FXML ya inyectados. */
	@Override
	public void initialize() {
		imgFbiImage.setImage(new Image(
				getClass().getResource("/com/fbi/criminal_justice_system/images/FBI_icon.png").toExternalForm()));

		UserDTO user = Session.getUser();
		if (user == null) {
			// Defensa por si se abre el dashboard sin sesión (no debería pasar).
			lblAgent.setText("Agente: —");
			lblRol.setText("Sin sesión");
			lblWelcome.setText("No hay una sesión activa. Volvé a iniciar sesión.");
			return;
		}

		lblAgent.setText("Agente: " + user.getName());
		lblRol.setText(user.getRole() == null ? "" : user.getRole().getLabel());
		lblWelcome.setText("Bienvenido al sistema, " + user.getName() + ".\nSeleccioná una opción del menú.");

		// La búsqueda avanzada queda para supervisores y jefes.
		btnSearch.setDisable(!Session.hasAnyRole(Role.SUPERVISOR, Role.JEFE_FBI));
	}

	@Override
	public String getNombreVista() {
		return "Home - FBI System";
	}

	@FXML
	private void onActionBtnCriminals(ActionEvent event) {
		FlowController.getInstance().goView("CriminalView");
	}

	@FXML
	private void onActionBtnSearch(ActionEvent event) {
		// Pendiente: SearchView todavía no existe.
		lblWelcome.setText("La búsqueda avanzada se implementa en la próxima entrega.");
	}

	/**
	 * Cierra sesión.
	 *
	 * <p>
	 * Primero se abre el login y después se avisa al servidor: si el WS no
	 * responde, el usuario igual queda fuera de la aplicación.
	 */
	@FXML
	private void onActionBtnLogOut(ActionEvent event) {
		BackgroundTask.run(() -> {
			authService.logout();
			return null;
		}, ignored -> {
		}, error -> {
		});

		FlowController.getInstance().goViewInWindow("LoginView");
		FlowController.getInstance().salir();
	}
}
