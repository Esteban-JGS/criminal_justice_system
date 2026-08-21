// UserController.java
package com.fbi.criminal_justice_system.controllers;

import com.fbi.cjs.shared.dto.UserDTO;
import com.fbi.cjs.shared.enums.Role;
import com.fbi.criminal_justice_system.services.UserService;
import com.fbi.criminal_justice_system.utils.AppContext;
import com.fbi.criminal_justice_system.utils.BackgroundTask;
import com.fbi.criminal_justice_system.utils.FlowController;
import com.fbi.criminal_justice_system.utils.Session;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

/**
 * Administración de usuarios.
 *
 * <p>
 * Consultar es de supervisores y jefes; crear, editar y eliminar, solo del
 * jefe. Los botones se deshabilitan según el rol, pero quien manda es el WS:
 * responde 403 igual aunque alguien llame la API por fuera de esta pantalla.
 */
public class UserController extends Controller {

	@FXML
	private TableView<UserDTO> tableUsers;
	@FXML
	private TableColumn<UserDTO, Long> colUserId;
	@FXML
	private TableColumn<UserDTO, String> colUserName;
	@FXML
	private TableColumn<UserDTO, String> colUsername;
	@FXML
	private TableColumn<UserDTO, String> colUserRole;
	@FXML
	private TableColumn<UserDTO, String> colUserActive;
	@FXML
	private Label lblPermissionHint;
	@FXML
	private Button btnAddUser;
	@FXML
	private Button btnEditUser;
	@FXML
	private Button btnDeleteUser;
	@FXML
	private Button btnRefreshUser;

	private final UserService userService = new UserService();
	private final ObservableList<UserDTO> users = FXCollections.observableArrayList();

	@Override
	public void initialize() {
		colUserId.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue().getId()));
		colUserName.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getName()));
		colUsername.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getUsername()));
		colUserRole.setCellValueFactory(cell -> new SimpleStringProperty(
				cell.getValue().getRole() == null ? "" : cell.getValue().getRole().getLabel()));
		colUserActive.setCellValueFactory(cell -> new SimpleStringProperty(
				Boolean.FALSE.equals(cell.getValue().getActive()) ? "Inactivo" : "Activo"));

		tableUsers.setItems(users);
	}

	@Override
	public void onViewShown() {
		configurePermissions();
		loadData();
	}

	@Override
	public String getNombreVista() {
		return "Usuarios";
	}

	private void configurePermissions() {
		boolean canManage = Session.hasAnyRole(Role.JEFE_FBI);

		btnAddUser.setDisable(!canManage);
		btnEditUser.setDisable(!canManage);
		btnDeleteUser.setDisable(!canManage);

		lblPermissionHint.setText(canManage
				? "Podés crear, editar y eliminar usuarios del sistema."
				: "Tu rol permite consultar la lista, pero no modificarla.");
	}

	private void loadData() {
		tableUsers.setPlaceholder(new Label("Consultando el sistema..."));
		btnRefreshUser.setDisable(true);

		BackgroundTask.run(userService::getAll, result -> {
			users.setAll(result);
			tableUsers.setPlaceholder(new Label("No hay usuarios registrados."));
			btnRefreshUser.setDisable(false);
		}, error -> {
			btnRefreshUser.setDisable(false);
			if (handleExpiredSession(error)) {
				return;
			}
			users.clear();
			tableUsers.setPlaceholder(new Label(describeError(error)));
		});
	}

	@FXML
	private void onActionBtnRefreshUser(ActionEvent event) {
		loadData();
	}

	@FXML
	private void onActionBtnAddUser(ActionEvent event) {
		openForm(null);
	}

	@FXML
	private void onActionBtnEditUser(ActionEvent event) {
		UserDTO selected = requireSelection("Editar usuario");
		if (selected != null) {
			openForm(selected);
		}
	}

	@FXML
	private void onActionBtnDeleteUser(ActionEvent event) {
		UserDTO selected = requireSelection("Eliminar usuario");
		if (selected == null) {
			return;
		}

		// El WS también lo rechaza; acá se evita el viaje y se explica mejor.
		UserDTO current = Session.getUser();
		if (current != null && current.getId().equals(selected.getId())) {
			mensaje.showModal(AlertType.WARNING, "Eliminar usuario", stage,
					"No podés eliminar el usuario con el que estás conectado.");
			return;
		}

		boolean confirmed = mensaje.showConfirmation("Eliminar usuario", stage,
				"¿Eliminar a " + selected.getName() + " (" + selected.getUsername() + ")?");
		if (!confirmed) {
			return;
		}

		BackgroundTask.run(() -> {
			userService.delete(selected.getId());
			return null;
		}, ignored -> {
			mensaje.show(AlertType.INFORMATION, "Eliminar usuario", "Usuario eliminado correctamente.");
			loadData();
		}, error -> {
			if (!handleExpiredSession(error)) {
				mensaje.showModal(AlertType.ERROR, "Eliminar usuario", stage, describeError(error));
			}
		});
	}

	private UserDTO requireSelection(String titulo) {
		UserDTO selected = tableUsers.getSelectionModel().getSelectedItem();
		if (selected == null) {
			mensaje.showModal(AlertType.WARNING, titulo, stage, "Seleccioná primero un usuario de la tabla.");
		}
		return selected;
	}

	private void openForm(UserDTO user) {
		AppContext.getInstance().set(UserFormController.CONTEXT_SELECTED, user);
		FlowController.getInstance().goViewInWindowModal("UserFormView", stage, false);

		if (Boolean.TRUE.equals(AppContext.getInstance().get(UserFormController.CONTEXT_SAVED))) {
			loadData();
		}
		AppContext.getInstance().delete(UserFormController.CONTEXT_SELECTED);
	}
}
