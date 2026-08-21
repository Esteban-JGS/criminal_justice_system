package com.fbi.criminal_justice_system.utils;

import com.fbi.cjs.shared.dto.CriminalDTO;
import com.fbi.cjs.shared.enums.DangerLevel;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;

public final class CriminalTable {

	private CriminalTable() {
	}

	public static void configureColumns(TableColumn<CriminalDTO, Long> colId, TableColumn<CriminalDTO, String> colName,
			TableColumn<CriminalDTO, String> colAlias, TableColumn<CriminalDTO, String> colCrime,
			TableColumn<CriminalDTO, DangerLevel> colDangerLevel, TableColumn<CriminalDTO, String> colStatus) {
		colId.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue().getId()));
		colName.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getName()));
		colAlias.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getAlias()));
		colCrime.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getCrime()));
		colStatus.setCellValueFactory(cell -> new SimpleStringProperty(
				cell.getValue().getStatus() == null ? "" : cell.getValue().getStatus().getLabel()));

		colDangerLevel.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue().getDangerLevel()));
		colDangerLevel.setCellFactory(column -> new DangerLevelCell());
	}

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
