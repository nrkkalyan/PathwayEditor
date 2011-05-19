/*
  Licensed to the Court of the University of Edinburgh (UofE) under one
  or more contributor license agreements.  See the NOTICE file
  distributed with this work for additional information
  regarding copyright ownership.  The UofE licenses this file
  to you under the Apache License, Version 2.0 (the
  "License"); you may not use this file except in compliance
  with the License.  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing,
  software distributed under the License is distributed on an
  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  KIND, either express or implied.  See the License for the
  specific language governing permissions and limitations
  under the License.
*/
package org.pathwayeditor.visualeditor.selection;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.pathwayeditor.figure.geometry.Envelope;
import org.pathwayeditor.figure.geometry.Point;
import org.pathwayeditor.visualeditor.controller.INodeController;
import org.pathwayeditor.visualeditor.editingview.IMiniCanvas;
import org.pathwayeditor.visualeditor.editingview.SelectionShape;
import org.pathwayeditor.visualeditor.selection.ISelectionHandle.SelectionHandleType;

public class NodeSelection extends Selection implements INodeSelection {
	private static final int NUM_REGIONS = 9;
//	private INodeControllerChangeListener nodeControllerListener;
	private final INodeController nodeController;
	private final List<ISelectionHandle> selectionModels;
	private Envelope selectionBounds;

	public NodeSelection(SelectionType selectionType, INodeController nodeController){
		super(selectionType);
		this.nodeController = nodeController;
		this.selectionModels = new ArrayList<ISelectionHandle>(NUM_REGIONS);
		buildHandles();
//		this.nodeControllerListener = new INodeControllerChangeListener(){
//
//			@Override
//			public void nodeTranslated(INodeTranslationEvent e) {
//				buildHandles();
//			}
//
//			@Override
//			public void nodeResized(INodeResizeEvent e) {
//				buildHandles();
//			}
//
//			@Override
//			public void changedBounds(INodeBoundsChangeEvent e) {
//				buildHandles();
//			}
//			
//		};
//		this.nodeController.addNodePrimitiveChangeListener(this.nodeControllerListener);
	}
	
	@Override
	public INodeController getPrimitiveController() {
		return this.nodeController;
	}

	@Override
	public List<ISelectionHandle> getSelectionHandle(SelectionHandleType region) {
		List<ISelectionHandle> retVal = new LinkedList<ISelectionHandle>();
		Iterator<ISelectionHandle> iter = this.selectionModels.iterator();
		while(iter.hasNext() && retVal.isEmpty()){
			ISelectionHandle curr = iter.next();
			if(curr.getType().equals(region)){
				retVal.add(curr);
			}
		}
		return retVal;
	}

	private void buildHandles() {
		this.selectionModels.clear();
		this.selectionModels.add(SelectionHandle.createCentralRegion(this, nodeController));
		this.selectionModels.add(SelectionHandle.createNRegion(this, nodeController));
		this.selectionModels.add(SelectionHandle.createNERegion(this, nodeController));
		this.selectionModels.add(SelectionHandle.createERegion(this, nodeController));
		this.selectionModels.add(SelectionHandle.createSERegion(this, nodeController));
		this.selectionModels.add(SelectionHandle.createSRegion(this, nodeController));
		this.selectionModels.add(SelectionHandle.createSWRegion(this, nodeController));
		this.selectionModels.add(SelectionHandle.createWRegion(this, nodeController));
		this.selectionModels.add(SelectionHandle.createNWRegion(this, nodeController));
		calcSelectionBounds();
	}

	private void calcSelectionBounds() {
		double minX = Double.MAX_VALUE;
		double maxX = Double.MIN_VALUE;
		double minY = Double.MAX_VALUE;
		double maxY = Double.MIN_VALUE;
		for(ISelectionHandle model : this.selectionModels){
			Envelope bounds = model.getBounds();
			minX = Math.min(minX, bounds.getOrigin().getX());
			minY = Math.min(minY, bounds.getOrigin().getY());
			maxX = Math.max(maxX, bounds.getDiagonalCorner().getX());
			maxY = Math.max(maxY, bounds.getDiagonalCorner().getY());
		}
		this.selectionBounds = new Envelope(minX, minY, maxX-minX, maxY-minY);
	}

	@Override
	public ISelectionHandle findSelectionModelAt(Point point) {
		ISelectionHandle retVal = null;
		// check if in this selection at all.
		if(this.selectionBounds.containsPoint(point)){
			ISelectionHandle centralModel = null;
			Iterator<ISelectionHandle> iter = this.selectionModels.iterator();
			while(iter.hasNext() && retVal == null){
				ISelectionHandle model = iter.next();
				if(!model.getType().equals(SelectionHandleType.Central)){
					if(model.containsPoint(point)){
						retVal = model;
					}
				}
				else{
					centralModel = model;
				}
			}
			// central model is check last as the other blocks take precedence
			if(retVal == null && centralModel != null && centralModel.containsPoint(point)){
				retVal = centralModel;
			}
		}
		return retVal;
	}

	@Override
	public IMiniCanvas getMiniCanvas() {
		return new SelectionShape(this);
	}

}
