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
package org.pathwayeditor.visualeditor;

import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.WindowConstants;

import org.pathwayeditor.visualeditor.IPathwayEditorStateChangeEvent.StateChangeType;
import org.pathwayeditor.visualeditor.commands.ICommandChangeEvent;
import org.pathwayeditor.visualeditor.commands.ICommandChangeListener;

import com.apple.eawt.AboutHandler;
import com.apple.eawt.AppEvent.AboutEvent;
import com.apple.eawt.AppEvent.PreferencesEvent;
import com.apple.eawt.AppEvent.QuitEvent;
import com.apple.eawt.Application;
import com.apple.eawt.PreferencesHandler;
import com.apple.eawt.QuitHandler;
import com.apple.eawt.QuitResponse;

public class VisualEditor extends JFrame implements AboutHandler, QuitHandler, PreferencesHandler {
	private static final long serialVersionUID = 1L;

	private static final int WIDTH = 1200;
	private static final int HEIGHT = 800;
	private final JMenuBar menuBar;
	private final PathwayEditor insp;
	private IVisualEditorController visualEditorController;
	private JMenuItem fileMenuExitItem;
	private JMenuItem fileMenuSaveAsItem;
	private JMenuItem fileMenuSaveItem;
	private JMenuItem fileMenuCloseItem;
	private IPathwayEditorStateChangeListener pathwayEditorStateChangeListener;
	private JMenuItem editMenuRedoItem;
	private JMenuItem editMenuUndoItem;
	private ICommandChangeListener commandStackChangeListener;
	private JMenuItem editMenuDeleteItem;
	private JMenuItem editMenuSelectAllItem;
	private JToolBar toolbar;

	private JButton newb;

	private JButton openb;

	private JButton saveb;

	private JButton saveAsb;

	private JButton undob;

	private JButton redob;

	private JButton deleteb;
	
	public VisualEditor(String title, IVisualEditorController visualEditorController){
		super(title);
//		doSplashScreen();
//		MacOSXController macController = new MacOSXController();
		boolean isMacOS = System.getProperty("mrj.version") != null;
		if (isMacOS){
			Application.getApplication().setAboutHandler(this);
			Application.getApplication().setPreferencesHandler(this);
			Application.getApplication().setQuitHandler(this);
//		  Application.getApplication().setAboutHandler(macController);
//		  Application.getApplication().setPreferencesHandler(macController);
//		  Application.getApplication().setQuitHandler(macController);
		}
		this.visualEditorController = visualEditorController;
		this.visualEditorController.setVisualEditor(this);
		this.setLayout(new BorderLayout());
		this.menuBar = new JMenuBar();
		this.toolbar = new JToolBar();
		initFileMenu(isMacOS);
		initEditMenu();
		initToolBar();
		this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		this.addWindowListener(new WindowListener(){

			@Override
			public void windowActivated(WindowEvent e) {
				
			}

			@Override
			public void windowClosed(WindowEvent e) {
			}

			@Override
			public void windowClosing(WindowEvent e) {
				closeWindow();
			}

			@Override
			public void windowDeactivated(WindowEvent e) {
				
			}

			@Override
			public void windowDeiconified(WindowEvent e) {
				
			}

			@Override
			public void windowIconified(WindowEvent e) {
				
			}

			@Override
			public void windowOpened(WindowEvent e) {
				
			}
			
		});
		this.setJMenuBar(menuBar);
		this.insp = new PathwayEditor(new Dialog(this, true));
		this.insp.setPreferredSize(new Dimension(WIDTH, HEIGHT));
		this.visualEditorController.setPathwayEditor(this.insp);
		this.add(this.toolbar, BorderLayout.PAGE_START);
		this.add(this.insp, BorderLayout.CENTER);
		this.pack();
		this.setLocationByPlatform(true);
		this.setVisible(true);
		this.pathwayEditorStateChangeListener = new IPathwayEditorStateChangeListener() {
			@Override
			public void editorChangedEvent(IPathwayEditorStateChangeEvent e) {
				setFileMenuEnablement();
				setEditMenuEnablement();
				if(e.getChangeType().equals(StateChangeType.OPEN)){
					e.getSource().getCommandStack().addCommandChangeListener(commandStackChangeListener);
				}
				else if(e.getChangeType().equals(StateChangeType.CLOSED)){
					e.getSource().getCommandStack().removeCommandChangeListener(commandStackChangeListener);
				}
			}
		};
		this.commandStackChangeListener = new ICommandChangeListener() {
			
			@Override
			public void notifyCommandChange(ICommandChangeEvent e) {
				setEditMenuEnablement();
			}
		};
		setFileMenuEnablement();
		setEditMenuEnablement();
		setSelectionDependentMenuItemsEnablement(false);
		this.insp.addEditorStateChangeListener(pathwayEditorStateChangeListener);
		setSelectionDependentMenuItemsEnablement(false);
	}

