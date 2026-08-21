package com.fbi.criminal_justice_system.controllers;

import com.fbi.cjs.shared.dto.RoleDTO;
import com.fbi.cjs.shared.dto.UserDTO;
import com.fbi.criminal_justice_system.services.UserService;
import com.fbi.criminal_justice_system.utils.AppContext;
import com.fbi.criminal_justice_system.utils.BackgroundTask;
import com.fbi.criminal_justice_system.utils.ComboBoxUtils;
import java.util.List;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class UserFormController extends Controller {

	public static final String CONTEXT_SELECTED = "user.selected";

	public static final String CONTEXT_SAVED = "user.saved";

	@FXML
	private Label lblFormTitle;
	@FXML
	private Label lblFormHint;
	@FXML
	private Label lblPassword;
	@FXML
	private TextField txtName;
	@FXML
	private TextField txtUsername;
	@FXML
	private PasswordField txtPassword;
	@FXML
	private ComboBox<RoleDTO> cmbRole;
	@FXML
	private CheckBox chkActive;
	@FXML
	private Label lblError;
	@FXML
	private Button btnSave;
	@FXML
	private Button btnCancel;

	private final UserService userService = new UserService();

	private UserDTO editing;

	@Override
	public void onViewShown() {
		loadSelection();
		loadRoles();
	}

	@Override
	public String getNombreVista() {
		return editing == null ? "Agregar Usuario" : "Editar Usuario";
	}

	private void loadSelection() {
		editing = (UserDTO) AppContext.getInstance().get(CONTEXT_SELECTED);
		AppContext.getInstance().delete(CONTEXT_SAVED);

		lblError.setText("");
		txtPassword.clear();
		setLoading(false);

		if (editing == null) {
			lblFormTitle.setText("Agregar Usuario");
			lblFormHint.setText("Los campos marcados con * son obligatorios.");
			lblPassword.setText("CONTRASEÑA *");
			txtName.clear();
			txtUsername.clear();
			cmbRole.setValue(null);
			chkActive.setSelected(true);
		} else {
			lblFormTitle.setText("Editar Usuario");
			lblFormHint.setText("Dejá la contraseña vacía para conservar la actual.");
			lblPassword.setText("CONTRASEÑA");
			txtName.setText(editing.getName());
			txtUsername.setText(editing.getUsername());
			chkActive.setSelected(!Boolean.FALSE.equals(editing.getActive()));
		}

		txtName.requestFocus();
	}

	private void loadRoles() {
		if (!cmbRole.getItems().isEmpty()) {
			selectCurrentRole(cmbRole.getItems());
			return;
		}

		btnSave.setDisable(true);

		BackgroundTask.run(userService::getRoles, roles -> {
			ComboBoxUtils.configure(cmbRole, roles, RoleDTO::getLabel);
			selectCurrentRole(roles);
			btnSave.setDisable(false);
		}, error -> lblError.setText("No se pudo cargar el catálogo de roles.\n" + describeError(error)));
	}

	private void selectCurrentRole(List<? extends RoleDTO> roles) {
		if (editing == null || editing.getRole() == null) {
			return;
		}
		roles.stream().filter(role -> role.getValue() == editing.getRole()).findFirst().ifPresent(cmbRole::setValue);
	}

	@FXML
	private void onActionBtnSave(ActionEvent event) {
		String error = validate();
		if (error != null) {
			lblError.setText(error);
			return;
		}

		UserDTO user = new UserDTO(editing == null ? null : editing.getId(), txtName.getText().trim(),
				txtUsername.getText().trim(), passwordOrNull(), cmbRole.getValue().getValue(), chkActive.isSelected());

		setLoading(true);

		BackgroundTask.run(() -> editing == null ? userService.create(user) : userService.update(editing.getId(), user),
				saved -> {
					AppContext.getInstance().set(CONTEXT_SAVED, Boolean.TRUE);
					stage.close();
				}, failure -> {
					setLoading(false);
					if (isUnauthorized(failure)) {
						stage.close();
						handleExpiredSession(failure);
						return;
					}
					lblError.setText(describeError(failure));
				});
	}

	@FXML
	private void onActionBtnCancel(ActionEvent event) {
		stage.close();
	}

	private String passwordOrNull() {
		String password = txtPassword.getText();
		return password == null || password.isBlank() ? null : password;
	}

	private String validate() {
		if (txtName.getText() == null || txtName.getText().isBlank()) {
			return "El nombre es obligatorio.";
		}
		String username = txtUsername.getText() == null ? "" : txtUsername.getText().trim();
		if (username.length() < 4 || username.length() > 40) {
			return "El nombre de usuario debe tener entre 4 y 40 caracteres.";
		}
		if (cmbRole.getValue() == null) {
			return "Seleccioná un rol.";
		}
		String password = txtPassword.getText();
		if (editing == null && (password == null || password.length() < 4)) {
			return "La contraseña es obligatoria y debe tener al menos 4 caracteres.";
		}
		if (editing != null && password != null && !password.isBlank() && password.length() < 4) {
			return "La contraseña debe tener al menos 4 caracteres.";
		}
		return null;
	}

	private void setLoading(boolean loading) {
		btnSave.setDisable(loading);
		btnCancel.setDisable(loading);
		btnSave.setText(loading ? "Guardando..." : "Guardar");
		if (loading) {
			lblError.setText("");
		}
	}
}
