package org.pathwayeditor.visualeditor.controller;

import java.util.Iterator;
import java.util.SortedSet;

import org.apache.log4j.Logger;
import org.pathwayeditor.businessobjects.drawingprimitives.IDrawingNodeAttribute;
import org.pathwayeditor.businessobjects.drawingprimitives.IShapeAttribute;
import org.pathwayeditor.businessobjects.drawingprimitives.IShapeNode;
import org.pathwayeditor.businessobjects.drawingprimitives.attributes.RGB;
import org.pathwayeditor.businessobjects.drawingprimitives.listeners.CanvasAttributePropertyChange;
import org.pathwayeditor.businessobjects.drawingprimitives.listeners.IAnnotationPropertyChangeEvent;
import org.pathwayeditor.businessobjects.drawingprimitives.listeners.IAnnotationPropertyChangeListener;
import org.pathwayeditor.businessobjects.drawingprimitives.listeners.ICanvasAttributePropertyChangeEvent;
import org.pathwayeditor.businessobjects.drawingprimitives.listeners.ICanvasAttributePropertyChangeListener;
import org.pathwayeditor.businessobjects.drawingprimitives.listeners.IDrawingNodeAttributeListener;
import org.pathwayeditor.businessobjects.drawingprimitives.listeners.IDrawingNodeAttributeResizedEvent;
import org.pathwayeditor.businessobjects.drawingprimitives.listeners.IDrawingNodeAttributeTranslationEvent;
import org.pathwayeditor.businessobjects.drawingprimitives.properties.IAnnotationProperty;
import org.pathwayeditor.businessobjects.drawingprimitives.properties.IAnnotationPropertyVisitor;
import org.pathwayeditor.businessobjects.drawingprimitives.properties.IBooleanAnnotationProperty;
import org.pathwayeditor.businessobjects.drawingprimitives.properties.IIntegerAnnotationProperty;
import org.pathwayeditor.businessobjects.drawingprimitives.properties.IListAnnotationProperty;
import org.pathwayeditor.businessobjects.drawingprimitives.properties.INumberAnnotationProperty;
import org.pathwayeditor.businessobjects.drawingprimitives.properties.IPlainTextAnnotationProperty;
import org.pathwayeditor.figure.figuredefn.FigureController;
import org.pathwayeditor.figure.figuredefn.IFigureController;
import org.pathwayeditor.figure.geometry.Dimension;
import org.pathwayeditor.figure.geometry.Envelope;
import org.pathwayeditor.figure.geometry.IConvexHull;
import org.pathwayeditor.figure.geometry.Point;
import org.pathwayeditor.figurevm.FigureDefinitionCompiler;
import org.pathwayeditor.visualeditor.geometry.IIntersectionCalcnFilter;
import org.pathwayeditor.visualeditor.geometry.INodeIntersectionCalculator;
import org.pathwayeditor.visualeditor.geometry.ShapeIntersectionCalculator;

public class ShapeController extends NodeController implements IShapeController {
	private final Logger logger = Logger.getLogger(this.getClass());
	private IShapeAttribute domainNode;
	private IDrawingNodeAttribute parentAttribute;
	private final ICanvasAttributePropertyChangeListener shapePropertyChangeListener;
	private final IAnnotationPropertyChangeListener annotPropChangeListener;
	private IFigureController figureController;
	private final INodeControllerChangeListener parentNodePrimitivateChangeListener;
	private final IDrawingNodeAttributeListener parentDrawingNodePropertyChangeListener;
	private boolean isActive;
	
