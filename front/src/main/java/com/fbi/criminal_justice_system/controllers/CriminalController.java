// CriminalController.java
package com.fbi.criminal_justice_system.controllers;

import com.fbi.cjs.shared.dto.CriminalDTO;
import com.fbi.cjs.shared.enums.DangerLevel;
import com.fbi.cjs.shared.enums.Role;
import com.fbi.criminal_justice_system.services.CriminalService;
import com.fbi.criminal_justice_system.utils.AppContext;
import com.fbi.criminal_justice_system.utils.BackgroundTask;
import com.fbi.criminal_justice_system.utils.CriminalTable;
import com.fbi.criminal_justice_system.utils.FlowController;
import com.fbi.criminal_justice_system.utils.RequestGuard;
import com.fbi.criminal_justice_system.utils.Session;
import javafx.animation.PauseTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.util.Duration;

/**
 * Listado de criminales y punto de entrada al alta, edición y borrado. No sabe
 * de HTTP ni de JSON: le pide datos a {@link CriminalService} y pinta lo que
 * recibe.
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
	private TableColumn<CriminalDTO, DangerLevel> colDangerLevel;
	@FXML
	private TableColumn<CriminalDTO, String> colStatus;
	@FXML
	private TextField txtSearchCriminal;
	@FXML
	private Button btnSearchCriminal;
	@FXML
	private Button btnAddCriminal;
	@FXML
	private Button btnEditCriminal;
	@FXML
	private Button btnDeleteCriminal;

	private final CriminalService criminalService = new CriminalService();

	/** La tabla observa esta lista: al reemplazar su contenido se repinta sola. */
	private final ObservableList<CriminalDTO> criminals = FXCollections.observableArrayList();

	/**
	 * Espera a que el usuario deje de escribir antes de consultar el servidor. Sin
	 * esto, "salazar" dispararía siete peticiones, una por letra.
	 */
	private final PauseTransition searchDebounce = new PauseTransition(Duration.millis(350));

	/** Descarta respuestas de búsquedas que quedaron viejas. */
	private final RequestGuard requestGuard = new RequestGuard();

	@Override
	public void initialize() {
		CriminalTable.configureColumns(colId, colName, colAlias, colCrime, colDangerLevel, colStatus);
		tableCriminals.setItems(criminals);

		searchDebounce.setOnFinished(event -> loadData());
		txtSearchCriminal.textProperty()
				.addListener((observable, oldValue, newValue) -> searchDebounce.playFromStart());
	}

	@Override
	public void onViewShown() {
		configurePermissions();
		loadData();
	}

	@Override
	public String getNombreVista() {
		return "Criminales";
	}

	/**
	 * Un agente consulta pero no modifica. El WS igual rechaza la petición con 403;
	 * deshabilitar los botones es para no ofrecer algo que va a fallar.
	 */
	private void configurePermissions() {
		boolean canEdit = Session.hasAnyRole(Role.SUPERVISOR, Role.JEFE_FBI);
		btnAddCriminal.setDisable(!canEdit);
		btnEditCriminal.setDisable(!canEdit);
		btnDeleteCriminal.setDisable(!Session.hasAnyRole(Role.JEFE_FBI));
	}

	/**
	 * Carga la tabla desde el WS en otro hilo, para que la ventana siga
	 * respondiendo mientras el servidor contesta.
	 */
	private void loadData() {
		String filter = txtSearchCriminal.getText();
		long ticket = requestGuard.next();

		tableCriminals.setPlaceholder(new Label("Consultando el sistema..."));
		btnSearchCriminal.setDisable(true);

		BackgroundTask.run(() -> criminalService.search(filter, null, null), result -> {
			if (!requestGuard.isCurrent(ticket)) {
				return; // llegó tarde: ya hay una búsqueda más nueva
			}
			criminals.setAll(result);
			tableCriminals.setPlaceholder(new Label("No hay criminales que coincidan con la búsqueda."));
			btnSearchCriminal.setDisable(false);
		}, error -> {
			if (!requestGuard.isCurrent(ticket)) {
				return;
			}
			btnSearchCriminal.setDisable(false);
			if (handleExpiredSession(error)) {
				return;
			}
			criminals.clear();
			tableCriminals.setPlaceholder(new Label(describeError(error)));
		});
	}

	@FXML
	private void onActionBtnSearchCriminal(ActionEvent event) {
		searchDebounce.stop();
		loadData();
	}

	@FXML
	private void onActionBtnAddCriminal(ActionEvent event) {
		openForm(null);
	}

	@FXML
	private void onActionBtnEditCriminal(ActionEvent event) {
		CriminalDTO selected = requireSelection("Editar criminal");
		if (selected != null) {
			openForm(selected);
		}
	}

	@FXML
	private void onActionBtnDeleteCriminal(ActionEvent event) {
		CriminalDTO selected = requireSelection("Eliminar criminal");
		if (selected == null) {
			return;
		}

		boolean confirmed = mensaje.showConfirmation("Eliminar criminal", stage,
				"¿Eliminar a " + selected.getName() + " del sistema? Esta acción no se puede deshacer.");
		if (!confirmed) {
			return;
		}

		BackgroundTask.run(() -> {
			criminalService.delete(selected.getId());
			return null;
		}, ignored -> {
			mensaje.show(AlertType.INFORMATION, "Eliminar criminal", "Criminal eliminado correctamente.");
			loadData();
		}, error -> {
			if (!handleExpiredSession(error)) {
				mensaje.showModal(AlertType.ERROR, "Eliminar criminal", stage, describeError(error));
			}
		});
	}

	private CriminalDTO requireSelection(String titulo) {
		CriminalDTO selected = tableCriminals.getSelectionModel().getSelectedItem();
		if (selected == null) {
			mensaje.showModal(AlertType.WARNING, titulo, stage, "Seleccioná primero un criminal de la tabla.");
		}
		return selected;
	}

	/**
	 * Abre el formulario modal y recarga la tabla si guardó. Modal a propósito: no
	 * tiene sentido editar dos criminales a la vez ni dejar la tabla desfasada.
	 */
	private void openForm(CriminalDTO criminal) {
		AppContext.getInstance().set(CriminalFormController.CONTEXT_SELECTED, criminal);
		FlowController.getInstance().goViewInWindowModal("CriminalFormView", stage, false);

		if (Boolean.TRUE.equals(AppContext.getInstance().get(CriminalFormController.CONTEXT_SAVED))) {
			loadData();
		}
		AppContext.getInstance().delete(CriminalFormController.CONTEXT_SELECTED);
	}
}
