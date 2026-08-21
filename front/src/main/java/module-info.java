/**
 * CONCEPTOS CLAVE DE MODULARIDAD (Project Jigsaw):
 *
 * - module: Define el nombre de nuestro módulo. Aísla nuestro código del resto.
 *
 * - requires: Indica qué módulos externos necesitamos. Sin esto, Java no deja
 * usar 'import' de sus clases. ¿Por qué? Garantiza que tengamos todo lo
 * necesario antes de correr la app y permite crear ejecutables livianos
 * (JLink).
 *
 * - opens: Permite a otros módulos inspeccionar nuestras clases internamente
 * (Reflection) aunque sean privadas. ¿Cuándo usarlo? Obligatorio en JavaFX para
 * que pueda inyectar variables @FXML.
 *
 * - exports: Habilita a otros módulos externos a acceder a nuestras clases
 * públicas. ¿Cuándo usarlo? Principalmente cuando creas una librería, para
 * exponer tu API.
 */
module com.fbi.criminal_justice_system {

	// Componentes base de JavaFX (Botones, ventanas y soporte FXML)
	requires javafx.controls;
	requires javafx.fxml;

	// Librería externa de diseño
	requires MaterialFX;

	// DTOs compartidos con el web service (módulo shared del monorepo)
	requires com.fbi.cjs.shared;

	// Cliente HTTP del JDK: es con lo que hablamos con la API REST
	requires java.net.http;

	// Conversión JSON <-> objetos Java
	requires com.fasterxml.jackson.databind;

	// Módulos nativos de Java
	requires java.logging;

	// Permite a JavaFX arrancar la clase App y procesar la vista principal
	opens com.fbi.criminal_justice_system to javafx.graphics, javafx.fxml;

	// Deja que JavaFX asocie los controles de la pantalla con los controladores
	opens com.fbi.criminal_justice_system.controllers to javafx.fxml;

	// Hacemos visible nuestro paquete principal hacia el exterior
	exports com.fbi.criminal_justice_system;
}
