// CriminalController.java
package com.fbi.criminal_justice_system.controllers;

import com.fbi.cjs.shared.dto.CriminalDTO;
import com.fbi.cjs.shared.enums.Role;
import com.fbi.criminal_justice_system.services.CriminalService;
import com.fbi.criminal_justice_system.utils.ApiException;
import com.fbi.criminal_justice_system.utils.BackgroundTask;
import com.fbi.criminal_justice_system.utils.Session;
import javafx.animation.PauseTransition;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.util.Duration;

/**
 * Listado de criminales. No sabe de HTTP ni de JSON: le pide datos a
 * {@link CriminalService} y pinta lo que recibe.
 */
public class CriminalController extends Controller {

	@FXML
	private TableView<CriminalDTO> tableCriminals;
	@FXML
	private TableColumn<CriminalDTO, Long> colId;
	@FXML
	private TableColumn<CriminalDTO, String> colName;
	@FXML
	private TableColumn<CriminalDTO, String> colAlias;
	@FXML
	private TableColumn<CriminalDTO, String> colCrime;
	@FXML
	private TableColumn<CriminalDTO, String> colDangerLevel;
	@FXML
	private TableColumn<CriminalDTO, String> colStatus;
	@FXML
	private TextField txtSearchCriminal;
	@FXML
	private Button btnSearchCriminal;
	@FXML
	private Button btnAddCriminal;

	private final CriminalService criminalService = new CriminalService();

	/** La tabla observa esta lista: al reemplazar su contenido se repinta sola. */
	private final ObservableList<CriminalDTO> criminals = FXCollections.observableArrayList();

	/**
	 * Espera a que el usuario deje de escribir antes de consultar el servidor. Sin
	 * esto, "salazar" dispararía siete peticiones, una por letra.
	 */
	private final PauseTransition searchDebounce = new PauseTransition(Duration.millis(350));

	/**
	 * {@code initialize()} se ejecuta cada vez que el FlowController muestra la
	 * vista, no solo la primera. Columnas y listeners se configuran una única vez;
	 * los datos, en cambio, se recargan siempre para no mostrar información vieja.
	 */
	private boolean configured;

	@Override
	public void initialize() {
		if (!configured) {
			configureColumns();
			configureSearch();
			configurePermissions();
			configured = true;
		}
		loadData();
	}

	@Override
	public String getNombreVista() {
		return "Criminales";
	}

	/**
	 * Se usan lambdas en vez de {@code PropertyValueFactory}: la fábrica busca el
	 * getter por reflexión y, si el nombre no coincide, la columna sale vacía sin
	 * error alguno. Con lambda, un nombre mal escrito no compila.
	 */
	private void configureColumns() {
		colId.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue().getId()));
		colName.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getName()));
		colAlias.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getAlias()));
		colCrime.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getCrime()));

		// Los enums se muestran con su etiqueta ("Alto"), pero viajan como ALTO.
		colDangerLevel.setCellValueFactory(cell -> new SimpleStringProperty(
				cell.getValue().getDangerLevel() == null ? "" : cell.getValue().getDangerLevel().getLabel()));
		colStatus.setCellValueFactory(cell -> new SimpleStringProperty(
				cell.getValue().getStatus() == null ? "" : cell.getValue().getStatus().getLabel()));

		tableCriminals.setItems(criminals);
	}

	/** La búsqueda la resuelve el servidor; acá solo se manda el texto. */
	private void configureSearch() {
		searchDebounce.setOnFinished(event -> loadData());
		txtSearchCriminal.textProperty()
				.addListener((observable, oldValue, newValue) -> searchDebounce.playFromStart());
	}

	/**
	 * Un agente puede consultar, pero no registrar. El WS igual rechaza la petición
	 * con 403; deshabilitar el botón es solo para no ofrecer algo que va a fallar.
	 */
	private void configurePermissions() {
		btnAddCriminal.setDisable(!Session.hasAnyRole(Role.SUPERVISOR, Role.JEFE_FBI));
	}

	/**
	 * Carga la tabla desde el WS.
	 *
	 * <p>
	 * La llamada corre en otro hilo ({@link BackgroundTask}) para que la ventana
	 * siga respondiendo mientras el servidor contesta.
	 */
	private void loadData() {
		String filter = txtSearchCriminal.getText();

		tableCriminals.setPlaceholder(new Label("Consultando el sistema..."));
		btnSearchCriminal.setDisable(true);

		BackgroundTask.run(() -> criminalService.search(filter, null, null), result -> {
			criminals.setAll(result);
			tableCriminals.setPlaceholder(new Label("No hay criminales que coincidan con la búsqueda."));
			btnSearchCriminal.setDisable(false);
		}, error -> {
			criminals.clear();
			tableCriminals.setPlaceholder(new Label(describe(error)));
			btnSearchCriminal.setDisable(false);
		});
	}

	private String describe(Throwable error) {
		if (error instanceof ApiException apiException) {
			return apiException.getDisplayMessage();
		}
		return "Error inesperado: " + error.getMessage();
	}

	@FXML
	private void onActionBtnSearchCriminal(ActionEvent event) {
		searchDebounce.stop();
		loadData();
	}

	@FXML
	private void onActionBtnAddCriminal(ActionEvent event) {
		// Próxima semana: formulario de registro que llame a
		// criminalService.create(...)
		// FlowController.getInstance().goViewInWindowModal("CriminalRegisterView",
		// stage, false);
		System.out.println("Abrir formulario de registro (próxima semana)");
	}
}
