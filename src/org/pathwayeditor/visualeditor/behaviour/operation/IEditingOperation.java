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
package org.pathwayeditor.visualeditor.behaviour.operation;

import org.pathwayeditor.figure.geometry.Point;

public interface IEditingOperation {
	enum ReparentingStateType { CAN_REPARENT, CAN_MOVE, FORBIDDEN }
	
	
	ReparentingStateType getReparentingState(Point delta);
	
	void moveStarted();

	void moveOngoing(Point delta);

	void moveFinished(Point delta, ReparentingStateType reparentingState);

	void deleteSelection();

	/**
	 * Indicates whether the move operation should actually copy the selected
	 * elements.
	 * @param isSelected true if it is a copy, false otherwise.
	 */
	void setCopyOnMove(boolean isSelected);

}
