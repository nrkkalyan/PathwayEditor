package org.pathwayeditor.visualeditor.behaviour;

import org.pathwayeditor.figure.geometry.Dimension;
import org.pathwayeditor.figure.geometry.Point;

public class WestHandleResponse implements IDragResponse {
	private final IResizeOperation operation;
	
	public WestHandleResponse(IResizeOperation operation){
		this.operation = operation;
	}
	
	@Override
	public void dragContinuing(Point delta) {
		Point originDelta = new Point(delta.getX(), 0.0);
		Dimension resizeDelta = new Dimension(-delta.getX(), 0);
		this.operation.resizeContinuing(originDelta, resizeDelta);
	}

	@Override
	public void dragFinished(Point delta) {
		Point originDelta = new Point(delta.getX(), 0.0);
		Dimension resizeDelta = new Dimension(-delta.getX(), 0);
		this.operation.resizeFinished(originDelta, resizeDelta);
	}

	@Override
	public void dragStarted() {
		this.operation.resizeStarted();
	}

}
