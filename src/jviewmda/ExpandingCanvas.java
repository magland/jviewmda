package jviewmda;

import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;

/**
 *
 * @author magland
 */
class ExpandingCanvas extends Pane {

	private Canvas m_canvas;
	private EventHandler<Event> m_on_refresh;

	public Canvas canvas() {
		return m_canvas;
	}

	public GraphicsContext getGraphicsContext2D() {
		return m_canvas.getGraphicsContext2D();
	}

	public ExpandingCanvas() {
		m_canvas = new Canvas();
		getChildren().add(m_canvas);
		setPrefWidth(Integer.MAX_VALUE);
		setPrefHeight(Integer.MAX_VALUE);
	}

	public void setOnRefresh(EventHandler<Event> X) {
		m_on_refresh = X;
	}

	@Override
	public boolean isResizable() {
		return true;
	}

	@Override
	protected void layoutChildren() {
		final int top = (int) snappedTopInset();
		final int right = (int) snappedRightInset();
		final int bottom = (int) snappedBottomInset();
		final int left = (int) snappedLeftInset();
		final int w = (int) getWidth() - left - right;
		final int h = (int) getHeight() - top - bottom;
		m_canvas.setLayoutX(left);
		m_canvas.setLayoutY(top);
		if (w != m_canvas.getWidth() || h != m_canvas.getHeight()) {
			m_canvas.setWidth(w);
			m_canvas.setHeight(h);
			m_on_refresh.handle(new Event(Event.ANY));
		}
	}
}
