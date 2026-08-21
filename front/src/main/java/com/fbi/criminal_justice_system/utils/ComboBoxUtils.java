package com.fbi.criminal_justice_system.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import javafx.collections.FXCollections;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.util.StringConverter;

public final class ComboBoxUtils {

	private ComboBoxUtils() {
	}

	public static <E> void configure(ComboBox<E> combo, Collection<E> items, Function<E, String> labelOf) {
		combo.setItems(FXCollections.observableArrayList(items));
		applyLabels(combo, labelOf, "");
	}

	public static <E> void configureWithAllOption(ComboBox<E> combo, Collection<E> items, Function<E, String> labelOf,
			String allLabel) {

		List<E> withAll = new ArrayList<>();
		withAll.add(null);
		withAll.addAll(items);

		combo.setItems(FXCollections.observableArrayList(withAll));
		applyLabels(combo, labelOf, allLabel);
	}

	private static <E> void applyLabels(ComboBox<E> combo, Function<E, String> labelOf, String nullLabel) {
		combo.setConverter(new StringConverter<>() {
			@Override
			public String toString(E value) {
				return value == null ? nullLabel : labelOf.apply(value);
			}

			@Override
			public E fromString(String text) {
				return null; // los combos no son editables
			}
		});

		combo.setCellFactory(list -> new ListCell<>() {
			@Override
			protected void updateItem(E item, boolean empty) {
				super.updateItem(item, empty);
				setText(empty ? null : (item == null ? nullLabel : labelOf.apply(item)));
			}
		});
	}
}
