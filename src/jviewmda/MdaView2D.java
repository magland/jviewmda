package jviewmda;

import static java.lang.Integer.max;
import static java.lang.Integer.min;
import static java.lang.Math.abs;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.util.Duration;

/**
 *
 * @author magland
 */
public class MdaView2D extends StackPane {

	private ExpandingCanvas m_image_canvas;
	private ExpandingCanvas m_cursor_canvas;
	private Mda m_array;
	int[] m_current_index = new int[2];
	int[] m_selected_rect = new int[4];
	int[] m_zoom_rect = new int[4];
	double m_scale_x = 1;
	double m_scale_y = 1;
	int m_offset_x = 0;
	int m_offset_y = 0;
	int m_image_width = 1;
	int m_image_height = 1;
	double m_window_min = 0;
	double m_window_max = 100;
	String m_selection_mode = "rectangle";
	CallbackHandler CH = new CallbackHandler();

	public void setArray(Mda X) {
		m_array = X;
		schedule_refresh_image();
		refresh_cursor();
	}

	public Mda array() {
		return m_array;
	}

	public void setCurrentIndex(int[] ind) {
		if ((m_current_index[0] == ind[0]) && (m_current_index[1] == ind[1])) {
			return;
		}
		m_current_index[0] = ind[0];
		m_current_index[1] = ind[1];
		refresh_cursor();
		CH.scheduleTrigger("current-index-changed", 100);
	}

	public void setSelectedRect(int[] rr) {
		m_selected_rect = rr.clone();
		refresh_cursor();
		CH.scheduleTrigger("selected-rect-changed", 500);
	}

	public void onCurrentIndexChanged(EventHandler<ActionEvent> handler) {
		CH.bind("current-index-changed", handler);
	}

	public void onSelectedRectChanged(EventHandler<ActionEvent> handler) {
		CH.bind("selected-rect-changed", handler);
	}

	public int[] currentIndex() {
		return m_current_index.clone();
	}

	public int[] selectedRect() {
		return m_selected_rect.clone();
	}

	public void setWindowLevels(double min, double max) {
		m_window_min = min;
		m_window_max = max;
		schedule_refresh_image();
	}

	public void setSelectionMode(String mode) {
		m_selection_mode = mode;
		CH.scheduleTrigger("selected-rect-changed", 500);
		refresh_cursor();
	}

	public String selectionMode() {
		return m_selection_mode;
	}

	public void setZoomRect(int[] rr) {
		m_zoom_rect = rr.clone();
		schedule_refresh_image();
	}

	public int[] zoomRect() {
		return m_zoom_rect.clone();
	}

	public MdaView2D() {
		m_current_index[0] = -1;
		m_current_index[1] = -1;
		m_selected_rect[0] = -1;
		m_selected_rect[1] = -1;
		m_selected_rect[2] = -1;
		m_selected_rect[3] = -1;
		m_zoom_rect[0] = -1;
		m_zoom_rect[1] = -1;
		m_zoom_rect[2] = -1;
		m_zoom_rect[3] = -1;
		m_image_canvas = new ExpandingCanvas();
		m_cursor_canvas = new ExpandingCanvas();
		getChildren().add(m_image_canvas);
		getChildren().add(m_cursor_canvas);
		m_image_canvas.setOnRefresh(evt -> schedule_refresh_image());
		m_cursor_canvas.setOnRefresh(evt -> refresh_cursor());

		this.setOnMousePressed(evt -> {
			on_mouse_pressed(evt, evt.getX(), evt.getY());
		});
		this.setOnMouseDragged(evt -> {
			on_mouse_dragged(evt, evt.getX(), evt.getY());
		});
		this.setOnKeyPressed(evt -> on_key_pressed(evt));
	}
	boolean m_refresh_image_scheduled = false;

	private void schedule_refresh_image() {
		if (m_refresh_image_scheduled) {
			return;
		}
		int dur = 100;
		m_refresh_image_scheduled = true;
		new Timeline(new KeyFrame(Duration.millis(dur), e -> {
			m_refresh_image_scheduled = false;
			do_refresh_image();
		})).play();
	}

