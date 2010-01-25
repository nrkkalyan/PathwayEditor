package org.pathwayeditor.visualeditor.controller;

import java.util.List;

import org.pathwayeditor.businessobjects.drawingprimitives.IDrawingNodeAttribute;
import org.pathwayeditor.figure.geometry.Dimension;
import org.pathwayeditor.figure.geometry.Envelope;
import org.pathwayeditor.figure.geometry.IConvexHull;
import org.pathwayeditor.figure.geometry.Point;

public interface INodeController extends IDrawingPrimitiveController {

	/**
	 * Returns the bounds of the drawn node, which may be different from that of the underlying model.
	 * @return the envelope of the drawn node.
	 */
	Envelope getBounds();
	
	/**
	 * Returns the hull of the drawn node primitive. 
	 * @return the hull of the drawn node primitive.
	 */
	IConvexHull getConvexHull();
	
	/**
	 * Translates the node primitive only, does not change the underlying domain model
	 * @param translation the change from the current domain model location
	 */
	void translatePrimitive(Point translation);
	
	@Override
	IDrawingNodeAttribute getDrawingElement();
	
	void addNodePrimitiveChangeListener(INodeControllerChangeListener listener);
	
	void removeNodePrimitiveChangeListener(INodeControllerChangeListener listener);
	
	List<INodeControllerChangeListener> getNodePrimitiveChangeListeners();

	void resizePrimitive(Point originDelta, Dimension resizeDelta);

	boolean canResize(Point originDelta, Dimension resizeDelta);
	
}
