package org.pathwayeditor.visualeditor.controller;

import org.apache.log4j.Logger;
import org.pathwayeditor.businessobjects.drawingprimitives.IDrawingNodeAttribute;
import org.pathwayeditor.businessobjects.drawingprimitives.ILabelAttribute;
import org.pathwayeditor.businessobjects.drawingprimitives.ILabelNode;
import org.pathwayeditor.businessobjects.drawingprimitives.listeners.CanvasAttributePropertyChange;
import org.pathwayeditor.businessobjects.drawingprimitives.listeners.ICanvasAttributeChangeListener;
import org.pathwayeditor.businessobjects.drawingprimitives.listeners.ICanvasAttributePropertyChangeEvent;
import org.pathwayeditor.businessobjects.drawingprimitives.listeners.ICanvasAttributeResizedEvent;
import org.pathwayeditor.businessobjects.drawingprimitives.listeners.ICanvasAttributeTranslationEvent;
import org.pathwayeditor.figure.figuredefn.FigureController;
import org.pathwayeditor.figure.figuredefn.IFigureController;
import org.pathwayeditor.figure.geometry.Dimension;
import org.pathwayeditor.figure.geometry.Envelope;
import org.pathwayeditor.figure.geometry.IConvexHull;
import org.pathwayeditor.figure.geometry.Point;
import org.pathwayeditor.figure.geometry.RectangleHull;
import org.pathwayeditor.visualeditor.feedback.FigureCompilationCache;

public class LabelController extends NodeController implements ILabelController {
	private final Logger logger = Logger.getLogger(this.getClass());
	private final String LABEL_DEFINITION =
		"curbounds /h exch def /w exch def /y exch def /x exch def\n" +
		"/xoffset { w mul x add } def /yoffset { h mul y add } def\n" +
		"/cardinalityBox { /card exch def /fsize exch def /cpy exch def /cpx exch def\n" +
		"card cvs textbounds /hoff exch curlinewidth 2 mul add h div def /woff exch curlinewidth 2 mul add w div def \n" +
		"gsave\n" +
		"null setfillcol cpx xoffset cpy yoffset (C) card cvs text\n" +
		"grestore\n" +
		"} def\n" +
		"gsave\n" +
		":noborderFlag \n{" +
		"null setlinecol\n" +
		"} if\n" +
		"0.0 xoffset 0.0 yoffset w h rect\n" +
		"grestore\n" +
		"0.5 0.5 :labelFontSize :labelText cardinalityBox\n";
	private final ILabelNode domainNode;
	private IDrawingNodeAttribute parentAttribute;
	private final ICanvasAttributeChangeListener drawingNodePropertyChangeListener;
	private final ICanvasAttributeChangeListener parentDrawingNodePropertyChangeListener;
//	private INodeControllerChangeListener parentNodePrimitiveChangeListener;
	private final IFigureController controller;
	private boolean isActive;
	
	public LabelController(IViewControllerModel viewModel, ILabelNode node, int index) {
		super(viewModel, index);
		this.domainNode = node;
		this.parentAttribute = (IDrawingNodeAttribute)this.domainNode.getGraphElement().getParent().getAttribute();
		this.isActive = false;
		drawingNodePropertyChangeListener = new ICanvasAttributeChangeListener() {
			@Override
			public void propertyChange(ICanvasAttributePropertyChangeEvent e) {
				if(e.getPropertyChange().equals(CanvasAttributePropertyChange.BOUNDS)){
					Envelope oldDrawnBounds = controller.getConvexHull().getEnvelope();
					IDrawingNodeAttribute attribute = (IDrawingNodeAttribute)e.getAttribute();
					controller.setRequestedEnvelope(attribute.getBounds());
					controller.generateFigureDefinition();
					notifyDrawnBoundsChanged(oldDrawnBounds, controller.getConvexHull().getEnvelope());
				}
			}

			@Override
			public void nodeTranslated(ICanvasAttributeTranslationEvent e) {
			}

			@Override
			public void nodeResized(ICanvasAttributeResizedEvent e) {
			}
		};
//		this.parentNodePrimitiveChangeListener = new INodeControllerChangeListener(){
//
//			@Override
//			public void nodeTranslated(INodeTranslationEvent e) {
//				translatePrimitive(e.getTranslationDelta());
//			}
//
//			@Override
//			public void nodeResized(INodeResizeEvent e) {
//			}
//
//			@Override
//			public void changedBounds(INodeBoundsChangeEvent e) {
//				
//			}
//			
//		};
		parentDrawingNodePropertyChangeListener = new ICanvasAttributeChangeListener() {
			
			@Override
			public void propertyChange(ICanvasAttributePropertyChangeEvent e) {
			}
			
			@Override
			public void nodeTranslated(ICanvasAttributeTranslationEvent e) {
				domainNode.getAttribute().translate(e.getTranslationDelta());
			}
			
			@Override
			public void nodeResized(ICanvasAttributeResizedEvent e) {
			}
		};
		this.controller = createController(node.getAttribute());
	}

