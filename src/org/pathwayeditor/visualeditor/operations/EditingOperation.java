package org.pathwayeditor.visualeditor.operations;

import java.util.Iterator;

import org.apache.log4j.Logger;
import org.pathwayeditor.businessobjects.drawingprimitives.ILinkAttribute;
import org.pathwayeditor.figure.geometry.Envelope;
import org.pathwayeditor.figure.geometry.Point;
import org.pathwayeditor.visualeditor.behaviour.IEditingOperation;
import org.pathwayeditor.visualeditor.commands.CompoundCommand;
import org.pathwayeditor.visualeditor.commands.ICommand;
import org.pathwayeditor.visualeditor.commands.ICommandStack;
import org.pathwayeditor.visualeditor.commands.ICompoundCommand;
import org.pathwayeditor.visualeditor.commands.MoveBendPointCommand;
import org.pathwayeditor.visualeditor.commands.MoveNodeCommand;
import org.pathwayeditor.visualeditor.commands.ReparentSelectionCommand;
import org.pathwayeditor.visualeditor.controller.IDrawingElementController;
import org.pathwayeditor.visualeditor.controller.INodeController;
import org.pathwayeditor.visualeditor.editingview.IShapePane;
import org.pathwayeditor.visualeditor.feedback.IFeedbackElement;
import org.pathwayeditor.visualeditor.feedback.IFeedbackLink;
import org.pathwayeditor.visualeditor.feedback.IFeedbackLinkChangeEvent;
import org.pathwayeditor.visualeditor.feedback.IFeedbackLinkListener;
import org.pathwayeditor.visualeditor.feedback.IFeedbackModel;
import org.pathwayeditor.visualeditor.feedback.IFeedbackNode;
import org.pathwayeditor.visualeditor.feedback.IFeedbackNodeListener;
import org.pathwayeditor.visualeditor.feedback.IFeedbackNodeResizeEvent;
import org.pathwayeditor.visualeditor.feedback.IFeedbackNodeTranslationEvent;
import org.pathwayeditor.visualeditor.geometry.EnvelopeBuilder;
import org.pathwayeditor.visualeditor.geometry.ICommonParentCalculator;
import org.pathwayeditor.visualeditor.selection.ILinkSelection;
import org.pathwayeditor.visualeditor.selection.INodeSelection;
import org.pathwayeditor.visualeditor.selection.ISelection;
import org.pathwayeditor.visualeditor.selection.ISelectionRecord;
import org.pathwayeditor.visualeditor.selection.ISubgraphSelection;

public class EditingOperation implements IEditingOperation {
	private final Logger logger = Logger.getLogger(this.getClass());
	private final IShapePane shapePane;
	private final IFeedbackModel feedbackModel; 
	private final ISelectionRecord selectionRecord;
	private final ICommonParentCalculator newParentCalc;
	private final ICommandStack commandStack;
	private EnvelopeBuilder refreshBoundsBuilder;
	private final IFeedbackLinkListener feedbackLinkListener;
	private final IFeedbackNodeListener feedbackNodeListener;
	
	public EditingOperation(IShapePane shapePane, IFeedbackModel feedbackModel, ISelectionRecord selectionRecord,
			ICommonParentCalculator newParentCalc, ICommandStack commandStack){
		this.shapePane = shapePane;
		this.feedbackModel = feedbackModel;
		this.selectionRecord = selectionRecord;
		this.newParentCalc = newParentCalc;
		this.commandStack = commandStack;
		this.feedbackLinkListener = new IFeedbackLinkListener() {
			@Override
			public void linkChangeEvent(IFeedbackLinkChangeEvent e) {
				refreshBoundsBuilder.union(e.getNewLinkDefintion().getBounds());
			}
		};
		this.feedbackNodeListener = new IFeedbackNodeListener() {
			@Override
			public void nodeTranslationEvent(IFeedbackNodeTranslationEvent e) {
				refreshBoundsBuilder.union(e.getNode().getBounds());
			}
			@Override
			public void nodeResizeEvent(IFeedbackNodeResizeEvent e) {
				refreshBoundsBuilder.union(e.getNode().getBounds());
			}
		};
	}
	
	@Override
	public void moveFinished(Point delta, ReparentingStateType reparentingState) {
		Envelope refreshBounds = refreshBoundsBuilder.getEnvelope();
		if(logger.isTraceEnabled()){
			logger.trace("Move finished. Delta=" + delta + ", Refresh Bounds=" + refreshBounds);
		}
		Iterator<IFeedbackNode> nodeIter = feedbackModel.nodeIterator();
		while(nodeIter.hasNext()){
			IFeedbackNode node = nodeIter.next();
			node.removeFeedbackNodeListener(feedbackNodeListener);
		}
		Iterator<IFeedbackLink> linkIter = feedbackModel.linkIterator();
		while(linkIter.hasNext()){
			IFeedbackLink link = linkIter.next();
			link.removeFeedbackLinkListener(feedbackLinkListener);
		}
		if(reparentingState.equals(ReparentingStateType.CAN_REPARENT)){
			createMoveCommand(delta, true);
			selectionRecord.restoreSelection();
		}
		else if(reparentingState.equals(ReparentingStateType.CAN_MOVE)){
			createMoveCommand(delta, false);
			selectionRecord.restoreSelection();
		}
		feedbackModel.clear();
		shapePane.updateView(refreshBounds);
		
	}

	@Override
	public void moveOngoing(Point delta) {
		Envelope refreshBounds = refreshBoundsBuilder.getEnvelope();
		if(logger.isTraceEnabled()){
			logger.trace("Ongoning move. Delta=" + delta + ", Refresh Bounds=" + refreshBounds);
		}
		moveSelection(delta);
		shapePane.updateView(refreshBounds);
	}

