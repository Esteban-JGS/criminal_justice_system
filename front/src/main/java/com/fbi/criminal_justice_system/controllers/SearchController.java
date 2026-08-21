package com.fbi.criminal_justice_system.controllers;

import com.fbi.cjs.shared.dto.CriminalDTO;
import com.fbi.cjs.shared.enums.CriminalStatus;
import com.fbi.cjs.shared.enums.DangerLevel;
import com.fbi.criminal_justice_system.services.CriminalService;
import com.fbi.criminal_justice_system.utils.BackgroundTask;
import com.fbi.criminal_justice_system.utils.ComboBoxUtils;
import com.fbi.criminal_justice_system.utils.CriminalTable;
import com.fbi.criminal_justice_system.utils.RequestGuard;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

public class SearchController extends Controller {

	@FXML
	private TextField txtSearchText;
	@FXML
	private ComboBox<CriminalStatus> cmbStatus;
	@FXML
	private ComboBox<DangerLevel> cmbDangerLevel;
	@FXML
	private Button btnSearch;
	@FXML
	private Button btnClear;
	@FXML
	private Label lblResults;
	@FXML
	private TableView<CriminalDTO> tableResults;
	@FXML
	private TableColumn<CriminalDTO, Long> colId;
	@FXML
	private TableColumn<CriminalDTO, String> colName;
	@FXML
	private TableColumn<CriminalDTO, String> colAlias;
	@FXML
	private TableColumn<CriminalDTO, String> colCrime;
	@FXML
	private TableColumn<CriminalDTO, DangerLevel> colDangerLevel;
	@FXML
	private TableColumn<CriminalDTO, String> colStatus;

	private final CriminalService criminalService = new CriminalService();
	private final ObservableList<CriminalDTO> results = FXCollections.observableArrayList();

	private final RequestGuard requestGuard = new RequestGuard();

	@Override
	public void initialize() {
		CriminalTable.configureColumns(colId, colName, colAlias, colCrime, colDangerLevel, colStatus);
		tableResults.setItems(results);

		ComboBoxUtils.configureWithAllOption(cmbStatus, List.of(CriminalStatus.values()), CriminalStatus::getLabel,
				"Todos");
		ComboBoxUtils.configureWithAllOption(cmbDangerLevel, List.of(DangerLevel.values()), DangerLevel::getLabel,
				"Todas");
		txtSearchText.setOnAction(event -> search());
	}

	@Override
	public void onViewShown() {
		clearFilters();
	}

	@Override
	public String getNombreVista() {
		return "Búsqueda Avanzada";
	}

	@FXML
	private void onActionBtnSearch(ActionEvent event) {
		search();
	}

	@FXML
	private void onActionBtnClear(ActionEvent event) {
		clearFilters();
	}

	private void search() {
		String text = txtSearchText.getText();
		CriminalStatus status = cmbStatus.getValue();
		DangerLevel dangerLevel = cmbDangerLevel.getValue();
		long ticket = requestGuard.next();

		lblResults.setText("Consultando el sistema...");
		tableResults.setPlaceholder(new Label("Consultando el sistema..."));
		btnSearch.setDisable(true);

		BackgroundTask.run(() -> criminalService.search(text, status, dangerLevel), found -> {
			if (!requestGuard.isCurrent(ticket)) {
				return; // llegó tarde: ya hay una búsqueda más nueva
			}
			results.setAll(found);
			lblResults.setText(found.size() + " resultado(s) con los filtros aplicados.");
			tableResults.setPlaceholder(new Label("Ningún criminal coincide con esos filtros."));
			btnSearch.setDisable(false);
		}, error -> {
			if (!requestGuard.isCurrent(ticket)) {
				return;
			}
			btnSearch.setDisable(false);
			if (handleExpiredSession(error)) {
				return;
			}
			results.clear();
			lblResults.setText("");
			tableResults.setPlaceholder(new Label(describeError(error)));
		});
	}

	private void clearFilters() {
		txtSearchText.clear();
		cmbStatus.setValue(null);
		cmbDangerLevel.setValue(null);
		results.clear();
		lblResults.setText("");
		tableResults.setPlaceholder(new Label("Aplicá un filtro y presioná Buscar."));
	}
}
