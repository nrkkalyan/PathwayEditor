package org.pathwayeditor.visualeditor;

import java.awt.FileDialog;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;

import org.pathwayeditor.businessobjects.drawingprimitives.IModel;
import org.pathwayeditor.businessobjects.exchange.FileXmlCanvasPersistenceManager;
import org.pathwayeditor.businessobjects.exchange.IXmlPersistenceManager;
import org.pathwayeditor.businessobjects.management.INotationSubsystemPool;
import org.pathwayeditor.businessobjects.notationsubsystem.INotationSubsystem;

public class VisualEditorController implements IVisualEditorController {
	private static final int BUF_SIZE = 1024;
	private VisualEditor visualEditor;
	private final Map<String, INotationSubsystem> nsMap;
	private PathwayEditor pathwayEditor;
	private File currentFile;
	private IXmlPersistenceManager canvasPersistenceManager;

	
	public VisualEditorController(INotationSubsystemPool subsystemPool){
		this.nsMap = new HashMap<String, INotationSubsystem>();
		Iterator<INotationSubsystem> notIter = subsystemPool.subsystemIterator();
		while(notIter.hasNext()){
			INotationSubsystem ns = notIter.next();
			this.nsMap.put(ns.getNotation().getDisplayName(), ns);
		}
		canvasPersistenceManager = new FileXmlCanvasPersistenceManager(subsystemPool);
	}
	
	@Override
	public void openFileAction() {
		FileDialog chooser = new FileDialog(this.visualEditor, "Open Diagram File", FileDialog.LOAD);
		chooser.setDirectory(System.getProperty("user.dir"));
		chooser.setFilenameFilter(new FilenameFilter(){
			@Override
			public boolean accept(File dir, String name) {
				return Pattern.matches(".*\\.pwe$", name);
			}
			
		});
		chooser.setVisible(true);
		String openFile = chooser.getFile();
		if(openFile != null){
			openFile(new File(openFile));
		}
	}

	@Override
	public void openFile(File file){
		try{
			this.currentFile = file;
			InputStream in = new FileInputStream(file);
			canvasPersistenceManager.readCanvasFromStream(in);
			in.close();
			pathwayEditor.renderModel(canvasPersistenceManager.getCurrentModel());
		}
		catch(IOException ex){
			JOptionPane.showMessageDialog(visualEditor, ex.getMessage(), "Error Opening File", JOptionPane.ERROR_MESSAGE);
		}
	}

	@Override
	public void newDiagram() {
		String selection = (String)JOptionPane.showInputDialog(this.visualEditor, "Select a notation:", "New Diagram", JOptionPane.PLAIN_MESSAGE, null, nsMap.keySet().toArray(), "foo");
		INotationSubsystem selectedNs = nsMap.get(selection);
		this.canvasPersistenceManager.createNewModelStream(selectedNs, "New Map");
		this.currentFile = null;
		IModel newModel = this.canvasPersistenceManager.getCurrentModel();
		this.pathwayEditor.renderModel(newModel);
	}

	@Override
	public void setPathwayEditor(PathwayEditor insp) {
		this.pathwayEditor = insp;
	}

	@Override
	public PathwayEditor getPathwayEditor() {
		return this.pathwayEditor;
	}

	@Override
	public void saveFile() {
		try{
			if(this.currentFile != null){
				saveFile(this.currentFile);
			}
			else{
				saveFileAs();
			}
		}
		catch(IOException ex){
			JOptionPane.showMessageDialog(visualEditor, ex.getMessage(), "Error Saving File", JOptionPane.ERROR_MESSAGE);
		}
	}
	
	private void saveFile(File saveFile) throws IOException{
		InputStream in = this.canvasPersistenceManager.writeCanvasToStream();
		OutputStream out = new FileOutputStream(saveFile);
		int r = 0;
		byte buf[] = new byte[BUF_SIZE]; 
		while((r = in.read(buf)) >= 0){
			out.write(buf, 0, r);
		}
		out.close();
		in.close();
		this.pathwayEditor.resetEdited();
	}

	@Override
	public void saveFileAs() {
		try{
			FileDialog chooser = new FileDialog(this.visualEditor, "Save Diagram File", FileDialog.SAVE);
			chooser.setDirectory(System.getProperty("user.dir"));
			chooser.setFilenameFilter(new FilenameFilter(){
				@Override
				public boolean accept(File dir, String name) {
					return Pattern.matches(".*\\.pwe$", name);
				}
				
			});
			chooser.setVisible(true);
			String saveFileName = chooser.getFile();
			if(saveFileName != null){
				File saveFile = new File(saveFileName);
//				if(saveFile.exists()){
//					int response = JOptionPane.showConfirmDialog(this.visualEditor, "Do you want to overwrite file? : " + saveFile.getName(), "File exists", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
//					if(response == JOptionPane.YES_OPTION){
//						saveFile(saveFile);
//					}
//					else if(response == JOptionPane.NO_OPTION){
//						chooser.setVisible(true);
//					}
//				}
//				else{
					saveFile(saveFile);
//				}
			}
		}
		catch(IOException ex){
			JOptionPane.showMessageDialog(visualEditor, ex.getMessage(), "Error Saving File", JOptionPane.ERROR_MESSAGE);
		}
	}

	@Override
	public void setVisualEditor(VisualEditor visualEditor) {
		this.visualEditor = visualEditor;
	}

	@Override
	public VisualEditor getVisualEditor() {
		return this.visualEditor;
	}

	@Override
	public void closeFile() {
		this.pathwayEditor.close();
	}

	@Override
	public void undoAction() {
		this.pathwayEditor.getCommandStack().undo();
	}

	@Override
	public void redoAction() {
		this.pathwayEditor.getCommandStack().redo();
	}
	
}
