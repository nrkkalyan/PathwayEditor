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
package demo.view.realizer;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javax.swing.JFrame;
import javax.swing.JRootPane;

import demo.view.DemoDefaults;
import y.util.D;
import y.util.YVersion;
import y.view.EditMode;
import y.view.Graph2DView;
import y.view.NodeLabel;
import y.view.NodeRealizer;
import y.view.ShapeNodeRealizer;
import y.view.YLabel;

/**
 * NodeRealizer implementation that represents a UML Class Node.
 * This node realizer displays the following properties of a class
 * in UML notation:
 * <ul>
 *  <li>class name</li>
 *  <li>stereotype property</li>
 *  <li>constraint property</li>
 *  <li>attribute list</li>
 *  <li>method list</li>
 * </ul>
 * Executing this class will display a sample instance of this realizer.
 */
public class UMLClassNodeRealizer extends ShapeNodeRealizer
{
  private NodeLabel aLabel; //attributeLabel
  private NodeLabel mLabel; //methodLabel
  private boolean clipContent;
  private boolean omitDetails;
  private String stereotype = "";
  private String constraint = "";

  /**
   * Instantiates a new UMLNodeRealizer.
   */ 
  public UMLClassNodeRealizer()
  {
    init();
  }
  
  void init()
  {
    setShapeType(RECT_3D);
    
    getLabel().setModel(NodeLabel.INTERNAL);
    getLabel().setPosition(NodeLabel.TOP);
    
    getLabel().setFontSize(13);
    getLabel().setFontStyle(Font.BOLD);
        
    aLabel = new NodeLabel();
    aLabel.bindRealizer(this);
    aLabel.setAlignment(YLabel.ALIGN_LEFT);
    aLabel.setModel(NodeLabel.FREE);
    
    mLabel = new NodeLabel();
    mLabel.bindRealizer(this);
    mLabel.setAlignment(YLabel.ALIGN_LEFT);
    mLabel.setModel(NodeLabel.FREE);
    
    clipContent = true;
    omitDetails = false;
  }
  
  /**
   * Instantiates a new UMLNodeRealizer as a copy of a given
   * realizer.
   */ 
  public UMLClassNodeRealizer(NodeRealizer r)
  {
    super(r);
    if(r instanceof UMLClassNodeRealizer)
    {
      UMLClassNodeRealizer cnr = (UMLClassNodeRealizer)r;
      aLabel = (NodeLabel)cnr.aLabel.clone();
      aLabel.bindRealizer(this);
      mLabel = (NodeLabel)cnr.mLabel.clone();
      mLabel.bindRealizer(this);
      constraint = cnr.constraint;
      stereotype = cnr.stereotype;
      clipContent = cnr.clipContent;
      omitDetails = cnr.omitDetails;  
    }
    else {
      init();
    }
  }
  
  /**
   * Returns a UMLNodERealizer that is a copy of the given 
   * realizer.
   */ 
  public NodeRealizer createCopy(NodeRealizer r)
  {
    return new UMLClassNodeRealizer(r);
  }
    
  
  //////////////////////////////////////////////////////////////////////////////
  // SETTER & GETTER ///////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////
  
  
  /**
   * Set the class name to be displayed by this realizer.
   */
  public void setClassName(String name)
  {
    setLabelText(name);
  }
  
  /**
   * Returns the class name to be displayed by this realizer.
   */
  public String getClassName()
  {
    return getLabelText();
  }
  
  
  /**
   * Sets the constraint property of this realizer.
   */
  public void setConstraint(String constraint)
  {
    this.constraint = constraint;
  }

  
  /**
   * Sets the stereotype property of this realizer.
   */
  public void setStereotype(String stereotype)
  {
    this.stereotype = stereotype;
  }
  
  
  /**
   * Returns the constraint property of this realizer.
   */
  public String getConstraint()
  {
    return constraint;
  }