	private void do_refresh_image() {
		GraphicsContext gc = m_image_canvas.getGraphicsContext2D();
		gc.clearRect(0, 0, getWidth(), getHeight());

		int margin = 5;

		double scale_factor = 1;
		int N1 = m_array.N1();
		int N2 = m_array.N2();
		if ((m_zoom_rect[2] > 1) && (m_zoom_rect[3] > 1)) {
			N1 = m_zoom_rect[2] + 1;
			N2 = m_zoom_rect[3] + 1;
		}
		double W0 = getWidth() - margin * 2;
		double H0 = getHeight() - margin * 2;
		if (W0 * N2 < N1 * H0) { //width is the limiting direction
			scale_factor = W0 * 1.0 / N1;
		} else { //height is the limiting direction
			scale_factor = H0 * 1.0 / N2;
		}
		m_scale_x = scale_factor;
		m_scale_y = scale_factor;
		m_offset_x = (int) (margin + (W0 - N1 * m_scale_x) / 2);
		m_offset_y = (int) (margin + (H0 - N2 * m_scale_y) / 2);

		int M1 = (int) (N1 * m_scale_x);
		int M2 = (int) (N2 * m_scale_y);

		if (M1 < 1) {
			return;
		}
		if (M2 < 1) {
			return;
		}

		m_image_width = M1;
		m_image_height = M2;

		WritableImage img = new WritableImage(M1, M2);
		PixelWriter W = img.getPixelWriter();
		int[] tmp = new int[2];
		for (int y = 0; y < M2; y++) {
			for (int x = 0; x < M1; x++) {
				tmp = pixel2index(x + m_offset_x, y + m_offset_y);
				Color c = get_color_at(tmp[0], tmp[1]);
				W.setColor(x, y, c);
			}
		}
		gc.drawImage(img, m_offset_x, m_offset_y);

		refresh_cursor();
	}

	private int[] pixel2index(double x, double y) {
		int tmp[] = new int[2];
		int N1 = m_array.N1();
		int N2 = m_array.N2();
		int x0 = 0, y0 = 0;
		if ((m_zoom_rect[2] > 1) && (m_zoom_rect[3] > 1)) {
			N1 = m_zoom_rect[2] + 1;
			N2 = m_zoom_rect[3] + 1;
			x0 = m_zoom_rect[0];
			y0 = m_zoom_rect[1];
		}

		tmp[0] = x0 + (int) ((x - m_offset_x) / m_scale_x);
		tmp[1] = y0 + (int) ((y - m_offset_y) / m_scale_y);
		if ((tmp[0] < x0) || (tmp[1] < y0) || (tmp[0] >= x0 + N1) || (tmp[1] >= y0 + N2)) {
			tmp[0] = -1;
			tmp[1] = -1;
		}
		return tmp;
	}

	private int[] index2pixel(double x, double y) {
		int tmp[] = new int[2];
		int x0 = 0, y0 = 0;
		if ((m_zoom_rect[2] > 1) && (m_zoom_rect[3] > 1)) {
			x0 = m_zoom_rect[0];
			y0 = m_zoom_rect[1];
		}
		tmp[0] = (int) (m_offset_x + (x - x0 + 0.5) * m_scale_x);
		tmp[1] = (int) (m_offset_y + (y - y0 + 0.5) * m_scale_y);
		return tmp;
	}

	private Color get_color_at(int x, int y) {
		double val = m_array.value(x, y);
		double val0 = (val - m_window_min) / m_window_max;
		if (val0 > 1) {
			val0 = 1;
		}
		if (val0 < 0) {
			val0 = 0;
		}
		return new Color((float) val0, (float) val0, (float) val0, 1);
	}

