package com.fbi.criminal_justice_system.utils;

import com.fbi.cjs.shared.dto.CriminalDTO;
import com.fbi.cjs.shared.enums.DangerLevel;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;

/**
 * Configuración de las columnas de una tabla de criminales.
 *
 * <p>
 * La usan el listado y la búsqueda avanzada, que muestran exactamente las
 * mismas columnas; tenerla acá evita que las dos pantallas se vayan separando
 * con el tiempo.
 */
public final class CriminalTable {

	private CriminalTable() {
	}

	public static void configureColumns(TableColumn<CriminalDTO, Long> colId, TableColumn<CriminalDTO, String> colName,
			TableColumn<CriminalDTO, String> colAlias, TableColumn<CriminalDTO, String> colCrime,
			TableColumn<CriminalDTO, DangerLevel> colDangerLevel, TableColumn<CriminalDTO, String> colStatus) {

		// Lambdas en vez de PropertyValueFactory: la fábrica busca el getter por
		// reflexión y, si el nombre no coincide, la columna sale vacía sin error
		// alguno.
		colId.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue().getId()));
		colName.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getName()));
		colAlias.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getAlias()));
		colCrime.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getCrime()));
		colStatus.setCellValueFactory(cell -> new SimpleStringProperty(
				cell.getValue().getStatus() == null ? "" : cell.getValue().getStatus().getLabel()));

		colDangerLevel.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue().getDangerLevel()));
		colDangerLevel.setCellFactory(column -> new DangerLevelCell());
	}

	/** Pinta la peligrosidad como etiqueta de color, con los estilos del tema. */
	private static class DangerLevelCell extends TableCell<CriminalDTO, DangerLevel> {

		@Override
		protected void updateItem(DangerLevel dangerLevel, boolean empty) {
			super.updateItem(dangerLevel, empty);

			if (empty || dangerLevel == null) {
				setGraphic(null);
				return;
			}

			Label tag = new Label(dangerLevel.getLabel());
			tag.getStyleClass().add(switch (dangerLevel) {
				case ALTO -> "tag-high";
				case MEDIO -> "tag-medium";
				case BAJO -> "tag-low";
			});
			setGraphic(tag);
		}
	}
}
