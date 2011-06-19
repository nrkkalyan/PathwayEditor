package org.pathwayeditor.visualeditor.commands;

import org.pathwayeditor.businessobjects.drawingprimitives.IShapeAttribute;
import org.pathwayeditor.businessobjects.drawingprimitives.attributes.Colour;

public class ChangeShapeLinePropertyChange implements ICommand {
	private IShapeAttribute attribute;
	private Colour newLineColour;
	private Colour oldLineColour;

	public ChangeShapeLinePropertyChange(IShapeAttribute attribute, Colour lineColour) {
		this.attribute = attribute;
		this.newLineColour = lineColour;
	}

	@Override
	public void execute() {
		this.oldLineColour = this.attribute.getLineColour(); 
		redo();
	}

	@Override
	public void undo() {
		this.attribute.setLineColour(oldLineColour);
	}

	@Override
	public void redo() {
		this.attribute.setLineColour(newLineColour);
	}

}