	public ShapeController(IViewControllerStore viewModel, IShapeAttribute node) {
		super(viewModel);
		
		this.domainNode = node;
		this.parentAttribute = this.domainNode.getCurrentDrawingElement().getParentNode().getAttribute();
		shapePropertyChangeListener = new ICanvasAttributePropertyChangeListener() {
			public void propertyChange(ICanvasAttributePropertyChangeEvent e) {
				if(e.getPropertyChange().equals(CanvasAttributePropertyChange.LINE_COLOUR)){
					figureController.setLineColour((RGB)e.getNewValue());
					figureController.generateFigureDefinition();
				}
				else if(e.getPropertyChange().equals(CanvasAttributePropertyChange.FILL_COLOUR)){
					figureController.setFillColour((RGB)e.getNewValue());
					figureController.generateFigureDefinition();
				}
				else if(e.getPropertyChange().equals(CanvasAttributePropertyChange.LINE_WIDTH)){
					Double newLineWidth = (Double)e.getNewValue();
					figureController.setLineWidth(newLineWidth);
					figureController.generateFigureDefinition();
				}
				else if(e.getPropertyChange().equals(CanvasAttributePropertyChange.SIZE)
						|| e.getPropertyChange().equals(CanvasAttributePropertyChange.LOCATION)){
					IShapeAttribute attribute = (IShapeAttribute)e.getAttribute();
					Envelope originalBounds = getBounds();
					figureController.setRequestedEnvelope(attribute.getBounds());
					figureController.generateFigureDefinition();
					notifyChangedBounds(originalBounds, getBounds());
				}
				else if(e.getPropertyChange().equals(CanvasAttributePropertyChange.LINE_STYLE)){
					IShapeAttribute attribute = (IShapeAttribute)e.getAttribute();
					figureController.setLineStyle(attribute.getLineStyle());
					figureController.generateFigureDefinition();
				}
			}
		};
		annotPropChangeListener = new IAnnotationPropertyChangeListener() {
			public void propertyChange(IAnnotationPropertyChangeEvent e) {
				IAnnotationProperty prop = e.getPropertyDefinition();
				IShapeNode node = ((IShapeAttribute)prop.getOwner()).getCurrentDrawingElement();
				assignBindVariablesToProperties(node.getAttribute(), figureController);
				figureController.generateFigureDefinition();
			}	
		};
		parentNodePrimitivateChangeListener = new INodeControllerChangeListener(){

			@Override
			public void nodeTranslated(INodeTranslationEvent e) {
				translatePrimitive(e.getTranslationDelta());
			}

			@Override
			public void nodeResized(INodeResizeEvent e) {
			}

			@Override
			public void changedBounds(INodeBoundsChangeEvent e) {
				
			}

		};
		parentDrawingNodePropertyChangeListener = new IDrawingNodeAttributeListener() {
			
			@Override
			public void nodeTranslated(IDrawingNodeAttributeTranslationEvent e) {
				domainNode.translate(e.getTranslationDelta());
			}
			
			@Override
			public void nodeResized(IDrawingNodeAttributeResizedEvent e) {
			}
		};
		this.figureController = createController(domainNode);
	}

	public void activate(){
		addListeners(this.domainNode, figureController);
		this.resyncToModel();
		this.isActive = true;
	}
	
	private void assignBindVariablesToProperties(IShapeAttribute att, final IFigureController figureController) {
		for(final String varName : figureController.getBindVariableNames()){
			if(att.containsProperty(varName)){
				IAnnotationProperty prop = att.getProperty(varName);
				prop.visit(new IAnnotationPropertyVisitor(){

					public void visitBooleanAnnotationProperty(IBooleanAnnotationProperty prop) {
						figureController.setBindBoolean(varName, prop.getValue());
					}

					public void visitIntegerAnnotationProperty(IIntegerAnnotationProperty prop) {
						figureController.setBindInteger(varName, prop.getValue());
					}

					public void visitListAnnotationProperty(IListAnnotationProperty prop) {
						logger.error("Unmatched bind variable: " + varName + ". Property has type that cannot be matched to bind variable of same name: " + prop);
					}

					public void visitNumberAnnotationProperty(INumberAnnotationProperty numProp) {
						figureController.setBindDouble(varName, numProp.getValue().doubleValue());
					}

					public void visitPlainTextAnnotationProperty(IPlainTextAnnotationProperty prop) {
						figureController.setBindString(varName, prop.getValue());
					}
					
				});
			}
			else{
				logger.error("Unmatched bind variable: " + varName
						+ ". No property matched bind variable name was found.");
			}
		}
	}

	private IFigureController createController(IShapeAttribute attribute){
		FigureDefinitionCompiler compiler = new FigureDefinitionCompiler(attribute.getShapeDefinition());
		compiler.compile();
		IFigureController figureController = new FigureController(compiler.getCompiledFigureDefinition());
		figureController.setRequestedEnvelope(attribute.getBounds());
		figureController.setFillColour(attribute.getFillColour());
		figureController.setLineColour(attribute.getLineColour());
		figureController.setLineStyle(attribute.getLineStyle());
		figureController.setLineWidth(attribute.getLineWidth());
		assignBindVariablesToProperties(attribute, figureController);
		figureController.generateFigureDefinition();
		return figureController;
	}

	private void addListeners(IShapeAttribute attribute, IFigureController figureController) {
		attribute.addChangeListener(shapePropertyChangeListener);
		Iterator<IAnnotationProperty> iter = attribute.propertyIterator();
		while(iter.hasNext()){
			IAnnotationProperty prop = iter.next();
			prop.addChangeListener(annotPropChangeListener);
		}
		this.parentAttribute.addDrawingNodeAttributeListener(parentDrawingNodePropertyChangeListener);
		INodeController parentNode = this.getViewModel().getNodePrimitive(parentAttribute);
		parentNode.addNodePrimitiveChangeListener(this.parentNodePrimitivateChangeListener);
	}
	
