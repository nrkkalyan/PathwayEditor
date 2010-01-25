package org.pathwayeditor.visualeditor.controller;

import org.pathwayeditor.businessobjects.drawingprimitives.ICanvasAttribute;

public interface IDrawingPrimitiveController extends Comparable<IDrawingPrimitiveController> {

	IViewControllerStore getViewModel();
	
	/**
	 * Gets the drawing element that is the domain model for this primitive.
	 * @return the drawing element, which cannot be null.
	 */
	ICanvasAttribute getDrawingElement();
	
	/**
	 * Adds listeners and takes other steps which require that the view model has been fully created  
	 */
	void activate();
	
	boolean isActive();
	
	void inactivate();
	
	/**
	 * Turns off listeners and finalises resources on the assumption that this primitive is to be discarded.
	 */
	void dispose();

	void resyncToModel();
	
//	void addDrawingPrimitiveControllerListener(IDrawingPrimitiveControllerListener listener);
//
//	void removeDrawingPrimitiveControllerListener(IDrawingPrimitiveControllerListener listener);
//	
//	List<IDrawingPrimitiveControllerListener> getDrawingPrimitiveControllerListeners();
}
