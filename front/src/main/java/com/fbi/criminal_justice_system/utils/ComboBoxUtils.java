package com.fbi.criminal_justice_system.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import javafx.collections.FXCollections;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.util.StringConverter;

/**
 * Combos que guardan un objeto del dominio y muestran una etiqueta legible.
 *
 * <p>
 * Con esto el valor seleccionado es siempre un enum o un DTO válido: el usuario
 * no puede escribir un estado inexistente, y al WS nunca le llega texto libre
 * donde espera un valor del catálogo.
 */
public final class ComboBoxUtils {

	private ComboBoxUtils() {
	}

	/**
	 * @param labelOf
	 *            cómo se muestra cada elemento, tanto en la lista como una vez
	 *            seleccionado
	 */
	public static <E> void configure(ComboBox<E> combo, Collection<E> items, Function<E, String> labelOf) {
		combo.setItems(FXCollections.observableArrayList(items));
		applyLabels(combo, labelOf, "");
	}

	/**
	 * Igual que {@link #configure}, más una opción inicial que representa "sin
	 * filtro" y vale {@code null}. Para pantallas de búsqueda, donde hay que poder
	 * volver a "todos" después de haber elegido algo.
	 */
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
