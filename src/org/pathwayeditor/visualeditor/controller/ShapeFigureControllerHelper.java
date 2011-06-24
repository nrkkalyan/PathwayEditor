/*
  Licensed to the Court of the University of Edinburgh (UofE) under one
  or more contributor license agreements.  See the NOTICE file
  distributed with this work for additional information
  regarding copyright ownership.  The UofE licenses this file
  to you under the Apache License, Version 2.0 (the
  "License"); you may not use this file except in compliance
  with the License.  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing,
  software distributed under the License is distributed on an
  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  KIND, either express or implied.  See the License for the
  specific language governing permissions and limitations
  under the License.
*/
package org.pathwayeditor.visualeditor.controller;

import org.apache.log4j.Logger;
import org.pathwayeditor.businessobjects.drawingprimitives.IShapeAttribute;
import org.pathwayeditor.businessobjects.drawingprimitives.properties.IAnnotationProperty;
import org.pathwayeditor.businessobjects.drawingprimitives.properties.IAnnotationPropertyVisitor;
import org.pathwayeditor.businessobjects.drawingprimitives.properties.IBooleanAnnotationProperty;
import org.pathwayeditor.businessobjects.drawingprimitives.properties.IIntegerAnnotationProperty;
import org.pathwayeditor.businessobjects.drawingprimitives.properties.IListAnnotationProperty;
import org.pathwayeditor.businessobjects.drawingprimitives.properties.INumberAnnotationProperty;
import org.pathwayeditor.businessobjects.drawingprimitives.properties.IPlainTextAnnotationProperty;
import org.pathwayeditor.figure.rendering.FigureRenderingController;
import org.pathwayeditor.figure.rendering.IFigureRenderingController;
import org.pathwayeditor.visualeditor.feedback.FigureCompilationCache;

public class ShapeFigureControllerHelper implements IFigureControllerHelper {
	private final Logger logger = Logger.getLogger(this.getClass());
	private final IShapeAttribute attribute;
	private IFigureRenderingController figureRenderingController; 

	public ShapeFigureControllerHelper(IShapeAttribute nodeAttribute){
		this.attribute = nodeAttribute; 
	}

	
	/* (non-Javadoc)
	 * @see org.pathwayeditor.visualeditor.controller.IFigureControllerFactory#createFigureController(org.pathwayeditor.businessobjects.drawingprimitives.IShapeAttribute)
	 */
	@Override
	public void createFigureController(){
		figureRenderingController = new FigureRenderingController(FigureCompilationCache.getInstance().lookup(attribute.getShapeDefinition()));
		figureRenderingController.setEnvelope(attribute.getBounds());
		figureRenderingController.setFillColour(attribute.getFillColour());
		figureRenderingController.setLineColour(attribute.getLineColour());
		figureRenderingController.setLineStyle(attribute.getLineStyle());
		figureRenderingController.setLineWidth(attribute.getLineWidth());
		refreshBoundProperties();
		figureRenderingController.generateFigureDefinition();
	}
	
	@Override
	public IFigureRenderingController getFigureController(){
		return this.figureRenderingController;
	}
	
	@Override
	public void refreshBoundProperties() {
		assignBoundVariablesToProperties();
		figureRenderingController.generateFigureDefinition();
	}
	
	
	private void assignBoundVariablesToProperties(){
		for(final String varName : figureRenderingController.getBindVariableNames()){
			if(attribute.containsProperty(varName)){
				IAnnotationProperty prop = attribute.getProperty(varName);
				prop.visit(new IAnnotationPropertyVisitor(){

					@Override
					public void visitBooleanAnnotationProperty(IBooleanAnnotationProperty prop) {
						figureRenderingController.setBindBoolean(varName, prop.getValue());
					}

					@Override
					public void visitIntegerAnnotationProperty(IIntegerAnnotationProperty prop) {
						figureRenderingController.setBindInteger(varName, prop.getValue());
					}

					@Override
					public void visitListAnnotationProperty(IListAnnotationProperty prop) {
						logger.error("Unmatched bind variable: " + varName + ". Property has type that cannot be matched to bind variable of same name: " + prop);
					}

					@Override
					public void visitNumberAnnotationProperty(INumberAnnotationProperty numProp) {
						figureRenderingController.setBindDouble(varName, numProp.getValue().doubleValue());
					}

					@Override
					public void visitPlainTextAnnotationProperty(IPlainTextAnnotationProperty prop) {
						figureRenderingController.setBindString(varName, prop.getValue());
					}
					
				});
			}
			else{
				logger.warn("Unmatched bind variable: " + varName
						+ ". No property matched bind variable name was found.");
			}
		}
	}


	@Override
	public void refreshGraphicalAttributes() {
		this.figureRenderingController.generateFigureDefinition();
	}


	@Override
	public void refreshAll() {
		this.refreshBoundProperties();
	}
}
