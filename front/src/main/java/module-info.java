module com.fbi.criminal_justice_system {
	requires javafx.controls;
	requires javafx.fxml;
	requires MaterialFX;
	requires com.fbi.cjs.shared;
	requires java.net.http;
	requires com.fasterxml.jackson.databind;
	requires java.logging;
	opens com.fbi.criminal_justice_system to javafx.graphics, javafx.fxml;
	opens com.fbi.criminal_justice_system.controllers to javafx.fxml;
	exports com.fbi.criminal_justice_system;
}