  /**
   * Returns the stereotype property of this realizer.
   */
  public String getStereotype()
  {
    return stereotype;
  }
  
  
  /**
   * Returns the node label that represents all added
   * method strings.
   */
  public NodeLabel getMethodLabel()
  {
    return mLabel;
  }
  
  /**
   * Returns the node label that represents all added
   * attribute strings.
   */
  public NodeLabel getAttributeLabel()
  {
    return aLabel;
  }
  
  /**
   * Returns whether or not the display of the labels should be
   * clipped with the bounding box of the realizer.
   */
  public boolean getClipContent()
  {
    return clipContent;
  }
 
  /**
   * Sets whether or not the display of the labels should be
   * clipped with the bounding box of the realizer.
   */
  public void setClipContent(boolean clipping)
  {
    clipContent = clipping;
  }
  
 
  /**
   * Set whether or not this realizer should omit details when being displayed.
   */
  public void setOmitDetails(boolean b)
  {
    omitDetails = b;
  }
  
  
  /**
   * Returns whether or not this realizer should omit details when being displayed.
   */ 
  public boolean getOmitDetails()
  {
    return omitDetails;
  }
  
  private void addToLabel(NodeLabel l, String s)
  {
    if(l.getText().length() > 0) {
      l.setText(l.getText() + '\n' + s);
    } else {
      l.setText(s);
    }
  }
  
  
  /**
   * Adds a class method label to this realizer.
   */ 
  public void addMethod(String method)
  {
    addToLabel(mLabel,method);
  }
  
  /**
   * Adds a class attribute label to this realizer.
   */ 
  public void addAttribute(String attr)
  {
    addToLabel(aLabel,attr);
  }
  
  /**
   * Set the size of this realizer automatically. This method will adapt the size
   * of this realizer so that the labels defined for it will fit within its
   * bounding box.
   */
  public void fitContent()
  {
    double height = 3.0;
    double width = getLabel().getWidth() + 10.0;
    
    if(stereotype.length() > 0)
    {
      NodeLabel l = new NodeLabel();
      l.setText("<<" + getStereotype() + ">>");
      l.setModel(NodeLabel.FREE);
      l.bindRealizer( this );
      height += l.getHeight() + 5.0;
      width = Math.max(l.getWidth()+ 10.0, width);
    }
    
    height += getLabel().getHeight() + 3.0;

    if(constraint.length() > 0)
    {
      NodeLabel l = new NodeLabel();
      l.setText("{" + getConstraint() + "}");
      l.setModel(NodeLabel.FREE);
      height += l.getHeight() + 5.0;
      width = Math.max(l.getWidth() + 10.0, width);
    }
    
    if(!omitDetails && !(aLabel.getText().length() == 0 && mLabel.getText().length() == 0))
    {
      height += 3.0;
      height += aLabel.getHeight() + 3.0;
      width = Math.max(aLabel.getWidth() + 10.0,width);
      height += 3.0;
      height += mLabel.getHeight() + 3.0;
      width = Math.max(mLabel.getWidth() + 10.0,width);
    }
    
    setSize(width, height);
  }
  
  
  //////////////////////////////////////////////////////////////////////////////
  // GRAPHICS  /////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////
  
