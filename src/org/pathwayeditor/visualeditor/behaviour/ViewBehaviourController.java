package org.pathwayeditor.visualeditor.behaviour;

import org.apache.log4j.Logger;
import org.pathwayeditor.businessobjects.typedefn.ILinkObjectType;
import org.pathwayeditor.businessobjects.typedefn.IObjectType;
import org.pathwayeditor.businessobjects.typedefn.IShapeObjectType;
import org.pathwayeditor.visualeditor.behaviour.creation.CreationControllerResponses;
import org.pathwayeditor.visualeditor.behaviour.creation.CreationDragResponse;
import org.pathwayeditor.visualeditor.behaviour.creation.IShapeTypeInspector;
import org.pathwayeditor.visualeditor.behaviour.creation.MouseCreationFeedbackResponse;
import org.pathwayeditor.visualeditor.behaviour.creation.ShapeCreationBehaviourStateHandler;
import org.pathwayeditor.visualeditor.behaviour.linkcreation.ILinkTypeInspector;
import org.pathwayeditor.visualeditor.behaviour.linkcreation.LinkCreationBehaviourStateHandler;
import org.pathwayeditor.visualeditor.behaviour.operation.IOperationFactory;
import org.pathwayeditor.visualeditor.behaviour.selection.SelectionControllerResponses;
import org.pathwayeditor.visualeditor.behaviour.selection.SelectionViewBehaviourStateHandler;
import org.pathwayeditor.visualeditor.editingview.IShapePane;

public class ViewBehaviourController implements IViewBehaviourController {
	private final Logger logger = Logger.getLogger(this.getClass());
	
	private IViewBehaviourStateHandler currentStateController; 
	private final IViewBehaviourStateHandler selectionStateController;
	private final IViewBehaviourStateHandler shapeCreationStateController;
	private final IViewBehaviourStateHandler linkCreationStateController;

	private boolean activated;
	private IObjectType currShapeType;
	private final IShapePane shapePane;
	
	public ViewBehaviourController(IShapePane pane, IOperationFactory opFactory){
		this.shapePane = pane;
		IControllerResponses selectionResponse = new SelectionControllerResponses(opFactory);
		ISelectionStateBehaviourController selectionController = new GeneralStateController(pane, new HitCalculator(pane), selectionResponse);
		this.selectionStateController = new SelectionViewBehaviourStateHandler(selectionController, selectionResponse);
		this.shapeCreationStateController = new ShapeCreationBehaviourStateHandler(new GeneralStateController(pane, new HitCalculator(pane),
				new CreationControllerResponses(opFactory,
				new IShapeTypeInspector() {
					
					@Override
					public IShapeObjectType getCurrentShapeType() {
						return (IShapeObjectType)currShapeType;
					}
		})), new CreationDragResponse(opFactory.getShapeCreationOperation(), new IShapeTypeInspector() {
			
			@Override
			public IShapeObjectType getCurrentShapeType() {
				return (IShapeObjectType)currShapeType;
			}
		}), new MouseCreationFeedbackResponse());
		this.linkCreationStateController = new LinkCreationBehaviourStateHandler(new HitCalculator(pane),
				opFactory.getLinkCreationOperation(),
				new ILinkTypeInspector() {
			@Override
			public ILinkObjectType getCurrentLinkType() {
				return (ILinkObjectType)currShapeType;
			}
		});
		this.currentStateController = this.selectionStateController;
	}

	@Override
	public void activate(){
//		this.shapePane.addKeyListener(this.keyListener);
//        this.shapePane.addMouseMotionListener(this.currentStateController);
//        this.shapePane.addMouseListener(this.currentStateController);
//        this.shapePane.addMouseListener(popupMenuListener);
//        this.popupMenuListener.activate();
		this.currentStateController.activate(shapePane);
        this.activated = true;
	}

	@Override
	public void deactivate(){
//		this.shapePane.removeKeyListener(this.keyListener);
//        this.shapePane.removeMouseMotionListener(this.currentStateController);
//        this.shapePane.removeMouseListener(this.currentStateController);
//        this.shapePane.removeMouseListener(popupMenuListener);
//        this.popupMenuListener.deactivate();
		this.currentStateController.deactivate(shapePane);
        this.activated = false;
	}

	@Override
	public boolean isActivated(){
		return this.activated;
	}
	
	@Override
	public void setLinkCreationMode(ILinkObjectType linkType) {
		this.currShapeType = linkType;
		if(!this.currentStateController.equals(this.linkCreationStateController)){
			if(this.isActivated()){
				this.currentStateController.deactivate(shapePane);
			}
			this.currentStateController = this.linkCreationStateController;
			if(this.isActivated()){
				this.currentStateController.activate(shapePane);
			}
		}
		if(logger.isDebugEnabled()){
			logger.debug("In link creation mode for object type: " + linkType.getName());
		}
	}

	@Override
	public void setSelectionMode() {
		if(this.currentStateController != this.selectionStateController){
			if(this.isActivated()){
//		        this.shapePane.removeMouseMotionListener(this.currentStateController);
//		        this.shapePane.removeMouseListener(this.currentStateController);
				this.currentStateController.deactivate(shapePane);
			}
			this.currentStateController = this.selectionStateController;
			if(this.isActivated()){
//		        this.shapePane.addMouseMotionListener(this.currentStateController);
//		        this.shapePane.addMouseListener(this.currentStateController);
				this.currentStateController.activate(shapePane);
			}
			logger.debug("Setting selection controller state");
		}
	}

	@Override
	public void setShapeCreationMode(IShapeObjectType shapeType) {
		this.currShapeType = shapeType;
		if(!this.currentStateController.equals(this.shapeCreationStateController)){
			if(this.isActivated()){
				this.currentStateController.deactivate(shapePane);
			}
			this.currentStateController = this.shapeCreationStateController;
			if(this.isActivated()){
				this.currentStateController.activate(shapePane);
			}
		}
		if(logger.isDebugEnabled()){
			logger.debug("In shape creation mode for object type: " + shapeType.getName());
		}
	}

}
