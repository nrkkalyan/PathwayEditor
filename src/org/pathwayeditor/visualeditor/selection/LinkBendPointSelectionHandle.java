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

import java.util.Iterator;

import org.pathwayeditor.figure.geometry.Envelope;
import org.pathwayeditor.figure.geometry.IConvexHull;
import org.pathwayeditor.figure.geometry.Point;
import org.pathwayeditor.figure.geometry.RectangleHull;
import org.pathwayeditor.visualeditor.controller.ILinkController;

public class LinkBendPointSelectionHandle extends SelectionHandle implements ILinkBendPointHandleShape {
	private static final double HANDLE_OFFSET = 4.0;
	private static final double HANDLE_WIDTH = HANDLE_OFFSET*2;
	private static final double HANDLE_HEIGHT = HANDLE_OFFSET*2;
	
	private IConvexHull rectangleHull;

	public LinkBendPointSelectionHandle(ISelection selection, ILinkController nodeController, Point bp, int bpIdx){
		super(nodeController, SelectionHandleType.LinkBendPoint, selection, bpIdx);
		Point bisection = bp;
		this.rectangleHull = new RectangleHull(new Envelope(bisection.getX()-HANDLE_OFFSET, bisection.getY()-HANDLE_OFFSET,	HANDLE_WIDTH, HANDLE_HEIGHT));
	}
	
	@Override
	public boolean containsPoint(Point point) {
		return this.rectangleHull.containsPoint(point);
	}

	@Override
	public Envelope getBounds() {
		return this.rectangleHull.getEnvelope();
	}

	@Override
	public int compareTo(ISelectionHandle o) {
		return this.getDrawingPrimitiveController().compareTo(o.getDrawingPrimitiveController());
	}

	@Override
	public void translate(Point delta) {
		Envelope originalEvelope = rectangleHull.getEnvelope();
		this.rectangleHull = this.rectangleHull.changeEnvelope(originalEvelope.translate(delta));
	}

	@Override
	public void drawShape(IHandleShapeDrawer drawer) {
		drawer.drawHandle(this);
	}

	@Override
	public Iterator<Point> pointIterator() {
		return this.rectangleHull.pointIterator();
	}

}
