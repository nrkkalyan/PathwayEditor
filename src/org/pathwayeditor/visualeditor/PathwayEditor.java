package org.pathwayeditor.visualeditor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

import org.apache.log4j.Logger;
import org.pathwayeditor.businessobjects.drawingprimitives.ILinkEdge;
import org.pathwayeditor.businessobjects.drawingprimitives.IModel;
import org.pathwayeditor.businessobjects.notationsubsystem.INotationSubsystem;
import org.pathwayeditor.figure.geometry.Envelope;
import org.pathwayeditor.visualeditor.IPathwayEditorStateChangeEvent.StateChangeType;
import org.pathwayeditor.visualeditor.behaviour.IViewBehaviourController;
import org.pathwayeditor.visualeditor.behaviour.ViewBehaviourController;
import org.pathwayeditor.visualeditor.commands.CommandStack;
import org.pathwayeditor.visualeditor.commands.ICommandChangeEvent;
import org.pathwayeditor.visualeditor.commands.ICommandChangeEvent.CommandChangeType;
import org.pathwayeditor.visualeditor.commands.ICommandChangeListener;
import org.pathwayeditor.visualeditor.commands.ICommandStack;
import org.pathwayeditor.visualeditor.controller.IDrawingElementController;
import org.pathwayeditor.visualeditor.controller.IViewControllerModel;
import org.pathwayeditor.visualeditor.controller.ViewControllerStore;
import org.pathwayeditor.visualeditor.editingview.DomainModelLayer;
import org.pathwayeditor.visualeditor.editingview.FeedbackLayer;
import org.pathwayeditor.visualeditor.editingview.IShapePane;
import org.pathwayeditor.visualeditor.editingview.SelectionLayer;
import org.pathwayeditor.visualeditor.editingview.ShapePane;
import org.pathwayeditor.visualeditor.feedback.FeedbackModel;
import org.pathwayeditor.visualeditor.feedback.IFeedbackModel;
import org.pathwayeditor.visualeditor.geometry.EnvelopeBuilder;
import org.pathwayeditor.visualeditor.operations.LabelPropValueDialog;
import org.pathwayeditor.visualeditor.operations.OperationFactory;
import org.pathwayeditor.visualeditor.selection.ISelection;
import org.pathwayeditor.visualeditor.selection.ISelectionChangeEvent;
import org.pathwayeditor.visualeditor.selection.ISelectionChangeListener;
import org.pathwayeditor.visualeditor.selection.ISelectionRecord;
import org.pathwayeditor.visualeditor.selection.SelectionRecord;

public class PathwayEditor extends JPanel {
	private static final double REFRESH_EXPANSION_Y = 20.0;
	private static final double REFRESH_EXPANSION_X = REFRESH_EXPANSION_Y;
	private static final long serialVersionUID = 1L;
	private final Logger logger = Logger.getLogger(this.getClass());
	private IShapePane shapePane;
	private JScrollPane scrollPane;
	private PalettePanel palettePane;
	private IViewControllerModel viewModel;
	private ISelectionRecord selectionRecord;
	private final ICommandStack commandStack;
	private IViewBehaviourController editBehaviourController;
	private final ISelectionChangeListener selectionChangeListener;
	private IFeedbackModel feedbackModel;
	private boolean isOpen = false;
	private ILayoutCalculator layoutCalculator;
	private List<IPathwayEditorStateChangeListener> listeners = new LinkedList<IPathwayEditorStateChangeListener>();
//	private final IGraphStructureChangeListener graphStuctureChangeListener;
	private final ICommandChangeListener commandStackListener;
	private Dialog dialog;

