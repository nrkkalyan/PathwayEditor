package org.pathwayeditor.visualeditor.behaviour;

import org.pathwayeditor.figure.geometry.Point;
import org.pathwayeditor.visualeditor.controller.IDrawingElementController;


public interface IMouseStateBehaviourController {

	void activate();

	boolean isActivated();
	
	void deactivate();

	Point getAdjustedMousePosition(double x, double y);

	IDrawingElementController findDrawingElementAt(Point location);

}
