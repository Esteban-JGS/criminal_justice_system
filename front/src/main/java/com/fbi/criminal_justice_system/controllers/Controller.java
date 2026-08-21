// Controller.java
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

/**
 * Base de los controladores, con el ciclo de vida de una vista.
 *
 * <p>
 * Hay dos momentos distintos y conviene no confundirlos:
 * <ul>
 * <li>{@link #initialize()} lo llama JavaFX <b>una sola vez</b>, al cargar el
 * FXML. Va la configuración que no cambia: columnas, listeners, permisos.
 * <li>{@link #onViewShown()} lo llama el {@code FlowController} <b>cada vez</b>
 * que la vista se muestra. Va lo que hay que refrescar: consultas al servidor,
 * limpiar el formulario.
 * </ul>
 *
 * <p>
 * Antes todo vivía en {@code initialize()}, y como JavaFX ya lo invoca al
 * cargar el FXML, la primera apertura de cada pantalla disparaba la consulta
 * dos veces.
 */
public abstract class Controller {

	protected Stage stage;
	protected final Mensaje mensaje = new Mensaje();

	private String accion;
	private String nombreVista;

	/** Configuración de una sola vez. La invoca JavaFX al cargar el FXML. */
	public void initialize() {
	}

	/**
	 * Refresco de cada vez que se muestra la vista. Lo invoca el FlowController.
	 */
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

	/** {@code true} si el WS rechazó el token (401). */
	protected boolean isUnauthorized(Throwable error) {
		return error instanceof ApiException apiException && apiException.getStatus() == 401;
	}

	/**
	 * Ante un 401 la sesión venció o el token fue revocado: se limpia y se devuelve
	 * al usuario al login. Sin esto la aplicación quedaba en una sesión zombi,
	 * mostrando errores en cada pantalla hasta que el usuario la cerrara.
	 *
	 * @return {@code true} si ya se manejó y quien llama no debe hacer nada más
	 */
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