	@Override
	public void moveStarted() {
		Envelope originalBounds = this.selectionRecord.getTotalSelectionBounds();
		refreshBoundsBuilder = new EnvelopeBuilder(originalBounds);
		logger.trace("Move started.");
		feedbackModel.rebuildIncludingHierarchy();
		Iterator<IFeedbackNode> nodeIter = feedbackModel.nodeIterator();
		while(nodeIter.hasNext()){
			IFeedbackNode node = nodeIter.next();
			node.addFeedbackNodeListener(feedbackNodeListener);
		}
		Iterator<IFeedbackLink> linkIter = feedbackModel.linkIterator();
		while(linkIter.hasNext()){
			IFeedbackLink link = linkIter.next();
			link.addFeedbackLinkListener(feedbackLinkListener);
		}
	}

	@Override
	public ReparentingStateType getReparentingState(Point delta) {
		ReparentingStateType retVal = ReparentingStateType.FORBIDDEN;
		newParentCalc.findCommonParent(selectionRecord.getSubgraphSelection(), delta);
        if(newParentCalc.hasFoundCommonParent()) {
        	if(logger.isTraceEnabled()){
        		logger.trace("Common parent found. Node=" + newParentCalc.getCommonParent());
        	}
        	// parent is consistent - now we need to check if any node already has this parent
        	// if all do then we move, in one or more doesn't then we fail reparenting
        	if(newParentCalc.canReparentSelection()){
        		retVal = ReparentingStateType.CAN_REPARENT;
        	}
        	else if(newParentCalc.canMoveSelection()){
        		retVal = ReparentingStateType.CAN_MOVE;
        	}
        }
        else{
        	logger.trace("No common parent found.");
        }
    	if(logger.isTraceEnabled()){
    		logger.trace("Reparent state=" + retVal);
    	}
        return retVal;
	}

	private void moveSelection(Point delta) {
		ISubgraphSelection subgraphSelection = this.selectionRecord.getSubgraphSelection();
		Iterator<INodeSelection> moveNodeIterator = subgraphSelection.selectedNodeIterator();
		while(moveNodeIterator.hasNext()){
			ISelection selection = moveNodeIterator.next();
			IFeedbackElement feedbackElement = this.feedbackModel.getFeedbackElement(selection.getPrimitiveController());
			feedbackElement.translatePrimitive(delta);
			if(logger.isTraceEnabled()){
				logger.trace("Dragged feedback element: " + feedbackElement);
			}
		}
		Iterator<ILinkSelection> moveLinkIterator = subgraphSelection.selectedLinkIterator();
		while(moveLinkIterator.hasNext()){
			ILinkSelection selection = moveLinkIterator.next();
			IFeedbackLink feedbackLink = (IFeedbackLink)this.feedbackModel.getFeedbackElement(selection.getPrimitiveController());
			for(int bpIdx = 0; bpIdx < feedbackLink.getLinkDefinition().numBendPoints(); bpIdx++){
				feedbackLink.translateBendPoint(bpIdx, delta);
				if(logger.isTraceEnabled()){
					logger.trace("Moved bendpont=" + bpIdx + " of feedback element: " + feedbackLink);
				}
			}
		}
	}

	private void createMoveCommand(Point delta, boolean reparentingEnabled){
		ICompoundCommand cmpCommand = new CompoundCommand();
		Iterator<INodeSelection> moveNodeIterator = this.selectionRecord.getSubgraphSelection().topSelectedNodeIterator();
		while(moveNodeIterator.hasNext()){
			INodeController nodePrimitive = moveNodeIterator.next().getPrimitiveController();
			ICommand cmd = new MoveNodeCommand(nodePrimitive.getDrawingElement().getAttribute(), delta);
			cmpCommand.addCommand(cmd);
			if(logger.isTraceEnabled()){
				logger.trace("Dragged shape to location: " + nodePrimitive.getBounds().getOrigin());
			}
		}
		Iterator<ILinkSelection> moveLinkIterator = this.selectionRecord.getSubgraphSelection().selectedLinkIterator();
		while(moveLinkIterator.hasNext()){
			ILinkAttribute nodePrimitive = moveLinkIterator.next().getPrimitiveController().getDrawingElement().getAttribute();
			int bpIdx = 0;
			Iterator<Point> bpIter = nodePrimitive.getBendPointContainer().bendPointIterator();
			while(bpIter.hasNext()){
				bpIter.next();
				ICommand cmd = new MoveBendPointCommand(nodePrimitive.getBendPointContainer(), bpIdx, delta);
				cmpCommand.addCommand(cmd);
				bpIdx++;
			}
		}
		if(reparentingEnabled){
			IDrawingElementController target = calculateReparentTarget(delta);
			ICommand cmd = new ReparentSelectionCommand(target.getDrawingElement(), this.selectionRecord.getSubgraphSelection().getDrawingElementSelection());
			cmpCommand.addCommand(cmd);
		}
		this.commandStack.execute(cmpCommand);
	}

	private IDrawingElementController calculateReparentTarget(Point delta) {
		IDrawingElementController retVal = null;
		newParentCalc.findCommonParent(selectionRecord.getSubgraphSelection(), delta);
        if(newParentCalc.hasFoundCommonParent()) {
        	if(logger.isTraceEnabled()){
        		logger.trace("Common parent found. Node=" + newParentCalc.getCommonParent());
        	}
        	// parent is consistent - now we need to check if any node already has this parent
        	// if all do then we move, in one or more doesn't then we fail reparenting
        	retVal = newParentCalc.getCommonParent();
        }
        else{
        	logger.trace("No common parent found.");
        }
    	if(logger.isTraceEnabled()){
    		logger.trace("Can reparent=" + retVal);
    	}
        return retVal;
	}
}
