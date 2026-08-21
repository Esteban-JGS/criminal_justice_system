package com.fbi.criminal_justice_system.utils;

import java.util.Optional;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.stage.Window;

/**
 * Diálogos de la aplicación.
 *
 * <p>
 * Las versiones modales bloquean hasta que el usuario responde, así que solo
 * sirven para avisos y confirmaciones; nunca para esperar al servidor.
 */
public class Mensaje {

	public void show(AlertType tipo, String titulo, String mensaje) {
		Alert alert = new Alert(tipo);
		alert.setTitle(titulo);
		alert.setHeaderText(null);
		alert.setContentText(mensaje);
		alert.show();
	}

	public void showModal(AlertType tipo, String titulo, Window padre, String mensaje) {
		Alert alert = new Alert(tipo);
		alert.setTitle(titulo);
		alert.setHeaderText(null);
		alert.initOwner(padre);
		alert.setContentText(mensaje);
		alert.showAndWait();
	}

	public boolean showConfirmation(String titulo, Window padre, String mensaje) {
		Alert alert = new Alert(AlertType.CONFIRMATION);
		alert.setTitle(titulo);
		alert.setHeaderText(null);
		alert.initOwner(padre);
		alert.setContentText(mensaje);

		Optional<ButtonType> result = alert.showAndWait();
		return result.isPresent() && result.get() == ButtonType.OK;
	}
}
