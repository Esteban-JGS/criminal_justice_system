package com.fbi.criminal_justice_system.controllers;

import com.fbi.criminal_justice_system.utils.ApiException;
import com.fbi.criminal_justice_system.utils.FlowController;
import com.fbi.criminal_justice_system.utils.Mensaje;
import com.fbi.criminal_justice_system.utils.Session;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Control;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import javafx.stage.Window;

public abstract class Controller {

	protected Stage stage;
	protected final Mensaje mensaje = new Mensaje();

	private String accion;
	private String nombreVista;

	public void initialize() {
	}

	public void onViewShown() {
	}

	public String getAccion() {
		return accion;
	}

	public void setAccion(String accion) {
		this.accion = accion;
	}

	public void setStage(Stage stage) {
		this.stage = stage;
	}

	public Stage getStage() {
		return stage;
	}

	public String getNombreVista() {
		return nombreVista;
	}

	public void setNombreVista(String nombreVista) {
		this.nombreVista = nombreVista;
	}

	public void sendTabEvent(KeyEvent event) {
		event.consume();
		KeyEvent keyEvent = new KeyEvent(KeyEvent.KEY_PRESSED, null, null, KeyCode.TAB, false, false, false, false);
		((Control) event.getSource()).fireEvent(keyEvent);
	}

	protected String describeError(Throwable error) {
		if (error instanceof ApiException apiException) {
			if (apiException.isConnectionProblem()) {
				return "No se pudo conectar con el servidor del FBI.\nVerificá que Payara esté corriendo.";
			}
			return apiException.getDisplayMessage();
		}
		return "Error inesperado: " + error.getMessage();
	}

	protected boolean isUnauthorized(Throwable error) {
		return error instanceof ApiException apiException && apiException.getStatus() == 401;
	}

	protected boolean handleExpiredSession(Throwable error) {
		if (!isUnauthorized(error)) {
			return false;
		}

		Session.clear();

		Window owner = stage != null && stage.isShowing() ? stage : null;
		mensaje.showModal(AlertType.WARNING, "Sesión finalizada", owner,
				"Tu sesión venció o fue cerrada desde el servidor.\nIniciá sesión de nuevo.");

		FlowController.getInstance().goViewInWindow("LoginView");
		FlowController.getInstance().salir();
		return true;
	}
}