	public PathwayEditor(Dialog dialog){
		super();
		this.dialog = dialog;
		this.setLayout(new BorderLayout());
		this.setBorder(BorderFactory.createLineBorder(Color.BLACK));
//		this.graphStuctureChangeListener = new IGraphStructureChangeListener() {
//			@Override
//			public void graphStructureChange(IGraphStructureChangeAction iGraphStructureChangeAction) {
//				GraphStructureChangeType type = iGraphStructureChangeAction.getChangeType();
//				if(type.equals(GraphStructureChangeType.ELEMENT_ADDED)){
//					registerWithPropChangeListeners(iGraphStructureChangeAction.changedSubgraph());
//				}
//				else if(type.equals(GraphStructureChangeType.SUBGRAPH_COPIED)){
//					registerWithPropChangeListeners(iGraphStructureChangeAction.changedSubgraph());
//				}
//				else if(type.equals(GraphStructureChangeType.SUBGRAPH_MOVED)){
//					registerWithPropChangeListeners(iGraphStructureChangeAction.changedSubgraph());
//					deregisterWithPropChangeListeners(iGraphStructureChangeAction.originalSubgraph());
//				}
//				else if(type.equals(GraphStructureChangeType.SUBGRAPH_REMOVED)){
//					deregisterWithPropChangeListeners(iGraphStructureChangeAction.originalSubgraph());
//				}
//				isEdited = true;
//				notifyStateChange(StateChangeType.EDITED, viewModel.getDomainModel());
//			}
//		};
		this.commandStackListener = new ICommandChangeListener() {
			
			@Override
			public void notifyCommandChange(ICommandChangeEvent e) {
				if(e.getChangeType().equals(CommandChangeType.EXECUTE)){
					notifyStateChange(StateChangeType.EDITED, viewModel.getDomainModel());
				}
				else if(e.getChangeType().equals(CommandChangeType.REDO)){
					notifyStateChange(StateChangeType.EDITED, viewModel.getDomainModel());
					selectionRecord.restoreSelection();
					shapePane.updateView();
				}
				else if(e.getChangeType().equals(CommandChangeType.UNDO)){
					if(!commandStack.canUndo()){
						notifyStateChange(StateChangeType.UNEDITED, viewModel.getDomainModel());
					}
					selectionRecord.restoreSelection();
					shapePane.updateView();
				}
			}
		};
		this.selectionChangeListener = new ISelectionChangeListener() {

			@Override
			public void selectionChanged(ISelectionChangeEvent event) {
				EnvelopeBuilder builder = null;
				Iterator<ISelection> oldIter = event.oldSelectionIter();
				while (oldIter.hasNext()) {
					ISelection seln = oldIter.next();
					if (logger.isTraceEnabled()) {
						logger.trace("Union old selection drawnBounds="
								+ seln.getPrimitiveController()
										.getDrawnBounds());
					}
					Envelope bounds = seln.getPrimitiveController()
							.getDrawnBounds();
					if (builder == null) {
						builder = new EnvelopeBuilder(bounds);
					} else {
						builder.union(bounds);
					}
				}
				if (builder != null) {
					builder.expand(REFRESH_EXPANSION_X, REFRESH_EXPANSION_Y);
					Envelope refreshBounds = builder.getEnvelope();
					if (logger.isTraceEnabled()) {
						logger.trace("Unselection refresh bounds: "
								+ refreshBounds);
					}
					shapePane.updateView(refreshBounds);
				} else {
					logger.debug("No old selection bounds identified");
				}
				builder = null;
				Iterator<ISelection> newIter = event.newSelectionIter();
				while (newIter.hasNext()) {
					ISelection seln = newIter.next();
					Envelope bounds = seln.getPrimitiveController()
							.getDrawnBounds();
					if (logger.isTraceEnabled()) {
						logger.trace("Union new selection drawnBounds="
								+ seln.getPrimitiveController()
										.getDrawnBounds());
					}
					if (builder == null) {
						builder = new EnvelopeBuilder(bounds);
					} else {
						builder.union(bounds);
					}
				}
				if (builder != null) {
					builder.expand(REFRESH_EXPANSION_X, REFRESH_EXPANSION_Y);
					Envelope refreshBounds = builder.getEnvelope();
					if (logger.isTraceEnabled()) {
						logger.trace("Selection refresh bounds: "
								+ refreshBounds);
					}
					shapePane.updateView(refreshBounds);
				} else {
					logger.debug("No new selection bounds identified");
				}
			}
		};
		this.commandStack = new CommandStack();
	}
	
	public boolean isOpen(){
		return isOpen;
	}
	
	public boolean isEdited(){
		return this.commandStack.canUndo();
	}
	
	public void resetEdited(){
		this.commandStack.clear();
	}
	
	public void close(){
		IModel model = this.viewModel.getDomainModel();
		if(isOpen){
			reset();
			this.repaint();
			notifyStateChange(StateChangeType.CLOSED, model);
		}
		isOpen = false;
	}
	
	public ICommandStack getCommandStack(){
		return this.commandStack;
	}
	
	public void addEditorStateChangeListener(IPathwayEditorStateChangeListener l){
		this.listeners.add(l);
	}
	
	public void removeEditorStateChangeListener(IPathwayEditorStateChangeListener l){
		this.listeners.remove(l);
	}
	
	public List<IPathwayEditorStateChangeListener> getEditorStateChangeListeners(){
		return new ArrayList<IPathwayEditorStateChangeListener>(this.listeners);
	}
	
