/****************************************************************************
 **
 ** This file is part of yFiles-2.8. 
 ** 
 ** yWorks proprietary/confidential. Use is subject to license terms.
 **
 ** Redistribution of this file or of an unauthorized byte-code version
 ** of this file is strictly forbidden.
 **
 ** Copyright (c) 2000-2010 by yWorks GmbH, Vor dem Kreuzberg 28, 
 ** 72070 Tuebingen, Germany. All rights reserved.
 **
 ***************************************************************************/
package demo.view.flowchart.painters;

import y.view.NodeRealizer;
import y.view.GenericNodeRealizer;

import java.awt.geom.GeneralPath;

/**
* This class is an implementation of {@link y.view.GenericNodeRealizer.Painter} that draws the stored data symbol of flowchart diagrams.
 **/
public class FlowchartStoredDataPainter extends AbstractFlowchartPainter {

  public FlowchartStoredDataPainter() {
    super();
    outline = new GeneralPath();
  }

  protected void updateOutline(NodeRealizer context) {
    GeneralPath shapePath = (GeneralPath) getOutline();
    shapePath.reset();
    double height = context.getHeight();
    double width = context.getWidth();
    double x = context.getX();
    double y = context.getY();
    double capRadiusHighDependency;

    GenericNodeRealizer cast_gnr = (GenericNodeRealizer) context;
    if (cast_gnr.getStyleProperty(PROPERTY_RADIUS) != null) {
      capRadiusHighDependency = ((Double) cast_gnr.getStyleProperty(PROPERTY_RADIUS)).doubleValue();
      capRadiusHighDependency = Math.min(capRadiusHighDependency, 0.5);
    } else {
      capRadiusHighDependency = FLOWCHART_DEFAULT_STORED_DATA_RADIUS;
    }
    double borderDistance = capRadiusHighDependency * Math.min(width, height);

    shapePath.moveTo((float)(x + borderDistance), (float)y);
    shapePath.lineTo((float)(x + width), (float)y);
    shapePath.quadTo((float)(x + width - 2*borderDistance), (float)(y + height / 2), (float)(x + width), (float)(y + height));
    shapePath.lineTo((float)(x + borderDistance), (float)(y + height));
    shapePath.quadTo((float)(x - borderDistance), (float)(y + height / 2), (float)(x + borderDistance), (float)y);
    shapePath.closePath();
  }
}