	private void removeListeners() {
		final IShapeAttribute attribute = this.domainNode;
		attribute.removeChangeListener(shapePropertyChangeListener);
		Iterator<IAnnotationProperty> iter = attribute.propertyIterator();
		while(iter.hasNext()){
			IAnnotationProperty prop = iter.next();
			prop.removeChangeListener(annotPropChangeListener);
		}
		parentAttribute.removeDrawingNodeAttributeListener(parentDrawingNodePropertyChangeListener);
		if(this.getViewModel().containsDrawingElement(parentAttribute)){
			INodeController parentNode = this.getViewModel().getNodePrimitive(parentAttribute);
			parentNode.removeNodePrimitiveChangeListener(this.parentNodePrimitivateChangeListener);
		}
	}
		
	@Override
	public IShapeAttribute getDrawingElement() {
		return this.domainNode;
	}

	@Override
	public IFigureController getFigureController() {
		return this.figureController;
	}

	@Override
	public Envelope getBounds() {
		return this.figureController.getRequestedEnvelope();
	}

	@Override
	public IConvexHull getConvexHull() {
		return this.figureController.getConvexHull();
	}

	@Override
	public void translatePrimitive(Point translation) {
		Envelope currBounds = this.domainNode.getBounds();
		figureController.setRequestedEnvelope(currBounds.translate(translation));
		figureController.generateFigureDefinition();
		notifyTranslation(translation);
	}

	@Override
	public int compareTo(IDrawingPrimitiveController o) {
		Integer otherIndex = o.getDrawingElement().getCreationSerial();
		return Integer.valueOf(this.domainNode.getCreationSerial()).compareTo(otherIndex);
	}

	@Override
	protected void nodeDisposalHook() {
		if(this.isActive()){
			inactivate();
		}
		// clear all listeners to this instance too.
		this.figureController = null;
		this.domainNode = null;
		this.parentAttribute = null;
	}

	@Override
	public void resizePrimitive(Point originDelta, Dimension resizeDelta) {
		Envelope currBounds = this.domainNode.getBounds();
		figureController.setRequestedEnvelope(currBounds.resize(originDelta, resizeDelta));
		figureController.generateFigureDefinition();
		this.notifyResize(originDelta, resizeDelta);
	}

	@Override
	public boolean canResize(Point originDelta, Dimension resizeDelta) {
		boolean retVal = false;
		// algorithm is to find the intersecting shapes and then check if the
		// children and parents are in the intersection list
		Envelope newBounds = this.domainNode.getBounds().resize(originDelta, resizeDelta);
		if(logger.isTraceEnabled()){
			logger.trace("In can resize. New bounds = " + newBounds + ",originDelta=" + originDelta + ",resizeDelta=" + resizeDelta);
		}
		if(newBounds.getDimension().getWidth() > 0.0 && newBounds.getDimension().getHeight() > 0.0){
			INodeController parentNode = this.getViewModel().getNodePrimitive(this.domainNode.getCurrentDrawingElement().getParentNode().getAttribute());
			INodeIntersectionCalculator intCal = new ShapeIntersectionCalculator(this.getViewModel());
			intCal.setFilter(new IIntersectionCalcnFilter() {

				@Override
				public boolean accept(INodeController node) {
					return !(node instanceof ILabelController);
				}

			});
			SortedSet<INodeController> intersectingNodes = intCal.findIntersectingNodes(this.getConvexHull().changeEnvelope(newBounds), this);
			boolean parentIntersects = intersectingNodes.contains(parentNode);
			if(logger.isTraceEnabled()){
				logger.trace("CanResize: intersects with parent" + parentIntersects);
			}
			boolean childrenIntersect = childrenIntersect(this, intersectingNodes);
			if(logger.isTraceEnabled()){
				logger.trace("CanResize: intersects with children" + childrenIntersect);
			}
			retVal = parentIntersects && childrenIntersect;
		}
		return retVal;
	}

	private boolean childrenIntersect(IShapeController parentNode, SortedSet<INodeController> intersectingNodes){
		Iterator<IShapeNode> iter = parentNode.getDrawingElement().getCurrentDrawingElement().getSubModel().shapeNodeIterator();
		boolean retVal = true;
		while(iter.hasNext() && retVal){
			INodeController child = this.getViewModel().getNodePrimitive(iter.next().getAttribute());
			retVal = intersectingNodes.contains(child);
		}
		return retVal;
	}
	
	@Override
	public void redefinedSyncroniseToModel() {
		IShapeAttribute attribute = this.domainNode;
		Envelope originalBounds = this.getBounds();
		figureController.setRequestedEnvelope(attribute.getBounds());
		figureController.setFillColour(attribute.getFillColour());
		figureController.setLineColour(attribute.getLineColour());
		figureController.setLineStyle(attribute.getLineStyle());
		figureController.setLineWidth(attribute.getLineWidth());
		assignBindVariablesToProperties(attribute, figureController);
		figureController.generateFigureDefinition();
		notifyChangedBounds(originalBounds, getBounds());
	}

	@Override
	public void inactivate() {
		removeListeners();
		this.isActive = false;
	}

	@Override
	public boolean isActive() {
		return this.isActive;
	}
}
