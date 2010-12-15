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
package demo.browser;

import java.net.URL;

/**
 * TODO: add documentation
 *
 */
public interface Displayable
{
  String getQualifiedName();

  String getDisplayName();

  String getSummary();

  String getDescription();

  boolean isDemo();

  boolean isExecutable();

  URL getBase();
}
