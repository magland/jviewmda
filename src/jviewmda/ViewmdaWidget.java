package jviewmda;

import static java.lang.Math.max;
import java.util.LinkedList;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.layout.*;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

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
	private double m_stats_mean = 0;
	HBox m_top_controls;
	HBox m_bottom_controls;
	HBox m_view_section;
	BrightnessContrastControl m_bc_control;
	CallbackHandler CH = new CallbackHandler();

	public void setArray(Mda X) {
		m_array = X;
		auto_set_window_levels();
		m_dim_choices[0] = 1;
		m_dim_choices[1] = 2;
		m_dim_choices[2] = 3;
		update_slice_slider(); //apparently it is important to update the slice slider before and after setting the default current index so that the slice does not get set to zero
		for (int i = 0; i < Mda.MAX_DIMS; i++) {
			m_current_index[i] = m_array.size(i) / 2;
		}
		update_slice_slider();
		refresh_dims();
	}

	public ViewmdaWidget() {

		CustomTooltipBehavior.setup(50, 10000, 200);

		this.m_dim1_box = new ComboBox<>();
		this.m_dim2_box = new ComboBox<>();
		this.m_dim3_box = new ComboBox<>();
		this.m_dim_boxes = new LinkedList<>();
		m_dim_boxes.add(m_dim1_box);
		m_dim_boxes.add(m_dim2_box);
		m_dim_boxes.add(m_dim3_box);

		setStyle("-fx-background-color: lightgray;");

		HBox top_controls = new HBox();
		top_controls.getChildren().addAll(m_dim1_box, m_dim2_box, m_dim3_box);
		m_top_controls = top_controls;

		HBox view_section = new HBox();
		m_view = new MdaView2D();
		BrightnessContrastControl bc_control = new BrightnessContrastControl();
		m_view.setBrightnessContrastControl(bc_control);
		view_section.getChildren().addAll(m_view, bc_control);
		m_bc_control = bc_control;
		m_view_section = view_section;

		m_slice_slider = new Slider();
		m_slice_slider.setTooltip(new Tooltip("Scroll through slices. Also use Shift+UP and Shift+DOWN."));

		m_status_label = new Label();
		m_status_label.setText("");

		HBox bottom_controls = new HBox();
		bottom_controls.getChildren().addAll(m_status_label);
		m_bottom_controls = bottom_controls;

		VBox.setVgrow(m_view, Priority.ALWAYS);
		this.getChildren().addAll(top_controls, view_section, m_slice_slider, bottom_controls);
		//this.setTop(top_controls);
		//this.setCenter(m_view);
		//this.setBottom(bottom_controls);

		for (int i = 0; i < Mda.MAX_DIMS; i++) {
			m_current_index[i] = 0;
		}

		setup_boxes();

		refresh_dims();

		m_view.onCurrentIndexChanged(() -> on_current_index_changed());
		m_view.onSelectedRectChanged(() -> on_selected_rect_changed());
		m_slice_slider.valueProperty().addListener(ov -> on_slice_slider_changed());

		this.widthProperty().addListener(ov -> {
			update_status_label_height();
		});
		update_status_label_height();

		this.setOnKeyPressed(evt -> on_key_pressed(evt));
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
		update_slice_slider_value();
		update_status();

		if (!only_inplane_changed) {
			CH.trigger("current-slice-changed");
		}
	}

	public int[] currentIndex() {
		return m_current_index.clone();
	}

	public void setSelectionMode(String mode) {
		m_view.setSelectionMode(mode);
	}

	public String selectionMode() {
		return m_view.selectionMode();
	}

	public int[] zoomRect() {
		return m_view.zoomRect();
	}

	public void setZoomRect(int[] rr) {
		m_view.setZoomRect(rr);
	}

	public void zoomIn() {
		int[] rr = m_view.selectedRect();
		m_view.setZoomRect(rr);
	}

	public void zoomOut() {
		int[] rr = new int[4];
		rr[0] = rr[1] = rr[2] = rr[3] = -1;
		m_view.setZoomRect(rr);
	}

	public void setTopControlsVisible(boolean val) {
		if (this.topControlsVisible() == val) {
			return;
		}
		if (val) {
			this.getChildren().add(0, m_top_controls);
		} else {
			this.getChildren().remove(m_top_controls);
		}
	}

	public boolean topControlsVisible() {
		return this.getChildren().contains(m_top_controls);
	}

	public void setBottomControlsVisible(boolean val) {
		if (this.bottomControlsVisible() == val) {
			return;
		}
		if (val) {
			this.getChildren().add(m_bottom_controls);
		} else {
			this.getChildren().remove(m_bottom_controls);
		}
	}

	public boolean bottomControlsVisible() {
		return this.getChildren().contains(m_bottom_controls);
	}

	public void setBrightnessContrastVisible(boolean val) {
		if (this.brightnessContrastVisible() == val) {
			return;
		}
		if (val) {
			m_view_section.getChildren().add(m_bc_control);
		} else {
			m_view_section.getChildren().remove(m_bc_control);
		}
	}

	public boolean brightnessContrastVisible() {
		return m_view_section.getChildren().contains(m_bc_control);
	}

	public void setSliceSliderVisible(boolean val) {
		if (this.sliceSliderVisible() == val) {
			return;
		}
		if (val) {
			if (getChildren().contains(m_bottom_controls)) {
				getChildren().add(getChildren().size() - 1, m_slice_slider);
			} else {
				getChildren().add(m_slice_slider);
			}
		} else {
			getChildren().remove(m_slice_slider);
		}
	}

	public boolean sliceSliderVisible() {
		return getChildren().contains(m_slice_slider);
	}

	public void setCursorVisible(boolean val) {
		m_view.setCursorVisible(val);
	}

	public void onCurrentSliceChanged(Runnable callback) {
		CH.bind("current-slice-changed", callback);
	}

	public ExpandingCanvas customCanvas(String name) {
		return m_view.customCanvas(name);
	}

	public int[] indexToPixel(double x, double y) {
		return m_view.indexToPixel(x, y);
	}

	public int[] pixelToIndex(double x, double y) {
		return m_view.pixelToIndex(x, y);
	}

	public int[] imageRect() {
		return m_view.imageRect();
	}

	public void onImageRefreshed(Runnable callback) {
		m_view.onImageRefreshed(callback);
	}

	/////////////////// PRIVATE /////////////////////////
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

	private void on_key_pressed(KeyEvent evt) {
		KeyCode code = evt.getCode();
		int d3 = m_dim_choices[2] - 1;
		if (evt.isShiftDown()) {
			if (code.equals(KeyCode.UP)) {
				int[] ind = currentIndex();
				ind[d3]++;
				setCurrentIndex(ind);
				evt.consume();
			} else if (code.equals(KeyCode.DOWN)) {
				int[] ind = currentIndex();
				ind[d3]--;
				setCurrentIndex(ind);
				evt.consume();
			}
		}
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

	private double compute_selection_mean() {
		int[] rr = m_view.selectedRect();
		if (rr[0] < 0) {
			return 0;
		}
		Mda X = m_view.array();
		double sum = 0, count = 0;
		boolean ellipse_mode = false;
		if (m_view.selectionMode().equals("ellipse")) {
			ellipse_mode = true;
		}
		double x0 = (rr[0] + rr[0] + rr[2] + 1) * 1.0 / 2;
		double y0 = (rr[1] + rr[1] + rr[3] + 1) * 1.0 / 2;
		double radx = (rr[2] + 1) * 1.0 / 2;
		double rady = (rr[3] + 1) * 1.0 / 2;
		for (int y = rr[1]; y <= rr[1] + rr[3]; y++) {
			for (int x = rr[0]; x <= rr[0] + rr[2]; x++) {
				boolean use = true;
				if (ellipse_mode) {
					double tmp = ((x - x0) * (x - x0)) / (radx * radx) + ((y - y0) * (y - y0)) / (rady * rady);
					use = (tmp <= 1);
				}
				if (use) {
					sum += X.value(x, y);
					count++;
				}
			}
		}
		if (count == 0) {
			return 0;
		}
		System.out.format("count = %g\n", count);
		return sum / count;
	}

	private void compute_selected_rect_stats() {
		m_stats_mean = compute_selection_mean();
	}

	private void on_selected_rect_changed() {
		compute_selected_rect_stats();
		update_status();
	}

	private String get_dim_string() {
		String ret = "";
		int numdims = m_array.dimCount();
		for (int i = 0; i < numdims; i++) {
			if (i > 0) {
				ret += "x";
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
				ret += ",";
			}
			ret += String.format("%d", m_current_index[i]);
		}
		ret += ")";
		return ret;
	}

	private String format_val(double val) {
		if (val == (int) val) {
			return String.format("%d", (int) val);
		}
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

	private String get_selected_rect_string() {
		String ret = "";
		int[] rr = m_view.selectedRect();
		ret += String.format("%dx%d", rr[2] + 1, rr[3] + 1);
		return ret;
	}

	private String get_selected_rect_stats_string() {
		String ret = "";
		return ret += String.format("mean = %s", format_val(m_stats_mean));
	}

	private String get_status_string() {
		String str = "";
		str += get_dim_string() + "; ";
		int[] ind = m_view.currentIndex();
		str += get_current_index_string() + "; ";
		if (m_view.selectedRect()[0] >= 0) {
			str += get_selected_rect_string() + "; ";
			str += get_selected_rect_stats_string() + "; ";
		} else {
			str += get_current_value_string() + "; ";
		}
		return str;
	}

	private void update_status() {
		String str = get_status_string();
		m_status_label.setText(str);
		update_status_label_height();
	}

	private void update_slice_slider() {
		int d3 = m_dim_choices[2] - 1;
		int N3 = m_array.size(d3) - 1;
		m_slice_slider.setMin(0);
		m_slice_slider.setMax(N3 - 1);
		update_slice_slider_value();
	}

	private void update_slice_slider_value() {
		int d3 = m_dim_choices[2] - 1;
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

	private void update_status_label_height() {
		m_status_label.setWrapText(true);
		int font_size = 11;
		Font font = new Font(m_status_label.getFont().getFamily(), font_size);
		double text_width = get_text_width(m_status_label.getText());
		int num_lines = 1;
		if (text_width > 0) {
			num_lines = max((int) (text_width * 1.0 / this.getWidth() + 0.99), 1);
		}
		double factor = num_lines * 1.4;
		m_status_label.setFont(font);
		m_status_label.setMinHeight(font_size * factor);
		m_status_label.setAlignment(Pos.TOP_LEFT);
	}

	private double get_text_width(String txt) {
		Text text = new Text(txt);
		new Scene(new Group(text));
		// java 7 => 
		//    text.snapshot(null, null);
		// java 8 =>
		text.applyCss();
		return text.getLayoutBounds().getWidth();
	}
}
