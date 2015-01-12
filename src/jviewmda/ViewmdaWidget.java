package jviewmda;

import java.util.LinkedList;
import javafx.scene.layout.*;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;

/**
 *
 * @author magland
 */
public class ViewmdaWidget extends VBox {

	public void setArray(Mda X) {
		m_array = X;
		refresh();
	}

	public ViewmdaWidget() {
		this.m_dim1_box = new ComboBox<>();
		this.m_dim2_box = new ComboBox<>();
		this.m_boxes = new LinkedList<>();
		this.m_dim3_box = new ComboBox<>();
		
		
		
		m_boxes.add(m_dim1_box);
		m_boxes.add(m_dim2_box);
		m_boxes.add(m_dim3_box);

		setStyle("-fx-background-color: lightblue;");

		HBox top_controls = new HBox();
		top_controls.getChildren().addAll(m_dim1_box, m_dim2_box, m_dim3_box);

		TextArea TA = new TextArea();
		TA.setText("This is the text.");

		Label label1 = new Label();
		label1.setText("label1");

		HBox bottom_controls = new HBox();
		bottom_controls.getChildren().addAll(label1);

		getChildren().addAll(top_controls, TA, bottom_controls);

		refresh();
	}

	Mda m_array = new Mda();
	ComboBox<String> m_dim1_box;
	ComboBox<String> m_dim2_box;
	ComboBox<String> m_dim3_box;
	LinkedList<ComboBox<String>> m_boxes;

	private void refresh() {
		int dimcount = m_array.dimCount();
		
		for (int i = 0; i < m_boxes.size(); i++) {
			ComboBox<String> box = m_boxes.get(i);
			box.getItems().clear();
			if (i < dimcount) {
				for (int j = 0; j < dimcount; j++) {
					String str = String.format("%d", j + 1);
					box.getItems().add(str);
				}
				box.setVisible(true);
			}
			else box.setVisible(false);
			String str2 = String.format("%d", i + 1);
			if (i < dimcount) {
				box.setValue(str2);
			} else {
				box.setValue("");
			}

		}
	}
}
