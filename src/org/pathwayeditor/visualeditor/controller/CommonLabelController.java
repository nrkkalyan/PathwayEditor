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

import java.awt.Font;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;

import org.apache.log4j.Logger;
import org.pathwayeditor.businessobjects.drawingprimitives.IDrawingNodeAttribute;
import org.pathwayeditor.businessobjects.drawingprimitives.ILabelAttribute;
import org.pathwayeditor.businessobjects.drawingprimitives.ILabelNode;
import org.pathwayeditor.businessobjects.drawingprimitives.attributes.Colour;
import org.pathwayeditor.businessobjects.drawingprimitives.listeners.CanvasAttributePropertyChange;
import org.pathwayeditor.businessobjects.drawingprimitives.listeners.IAnnotationPropertyChangeEvent;
import org.pathwayeditor.businessobjects.drawingprimitives.listeners.IAnnotationPropertyChangeListener;
import org.pathwayeditor.businessobjects.drawingprimitives.listeners.ICanvasAttributeChangeListener;
import org.pathwayeditor.businessobjects.drawingprimitives.listeners.ICanvasAttributePropertyChangeEvent;
import org.pathwayeditor.businessobjects.drawingprimitives.listeners.ICanvasAttributeResizedEvent;
import org.pathwayeditor.businessobjects.drawingprimitives.listeners.ICanvasAttributeTranslationEvent;
import org.pathwayeditor.figure.geometry.Dimension;
import org.pathwayeditor.figure.geometry.Envelope;
import org.pathwayeditor.figure.geometry.IConvexHull;
import org.pathwayeditor.figure.geometry.Point;
import org.pathwayeditor.figure.geometry.RectangleHull;
import org.pathwayeditor.figure.rendering.FigureRenderingController;
import org.pathwayeditor.figure.rendering.IFigureRenderingController;
import org.pathwayeditor.visualeditor.editingview.FigureDefinitionMiniCanvas;
import org.pathwayeditor.visualeditor.editingview.IMiniCanvas;
import org.pathwayeditor.visualeditor.feedback.FigureCompilationCache;

public abstract class CommonLabelController extends NodeController implements ILabelController {
	private static final String FONT_NAME = "Arial";
	private static final double DEFAULT_FONT_HEIGHT = 15.0;
	private static final String LABEL_TEXT = "labelText";
	private static final Dimension MIN_LABEL_SIZE = new Dimension(2.0, DEFAULT_FONT_HEIGHT);
	private final Logger logger = Logger.getLogger(this.getClass());
	private final String LABEL_DEFINITION =
		"curbounds /h exch def /w exch def /y exch def /x exch def\n" +
		"/xoffset { w mul x add } def /yoffset { h mul y add } def\n" +
		"/cardinalityBox { /card exch def /fsize exch def /cpy exch def /cpx exch def\n" +
		"card cvs textbounds /hoff exch curlinewidth 2 mul add h div def /woff exch curlinewidth 2 mul add w div def \n" +
		"gsave\n" +
		"cpx xoffset cpy yoffset (C) card cvs text\n" +
		"grestore\n" +
		"} def\n" +
		"gsave\n" +
		":noborderFlag \n{" +
//		"0 0 0 255 setlinecol\n" +
		"} if\n" +
		"0.0 xoffset 0.0 yoffset w h rect\n" +
		"grestore\n" +
		"0.5 0.5 :labelFontSize :labelText cardinalityBox\n";
	private final ILabelNode domainNode;
	private final IFigureRenderingController controller;
	private boolean isActive;
	private final ICanvasAttributeChangeListener drawingNodePropertyChangeListener;
	private IAnnotationPropertyChangeListener propertyValueChangeListener;
	
	protected CommonLabelController(IViewControllerModel viewModel, ILabelNode node, int index) {
		super(viewModel, index);
		this.domainNode = node;
		this.isActive = false;
		this.controller = createController(node.getAttribute());
		drawingNodePropertyChangeListener = new ICanvasAttributeChangeListener() {
			@Override
			public void propertyChange(ICanvasAttributePropertyChangeEvent e) {
				if(e.getPropertyChange().equals(CanvasAttributePropertyChange.BOUNDS)){
					Envelope oldDrawnBounds = getFigureController().getConvexHull().getEnvelope();
					IDrawingNodeAttribute attribute = (IDrawingNodeAttribute)e.getAttribute();
					getFigureController().setEnvelope(attribute.getBounds());
					getFigureController().generateFigureDefinition();
					notifyDrawnBoundsChanged(oldDrawnBounds, getFigureController().getConvexHull().getEnvelope());
				}
				else if(e.getPropertyChange().equals(CanvasAttributePropertyChange.LINE_COLOUR)){
					getFigureController().setLineColour((Colour)e.getNewValue());
					getFigureController().generateFigureDefinition();
				}
				else if(e.getPropertyChange().equals(CanvasAttributePropertyChange.FILL_COLOUR)){
					getFigureController().setFillColour((Colour)e.getNewValue());
					getFigureController().generateFigureDefinition();
				}
				else if(e.getPropertyChange().equals(CanvasAttributePropertyChange.LINE_WIDTH)){
					Double newLineWidth = (Double)e.getNewValue();
					getFigureController().setLineWidth(newLineWidth);
					getFigureController().generateFigureDefinition();
				}
			}

			@Override
			public void elementTranslated(ICanvasAttributeTranslationEvent e) {
			}

			@Override
			public void nodeResized(ICanvasAttributeResizedEvent e) {
			}
		};
		propertyValueChangeListener = new IAnnotationPropertyChangeListener() {
			@Override
			public void propertyChange(IAnnotationPropertyChangeEvent e) {
				String text = domainNode.getAttribute().getDisplayedContent();
				getFigureController().setBindString(LABEL_TEXT, text);
				Dimension textExtent = MIN_LABEL_SIZE;
				if(!text.isEmpty()){
					textExtent = handleGetTextBounds(text);
				}
				Envelope newBounds = adjustForDefaultTextLength(getConvexHull().getCentre(), textExtent);
				getFigureController().setEnvelope(newBounds);
				getFigureController().generateFigureDefinition();
			}
		};
	}

