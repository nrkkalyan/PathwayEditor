package org.pathwayeditor.visualeditor;

import org.pathwayeditor.businessobjects.drawingprimitives.IShapeNode;
import org.pathwayeditor.figure.figuredefn.IFigureController;

public interface IShapePrimitive extends INodePrimitive {

	IFigureController getFigureController();
	
	@Override
	IShapeNode getDrawingElement();
	
}
