package org.pathwayeditor.visualeditor.operations;

import java.util.Iterator;

import org.pathwayeditor.businessobjects.drawingprimitives.IDrawingElementSelection;
import org.pathwayeditor.businessobjects.drawingprimitives.ISelectionFactory;
import org.pathwayeditor.businessobjects.impl.facades.SelectionFactoryFacade;
import org.pathwayeditor.visualeditor.behaviour.operation.IDefaultPopupActions;
import org.pathwayeditor.visualeditor.behaviour.operation.IEditingOperation;
import org.pathwayeditor.visualeditor.behaviour.operation.ILinkBendPointPopupActions;
import org.pathwayeditor.visualeditor.behaviour.operation.ILinkCreationOperation;
import org.pathwayeditor.visualeditor.behaviour.operation.ILinkOperation;
import org.pathwayeditor.visualeditor.behaviour.operation.ILinkPopupActions;
import org.pathwayeditor.visualeditor.behaviour.operation.IMarqueeOperation;
import org.pathwayeditor.visualeditor.behaviour.operation.IOperationFactory;
import org.pathwayeditor.visualeditor.behaviour.operation.IResizeOperation;
import org.pathwayeditor.visualeditor.behaviour.operation.ISelectionOperation;
import org.pathwayeditor.visualeditor.behaviour.operation.IShapeCreationOperation;
import org.pathwayeditor.visualeditor.behaviour.operation.IShapePopupActions;
import org.pathwayeditor.visualeditor.commands.DeleteBendPointCommand;
import org.pathwayeditor.visualeditor.commands.ICommand;
import org.pathwayeditor.visualeditor.commands.ICommandStack;
import org.pathwayeditor.visualeditor.controller.IDrawingElementController;
import org.pathwayeditor.visualeditor.controller.IRootController;
import org.pathwayeditor.visualeditor.controller.IViewControllerModel;
import org.pathwayeditor.visualeditor.editingview.IShapePane;
import org.pathwayeditor.visualeditor.feedback.IFeedbackModel;
import org.pathwayeditor.visualeditor.geometry.CommonParentCalculator;
import org.pathwayeditor.visualeditor.geometry.ICommonParentCalculator;
import org.pathwayeditor.visualeditor.selection.ILinkSelection;
import org.pathwayeditor.visualeditor.selection.INodeSelection;
import org.pathwayeditor.visualeditor.selection.ISelectionRecord;

import uk.ac.ed.inf.graph.compound.ISubgraphRemovalBuilder;

public class OperationFactory implements IOperationFactory {
	private final IEditingOperation editOperation;
	private final IResizeOperation resizeOperation;
	private final ILinkOperation linkOperation;
	private final IMarqueeOperation marqueeOperation;
	private IShapePopupActions shapePopupMenuResponse;
	private ILinkPopupActions linkPopupMenuResponse;
	private ILinkBendPointPopupActions linkBendpointPopupResponse;
	private IDefaultPopupActions defaultPopupMenuResponse;
	private final IShapePane shapePane;
	private final ISelectionRecord selectionRecord;
	private final ICommandStack commandStack;
	private final IViewControllerModel viewModel;
	private final IShapeCreationOperation shapeCreationOperation;
	private final ISelectionOperation selectionOperations;
	private final ILinkCreationOperation linkCreationOperation;

	public OperationFactory(IShapePane shapePane, IFeedbackModel feedbackModel, ISelectionRecord selectionRecord, IViewControllerModel viewModel,
			ICommandStack commandStack){
		ICommonParentCalculator newParentCalc = new CommonParentCalculator(viewModel.getIntersectionCalculator());
		this.shapePane = shapePane;
		this.selectionRecord = selectionRecord;
		this.commandStack = commandStack;
		this.viewModel = viewModel;
        editOperation = new EditingOperation(shapePane, feedbackModel, selectionRecord, newParentCalc, commandStack);
        resizeOperation = new ResizeOperation(shapePane, feedbackModel, selectionRecord, commandStack);
		linkOperation = new LinkOperation(shapePane, feedbackModel, selectionRecord, commandStack);
		marqueeOperation = new MarqueeOperation(shapePane, feedbackModel, selectionRecord, viewModel.getIntersectionCalculator());
		initResponses();
		shapeCreationOperation = new ShapeCreationOperation(shapePane, feedbackModel, viewModel, commandStack);
		this.selectionOperations = new SelectionOperation(selectionRecord, viewModel.getIntersectionCalculator());
		this.linkCreationOperation = new LinkCreationOperation(shapePane, feedbackModel, viewModel, commandStack);
	}
	
