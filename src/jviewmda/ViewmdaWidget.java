package jviewmda;

import java.util.LinkedList;
import javafx.scene.Group;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.*;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Slider;
import javafx.scene.control.TextArea;
import javafx.scene.paint.Color;

/**
 *
 * @author magland
 */
public class ViewmdaWidget extends VBox {

	private Mda m_array = new Mda();
	private ComboBox<String> m_dim1_box;
	private ComboBox<String> m_dim2_box;
	private ComboBox<String> m_dim3_box;
	private LinkedList<ComboBox<String>> m_dim_boxes;
	private int[] m_dim_choices = {1, 2, 3};
	private int[] m_current_index = new int[Mda.MAX_DIMS];
	private MdaView2D m_view;
	private Label m_status_label;
	private Slider m_slice_slider;

	public void setArray(Mda X) {
		m_array = X;
		auto_set_window_levels();
		m_dim_choices[0] = 1;
		m_dim_choices[1] = 2;
		m_dim_choices[2] = 3;
		for (int i = 0; i < Mda.MAX_DIMS; i++) {
			m_current_index[i] = m_array.size(i) / 2;
		}
		refresh_dims();
	}

	public ViewmdaWidget() {
		this.m_dim1_box = new ComboBox<>();
		this.m_dim2_box = new ComboBox<>();
		this.m_dim3_box = new ComboBox<>();
		this.m_dim_boxes = new LinkedList<>();
		m_dim_boxes.add(m_dim1_box);
		m_dim_boxes.add(m_dim2_box);
		m_dim_boxes.add(m_dim3_box);

		setStyle("-fx-background-color: lightblue;");

		HBox top_controls = new HBox();
		top_controls.getChildren().addAll(m_dim1_box, m_dim2_box, m_dim3_box);

		m_view = new MdaView2D();

		m_slice_slider = new Slider();

		m_status_label = new Label();
		m_status_label.setText("");

		HBox bottom_controls = new HBox();
		bottom_controls.getChildren().addAll(m_status_label);

		VBox.setVgrow(m_view, Priority.ALWAYS);
		this.getChildren().addAll(top_controls, m_view, m_slice_slider, bottom_controls);
		//this.setTop(top_controls);
		//this.setCenter(m_view);
		//this.setBottom(bottom_controls);

		for (int i = 0; i < Mda.MAX_DIMS; i++) {
			m_current_index[i] = 0;
		}

		setup_boxes();

		refresh_dims();

		m_view.onCurrentIndexChanged(evt -> on_current_index_changed());
		m_slice_slider.valueProperty().addListener(ov -> on_slice_slider_changed());
	}

	public void setCurrentIndex(int[] ind) {
		int d1 = m_dim_choices[0] - 1;
		int d2 = m_dim_choices[1] - 1;
		boolean only_inplane_changed = true;
		for (int i = 0; (i < ind.length) && (i < m_current_index.length); i++) {
			if (m_current_index[i] != ind[i]) {
				if ((i != d1) && (i != d2)) {
					only_inplane_changed = false;
				}
				m_current_index[i] = ind[i];
			}
		}
		if (!only_inplane_changed) {
			refresh_view();
		}
		int[] ind0 = new int[2];
		ind0[0] = m_current_index[d1];
		ind0[1] = m_current_index[d2];
		m_view.setCurrentIndex(ind0);
	}

	public int[] currentIndex() {
		return m_current_index.clone();
	}

	private void refresh_dims() {
		int dimcount = m_array.dimCount();

		for (int i = 0; i < m_dim_boxes.size(); i++) {
			ComboBox<String> box = m_dim_boxes.get(i);
			box.getItems().clear();
			if (i < dimcount) {
				for (int j = 0; j < dimcount; j++) {
					String str = String.format("%d", j + 1);
					box.getItems().add(str);
				}
				box.setVisible(true);
			} else {
				box.setVisible(false);
			}
			String str2 = String.format("%d", m_dim_choices[i]);
			if (i < dimcount) {
				box.setValue(str2);
			} else {
				box.setValue("");
			}
		}
		update_slice_slider();
		refresh_view();
		update_status();
	}

