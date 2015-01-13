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
import javafx.scene.control.TextArea;
import javafx.scene.paint.Color;

/**
 *
 * @author magland
 */
public class ViewmdaWidget extends BorderPane {

	public void setArray(Mda X) {
		m_array = X;
		m_dim_choices[0]=1;
		m_dim_choices[1]=2;
		m_dim_choices[2]=3;
		for (int i=0; i<Mda.MAX_DIMS; i++) {
			m_current_index[i]=m_array.size(i)/2;
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

		m_view=new MdaView2D();

		Label label1 = new Label();
		label1.setText("label1");

		HBox bottom_controls = new HBox();
		bottom_controls.getChildren().addAll(label1);

		this.setTop(top_controls);
		this.setCenter(m_view);
		this.setBottom(bottom_controls);
		
		for (int i=0; i<Mda.MAX_DIMS; i++) m_current_index[i]=0;
		
		setup_boxes();

		refresh_dims();
	}

	Mda m_array = new Mda();
	ComboBox<String> m_dim1_box;
	ComboBox<String> m_dim2_box;
	ComboBox<String> m_dim3_box;
	LinkedList<ComboBox<String>> m_dim_boxes;
	int[] m_dim_choices={1,2,3};
	int[] m_current_index=new int[Mda.MAX_DIMS];
	MdaView2D m_view;

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
			}
			else box.setVisible(false);
			String str2 = String.format("%d", m_dim_choices[i]);
			if (i < dimcount) {
				box.setValue(str2);
			} else {
				box.setValue("");
			}
		}
		refresh_view();
	}
	private void setup_boxes() {
		for (int i=0; i<m_dim_boxes.size(); i++) {
			ComboBox<String> box = m_dim_boxes.get(i);
			int index=i;
			box.setOnAction(evt->{
				String str=box.getSelectionModel().getSelectedItem();
				on_dim_changed(index);
			});
		}
	}
	private void on_dim_changed(int index) {
		String str=m_dim_boxes.get(index).getValue();
		
		try {
			int dim=Integer.parseInt(str);
			if (dim==m_dim_choices[index]) return;
			for (int j=0; j<m_dim_choices.length; j++) {
				if (m_dim_choices[j]==dim) {
					m_dim_choices[j]=m_dim_choices[index];
					m_dim_choices[index]=dim;
					refresh_dims();
					return;
				}
			}
			m_dim_choices[index]=dim;
			refresh_dims();
		}
		catch(Exception E) {
		}
	}
	private void refresh_view() {
		Mda X=extract_2d_array();
		m_view.setArray(X);
	}
	private Mda extract_2d_array() {
		Mda ret=new Mda();
		ret.setDataType(m_array.dataType());
		int d1=m_dim_choices[0]-1;
		int d2=m_dim_choices[1]-1;
		int N1=m_array.size(d1);
		int N2=m_array.size(d2);
		ret.allocate(N1,N2);
		int[] ind=m_current_index.clone();
		for (int i2=0; i2<N2; i2++)
		for (int i1=0; i1<N1; i1++) {
			ind[d1]=i1;
			ind[d2]=i2;
			ret.setValue(m_array.value(ind), i1,i2);
		}
		return ret;
	}
}

class DotGrid extends Pane {
    private static final double SPACING_X = 25;
    private static final double SPACING_Y = 20;
    private static final double RADIUS = 1.5;
    private Canvas canvas;
 
    public DotGrid() {
		canvas=new Canvas();
        getChildren().add(canvas);
    }
 
    @Override protected void layoutChildren() {
        final int top = (int)snappedTopInset();
        final int right = (int)snappedRightInset();
        final int bottom = (int)snappedBottomInset();
        final int left = (int)snappedLeftInset();
        final int w = (int)getWidth() - left - right;
        final int h = (int)getHeight() - top - bottom;
        canvas.setLayoutX(left);
        canvas.setLayoutY(top);
        if (w != canvas.getWidth() || h != canvas.getHeight()) {
            canvas.setWidth(w);
            canvas.setHeight(h);
            GraphicsContext g = canvas.getGraphicsContext2D();
            g.clearRect(0, 0, w, h);
            g.setFill(Color.gray(0,0.2));
 
            for (int x = 0; x < w; x += SPACING_X) {
                for (int y = 0; y < h; y += SPACING_Y) {
                    double offsetY = (y%(2*SPACING_Y)) == 0 ? SPACING_X /2 : 0;
                    g.fillOval(x-RADIUS+offsetY,y-RADIUS,RADIUS+RADIUS,RADIUS+RADIUS);
                }
            }
        }
    }
}