	private void notifyStateChange(final StateChangeType changeType, final IModel model){
		IPathwayEditorStateChangeEvent e = new IPathwayEditorStateChangeEvent(){
			@Override
			public PathwayEditor getSource() {
				return PathwayEditor.this;
			}

			@Override
			public IModel getModel() {
				return model;
			}

			@Override
			public StateChangeType getChangeType() {
				return changeType;
			}
			
		};
		for(IPathwayEditorStateChangeListener l : this.listeners){
			l.editorChangedEvent(e);
		}
	}
	
	private void setUpEditorViews(IModel canvas){
		this.selectionRecord = new SelectionRecord(viewModel);
		this.feedbackModel = new FeedbackModel(selectionRecord);
		this.shapePane = new ShapePane();
		this.shapePane.addLayer(new DomainModelLayer(viewModel));
		this.shapePane.addLayer(new SelectionLayer(selectionRecord));
		this.shapePane.addLayer(new FeedbackLayer(feedbackModel));
		Envelope canvasBounds = this.viewModel.getCanvasBounds();
		this.shapePane.setPaneBounds(canvasBounds);
		scrollPane = new JScrollPane((ShapePane)this.shapePane, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollPane.getVerticalScrollBar().setUnitIncrement(16);
		scrollPane.setFocusable(true);
		scrollPane.setWheelScrollingEnabled(true);
		LabelPropValueDialog labelDialog = new LabelPropValueDialog(this.dialog);
        this.editBehaviourController = new ViewBehaviourController(shapePane, new OperationFactory(this.shapePane, this.feedbackModel, this.selectionRecord, viewModel, this.commandStack, labelDialog));
        INotationSubsystem notationSubsystem = canvas.getNotationSubsystem();
		this.palettePane = new PalettePanel(notationSubsystem, editBehaviourController);
		this.add(palettePane, BorderLayout.LINE_START);
		this.add(scrollPane, BorderLayout.CENTER);
 		this.revalidate();
		this.editBehaviourController.activate();
		this.viewModel.activate();
		this.selectionRecord.addSelectionChangeListener(selectionChangeListener);
		this.shapePane.updateView();
		this.isOpen = true;
		this.commandStack.addCommandChangeListener(commandStackListener);
	}
	
	/**
	 * Sets the layout calculator to be used for auto-layout of the canvas.
	 * @param layoutCalculator the layout calculator 
	 */
	public void setLayoutCalculator(ILayoutCalculator layoutCalculator){
		this.layoutCalculator = layoutCalculator;
		
	}
	
	/**
	 * Carry out auto-layout on the canvas. Using the layout calculator set in {@link setLayoutCalculator}.
	 * @throws IllegalStateException if the layout calculator is not set
	 */
	public void layoutCanvas(){
		if(this.layoutCalculator != null){
			this.layoutCalculator.calculateLayout();
		}
		else{
			throw new IllegalStateException("No layout calculator was set");
		}
	}
	
	public void renderModel(IModel model){
		if(isOpen){
			reset();
		}
//        this.commandStack = new CommandStack();
		this.viewModel = new ViewControllerStore(model);
		setUpEditorViews(this.viewModel.getDomainModel());
		((ShapePane)this.shapePane).setPreferredSize(new Dimension(1800, 1800));
		((ShapePane)this.shapePane).revalidate();
		notifyStateChange(StateChangeType.OPEN, model);
	}

	public IViewControllerModel getViewControllerModel(){
		return this.viewModel;
	}
	
	
	private void reset(){
		this.scrollPane.remove((JComponent)this.shapePane);
		this.remove(scrollPane);
		this.remove(this.palettePane);
		this.validate();
		this.selectionRecord.removeSelectionChangeListener(selectionChangeListener);
		this.commandStack.removeCommandChangeListener(commandStackListener);
		this.commandStack.clear();
//		this.viewModel.getDomainModel().getGraph().removeGraphStructureChangeListener(this.graphStuctureChangeListener);
		this.viewModel.deactivate();
		this.editBehaviourController.deactivate();
		this.shapePane = null;
		this.scrollPane = null;
		this.selectionRecord = null;
		this.viewModel = null;
		this.editBehaviourController = null;
		this.feedbackModel = null;
	}
	
	public void selectAndFocusOnElement(ILinkEdge linkEdge) {
		selectionRecord.clear();
		IDrawingElementController linkController = viewModel.getLinkController(linkEdge);
		selectionRecord.setPrimarySelection(linkController);
	}
	
	public ISelectionRecord getSelectionRecord(){
		return this.selectionRecord;
	}
}
