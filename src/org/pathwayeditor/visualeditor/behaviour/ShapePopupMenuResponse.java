package org.pathwayeditor.visualeditor.behaviour;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.pathwayeditor.visualeditor.selection.ISelectionHandle;

public class ShapePopupMenuResponse implements IPopupMenuResponse {
	private final JPopupMenu popup;
	private final ActionListener deleteListener;
	private JMenuItem deleteShapeItem;
	
	public ShapePopupMenuResponse(final IShapePopupActions popupActions){
		popup = new JPopupMenu();
		deleteShapeItem = new JMenuItem("Delete");
		this.deleteListener = new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				popupActions.delete();
			}
			
		};
		popup.add(deleteShapeItem);
	}
	
	
	@Override
	public void activate(){
		deleteShapeItem.addActionListener(deleteListener);
	}
	
	@Override
	public void deactivate(){
		deleteShapeItem.removeActionListener(deleteListener);
	}
	
	@Override
	public JPopupMenu getPopupMenu(ISelectionHandle selectionHandle) {
		return this.popup;
	}
}