// Controller.java
package com.fbi.criminal_justice_system.controllers;

import com.fbi.criminal_justice_system.utils.ApiException;
import com.fbi.criminal_justice_system.utils.Mensaje;
import javafx.scene.control.Control;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

public abstract class Controller {

	protected Stage stage;
	protected final Mensaje mensaje = new Mensaje();

	private String accion;
	private String nombreVista;

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

	/**
	 * Texto para mostrarle al usuario ante un fallo de la API. Distingue el
	 * servidor caído de un error de negocio, que es la confusión más común
	 * trabajando con el WS en local.
	 */
	protected String describeError(Throwable error) {
		if (error instanceof ApiException apiException) {
			if (apiException.isConnectionProblem()) {
				return "No se pudo conectar con el servidor del FBI.\nVerificá que Payara esté corriendo.";
			}
			return apiException.getDisplayMessage();
		}
		return "Error inesperado: " + error.getMessage();
	}

	public abstract void initialize();
}
