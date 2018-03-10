package ca.queensu.cs.observer.ui.preferences.fieldeditors;

public interface IObserverListEditorMonitor {
	
	public boolean canReorder();
	
	public boolean canAdd();
	
	public boolean canRemove();
	
	public String getNewInputObject();
}