	private void refresh_cursor() {
		GraphicsContext gc = m_cursor_canvas.getGraphicsContext2D();
		gc.clearRect(0, 0, getWidth(), getHeight());
		gc.setStroke(Color.RED);
		gc.setLineWidth(3);

		if (m_selected_rect[0] >= 0) {
			int[] ind0 = new int[2];
			ind0[0] = m_selected_rect[0];
			ind0[1] = m_selected_rect[1];
			int[] ind1 = new int[2];
			ind1[0] = m_selected_rect[0] + m_selected_rect[2];
			ind1[1] = m_selected_rect[1] + m_selected_rect[3];
			int[] pix0 = index2pixel(ind0[0] - 0.5, ind0[1] - 0.5);
			int[] pix1 = index2pixel(ind1[0] + 0.5, ind1[1] + 0.5);
			if (m_selection_mode == "rectangle") {
				gc.strokeRect(pix0[0], pix0[1], pix1[0] - pix0[0], pix1[1] - pix0[1]);
			} else if (m_selection_mode == "ellipse") {
				gc.strokeOval(pix0[0], pix0[1], pix1[0] - pix0[0], pix1[1] - pix0[1]);
			}
			return;
		}

		if (m_current_index[0] < 0) {
			return;
		}
		if (m_current_index[1] < 0) {
			return;
		}

		int[] pix = index2pixel(m_current_index[0], m_current_index[1]);
		gc.strokeLine(m_offset_x, pix[1], m_offset_x + m_image_width, pix[1]);
		gc.strokeLine(pix[0], m_offset_y, pix[0], m_offset_y + m_image_height);
//		gc.setLineWidth(5);
//		gc.strokeLine(getWidth()-10,getHeight()-10,10,10);
	}
	double[] m_anchor_point = new double[2];

	private void on_mouse_pressed(MouseEvent evt, double x, double y) {
		int[] ind = pixel2index(x, y);
		this.setCurrentIndex(ind);
		this.requestFocus();
		m_anchor_point = new double[2];
		m_anchor_point[0] = x;
		m_anchor_point[1] = y;
		int[] rr = new int[4];
		rr[0] = rr[1] = rr[2] = rr[3] = -1;
		this.setSelectedRect(rr); //includes refresh_cursor
	}

	private void on_mouse_dragged(MouseEvent evt, double x, double y) {
		if (m_anchor_point[0] >= 0) {
			if ((abs(m_anchor_point[0] - x) > 2) && (abs(m_anchor_point[1] - y) > 2)) {
				int[] ind0 = pixel2index(m_anchor_point[0], m_anchor_point[1]);
				int[] ind1 = pixel2index(x, y);
				int[] rr = new int[4];
				rr[0] = min(ind0[0], ind1[0]);
				rr[1] = min(ind0[1], ind1[1]);
				rr[2] = max(ind0[0], ind1[0]) - min(ind0[0], ind1[0]);
				rr[3] = max(ind0[1], ind1[1]) - min(ind0[1], ind1[1]);
				setSelectedRect(rr);
			}
		}
	}

	private void on_key_pressed(KeyEvent evt) {
		int[] ind = m_current_index.clone();
		KeyCode code = evt.getCode();
		if (code == KeyCode.UP) {
			ind[1]--;
			if (ind[1] < 0) {
				ind[1] = 0;
			}
			this.setCurrentIndex(ind);
		} else if (code == KeyCode.DOWN) {
			ind[1]++;
			if (ind[1] >= m_array.N2()) {
				ind[1] = m_array.N2() - 1;
			}
			this.setCurrentIndex(ind);
		} else if (code == KeyCode.LEFT) {
			ind[0]--;
			if (ind[0] < 0) {
				ind[0] = 0;
			}
			this.setCurrentIndex(ind);
		} else if (code == KeyCode.RIGHT) {
			ind[0]++;
			if (ind[0] >= m_array.N1()) {
				ind[0] = m_array.N1() - 1;
			}
			this.setCurrentIndex(ind);
		} else {
			return; //don't consume
		}
		evt.consume();
	}
}