	private void initToolBar() {
		ImageIcon newi = new ImageIcon(getClass().getResource("/toolbarButtonGraphics/general/New24.gif"));
		newb = new JButton(newi);
		newb.setToolTipText("New");
		newb.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				visualEditorController.newDiagram();
			}
		});
		ImageIcon open = new ImageIcon(getClass().getResource("/toolbarButtonGraphics/general/Open24.gif"));
		openb = new JButton(open);
		openb.setToolTipText("Open");
		openb.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				visualEditorController.openFileAction();
			}
		});
		ImageIcon save = new ImageIcon(getClass().getResource("/toolbarButtonGraphics/general/Save24.gif"));
		saveb = new JButton(save);
		saveb.setToolTipText("Save");
		saveb.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				visualEditorController.saveFile();
			}
		});
		ImageIcon saveAs = new ImageIcon(getClass().getResource("/toolbarButtonGraphics/general/SaveAs24.gif"));
		saveAsb = new JButton(saveAs);
		saveAsb.setToolTipText("Save As");
		saveAsb.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				visualEditorController.saveFileAs();
			}
		});
		ImageIcon undo = new ImageIcon(getClass().getResource("/toolbarButtonGraphics/general/Undo24.gif"));
		undob = new JButton(undo);
		undob.setToolTipText("Undo");
		undob.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				visualEditorController.undoAction();
			}
		});
		ImageIcon redo = new ImageIcon(getClass().getResource("/toolbarButtonGraphics/general/Redo24.gif"));
		redob = new JButton(redo);
		redob.setToolTipText("Redo");
		redob.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				visualEditorController.redoAction();
			}
		});
		ImageIcon delete = new ImageIcon(getClass().getResource("/toolbarButtonGraphics/general/Delete24.gif"));
		deleteb = new JButton(delete);
		deleteb.setToolTipText("Delete");
		deleteb.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				visualEditorController.deleteAction();
			}
		});
		toolbar.add(newb);
		toolbar.add(openb);
		toolbar.add(saveb);
		toolbar.add(saveAsb);
		toolbar.addSeparator();
		toolbar.add(undob);
		toolbar.add(redob);
		toolbar.addSeparator();
		toolbar.add(deleteb);
		toolbar.setAlignmentX(0);
	}

	protected void setSelectionDependentMenuItemsEnablement(boolean b) {
		this.editMenuDeleteItem.setEnabled(b);
		this.deleteb.setEnabled(b);
	}

	protected void closeWindow() {
		if(this.visualEditorController.getPathwayEditor().isEdited()){
			int reply = JOptionPane.showConfirmDialog(VisualEditor.this, "Do you want to save your changes before exiting?", "Unsaved Changes to Diagram", JOptionPane.YES_NO_CANCEL_OPTION);
			if(reply == JOptionPane.YES_OPTION){
				this.visualEditorController.saveFile();
				System.exit(0);
			}
			else if(reply == JOptionPane.NO_OPTION){
				System.exit(0);
			}
		}
		else{
			int reply = JOptionPane.showConfirmDialog(VisualEditor.this, "Are you sure you want to exit?", "Exit Confirmation", JOptionPane.YES_NO_OPTION);
			if(reply == JOptionPane.YES_OPTION){
				System.exit(0);
			}
		}
	}

	private void initEditMenu(){
		JMenu editMenu = new JMenu("Edit");
		editMenu.setMnemonic(KeyEvent.VK_E);
		menuBar.add(editMenu);
		editMenuUndoItem = new JMenuItem("Undo", KeyEvent.VK_U);
		editMenuUndoItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		editMenuUndoItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				visualEditorController.undoAction();
			}
		});
		editMenu.add(editMenuUndoItem);
		editMenuRedoItem = new JMenuItem("Redo", KeyEvent.VK_R);
		editMenuRedoItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, ActionEvent.SHIFT_MASK|Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		editMenuRedoItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				visualEditorController.redoAction();
			}
		});
		editMenu.add(editMenuRedoItem);
		editMenuDeleteItem = new JMenuItem("Delete", KeyEvent.VK_D);
		editMenuDeleteItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				visualEditorController.deleteAction();
			}
		});
		editMenu.addSeparator();
		editMenu.add(editMenuDeleteItem);
		editMenuSelectAllItem = new JMenuItem("Select All", KeyEvent.VK_A);
		editMenuSelectAllItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		editMenuSelectAllItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				visualEditorController.selectAllAction();
			}
		});
		editMenu.add(editMenuSelectAllItem);
	}
	
	private void setEditMenuEnablement() {
		if(insp.getCommandStack().canRedo()){
			editMenuRedoItem.setEnabled(true);
			this.redob.setEnabled(true);
		}
		else{
			editMenuRedoItem.setEnabled(false);
			this.redob.setEnabled(false);
		}
		if(insp.getCommandStack().canUndo()){
			editMenuUndoItem.setEnabled(true);
			this.undob.setEnabled(true);
		}
		else{
			editMenuUndoItem.setEnabled(false);
			this.undob.setEnabled(false);
		}
		if(insp.isOpen()){
			this.editMenuSelectAllItem.setEnabled(true);
		}
		else{
			this.editMenuSelectAllItem.setEnabled(false);
		}
	}

	private void initFileMenu(boolean isMacOS){
		JMenu fileMenu = new JMenu("File");
		fileMenu.setMnemonic(KeyEvent.VK_F);
		menuBar.add(fileMenu);
		JMenuItem fileMenuNewItem = new JMenuItem("New", KeyEvent.VK_N);
		fileMenuNewItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		fileMenuNewItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				visualEditorController.newDiagram();
			}
		});
		fileMenu.add(fileMenuNewItem);
		//a group of JMenuItems
		JMenuItem fileMenuOpenItem = new JMenuItem("Open", KeyEvent.VK_O);
		fileMenuOpenItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		fileMenuOpenItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				visualEditorController.openFileAction();
			}
		});
		fileMenu.add(fileMenuOpenItem);
		fileMenu.addSeparator();
		fileMenuCloseItem = new JMenuItem("Close", KeyEvent.VK_C);
		fileMenuCloseItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		fileMenuCloseItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				visualEditorController.closeFile();
			}
		});
		fileMenu.add(fileMenuCloseItem);
		fileMenuSaveItem = new JMenuItem("Save", KeyEvent.VK_S);
		fileMenuSaveItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		fileMenuSaveItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				visualEditorController.saveFile();
			}
		});
		fileMenu.add(fileMenuSaveItem);
		fileMenuSaveAsItem = new JMenuItem("Save As ...", KeyEvent.VK_S);
		fileMenuSaveAsItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()|ActionEvent.SHIFT_MASK));
		fileMenuSaveAsItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				visualEditorController.saveFileAs();
			}
		});
		fileMenu.add(fileMenuSaveAsItem);
		if(!isMacOS){
			// Macs handle exit in another way.
			fileMenuExitItem = new JMenuItem("Exit", KeyEvent.VK_X);
			fileMenuExitItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
			fileMenuExitItem.getAccessibleContext().setAccessibleDescription("This doesn't really do anything");
			fileMenuExitItem.addActionListener(new ActionListener(){
				@Override
				public void actionPerformed(ActionEvent e) {
					processEvent(new WindowEvent(VisualEditor.this, WindowEvent.WINDOW_CLOSING));
				}
			});
			fileMenu.add(fileMenuExitItem);
		}
	}

	private void setFileMenuEnablement(){
		if(!this.insp.isOpen()){
			fileMenuSaveAsItem.setEnabled(false);
			fileMenuCloseItem.setEnabled(false);
			this.saveAsb.setEnabled(false);
		}
		else{
			fileMenuSaveAsItem.setEnabled(true);
			fileMenuCloseItem.setEnabled(true);
			this.saveAsb.setEnabled(true);
		}
		if(this.insp.isEdited()){
			fileMenuSaveItem.setEnabled(true);
			this.saveb.setEnabled(true);
		}
		else{
			fileMenuSaveItem.setEnabled(false);
			this.saveb.setEnabled(false);
		}
	}
	