	private IFigureController createController(ILabelAttribute attribute){
//		FigureDefinitionCompiler compiler = new FigureDefinitionCompiler(LABEL_DEFINITION);
//		compiler.compile();
//		IFigureController figureController = new FigureController(compiler.getCompiledFigureDefinition());
		IFigureController figureController = new FigureController(FigureCompilationCache.getInstance().lookup(LABEL_DEFINITION));
		figureController.setRequestedEnvelope(attribute.getBounds());
		figureController.setFillColour(attribute.getBackgroundColor());
		figureController.setLineColour(attribute.getForegroundColor());
		figureController.setLineStyle(attribute.getLineStyle());
		figureController.setLineWidth(attribute.getLineWidth());
		figureController.setBindDouble("labelFontSize", 10.0);
		figureController.setBindString("labelText", attribute.getProperty().getValue().toString());
		figureController.setBindBoolean("noborderFlag", attribute.hasNoBorder());
		figureController.generateFigureDefinition();
		return figureController;
	}

	@Override
	public ILabelNode getDrawingElement() {
		return this.domainNode;
	}

	@Override
	public Envelope getBounds() {
		return this.controller.getRequestedEnvelope();
	}

	@Override
	public IConvexHull getConvexHull() {
		return this.controller.getConvexHull();
	}

//	@Override
//	public void translatePrimitive(Point translation) {
//		Envelope currBounds = this.domainNode.getBounds();
//		controller.setRequestedEnvelope(currBounds.translate(translation));
//		controller.generateFigureDefinition();
//		this.notifyTranslation(translation);
//	}

	@Override
	public void inactivate() {
		this.domainNode.getAttribute().removeChangeListener(drawingNodePropertyChangeListener);
		parentAttribute = (IDrawingNodeAttribute)this.domainNode.getGraphElement().getParent().getAttribute(); 
		parentAttribute.removeChangeListener(parentDrawingNodePropertyChangeListener);
//		if(this.getViewModel().containsDrawingElement(parentAttribute)){
//			INodeController parentNode = this.getViewModel().getNodePrimitive(parentAttribute);
//			parentNode.removeNodePrimitiveChangeListener(this.parentNodePrimitiveChangeListener);
//		}
		this.isActive = false;
	}


	@Override
	public void activate() {
		this.domainNode.getAttribute().addChangeListener(this.drawingNodePropertyChangeListener);
		parentAttribute.addChangeListener(parentDrawingNodePropertyChangeListener);
//		INodeController parentNode = this.getViewModel().getNodePrimitive(this.domainNode.getCurrentDrawingElement().getParentNode().getAttribute());
//		parentNode.addNodePrimitiveChangeListener(this.parentNodePrimitiveChangeListener);
//		this.resyncToModel();
		this.isActive = true;
	}

	@Override
	public IFigureController getFigureController() {
		return this.controller;
	}

//	@Override
//	public void resizePrimitive(Point originDelta, Dimension resizeDelta) {
//		Envelope currBounds = this.domainNode.getBounds();
//		controller.setRequestedEnvelope(currBounds.translate(originDelta).changeDimension(currBounds.getDimension().resize(resizeDelta.getWidth(), resizeDelta.getHeight())));
//		controller.generateFigureDefinition();
//		this.notifyResize(originDelta, resizeDelta);
//	}

	@Override
	public boolean canResize(Point originDelta, Dimension resizeDelta) {
		Envelope newBounds = this.getBounds().resize(originDelta, resizeDelta);
		return (newBounds.getDimension().getWidth() > 0.0 && newBounds.getDimension().getHeight() > 0.0);
	}
	
//	@Override
//	public void redefinedSyncroniseToModel() {
//		ILabelAttribute attribute = this.domainNode;
//		Envelope originalBounds = getBounds();
//		controller.setRequestedEnvelope(attribute.getBounds());
//		controller.setFillColour(attribute.getBackgroundColor());
//		controller.setLineColour(attribute.getForegroundColor());
//		controller.setLineStyle(attribute.getLineStyle());
//		controller.setLineWidth(attribute.getLineWidth());
//		controller.generateFigureDefinition();
//		notifyChangedBounds(originalBounds, getBounds());
//	}

	@Override
	public boolean isActive() {
		return this.isActive;
	}

	@Override
	public Envelope getDrawnBounds() {
		return this.controller.getEnvelope();
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
		return this.controller.getConvexHull().hullsIntersect(queryHull);
	}

	@Override
	public boolean intersectsBounds(Envelope drawnBounds) {
		IConvexHull otherHull = new RectangleHull(drawnBounds);
		return intersectsHull(otherHull);
	}
}