	private Dimension handleGetTextBounds(String text) {
		double size = DEFAULT_FONT_HEIGHT;
		int style = Font.PLAIN;
    	Font f = new Font(FONT_NAME, style, (int)Math.ceil(size));
    	AffineTransform af = new AffineTransform();
    	FontRenderContext ctx = new FontRenderContext(af, false, false);
    	Rectangle2D bounds = f.getStringBounds(text, ctx);
		return new Dimension(bounds.getWidth(), bounds.getHeight());
	}

	private Envelope adjustForDefaultTextLength(Point centralPosition, Dimension textExtent){
		double textWidth = textExtent.getWidth();
		double textHeight = textExtent.getHeight();
		return new Envelope(new Point(centralPosition.getX() - (textWidth/2), centralPosition.getY() - (textHeight/2)), textExtent);
	}

	private IFigureRenderingController createController(ILabelAttribute attribute){
		IFigureRenderingController figureRenderingController = new FigureRenderingController(FigureCompilationCache.getInstance().lookup(LABEL_DEFINITION));
		figureRenderingController.setEnvelope(attribute.getBounds());
		figureRenderingController.setFillColour(attribute.getBackgroundColor());
		figureRenderingController.setLineColour(attribute.getForegroundColor());
		figureRenderingController.setLineStyle(attribute.getLineStyle());
		figureRenderingController.setLineWidth(attribute.getLineWidth());
		figureRenderingController.setBindDouble("labelFontSize", 10.0);
		figureRenderingController.setBindString(LABEL_TEXT, attribute.getDisplayedContent());
		figureRenderingController.setBindBoolean("noborderFlag", attribute.hasNoBorder());
		figureRenderingController.generateFigureDefinition();
		return figureRenderingController;
	}

	@Override
	public final ILabelNode getDrawingElement() {
		return this.domainNode;
	}

	@Override
	public final Envelope getBounds() {
		return this.controller.getEnvelope();
	}

	@Override
	public final IConvexHull getConvexHull() {
		return this.controller.getConvexHull();
	}

	@Override
	public void inactivate() {
		this.getDrawingElement().getAttribute().removeChangeListener(drawingNodePropertyChangeListener);
		this.getDrawingElement().getAttribute().getProperty().removeChangeListener(propertyValueChangeListener);
		inactivateOverride();
		this.isActive = false;
	}


	protected abstract void inactivateOverride();

	@Override
	public final void activate() {
		this.getDrawingElement().getAttribute().addChangeListener(this.drawingNodePropertyChangeListener);
		this.getDrawingElement().getAttribute().getProperty().addChangeListener(propertyValueChangeListener);
		activateOverride();
		this.isActive = true;
	}

	protected abstract void activateOverride();

	@Override
	public final IFigureRenderingController getFigureController() {
		return this.controller;
	}

	@Override
	public final boolean canResize(Point originDelta, Dimension resizeDelta) {
		Envelope newBounds = this.getBounds().resize(originDelta, resizeDelta);
		return (newBounds.getDimension().getWidth() > 0.0 && newBounds.getDimension().getHeight() > 0.0);
	}
	
	@Override
	public final boolean isActive() {
		return this.isActive;
	}

	@Override
	public final Envelope getDrawnBounds() {
		return this.controller.getEnvelope();
	}

	@Override
	public final boolean containsPoint(Point p) {
		IConvexHull attributeHull = this.getConvexHull();
		boolean retVal = attributeHull.containsPoint(p); 
		if(logger.isTraceEnabled()){
			logger.trace("Testing contains node:" + this + ",retVal=" + retVal + ", hull=" + attributeHull + ", point=" + p);
		}
		return retVal;
	}

	@Override
	public final boolean intersectsHull(IConvexHull queryHull) {
		return this.controller.getConvexHull().hullsIntersect(queryHull);
	}

	@Override
	public final boolean intersectsBounds(Envelope drawnBounds) {
		IConvexHull otherHull = new RectangleHull(drawnBounds);
		return intersectsHull(otherHull);
	}
	
	
	@Override
	public final IMiniCanvas getMiniCanvas(){
		return new FigureDefinitionMiniCanvas(this.controller.getFigureDefinition(), this.controller.getEnvelope());
	}
}
