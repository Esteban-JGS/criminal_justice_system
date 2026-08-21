package com.fbi.criminal_justice_system.controllers;

import com.fbi.cjs.shared.dto.CriminalDTO;
import com.fbi.cjs.shared.enums.CriminalStatus;
import com.fbi.cjs.shared.enums.DangerLevel;
import com.fbi.criminal_justice_system.services.CriminalService;
import com.fbi.criminal_justice_system.utils.AppContext;
import com.fbi.criminal_justice_system.utils.BackgroundTask;
import com.fbi.criminal_justice_system.utils.ComboBoxUtils;
import java.util.List;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class CriminalFormController extends Controller {

	public static final String CONTEXT_SELECTED = "criminal.selected";

	public static final String CONTEXT_SAVED = "criminal.saved";

	@FXML
	private Label lblFormTitle;
	@FXML
	private TextField txtName;
	@FXML
	private TextField txtAlias;
	@FXML
	private TextField txtCrime;
	@FXML
	private ComboBox<DangerLevel> cmbDangerLevel;
	@FXML
	private ComboBox<CriminalStatus> cmbStatus;
	@FXML
	private Label lblError;
	@FXML
	private Button btnSave;
	@FXML
	private Button btnCancel;

	private final CriminalService criminalService = new CriminalService();

	private CriminalDTO editing;

	@Override
	public void initialize() {
		ComboBoxUtils.configure(cmbDangerLevel, List.of(DangerLevel.values()), DangerLevel::getLabel);
		ComboBoxUtils.configure(cmbStatus, List.of(CriminalStatus.values()), CriminalStatus::getLabel);
	}

	@Override
	public void onViewShown() {
		loadSelection();
	}

	@Override
	public String getNombreVista() {
		return editing == null ? "Registrar Criminal" : "Editar Criminal";
	}

	private void loadSelection() {
		editing = (CriminalDTO) AppContext.getInstance().get(CONTEXT_SELECTED);
		AppContext.getInstance().delete(CONTEXT_SAVED);

		lblError.setText("");
		setLoading(false);

		if (editing == null) {
			lblFormTitle.setText("Registrar Criminal");
			txtName.clear();
			txtAlias.clear();
			txtCrime.clear();
			cmbDangerLevel.setValue(null);
			cmbStatus.setValue(CriminalStatus.ACTIVO);
		} else {
			lblFormTitle.setText("Editar Criminal");
			txtName.setText(editing.getName());
			txtAlias.setText(editing.getAlias());
			txtCrime.setText(editing.getCrime());
			cmbDangerLevel.setValue(editing.getDangerLevel());
			cmbStatus.setValue(editing.getStatus());
		}

		txtName.requestFocus();
	}

	@FXML
	private void onActionBtnSave(ActionEvent event) {
		String error = validate();
		if (error != null) {
			lblError.setText(error);
			return;
		}

		CriminalDTO criminal = new CriminalDTO(editing == null ? null : editing.getId(), txtName.getText().trim(),
				txtAlias.getText().trim(), txtCrime.getText().trim(), cmbDangerLevel.getValue(), cmbStatus.getValue());

		setLoading(true);

		BackgroundTask.run(() -> editing == null
				? criminalService.create(criminal)
				: criminalService.update(editing.getId(), criminal), saved -> {
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

	private String validate() {
		if (txtName.getText() == null || txtName.getText().isBlank()) {
			return "El nombre es obligatorio.";
		}
		if (txtCrime.getText() == null || txtCrime.getText().isBlank()) {
			return "El delito es obligatorio.";
		}
		if (cmbDangerLevel.getValue() == null) {
			return "Seleccioná el nivel de peligrosidad.";
		}
		if (cmbStatus.getValue() == null) {
			return "Seleccioná el estatus.";
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
