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

/**
 * 
 */
package org.pathwayeditor.graphicsengine.stubs.notationsubsystem;

import java.util.HashSet;

import org.pathwayeditor.businessobjects.drawingprimitives.attributes.LinkEndDecoratorShape;
import org.pathwayeditor.businessobjects.drawingprimitives.attributes.LinkTermType;
import org.pathwayeditor.businessobjects.drawingprimitives.properties.IPropertyDefinition;
import org.pathwayeditor.businessobjects.typedefn.ILinkTerminusDefaults;
import org.pathwayeditor.figure.geometry.Dimension;

/**
 * @author smoodie
 *
 */
public class StubTargetTerminusDefaults implements ILinkTerminusDefaults {
	public static final LinkTermType LINK_END_TYPE = LinkTermType.SOURCE;
	public static final double EXPECTED_OFFSET = 2.0 ;
	public static final Dimension EXPECTED_END_SIZE = new Dimension(5.0, 5.0);
	public static final LinkEndDecoratorShape EXPECTED_END_DEC = LinkEndDecoratorShape.ARROW;
	private final HashSet<IPropertyDefinition> propertyDefinitionList;

	public StubTargetTerminusDefaults() {
		this.propertyDefinitionList = new HashSet<IPropertyDefinition>();
		this.propertyDefinitionList.add(new StubNumberPropertyDefinition());
	}

	
	/* (non-Javadoc)
	 * @see org.pathwayeditor.businessobjects.typedefn.ILinkTerminusDefaults#getEndDecoratorType()
	 */
	@Override
	public LinkEndDecoratorShape getEndDecoratorType() {
		return EXPECTED_END_DEC;
	}

	/* (non-Javadoc)
	 * @see org.pathwayeditor.businessobjects.typedefn.ILinkTerminusDefaults#getEndSize()
	 */
	@Override
	public Dimension getEndSize() {
		return EXPECTED_END_SIZE;
	}

	/* (non-Javadoc)
	 * @see org.pathwayeditor.businessobjects.typedefn.ILinkTerminusDefaults#getGap()
	 */
	@Override
	public double getGap() {
		return EXPECTED_OFFSET;
	}

//	/* (non-Javadoc)
//	 * @see org.pathwayeditor.businessobjects.typedefn.ILinkTerminusDefaults#getPropertiesIterator()
//	 */
//	@Override
//	public Iterator<IPropertyDefinition> propertyDefinitionIterator() {
//		return this.propertyDefinitionList.iterator();
//	}

	/**
	 * @return
	 */
	private HashSet<IPropertyDefinition> getpropdefns() {
		return this.propertyDefinitionList;
	}

//	@Override
//	public boolean containsPropertyDefinition(String name) {
//		return findPropDefn(name) != null;
//	}

	IPropertyDefinition findPropDefn(String name){
		IPropertyDefinition retVal = null;
		for(IPropertyDefinition propDefn : this.getpropdefns()){
			if(propDefn.getName().equals(name)){
				retVal = propDefn;
				break;
			}
		}
		return retVal;
	}
	
//	@Override
//	public IPropertyDefinition getPropertyDefinition(String name) {
//		return findPropDefn(name);
//	}

//	@Override
//	public int numPropertyDefinitions() {
//		return this.getpropdefns().size();
//	}

//	@Override
//	public boolean containsPropertyDefinition(IPropertyDefinition propDefn){
//		return propDefn != null && this.getpropdefns().contains(propDefn);
//	}
}