	private void setup_boxes() {
		for (int i = 0; i < m_dim_boxes.size(); i++) {
			ComboBox<String> box = m_dim_boxes.get(i);
			int index = i;
			box.setOnAction(evt -> {
				String str = box.getSelectionModel().getSelectedItem();
				on_dim_changed(index);
			});
		}
	}

	private void on_dim_changed(int index) {
		String str = m_dim_boxes.get(index).getValue();
		m_view.requestFocus();

		try {
			int dim = Integer.parseInt(str);
			if (dim == m_dim_choices[index]) {
				return;
			}
			for (int j = 0; j < m_dim_choices.length; j++) {
				if (m_dim_choices[j] == dim) {
					m_dim_choices[j] = m_dim_choices[index];
					m_dim_choices[index] = dim;
					refresh_dims();
					return;
				}
			}
			m_dim_choices[index] = dim;
			refresh_dims();
		} catch (Exception E) {
		}
	}

	private void refresh_view() {
		Mda X = extract_2d_array();
		m_view.setArray(X);
	}

	private Mda extract_2d_array() {
		Mda ret = new Mda();
		ret.setDataType(m_array.dataType());
		int d1 = m_dim_choices[0] - 1;
		int d2 = m_dim_choices[1] - 1;
		int N1 = m_array.size(d1);
		int N2 = m_array.size(d2);
		ret.allocate(N1, N2);
		int[] ind = m_current_index.clone();
		for (int i2 = 0; i2 < N2; i2++) {
			for (int i1 = 0; i1 < N1; i1++) {
				ind[d1] = i1;
				ind[d2] = i2;
				ret.setValue(m_array.value(ind), i1, i2);
			}
		}
		return ret;
	}

	private void auto_set_window_levels() {
		double maxval = 0;
		int N = m_array.totalSize();
		for (int i = 0; i < N; i++) {
			double val = m_array.value1(i);
			if (val > maxval) {
				maxval = val;
			}
		}
		m_view.setWindowLevels(0, maxval);
	}

	private void on_current_index_changed() {
		int d1 = m_dim_choices[0] - 1;
		int d2 = m_dim_choices[1] - 1;
		int[] ind = m_view.currentIndex();
		if (ind[0] < 0) {
			ind[0] = (int) (m_array.size(d1) / 2);
		}
		if (ind[1] < 0) {
			ind[1] = (int) (m_array.size(d2) / 2);
		}

		m_current_index[d1] = ind[0];
		m_current_index[d2] = ind[1];

		update_status();
	}

	private String get_dim_string() {
		String ret = "";
		int numdims = m_array.dimCount();
		for (int i = 0; i < numdims; i++) {
			if (i > 0) {
				ret += " x ";
			}
			ret += String.format("%d", m_array.size(i));
		}
		return ret;
	}

	private String get_current_index_string() {
		String ret = "";
		ret += "(";
		int numdims = m_array.dimCount();
		for (int i = 0; i < numdims; i++) {
			if (i > 0) {
				ret += ", ";
			}
			ret += String.format("%d", m_current_index[i]);
		}
		ret += ")";
		return ret;
	}

	private String format_val(double val) {
		if (val==(int)val) return String.format("%d",(int)val);
		if (val > 100) {
			return String.format("%d", (int) (val + 0.5));
		} else {
			return String.format("%.3f", val);
		}
	}

	private String get_current_value_string() {
		String ret = "val = ";
		ret += format_val(m_array.value(m_current_index));
		return ret;
	}

	private String get_status_string() {
		String str = "";
		str += get_dim_string() + "; ";
		int[] ind = m_view.currentIndex();
		str += get_current_index_string() + "; ";
		str += get_current_value_string() + "; ";
		return str;
	}

	private void update_status() {
		String str = get_status_string();
		m_status_label.setText(str);
	}

	private void update_slice_slider() {
		int d3 = m_dim_choices[2] - 1;
		int N3 = m_array.size(d3) - 1;
		m_slice_slider.setMin(0);
		m_slice_slider.setMax(N3 - 1);
		m_slice_slider.setValue(m_current_index[d3]);
	}

	private void on_slice_slider_changed() {
		int d3 = m_dim_choices[2] - 1;
		int N3 = m_array.size(d3) - 1;
		int i3 = (int) m_slice_slider.getValue();
		int[] ind = m_current_index.clone();
		ind[d3] = i3;
		setCurrentIndex(ind);
	}
}