  /**
   * Paint the labels associated with this realizer.
   */
  public void paintText(Graphics2D gfx)
  {    
    Shape oldClip = null;
    if(clipContent)
    {
      oldClip = gfx.getClip();
      gfx.clip(getBoundingBox());//Rect((int)x,(int)y,(int)width,(int)height);
    }
    
    double yoff = 3.0;
    
    if(stereotype.length() > 0)
    {
      NodeLabel l = new NodeLabel();
      l.setText("<<" + getStereotype() + ">>");
      l.setModel(NodeLabel.FREE);
      l.setOffset( (getWidth()-l.getWidth())/2.0, yoff);
      l.bindRealizer( this );
      l.paint(gfx);
      yoff += l.getHeight() + 5.0;
    }
    
    NodeLabel label = getLabel();
    label.setOffset((getWidth()-label.getWidth())/2.0,yoff);
    label.paint(gfx);
    yoff += label.getHeight() + 3.0;

    if(constraint.length() > 0)
    {
      NodeLabel l = new NodeLabel();
      l.setText("{" + getConstraint() + "}");
      l.setModel(NodeLabel.FREE);
      l.setOffset( getWidth()-l.getWidth() - 5.0, yoff);
      l.bindRealizer( this );
      l.paint(gfx);
      yoff += l.getHeight() + 5.0;
    }
    
    if(!omitDetails && !(aLabel.getText().length() == 0 && mLabel.getText().length() == 0))
    {
      gfx.setColor(getLineColor());
      gfx.drawLine((int)x+1,(int)(y+yoff),(int)(x+width-1.0),(int)(y+yoff));
      yoff += 3.0;
      aLabel.setOffset(3.0,yoff);
      aLabel.paint(gfx);
      yoff += aLabel.getHeight() + 3.0;
      gfx.drawLine((int)x+1,(int)(y+yoff),(int)(x+width-1.0),(int)(y+yoff));
      yoff += 3.0;
      mLabel.setOffset(3.0,yoff);
      mLabel.paint(gfx);
    }
    
    if(clipContent)
    {
      gfx.setClip(oldClip);
    }
  }
  
  //////////////////////////////////////////////////////////////////////////////
  // SERIALIZATION /////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////
  
  /**
   * Serialization routine that allows this realizer to be written out
   * in YGF graph format.
   * @deprecated Use a custom {@link y.io.graphml.graph2d.NodeRealizerSerializer}
   * for serialization to the {@link y.io.GraphMLIOHandler GraphML format}
   * instead.
   */
  public void write(ObjectOutputStream out) throws IOException 
  {
    out.writeByte(YVersion.VERSION_1);
    super.write(out);
    aLabel.write( out );
    mLabel.write( out );
    out.writeBoolean(clipContent);
    out.writeBoolean(omitDetails);
    out.writeObject(getStereotype());
    out.writeObject(getConstraint());
  }
  
  
  /**
   * Deserialization routine that allows this realizer to be read in
   * from YGF graph format.
   * @deprecated Use a custom {@link y.io.graphml.graph2d.NodeRealizerSerializer}
   * for serialization to the {@link y.io.GraphMLIOHandler GraphML format}
   * instead.
   */
  public void read(ObjectInputStream in) throws IOException, ClassNotFoundException 
  {
    switch(in.readByte()) {
    case YVersion.VERSION_1:
      super.read(in);
      init();
      aLabel.read(in);
      mLabel.read(in);
      clipContent = in.readBoolean();
      omitDetails = in.readBoolean();
      stereotype = (String)in.readObject();
      constraint = (String)in.readObject();
      break;
    default:
      D.fatal("Unsupported Format");
    }
  }



  public static void addContentTo( final JRootPane rootPane )
  {
    final UMLClassNodeRealizer r = new UMLClassNodeRealizer();
    r.setClassName("com.mycompany.MyClass");
    r.setConstraint("abstract");
    r.setStereotype("factory");
    r.addAttribute("-graph");
    r.addAttribute("-id");
    r.addMethod("+setGraph(Graph)");
    r.addMethod("+getGraph():Graph");
    r.addMethod("+setID(int)");
    r.addMethod("+getID():int");
    r.fitContent();
    r.setFillColor(new Color(255, 153, 0));
    final Graph2DView view = new Graph2DView();
    view.setFitContentOnResize(true);
    view.getGraph2D().setDefaultNodeRealizer(r.createCopy());
    view.getGraph2D().createNode();
    view.addViewMode(new EditMode());

    rootPane.setContentPane(view);
  }

  /**
   * Launcher method. Execute this class to see a sample instantiation of
   * this node realizer in action.
   */
  public static void main(String[] args)
  {
    EventQueue.invokeLater(new Runnable() {
      public void run() {
        DemoDefaults.initLnF();
        final JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        addContentTo(frame.getRootPane());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
      }
    });
  }
}

