package org.pathwayeditor.visualeditor.controller;

import java.util.Iterator;
import java.util.SortedSet;

import org.apache.log4j.Logger;
import org.pathwayeditor.businessobjects.drawingprimitives.IDrawingNodeAttribute;
import org.pathwayeditor.businessobjects.drawingprimitives.ILinkAttribute;
import org.pathwayeditor.businessobjects.drawingprimitives.IShapeAttribute;
import org.pathwayeditor.businessobjects.drawingprimitives.attributes.RGB;
import org.pathwayeditor.businessobjects.drawingprimitives.listeners.CanvasAttributePropertyChange;
import org.pathwayeditor.businessobjects.drawingprimitives.listeners.IAnnotationPropertyChangeEvent;
import org.pathwayeditor.businessobjects.drawingprimitives.listeners.IAnnotationPropertyChangeListener;
import org.pathwayeditor.businessobjects.drawingprimitives.listeners.ICanvasAttributeChangeListener;
import org.pathwayeditor.businessobjects.drawingprimitives.listeners.ICanvasAttributePropertyChangeEvent;
import org.pathwayeditor.businessobjects.drawingprimitives.listeners.ICanvasAttributeResizedEvent;
import org.pathwayeditor.businessobjects.drawingprimitives.listeners.ICanvasAttributeTranslationEvent;
import org.pathwayeditor.businessobjects.drawingprimitives.properties.IAnnotationProperty;
import org.pathwayeditor.businessobjects.drawingprimitives.properties.IAnnotationPropertyVisitor;
import org.pathwayeditor.businessobjects.drawingprimitives.properties.IBooleanAnnotationProperty;
import org.pathwayeditor.businessobjects.drawingprimitives.properties.IIntegerAnnotationProperty;
import org.pathwayeditor.businessobjects.drawingprimitives.properties.IListAnnotationProperty;
import org.pathwayeditor.businessobjects.drawingprimitives.properties.INumberAnnotationProperty;
import org.pathwayeditor.businessobjects.drawingprimitives.properties.IPlainTextAnnotationProperty;
import org.pathwayeditor.figure.figuredefn.FigureController;
import org.pathwayeditor.figure.figuredefn.IAnchorLocator;
import org.pathwayeditor.figure.figuredefn.IFigureController;
import org.pathwayeditor.figure.geometry.Dimension;
import org.pathwayeditor.figure.geometry.Envelope;
import org.pathwayeditor.figure.geometry.IConvexHull;
import org.pathwayeditor.figure.geometry.Point;
import org.pathwayeditor.figure.geometry.RectangleHull;
import org.pathwayeditor.visualeditor.feedback.FigureCompilationCache;
import org.pathwayeditor.visualeditor.geometry.IIntersectionCalcnFilter;
import org.pathwayeditor.visualeditor.geometry.IIntersectionCalculator;

import uk.ac.ed.inf.graph.compound.CompoundNodePair;
import uk.ac.ed.inf.graph.compound.ICompoundEdge;
import uk.ac.ed.inf.graph.compound.ICompoundGraphElement;
import uk.ac.ed.inf.graph.compound.ICompoundNode;

public class ShapeController extends NodeController implements IShapeController {
	private final Logger logger = Logger.getLogger(this.getClass());
	private final ICompoundNode domainNode;
	private final IShapeAttribute shapeAttribute;
	private final ICompoundGraphElement parentAttribute;
	private final ICanvasAttributeChangeListener shapePropertyChangeListener;
	private final IAnnotationPropertyChangeListener annotPropChangeListener;
	private final IFigureController figureController;
	private final ICanvasAttributeChangeListener parentDrawingNodePropertyChangeListener;
	private boolean isActive;
	