	private void initResponses(){
		this.shapePopupMenuResponse = new IShapePopupActions() {
			
			@Override
			public void delete() {
				deleteSelection();
				selectionRecord.clear();
				shapePane.updateView();
			}
		};
		this.linkPopupMenuResponse = new ILinkPopupActions() {
			
			@Override
			public void delete() {
				deleteSelection();
				selectionRecord.clear();
				shapePane.updateView();
			}
		};
		this.linkBendpointPopupResponse = new ILinkBendPointPopupActions() {
			
			@Override
			public void deleteBendPoint(int bpIdx) {
				deleteBendpoint(bpIdx);
				selectionRecord.restoreSelection();
				shapePane.updateView();
			}
			
			@Override
			public void delete() {
				deleteSelection();
				selectionRecord.clear();
				shapePane.updateView();
			}

		};
		this.defaultPopupMenuResponse = new IDefaultPopupActions() {
			
			@Override
			public void selectAll() {
				selectAllElements();
				shapePane.updateView();
			}

			@Override
			public void delete() {
				deleteSelection();
				selectionRecord.clear();
				shapePane.updateView();
			}

			@Override
			public boolean isDeleteActionValid() {
				return selectionRecord.numSelected() > 0;
			}
		};
	}
	
	@Override
	public IDefaultPopupActions getDefaultPopupMenuResponse() {
		return this.defaultPopupMenuResponse;
	}

	@Override
	public ILinkBendPointPopupActions getLinkBendpointPopupMenuResponse() {
		return this.linkBendpointPopupResponse;
	}

	@Override
	public ILinkOperation getLinkOperation() {
		return this.linkOperation;
	}

	@Override
	public ILinkPopupActions getLinkPopupMenuResponse() {
		return this.linkPopupMenuResponse;
	}

	@Override
	public IMarqueeOperation getMarqueeOperation() {
		return this.marqueeOperation;
	}

	@Override
	public IEditingOperation getMoveOperation() {
		return this.editOperation;
	}

	@Override
	public IResizeOperation getResizeOperation() {
		return this.resizeOperation;
	}

	@Override
	public IShapePopupActions getShapePopupMenuResponse() {
		return this.shapePopupMenuResponse;
	}

	private void selectAllElements() {
		Iterator<IDrawingElementController> primIter = this.viewModel.drawingPrimitiveIterator();
		boolean firstTime = true;
		while(primIter.hasNext()){
			IDrawingElementController controller = primIter.next();
			if(!(controller instanceof IRootController)){
				if(firstTime){
					selectionRecord.setPrimarySelection(controller);
					firstTime = false;
				}
				else{
					selectionRecord.addSecondarySelection(controller);
				}
			}
		}
	}

	private void deleteBendpoint(int bpIdx) {
		ILinkSelection linkSelection = this.selectionRecord.getUniqueLinkSelection(); 
		ICommand cmd = new DeleteBendPointCommand(linkSelection.getPrimitiveController().getDrawingElement().getAttribute().getBendPointContainer(), bpIdx);
		this.commandStack.execute(cmd);
	}
	
	private void deleteSelection() {
		Iterator<INodeSelection> nodeSelectionIter = selectionRecord.selectedNodeIterator();
		ISelectionFactory selectionFact = null;
		while(nodeSelectionIter.hasNext()){
			INodeSelection selectedNode = nodeSelectionIter.next();
			if(selectionFact == null){
				selectionFact = new SelectionFactoryFacade(selectedNode.getPrimitiveController().getViewModel().getDomainModel().getGraph().subgraphFactory());
			}
			selectionFact.addDrawingNode(selectedNode.getPrimitiveController().getDrawingElement());
		}
		Iterator<ILinkSelection> linkSelectionIter = selectionRecord.selectedLinkIterator();
		while(linkSelectionIter.hasNext()){
			ILinkSelection selectedLink = linkSelectionIter.next();
			if(selectionFact == null){
				selectionFact = new SelectionFactoryFacade(selectedLink.getPrimitiveController().getViewModel().getDomainModel().getGraph().subgraphFactory());
			}
			selectionFact.addLink(selectedLink.getPrimitiveController().getDrawingElement());
		}
		if(selectionFact != null){
			IDrawingElementSelection seln = selectionFact.createGeneralSelection();
			ISubgraphRemovalBuilder removalBuilder = seln.getSubgraph().getSuperGraph().newSubgraphRemovalBuilder();
			removalBuilder.setRemovalSubgraph(seln.getSubgraph());
			removalBuilder.removeSubgraph();
		}
	}

	@Override
	public IShapeCreationOperation getShapeCreationOperation() {
		return this.shapeCreationOperation;
	}

	@Override
	public ISelectionOperation getSelectionOperation() {
		return this.selectionOperations;
	}

	@Override
	public ILinkCreationOperation getLinkCreationOperation() {
		return this.linkCreationOperation;
	}
}
