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
package tutorial.viewer;

import java.awt.BorderLayout;
import java.awt.Dimension;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;

import y.base.Node;
import y.view.Graph2D;
import y.view.Graph2DView;

public class SimpleGraphViewer2 {
  JFrame frame;
  /** The yFiles view component that displays (and holds) the graph. */
  Graph2DView view;
  /** The yFiles graph type. */
  Graph2D graph;

  public SimpleGraphViewer2(Dimension size, String title) {
    view = createGraph2DView();
    graph = view.getGraph2D();
    frame = createApplicationFrame(size, title, view);
  }

  public SimpleGraphViewer2() {
    this(new Dimension(400, 300), "");
    frame.setTitle(getClass().getName());
  }

  private Graph2DView createGraph2DView() {
    Graph2DView view = new Graph2DView();
    view.setAntialiasedPainting(true);
    return view;
  }

  /** Creates a JFrame that will show the demo graph. */
  private JFrame createApplicationFrame(Dimension size, String title, JComponent view) {
    JPanel panel = new JPanel(new BorderLayout());
    panel.setPreferredSize(size);
    // Add the given view to the panel.
    panel.add(view, BorderLayout.CENTER);
    JFrame frame = new JFrame(title);
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.getRootPane().setContentPane(panel);
    frame.pack();
    frame.setLocationRelativeTo(null);
    return frame;
  }

  /** Creates a simple graph structure. */
  private void populateGraph(Graph2D graph) {
    // In the given graph, create two nodes...
    Node hello = graph.createNode(100, 50, 80, 30, "Hello");
    Node world = graph.createNode(100, 100, 80, 30, "World!");
    // ...and an edge between.
    graph.createEdge(hello, world);
  }

  public void show() {
    frame.setVisible(true);
  }

  public Graph2DView getView() {
    return view;
  }

  public Graph2D getGraph() {
    return graph;
  }

  public static void main(String[] args) {
    SimpleGraphViewer2 sgv = 
      new SimpleGraphViewer2(new Dimension(400, 300), SimpleGraphViewer2.class.getName());
    sgv.populateGraph(sgv.getGraph());
    sgv.show();
  }
}
