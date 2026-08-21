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
	private Button btnUsers;
	@FXML
	private Button btnLogOut;

	private final AuthService authService = new AuthService();

	@Override
	public void initialize() {
		imgFbiImage.setImage(new Image(
				getClass().getResource("/com/fbi/criminal_justice_system/images/FBI_icon.png").toExternalForm()));

		UserDTO user = Session.getUser();
		if (user == null) {
			lblAgent.setText("Agente: —");
			lblRol.setText("Sin sesión");
			lblWelcome.setText("No hay una sesión activa. Volvé a iniciar sesión.");
			btnUsers.setVisible(false);
			return;
		}

		lblAgent.setText("Agente: " + user.getName());
		lblRol.setText(user.getRole() == null ? "" : user.getRole().getLabel());
		lblWelcome.setText("Bienvenido al sistema, " + user.getName() + ".\nSeleccioná una opción del menú.");
		boolean canSeeUsers = Session.hasAnyRole(Role.SUPERVISOR, Role.JEFE_FBI);
		btnUsers.setVisible(canSeeUsers);
		btnUsers.setManaged(canSeeUsers);
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
		FlowController.getInstance().goView("SearchView");
	}

	@FXML
	private void onActionBtnUsers(ActionEvent event) {
		FlowController.getInstance().goView("UserView");
	}

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