	public ShapeController(IViewControllerModel viewModel, ICompoundNode node, int index) {
		super(viewModel, index);
		
		this.domainNode = node;
		this.shapeAttribute = (IShapeAttribute)node.getAttribute();
		this.parentAttribute = this.domainNode.getParent();
		shapePropertyChangeListener = new ICanvasAttributeChangeListener() {
			@Override
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
				else if(e.getPropertyChange().equals(CanvasAttributePropertyChange.BOUNDS)){
					IShapeAttribute attribute = (IShapeAttribute)e.getAttribute();
					Envelope oldDrawnBounds = figureController.getConvexHull().getEnvelope();
					figureController.setRequestedEnvelope(attribute.getBounds());
					figureController.generateFigureDefinition();
					recalculateSrcLinks();
					recalculateTgtLinks();
					notifyDrawnBoundsChanged(oldDrawnBounds, figureController.getConvexHull().getEnvelope());
				}
				else if(e.getPropertyChange().equals(CanvasAttributePropertyChange.LINE_STYLE)){
					IShapeAttribute attribute = (IShapeAttribute)e.getAttribute();
					figureController.setLineStyle(attribute.getLineStyle());
					figureController.generateFigureDefinition();
				}
			}

			@Override
			public void nodeTranslated(ICanvasAttributeTranslationEvent e) {
			}

			@Override
			public void nodeResized(ICanvasAttributeResizedEvent e) {
			}
		};
		annotPropChangeListener = new IAnnotationPropertyChangeListener() {
			@Override
			public void propertyChange(IAnnotationPropertyChangeEvent e) {
				IAnnotationProperty prop = e.getPropertyDefinition();
				ICompoundGraphElement node = ((IShapeAttribute)prop.getOwner()).getCurrentElement();
				assignBindVariablesToProperties((IShapeAttribute)node.getAttribute(), figureController);
				figureController.generateFigureDefinition();
			}	
		};
		parentDrawingNodePropertyChangeListener = new ICanvasAttributeChangeListener() {
			@Override
			public void nodeTranslated(ICanvasAttributeTranslationEvent e) {
				shapeAttribute.translate(e.getTranslationDelta());
			}
			
			@Override
			public void nodeResized(ICanvasAttributeResizedEvent e) {
			}

			@Override
			public void propertyChange(ICanvasAttributePropertyChangeEvent e) {
			}
		};
		this.figureController = createController(shapeAttribute);
	}

	@Override
	public void activate(){
		addListeners(this.domainNode);
		this.isActive = true;
	}
	
	private void assignBindVariablesToProperties(IShapeAttribute att, final IFigureController figureController) {
		for(final String varName : figureController.getBindVariableNames()){
			if(att.containsProperty(varName)){
				IAnnotationProperty prop = att.getProperty(varName);
				prop.visit(new IAnnotationPropertyVisitor(){

					@Override
					public void visitBooleanAnnotationProperty(IBooleanAnnotationProperty prop) {
						figureController.setBindBoolean(varName, prop.getValue());
					}

					@Override
					public void visitIntegerAnnotationProperty(IIntegerAnnotationProperty prop) {
						figureController.setBindInteger(varName, prop.getValue());
					}

					@Override
					public void visitListAnnotationProperty(IListAnnotationProperty prop) {
						logger.error("Unmatched bind variable: " + varName + ". Property has type that cannot be matched to bind variable of same name: " + prop);
					}

					@Override
					public void visitNumberAnnotationProperty(INumberAnnotationProperty numProp) {
						figureController.setBindDouble(varName, numProp.getValue().doubleValue());
					}

					@Override
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
//		FigureDefinitionCompiler compiler = new FigureDefinitionCompiler(attribute.getShapeDefinition());
//		compiler.compile();
//		IFigureController figureController = new FigureController(compiler.getCompiledFigureDefinition());
		IFigureController figureController = new FigureController(FigureCompilationCache.getInstance().lookup(attribute.getShapeDefinition()));
		figureController.setRequestedEnvelope(attribute.getBounds());
		figureController.setFillColour(attribute.getFillColour());
		figureController.setLineColour(attribute.getLineColour());
		figureController.setLineStyle(attribute.getLineStyle());
		figureController.setLineWidth(attribute.getLineWidth());
		assignBindVariablesToProperties(attribute, figureController);
		figureController.generateFigureDefinition();
		return figureController;
	}

	private void recalculateSrcLinks(){
		Iterator<ICompoundEdge> edgeIter = this.domainNode.getOutEdgeIterator();
		while(edgeIter.hasNext()){
			ICompoundEdge link = edgeIter.next();
			ILinkController linkController = this.getViewModel().getLinkController(link);
			CompoundNodePair pair = link.getConnectedNodes();
			IShapeController srcNode = (IShapeController)this.getViewModel().getNodeController(pair.getOutNode());
			IShapeController tgtNode = (IShapeController)this.getViewModel().getNodeController(pair.getInNode());
			changeSourceAnchor(linkController, srcNode, tgtNode);
//			changeTargetAnchor(linkController, srcNode, tgtNode);
		}
	}

	private void recalculateTgtLinks(){
		Iterator<ICompoundEdge> edgeIter = this.domainNode.getInEdgeIterator();
		while(edgeIter.hasNext()){
			ICompoundEdge link = edgeIter.next();
			ILinkController linkController = this.getViewModel().getLinkController(link);
			CompoundNodePair pair = link.getConnectedNodes();
			IShapeController srcNode = (IShapeController)this.getViewModel().getNodeController(pair.getOutNode());
			IShapeController tgtNode = (IShapeController)this.getViewModel().getNodeController(pair.getInNode());
//			changeSourceAnchor(linkController, srcNode, tgtNode);
			changeTargetAnchor(linkController, srcNode, tgtNode);
		}
	}

	public void changeSourceAnchor(ILinkController linkController, IShapeController srcNode, IShapeController tgtNode){
		if(linkController.getLinkDefinition().numBendPoints() > 0){
			// there are bend-points so we use the bp as the reference position
			Point refPoint = linkController.getLinkDefinition().getSourceLineSegment().getTerminus();
			this.calculateSourceAnchor(linkController, srcNode, refPoint);
		}
		else{
			// otherwise we just use the centre positions of the shapes
			// as they will be after the move is completed
			this.calculateSourceAnchor(linkController, srcNode, tgtNode.getConvexHull().getCentre());
			this.calculateTargetAnchor(linkController, tgtNode, srcNode.getConvexHull().getCentre());
		}
	}

	public void changeTargetAnchor(ILinkController linkController, IShapeController srcNode, IShapeController tgtNode){
		if(linkController.getLinkDefinition().numBendPoints() > 0){
			// there are bend-points so we use the bp as the reference position
			Point refPoint = linkController.getLinkDefinition().getTargetLineSegment().getTerminus();
			this.calculateTargetAnchor(linkController, tgtNode, refPoint);
		}
		else{
			// otherwise we just use the centre positions of the shapes
			// as they will be after the move is completed
			this.calculateTargetAnchor(linkController, tgtNode, srcNode.getConvexHull().getCentre());
			this.calculateSourceAnchor(linkController, srcNode, tgtNode.getConvexHull().getCentre());
		}
	}
	
	private void calculateSourceAnchor(ILinkController linkController, IShapeController srcNode, Point refPosn){
		Point newAnchorLocn = getRevisedAnchorPosition(srcNode, refPosn);
		ILinkAttribute linkAtt = (ILinkAttribute)linkController.getDrawingElement().getAttribute();
		linkAtt.getSourceTerminus().setLocation(newAnchorLocn);
		if(logger.isTraceEnabled()){
			logger.trace("Recalculating src anchor. Reference bp = " + refPosn + ",anchor=" + newAnchorLocn);
		}
	}
	
	private void calculateTargetAnchor(ILinkController linkController, IShapeController tgtNode, Point refPosn){
		Point newAnchorLocn = getRevisedAnchorPosition(tgtNode, refPosn);
		ILinkAttribute linkAtt = (ILinkAttribute)linkController.getDrawingElement().getAttribute();
		linkAtt.getTargetTerminus().setLocation(newAnchorLocn);
		if(logger.isTraceEnabled()){
			logger.trace("Recalculating tgt anchor. Reference bp = " + refPosn + ",anchor=" + newAnchorLocn);
		}
	}
	
	private Point getRevisedAnchorPosition(IShapeController nodeController, Point refPoint){
		IFigureController controller = nodeController.getFigureController();
		IAnchorLocator calc = controller.getAnchorLocatorFactory().createAnchorLocator();
		calc.setOtherEndPoint(refPoint);
		return calc.calcAnchorPosition();
	}
	
	
	private void addListeners(ICompoundNode node) {
		IShapeAttribute attribute = (IShapeAttribute)node.getAttribute();
		attribute.addChangeListener(shapePropertyChangeListener);
		Iterator<IAnnotationProperty> iter = attribute.propertyIterator();
		while(iter.hasNext()){
			IAnnotationProperty prop = iter.next();
			prop.addChangeListener(annotPropChangeListener);
		}
		((IDrawingNodeAttribute)this.parentAttribute.getAttribute()).addChangeListener(parentDrawingNodePropertyChangeListener);
	}
	
	private void removeListeners() {
		final IShapeAttribute attribute = (IShapeAttribute)this.domainNode.getAttribute();
		attribute.removeChangeListener(shapePropertyChangeListener);
		Iterator<IAnnotationProperty> iter = attribute.propertyIterator();
		while(iter.hasNext()){
			IAnnotationProperty prop = iter.next();
			prop.removeChangeListener(annotPropChangeListener);
		}
		IDrawingNodeAttribute att = (IDrawingNodeAttribute)parentAttribute.getAttribute();
		att.removeChangeListener(parentDrawingNodePropertyChangeListener);
	}
		
	@Override
	public ICompoundNode getDrawingElement() {
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
	public boolean canResize(Point originDelta, Dimension resizeDelta) {
		boolean retVal = false;
		// algorithm is to find the intersecting shapes and then check if the
		// children and parents are in the intersection list
		IDrawingNodeAttribute nodeAtt = (IDrawingNodeAttribute)this.domainNode.getAttribute();
		Envelope newBounds = nodeAtt.getBounds().resize(originDelta, resizeDelta);
		if(logger.isTraceEnabled()){
			logger.trace("In can resize. New bounds = " + newBounds + ",originDelta=" + originDelta + ",resizeDelta=" + resizeDelta);
		}
		if(newBounds.getDimension().getWidth() > 0.0 && newBounds.getDimension().getHeight() > 0.0){
			INodeController parentNode = this.getViewModel().getNodeController((ICompoundNode)this.domainNode.getParent());
			IIntersectionCalculator intCal = this.getViewModel().getIntersectionCalculator();
			intCal.setFilter(new IIntersectionCalcnFilter() {
				@Override
				public boolean accept(IDrawingElementController node) {
					return !(node instanceof ILabelController);
				}
			});
			SortedSet<IDrawingElementController> intersectingNodes = intCal.findIntersectingNodes(this.getConvexHull().changeEnvelope(newBounds), this);
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

	private boolean childrenIntersect(IShapeController parentNode, SortedSet<IDrawingElementController> intersectingNodes){
		Iterator<ICompoundNode> iter = parentNode.getDrawingElement().getChildCompoundGraph().nodeIterator();
		boolean retVal = true;
		while(iter.hasNext() && retVal){
			INodeController child = this.getViewModel().getNodeController(iter.next());
			retVal = intersectingNodes.contains(child);
		}
		return retVal;
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

	@Override
	public Envelope getDrawnBounds() {
		return this.figureController.getEnvelope();
	}

	@Override
	public boolean containsPoint(Point p) {
		IConvexHull attributeHull = this.getConvexHull();
		boolean retVal = attributeHull.containsPoint(p); 
		if(logger.isTraceEnabled()){
			logger.trace("Testing contains node:" + this + ",retVal=" + retVal + ", hull=" + attributeHull + ", point=" + p);
		}
		return retVal;
	}

	@Override
	public boolean intersectsHull(IConvexHull queryHull) {
		return this.figureController.getConvexHull().hullsIntersect(queryHull);
	}

	@Override
	public boolean intersectsBounds(Envelope otherBounds) {
		IConvexHull otherHull = new RectangleHull(otherBounds);
		return intersectsHull(otherHull);
	}
}
