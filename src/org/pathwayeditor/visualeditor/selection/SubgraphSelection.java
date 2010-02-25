package org.pathwayeditor.visualeditor.selection;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.pathwayeditor.businessobjects.drawingprimitives.IDrawingElementSelection;
import org.pathwayeditor.businessobjects.drawingprimitives.IDrawingNode;
import org.pathwayeditor.businessobjects.drawingprimitives.ILinkEdge;
import org.pathwayeditor.visualeditor.controller.IDrawingPrimitiveController;
import org.pathwayeditor.visualeditor.controller.ILinkController;
import org.pathwayeditor.visualeditor.controller.INodeController;
import org.pathwayeditor.visualeditor.controller.IViewControllerStore;
import org.pathwayeditor.visualeditor.selection.ISelection.SelectionType;

public class SubgraphSelection implements ISubgraphSelection {
	private final IDrawingElementSelection subgraphSelection;
	private final SelectionRecord selectionRecord;
	private final IViewControllerStore viewControllerStore;
	private final Map<IDrawingPrimitiveController, ISelection> subGraphSelections = new HashMap<IDrawingPrimitiveController, ISelection>();
	
	public SubgraphSelection(SelectionRecord selectionRecord, IViewControllerStore viewControllerStore, IDrawingElementSelection currentSelectionSubgraph) {
		this.subgraphSelection = currentSelectionSubgraph;
		this.selectionRecord = selectionRecord;
		this.viewControllerStore = viewControllerStore;
		Iterator<IDrawingNode> nodeIter = currentSelectionSubgraph.drawingNodeIterator();
		while(nodeIter.hasNext()){
			IDrawingNode drawingNode = nodeIter.next();
			INodeController nodeController = this.viewControllerStore.getNodeController(drawingNode.getAttribute());
			if(!this.selectionRecord.containsSelection(nodeController)){
				this.subGraphSelections.put(nodeController, new NodeSelection(SelectionType.SUBGRAPH, nodeController));
			}
		}
		Iterator<ILinkEdge> linkIter = currentSelectionSubgraph.linkEdgeIterator();
		while(linkIter.hasNext()){
			ILinkEdge drawingNode = linkIter.next();
			ILinkController linkController = this.viewControllerStore.getLinkController(drawingNode.getAttribute());
			if(!this.selectionRecord.containsSelection(linkController)){
				this.subGraphSelections.put(linkController, new LinkSelection(SelectionType.SUBGRAPH, linkController));
			}
		}
	}

	@Override
	public IDrawingElementSelection getDrawingElementSelection() {
		return this.subgraphSelection;
	}

	@Override
	public ISelectionRecord getSelectionRecord() {
		return this.selectionRecord;
	}

	@Override
	public int numTopDrawingNodes() {
		return this.subgraphSelection.numTopDrawingNodes();
	}

	@Override
	public Iterator<ILinkSelection> selectedLinkIterator() {
		final Iterator<ILinkEdge> iter = this.subgraphSelection.linkEdgeIterator();
		Iterator<ILinkSelection> retVal = new Iterator<ILinkSelection>(){

			@Override
			public boolean hasNext() {
				return iter.hasNext();
			}

			@Override
			public ILinkSelection next() {
				ILinkController linkController = viewControllerStore.getLinkController(iter.next().getAttribute());
				ILinkSelection retVal = selectionRecord.getLinkSelection(linkController);
				if(retVal == null){
					retVal = (ILinkSelection)subGraphSelections.get(linkController);
				}
				return retVal;
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException("Removal not supported");
			}
			
		};
		return retVal;
	}

	@Override
	public Iterator<INodeSelection> selectedNodeIterator() {
		final Iterator<IDrawingNode> iter = this.subgraphSelection.drawingNodeIterator();
		Iterator<INodeSelection> retVal = new Iterator<INodeSelection>(){

			@Override
			public boolean hasNext() {
				return iter.hasNext();
			}

			@Override
			public INodeSelection next() {
				INodeController nodeController = viewControllerStore.getNodeController(iter.next().getAttribute());
				INodeSelection retVal = selectionRecord.getNodeSelection(nodeController);
				if(retVal == null){
					retVal = (INodeSelection)subGraphSelections.get(nodeController);
				}
				return retVal;
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException("Removal not supported");
			}
			
		};
		return retVal;
	}

	@Override
	public Iterator<INodeSelection> topSelectedNodeIterator() {
		// ensure that this graph selection is initialised
		final Iterator<IDrawingNode> iter = this.subgraphSelection.topDrawingNodeIterator();
		Iterator<INodeSelection> retVal = new Iterator<INodeSelection>(){

			@Override
			public boolean hasNext() {
				return iter.hasNext();
			}

			@Override
			public INodeSelection next() {
				INodeController nodeController = viewControllerStore.getNodeController(iter.next().getAttribute());
				INodeSelection retVal = selectionRecord.getNodeSelection(nodeController);
				if(retVal == null){
					retVal = (INodeSelection)subGraphSelections.get(nodeController);
				}
				return retVal;
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException("Removal not supported");
			}
			
		};
		return retVal;
	}

}