//	private void doSplashScreen(){
//		final SplashScreen splash = SplashScreen.getSplashScreen();
//		if (splash == null) {
//			System.out.println("SplashScreen.getSplashScreen() returned null");
//			return;
//		}
//		Graphics2D g = splash.createGraphics();
//		if (g == null) {
//			System.out.println("g is null");
//			return;
//		}
//		renderSplashFrame(g, 10);
//		renderSplashFrame(g, 100);
//		splash.close();
//	}
	
    static void renderSplashFrame(Graphics2D g, int frame) {
        final String[] comps = {"foo", "bar", "baz"};
        g.setComposite(AlphaComposite.Clear);
        g.fillRect(120,140,200,40);
        g.setPaintMode();
        g.setColor(Color.YELLOW);
        g.drawString("Loading "+comps[(frame/5)%3]+"...", 120, 150);
    }

	@Override
	public void handlePreferences(PreferencesEvent arg0) {
	    JOptionPane.showMessageDialog(this, 
                "prefs", 
                "prefs", 
                JOptionPane.INFORMATION_MESSAGE);
	}

	@Override
	public void handleQuitRequestWith(QuitEvent arg0, QuitResponse arg1) {
//		closeWindow();
//		processEvent(new WindowEvent(VisualEditor.this, WindowEvent.WINDOW_CLOSING));
		if(this.visualEditorController.getPathwayEditor().isEdited()){
			int reply = JOptionPane.showConfirmDialog(VisualEditor.this, "Do you want to save your changes before exiting?", "Unsaved Changes to Diagram", JOptionPane.YES_NO_CANCEL_OPTION);
			if(reply == JOptionPane.YES_OPTION){
				this.visualEditorController.saveFile();
				arg1.performQuit();
			}
			else if(reply == JOptionPane.NO_OPTION){
				arg1.performQuit();
			}
			else{
				arg1.cancelQuit();
			}
		}
		else{
			int ok = JOptionPane.showConfirmDialog(this, "Do you want to quit?", "Quit Dialog", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
			if(ok == JOptionPane.YES_OPTION){
				arg1.performQuit();
			}
			else{
				arg1.cancelQuit();
			}
		}
	}

	@Override
	public void handleAbout(AboutEvent arg0) {
	    JOptionPane.showMessageDialog(this, 
                "about", 
                "about", 
                JOptionPane.INFORMATION_MESSAGE);
	}
}
