package org.pathwayeditor.visualeditor.behaviour.linkcreation;

import java.awt.Cursor;
import java.awt.event.MouseEvent;

import org.apache.log4j.Logger;
import org.pathwayeditor.figure.geometry.Point;
import org.pathwayeditor.visualeditor.behaviour.IMouseBehaviourListener;
import org.pathwayeditor.visualeditor.behaviour.IMouseFeedbackResponse;
import org.pathwayeditor.visualeditor.behaviour.IMouseFeedbackResponse.StateType;
import org.pathwayeditor.visualeditor.behaviour.ISelectionResponse;
import org.pathwayeditor.visualeditor.behaviour.ISelectionStateBehaviourController;
import org.pathwayeditor.visualeditor.behaviour.creation.ILinkCreationDragResponse;
import org.pathwayeditor.visualeditor.controller.INodeController;

public class LinkCreationMouseBehaviourListener implements IMouseBehaviourListener {
	private ILinkCreationDragResponse currDragResponse;
	private IMouseFeedbackResponse currMouseFeedbackResponse;
	private final ISelectionStateBehaviourController mouseBehaviourController;
	private final Logger logger = Logger.getLogger(this.getClass());
	
	public LinkCreationMouseBehaviourListener(ISelectionStateBehaviourController mouseBehaviourController) {
		this.mouseBehaviourController = mouseBehaviourController;
	}

	private void setCurrentCursorResponse(){
		currMouseFeedbackResponse = this.mouseBehaviourController.getMouseFeedbackResponse();
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		if(e.getButton() == MouseEvent.BUTTON1){
			this.mouseBehaviourController.setMousePosition(e.getPoint().getX(), e.getPoint().getY());
			if(currDragResponse == null){
					currDragResponse = (ILinkCreationDragResponse)this.mouseBehaviourController.getDragResponse();
					currMouseFeedbackResponse = this.mouseBehaviourController.getMouseFeedbackResponse();
			}
			if(currDragResponse != null){
				Point location = this.mouseBehaviourController.getDiagramLocation();
				if(currDragResponse.isDragOngoing()){
					if(currDragResponse.canContinueDrag(location)){
						currDragResponse.dragContinuing(location);
						if(currDragResponse.canReparent()){
							currMouseFeedbackResponse.changeState(StateType.REPARENTING);
							logger.trace("Setting hand cursor as reparenting enabled");
						}
						else if(currDragResponse.canOperationSucceed()){
							logger.trace("Can move, but cannot reparent. Setting to default for current location");
							currMouseFeedbackResponse.changeState(StateType.DEFAULT);
						}
						else{
							currMouseFeedbackResponse.changeState(StateType.FORBIDDEN);
							logger.trace("Move is forbidden");
						}
					}
				}
				else{
					INodeController node = this.mouseBehaviourController.getNodeAtCurrentPoint();
					if(node != null){
						currDragResponse.setCurrentNode(node);
						currDragResponse.dragStarted(location);
					}
				}
			}
		}
		e.getComponent().setCursor(currMouseFeedbackResponse.getCurrentCursor());
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		this.mouseBehaviourController.setMousePosition(e.getPoint().getX(), e.getPoint().getY());
		IMouseFeedbackResponse currMouseFeedbackResponse = this.mouseBehaviourController.getMouseFeedbackResponse();
		Cursor feedbackCursor = currMouseFeedbackResponse.getCurrentCursor();
		if(logger.isTraceEnabled()){
			logger.trace("feedback cursor = " + feedbackCursor.getName());
		}
		e.getComponent().setCursor(feedbackCursor);
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		if(e.getButton() == MouseEvent.BUTTON1){
			this.mouseBehaviourController.setMousePosition(e.getPoint().getX(), e.getPoint().getY());
			Point location = this.mouseBehaviourController.getDiagramLocation();
			ISelectionResponse currSelnResponse = null;
			currSelnResponse = this.mouseBehaviourController.getClickResponse();
			if(!e.isShiftDown() && !e.isAltDown()){
				currSelnResponse.primaryClick(location);
			}
			else if(e.isShiftDown() && !e.isAltDown()){
				currSelnResponse.secondaryClick(location);
			}
		}
	}


	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}

	@Override
	public void mousePressed(MouseEvent e) {
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		if(currDragResponse != null){
			currDragResponse.dragFinished();
			currMouseFeedbackResponse.reset();
			currDragResponse = null;
		}
		this.mouseBehaviourController.setMousePosition(e.getPoint().getX(), e.getPoint().getY());
		setCurrentCursorResponse();
		e.getComponent().setCursor(currMouseFeedbackResponse.getCurrentCursor());
	}

}
