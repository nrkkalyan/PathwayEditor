package org.pathwayeditor.visualeditor.editingview;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.pathwayeditor.figure.geometry.Envelope;
import org.pathwayeditor.visualeditor.selection.ILinkSelection;
import org.pathwayeditor.visualeditor.selection.INodeSelection;
import org.pathwayeditor.visualeditor.selection.ISelectionRecord;

public class SelectionLayer implements ISelectionLayer {
	private final Logger logger = Logger.getLogger(this.getClass());
	
	private final ISelectionRecord selections;
//	private Iterator<INodeSelection> selectionIter;
//	private Iterator<ILinkSelection> linkSelectionIter;
	
	public SelectionLayer(ISelectionRecord selectionRecord){
		this.selections = selectionRecord;
	}
	
	
	@Override
	public ISelectionRecord getSelectionRecord() {
		return this.selections;
	}

	@Override
	public void paint(Graphics2D g2d) {
		Rectangle rectangleBounds = g2d.getClipBounds();
		Envelope updateBound = new Envelope(rectangleBounds.getX(),rectangleBounds.getY(), rectangleBounds.getWidth(), rectangleBounds.getHeight());
		if(logger.isDebugEnabled()){
			logger.debug("Selecting objects to update with bounds=" + updateBound);
		}
		Iterator<INodeSelection> selectionNodeIter = this.selections.selectedNodesIterator();
		while(selectionNodeIter.hasNext()){
			INodeSelection selectionNode = selectionNodeIter.next();
			if(selectionNode.getPrimitiveController().getDrawnBounds().intersects(updateBound)){
				if(logger.isTraceEnabled()){
					logger.trace("Will refresh node: " + selectionNode + ", with bounds=" + selectionNode.getPrimitiveController().getDrawnBounds());
				}
				SelectionShape selection = new SelectionShape(selectionNode);
				selection.paint(g2d);
			}
		}
		Iterator<ILinkSelection> selectionLinkIter = this.selections.selectedLinksIterator();
		while(selectionLinkIter.hasNext()){
			ILinkSelection selectionLink = selectionLinkIter.next();
			if(selectionLink.getPrimitiveController().getDrawnBounds().intersects(updateBound)){
				if(logger.isTraceEnabled()){
					logger.trace("Will refresh link: " + selectionLink + ", with bounds=" + selectionLink.getPrimitiveController().getDrawnBounds());
				}
				SelectionLinkDrawer selection = new SelectionLinkDrawer(selectionLink);
				selection.paint(g2d);
			}
		}
	}


	@Override
	public LayerType getLayerType() {
		return LayerType.SELECTION;
	}

//	@Override
//	public void setObjectsToUpdate(Envelope updateBound){
//		if(logger.isDebugEnabled()){
//			logger.debug("Selecting objects to update with bounds=" + updateBound);
//		}
//		List<INodeSelection> nodeList = new LinkedList<INodeSelection>(); 
//		Iterator<INodeSelection> selectionNodeIter = this.selections.selectedNodesIterator();
//		while(selectionNodeIter.hasNext()){
//			INodeSelection selectionNode = selectionNodeIter.next();
//			if(selectionNode.getPrimitiveController().intersectsBounds(updateBound)){
//				if(logger.isTraceEnabled()){
//					logger.trace("Will refresh node: " + selectionNode + ", with bounds=" + selectionNode.getPrimitiveController().getDrawnBounds());
//				}
//				nodeList.add(selectionNode);
//			}
//		}
//		this.selectionIter = nodeList.iterator();
//		List<ILinkSelection> edgeList = new LinkedList<ILinkSelection>(); 
//		Iterator<ILinkSelection> selectionLinkIter = this.selections.selectedLinksIterator();
//		while(selectionLinkIter.hasNext()){
//			ILinkSelection selectionLink = selectionLinkIter.next();
//			if(selectionLink.getPrimitiveController().intersectsBounds(updateBound)){
//				if(logger.isTraceEnabled()){
//					logger.trace("Will refresh link: " + selectionLink + ", with bounds=" + selectionLink.getPrimitiveController().getDrawnBounds());
//				}
//				edgeList.add(selectionLink);
//			}
//		}
//		this.linkSelectionIter = edgeList.iterator();
//	}
//
//	@Override
//	public void setAllObjectsToUpdate(){
//		logger.debug("Selecting all objects to update");
//		selectionIter = this.selections.selectedNodesIterator();
//		linkSelectionIter = this.selections.selectedLinksIterator();
//	}
}