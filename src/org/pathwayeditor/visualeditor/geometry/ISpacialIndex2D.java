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

/*
 Copyright (c) 2006, 2007, The Cytoscape Consortium (www.cytoscape.org)

 The Cytoscape Consortium is:
 - Institute for Systems Biology
 - University of California San Diego
 - Memorial Sloan-Kettering Cancer Center
 - Institut Pasteur
 - Agilent Technologies

 This library is free software; you can redistribute it and/or modify it
 under the terms of the GNU Lesser General Public License as published
 by the Free Software Foundation; either version 2.1 of the License, or
 any later version.

 This library is distributed in the hope that it will be useful, but
 WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 documentation provided hereunder is on an "as is" basis, and the
 Institute for Systems Biology and the Whitehead Institute
 have no obligations to provide maintenance, support,
 updates, enhancements or modifications.  In no event shall the
 Institute for Systems Biology and the Whitehead Institute
 be liable to any party for direct, indirect, special,
 incidental or consequential damages, including lost profits, arising
 out of the use of this software and its documentation, even if the
 Institute for Systems Biology and the Whitehead Institute
 have been advised of the possibility of such damage.  See
 the GNU Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public License
 along with this library; if not, write to the Free Software Foundation,
 Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
*/

package org.pathwayeditor.visualeditor.geometry;


/**
 * A spacial index for objects in two dimensions.
 */
public interface ISpacialIndex2D<K> {
	/**
	 * Returns the number of entries (objKeys) currently in this structure.<p>
	 * NOTE: To retrieve an enumeration of all entries in this structure, call
	 * queryOverlap() with Float.NEGATIVE_INFINITY minimum values and
	 * Float.POSITIVE_INFINITY maximum values.
	 */
	public int size();

	/**
	 * Determines whether or not a given entry exists
	 * and conditionally retrieves the extents of that entry.  The parameter
	 * extentsArr is written into by this method only if it is not null
	 * and if objKey exists.  The information written into
	 * extentsArr consists of the minimum bounding rectangle (MBR) of objKey:
	 * <blockquote><table border="1" cellpadding="5" cellspacing="0">
	 *   <tr>  <th>array index</th>  <th>value if objKey exists</th>  </tr>
	 *   <tr>  <td>offset</td>       <td>xMin of MBR</td>             </tr>
	 *   <tr>  <td>offset+1</td>     <td>yMin of MBR</td>             </tr>
	 *   <tr>  <td>offset+2</td>     <td>xMax of MBR</td>             </tr>
	 *   <tr>  <td>offset+3</td>     <td>yMax of MBR</td>             </tr>
	 * </table></blockquote>
	 * @param objKey the entry to query.
	 * @param extentsArr an array to which extent values will be written by this
	 *   method; may be null.
	 * @param offset specifies the beginning index of where to write extent
	 *   values into extentsArr; exactly four entries are written starting at
	 *   this index (see above table); if extentsArr is null then this offset
	 *   is ignored.
	 * @return true if and only if objKey exists as an entry in this structure.
	 * @exception ArrayIndexOutOfBoundsException if objKey exists, if
	 *   extentsArr is not null, and if extentsArr cannot be written
	 *   to in the index range [offset, offset+3].
	 */
	public boolean exists(K objKey, float[] extentsArr, int offset);

	/**
	 * Returns an enumeration of entries whose extents intersect the
	 * specified axis-aligned rectangular area.  By "axis-aligned" I mean that
	 * the query rectangle's sides are parallel to the axes of the data
	 * space.<p>
	 * The parameter extentsArr is written into by this method if it is not null.
	 * It provides a way for this method to communicate additional information
	 * to the caller of this method.  If not null, extentsArr is populated with
	 * information regarding the minimum bounding rectangle (MBR) that contains
	 * all returned entries.  The following table describes what is written to
	 * extentsArr if it is not null:
	 * <blockquote><table border="1" cellpadding="5" cellspacing="0">
	 *   <tr>  <th>array index</th>  <th>value if query generates results</th>
	 *           <th>value if query does not generate results</th>  </tr>
	 *   <tr>  <td>offset</td>       <td>xMin of MBR</td>
	 *           <td>Float.POSITIVE_INFINITY</td>                   </tr>
	 *   <tr>  <td>offset+1</td>     <td>yMin of MBR</td>
	 *           <td>Float.POSITIVE_INFINITY</td>                   </tr>
	 *   <tr>  <td>offset+2</td>     <td>xMax of MBR</td>
	 *           <td>Float.NEGATIVE_INFINITY</td>                   </tr>
	 *   <tr>  <td>offset+3</td>     <td>yMax of MBR</td>
	 *           <td>Float.NEGATIVE_INFINITY</td>                   </tr>
	 * </table></blockquote><p>
	 * Note that the order of query results, as returned in the enumeration,
	 * may be completely random as a function of the query rectangle.  However,
	 * any two subsequent, identical queries should produce an identical result
	 * order, provided that this structure does not undergo modification
	 * between those two queries (by modification I mean the possibility of
	 * entry shift, deletion of an entry, or insertion of an entry).
	 * @param xMin the minimum X coordinate of the query rectangle.
	 * @param yMin the minimum Y coordinate of the query rectangle.
	 * @param xMax the maximum X coordinate of the query rectangle.
	 * @param yMax the maximum Y coordinate of the query rectangle.
	 * @param extentsArr an array to which
	 *   extent values will be written by this method; may be null.
	 * @param offset specifies the beginning index of where to write extent
	 *   values into extentsArr; exactly four entries are written starting at
	 *   this index (see table above); if extentsArr is null then this offset
	 *   is ignored.
	 * @param reverse if true, the order in which the query hits
	 *   are returned is reversed.
	 * @return a non-null enumeration of all [distinct] entries
	 *   (objKeys) whose extents intersect the specified rectangular query area.
	 * @exception IllegalArgumentException if xMin is not less than or equal to
	 *   xMax or if yMin is not less than or equal to yMax.
	 * @exception ArrayIndexOutOfBoundsException if extentsArr is not null
	 *   and if it cannot be written to in the index range
	 *   [offset, offset+3].
	 */
	public ISpacialEntry2DEnumerator<K> queryOverlap(float xMin, float yMin, float xMax, float yMax,
	                                             float[] extentsArr, int offset, boolean reverse);
}
