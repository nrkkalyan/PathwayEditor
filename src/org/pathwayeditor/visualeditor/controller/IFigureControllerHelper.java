package org.pathwayeditor.visualeditor.controller;

import org.pathwayeditor.figure.rendering.IFigureRenderingController;

public interface IFigureControllerHelper {

	void createFigureController();

	IFigureRenderingController getFigureController();
	
	void refreshBoundProperties();

	void refreshGraphicalAttributes();
	
	void refreshAll();
	
}