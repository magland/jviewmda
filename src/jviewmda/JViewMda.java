/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jviewmda;

import java.io.File;
import java.util.prefs.Preferences;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

/**
 *
 * @author magland
 */
public class JViewMda extends Application {

	private Stage m_stage;
	private Mda m_array = new Mda();
	private Preferences m_prefs;
	private FileChooser m_fc = new FileChooser();
	private ViewmdaWidget m_widget = new ViewmdaWidget();

	@Override
	public void start(Stage primaryStage) {
		m_stage = primaryStage;

		Menu menu;
		MenuItem item;

		MenuBar menubar = new MenuBar();

		//file menu
		menu = new Menu("File");
		menubar.getMenus().add(menu);
		item = new MenuItem("Open...");
		item.setAccelerator(new KeyCodeCombination(KeyCode.O, KeyCombination.CONTROL_DOWN));
		item.setOnAction(e -> on_file_open());
		menu.getItems().add(item);
		item = new MenuItem("Save As...");
		item.setAccelerator(new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN));
		item.setOnAction(e -> on_file_saveas());
		menu.getItems().add(item);
		item = new MenuItem("Exit");
		item.setOnAction(e -> on_file_exit());
		menu.getItems().add(item);

		VBox root = new VBox();
		root.getChildren().addAll(menubar, m_widget);

		Scene scene = new Scene(root, 300, 250);

		primaryStage.setTitle("Hello World!");
		primaryStage.setScene(scene);
		primaryStage.show();

		m_prefs = Preferences.userNodeForPackage(this.getClass());

	}

	private void on_file_open() {
		m_fc.setInitialDirectory(new File(m_prefs.get("open_file_directory", System.getProperty("user.home"))));
		m_fc.setTitle("Open File");
		File file0 = m_fc.showOpenDialog(m_stage);
		if (file0 == null) {
			return;
		}
		m_prefs.put("open_file_directory", file0.getParentFile().getAbsolutePath());
		String path0 = file0.getAbsolutePath();

		if (!m_array.read(path0)) {
			System.err.println("Problem reading mda file.");
			return;
		}

		m_widget.setArray(m_array);
	}

	private void on_file_saveas() {
		m_fc.setInitialDirectory(new File(m_prefs.get("open_file_directory", System.getProperty("user.home"))));
		m_fc.setTitle("Open File");
		File file0 = m_fc.showSaveDialog(m_stage);
		if (file0 == null) {
			return;
		}
		m_prefs.put("open_file_directory", file0.getParentFile().getAbsolutePath());
		String path0 = file0.getAbsolutePath();

		if (!m_array.write(path0)) {
			System.err.println("Problem writing mda file.");
			return;
		}
	}

	private void on_file_exit() {
		Platform.exit();
	}

	/**
	 * @param args the command line arguments
	 */
	public static void main(String[] args) {
		launch(args);
	}

}
