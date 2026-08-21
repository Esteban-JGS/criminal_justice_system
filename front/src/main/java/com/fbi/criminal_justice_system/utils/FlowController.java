package com.fbi.criminal_justice_system.utils;

import com.fbi.criminal_justice_system.App;
import java.io.IOException;
import java.util.HashMap;
import java.util.ResourceBundle;
import java.util.logging.Level;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import com.fbi.criminal_justice_system.controllers.Controller;
import io.github.palexdev.materialfx.css.themes.MFXThemeManager;
import io.github.palexdev.materialfx.css.themes.Themes;

public class FlowController {

	private static FlowController INSTANCE = null;
	private static Stage mainStage;
	private static ResourceBundle idioma;
	private static HashMap<String, FXMLLoader> loaders = new HashMap<>();

	private FlowController() {
	}

	private static void createInstance() {
		if (INSTANCE == null) {
			synchronized (FlowController.class) {
				if (INSTANCE == null) {
					INSTANCE = new FlowController();
				}
			}
		}
	}

	public static FlowController getInstance() {
		if (INSTANCE == null) {
			createInstance();
		}
		return INSTANCE;
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}

	public void InitializeFlow(Stage stage, ResourceBundle idioma) {
		getInstance();
		this.mainStage = stage;
		this.idioma = idioma;
	}

	private FXMLLoader getLoader(String name) {
		FXMLLoader loader = loaders.get(name);
		if (loader == null) {
			synchronized (FlowController.class) {
				if (loader == null) {
					try {
						loader = new FXMLLoader(App.class.getResource("views/" + name + ".fxml"), this.idioma);
						loader.load();
						loaders.put(name, loader);
					} catch (Exception ex) {
						loader = null;
						java.util.logging.Logger.getLogger(FlowController.class.getName()).log(Level.SEVERE,
								"Creando loader [" + name + "].", ex);
					}
				}
			}
		}
		return loader;
	}

	public void goMain() {
		try {
			this.mainStage.setScene(
					new Scene(FXMLLoader.load(App.class.getResource("views/DashboardView.fxml"), this.idioma)));
			MFXThemeManager.addOn(this.mainStage.getScene(), Themes.DEFAULT, Themes.LEGACY);
			applyWindowConstraints(this.mainStage); // ← aquí
			this.mainStage.show();
		} catch (IOException ex) {
			java.util.logging.Logger.getLogger(FlowController.class.getName()).log(Level.SEVERE,
					"Error inicializando la vista base.", ex);
		}
	}

	public void goView(String viewName) {
		goView(viewName, "Center", null);
	}

	public void goView(String viewName, String accion) {
		goView(viewName, "Center", accion);
	}

	public void goView(String viewName, String location, String accion) {
		FXMLLoader loader = getLoader(viewName);
		Controller controller = loader.getController();
		Stage stage = controller.getStage();
		if (stage == null) {
			stage = this.mainStage;
			controller.setStage(stage);
		}
		controller.onViewShown();
		switch (location) {
			case "Center" :

				BorderPane borderPane = (BorderPane) stage.getScene().getRoot();
				VBox vBox = (VBox) borderPane.getCenter();
				vBox.getChildren().clear();
				vBox.getChildren().add(loader.getRoot());

				break;
		}
	}

	public void goViewInStage(String viewName, Stage stage) {
		FXMLLoader loader = getLoader(viewName);
		Controller controller = loader.getController();
		controller.setStage(stage);
		stage.getScene().setRoot(loader.getRoot());
		MFXThemeManager.addOn(stage.getScene(), Themes.DEFAULT, Themes.LEGACY);

	}
	public void goViewInWindow(String viewName) {
		FXMLLoader loader = getLoader(viewName);
		Controller controller = loader.getController();
		Stage stage = new Stage();
		stage.getIcons().add(new Image(
				App.class.getResource("/com/fbi/criminal_justice_system/images/FBI_icon.png").toExternalForm()));
		stage.setOnHidden((WindowEvent event) -> {
			controller.getStage().getScene().setRoot(new Pane());
			controller.setStage(null);
		});
		controller.setStage(stage);
		controller.onViewShown();
		stage.setTitle(controller.getNombreVista());
		Parent root = loader.getRoot();
		Scene scene = new Scene(root);
		MFXThemeManager.addOn(scene, Themes.DEFAULT, Themes.LEGACY);
		stage.setScene(scene);
		applyWindowConstraints(this.mainStage);
		stage.centerOnScreen();
		stage.show();
	}

	public void goViewInWindowModal(String viewName, Stage parentStage, Boolean resizable) {
		FXMLLoader loader = getLoader(viewName);
		Controller controller = loader.getController();
		Stage stage = new Stage();
		stage.getIcons().add(new Image(
				App.class.getResource("/com/fbi/criminal_justice_system/images/FBI_icon.png").toExternalForm()));
		stage.setResizable(resizable);
		stage.setOnHidden((WindowEvent event) -> {
			controller.getStage().getScene().setRoot(new Pane());
			controller.setStage(null);
		});
		controller.setStage(stage);
		controller.onViewShown();
		stage.setTitle(controller.getNombreVista());
		Parent root = loader.getRoot();
		Scene scene = new Scene(root);
		MFXThemeManager.addOn(scene, Themes.DEFAULT, Themes.LEGACY);
		stage.setScene(scene);
		applyModalWindowConstraints(stage);
		stage.initModality(Modality.WINDOW_MODAL);
		stage.initOwner(parentStage);
		stage.centerOnScreen();
		stage.showAndWait();
	}

	public Controller getController(String viewName) {
		return getLoader(viewName).getController();
	}

	public void limpiarLoader(String view) {
		this.loaders.remove(view);
	}

	public static void setIdioma(ResourceBundle idioma) {
		FlowController.idioma = idioma;
	}

	public void initialize() {
		this.loaders.clear();
	}

	public void salir() {
		this.mainStage.close();
	}

	private void applyWindowConstraints(Stage stage) {
		stage.setMinWidth(1200);
		stage.setMinHeight(900);
	}

	private void applyModalWindowConstraints(Stage stage) {
		stage.setMinWidth(700);
		stage.setMinHeight(600);
		stage.setMaxWidth(1200);
		stage.setMaxHeight(900);
	}
}
