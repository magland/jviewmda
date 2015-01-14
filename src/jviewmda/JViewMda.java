/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jviewmda;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.prefs.Preferences;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
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
	private String m_file_path;
	private Map<String, CheckMenuItem> m_selection_mode_items;

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
		menu.getItems().add(new SeparatorMenuItem()); /////////////////////////////////////////////
		item = new MenuItem("Exit");
		item.setAccelerator(new KeyCodeCombination(KeyCode.X, KeyCombination.CONTROL_DOWN));
		item.setOnAction(e -> on_file_exit());
		menu.getItems().add(item);

		//view menu
		menu = new Menu("View");
		menubar.getMenus().add(menu);
		item = new MenuItem("Zoom In");
		item.setAccelerator(new KeyCodeCombination(KeyCode.Z, KeyCombination.CONTROL_DOWN));
		item.setOnAction(e -> on_zoom_in());
		menu.getItems().add(item);
		item = new MenuItem("Zoom Out");
		item.setAccelerator(new KeyCodeCombination(KeyCode.Z, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN));
		item.setOnAction(e -> on_zoom_out());
		menu.getItems().add(item);

		//selection menu
		menu = new Menu("Selection");
		menubar.getMenus().add(menu);
		Map<String, CheckMenuItem> mode_items = new HashMap<>();
		m_selection_mode_items = mode_items;
		mode_items.put("rectangle", new CheckMenuItem("Rectangle"));
		mode_items.put("ellipse", new CheckMenuItem("Ellipse"));
		Set<String> keys = mode_items.keySet();
		for (String key : keys) {
			CheckMenuItem item0 = mode_items.get(key);
			menu.getItems().add(item0);
			item0.setOnAction(evt -> {
				on_selection_mode_changed(key);
			});

		}
		mode_items.get("rectangle").setSelected(true);

		VBox root = new VBox();
		root.getChildren().addAll(menubar, m_widget);

		Scene scene = new Scene(root, 300, 250);

		primaryStage.setTitle("JViewMda");
		primaryStage.setScene(scene);
		primaryStage.show();

		m_prefs = Preferences.userNodeForPackage(this.getClass());

	}

	private void on_selection_mode_changed(String selection_mode) {
		Set<String> keys = m_selection_mode_items.keySet();
		for (String key : keys) {
			if (key != selection_mode) {
				m_selection_mode_items.get(key).setSelected(false);
			}
		}
		m_widget.setSelectionMode(selection_mode);
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

		m_file_path = path0;
		update_title();
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
		m_file_path = path0;
		update_title();
	}

	private void on_file_exit() {
		Platform.exit();
	}

	private void update_title() {
		File FF = new File(m_file_path);
		String str = FF.getName() + ": " + FF.getParentFile().getAbsolutePath();
		m_stage.setTitle(str);
	}

	private void on_zoom_in() {
		m_widget.zoomIn();
	}

	private void on_zoom_out() {
		m_widget.zoomOut();
	}

	/**
	 * @param args the command line arguments
	 */
	public static void main(String[] args) {
		launch(args);
	}

}
