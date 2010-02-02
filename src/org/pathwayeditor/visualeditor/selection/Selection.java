package org.pathwayeditor.visualeditor.selection;


public abstract class Selection implements ISelection {
	private final boolean isPrimary;
	
	protected Selection(boolean isPrimary){
		this.isPrimary = isPrimary;
	}
	
	@Override
	public final boolean isPrimary() {
		return this.isPrimary;
	}

	@Override
	public final boolean isSecondary() {
		return !this.isPrimary;
	}

	@Override
	public final int compareTo(ISelection o) {
		int retVal = this.isPrimary() && o.isPrimary() ? 0 : (this.isPrimary() && o.isSecondary() ? -1 : 1); 
		if(retVal == 0){
			retVal = this.getPrimitiveController().compareTo(o.getPrimitiveController());
		}
		return retVal;
	}

}
