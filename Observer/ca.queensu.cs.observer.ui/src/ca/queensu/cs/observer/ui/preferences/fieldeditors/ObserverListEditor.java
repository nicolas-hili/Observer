package ca.queensu.cs.observer.ui.preferences.fieldeditors;

import org.eclipse.jface.preference.ListEditor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.widgets.Composite;

public class ObserverListEditor extends ListEditor {

	/**
     * Indicates whether the empty string is legal;
     * <code>true</code> by default.
     */
    private boolean emptyEntryAllowed = true;
    
	private boolean isValid;
	protected String oldValue;
	private ObserverListEditorBasicMonitor monitor;
	
	/**
     * The error message, or <code>null</code> if none.
     */
    private String errorMessage;
    
    public String separator;
	
    public ObserverListEditor() {
		super();
	}
    
    public void setMonitor(ObserverListEditorBasicMonitor monitor) {
		this.monitor = monitor;
		
		if (!this.monitor.canReorder()) {
    		this.getUpButton().dispose();
    		this.getDownButton().dispose();
    	}
		
		if (!this.monitor.canAdd())
			this.getAddButton().dispose();
		
		if (!this.monitor.canRemove())
			this.getRemoveButton().dispose();
		
	}
    
    public ObserverListEditorBasicMonitor getMonitor() {
		return monitor;
	}
    
    public ObserverListEditor(String name, String labelText, ObserverListEditorBasicMonitor monitor, Composite parent) {
    	super(name, labelText, parent);
    	this.setMonitor((monitor == null) ? new ObserverListEditorBasicMonitor() : monitor);
    }
    
    public void setSeparator(String separator) {
		this.separator = separator;
		valueChanged();
	}
    
    public String getSeparator() {
		return separator;
	}
    
	@Override
	protected void init(String name, String text) {
		isValid = false;
		super.init(name, text);
	}
	
	
	@Override
	protected void createControl(Composite parent) {
		super.createControl(parent);
	}
	
	@Override
	protected void selectionChanged() {

        if (monitor == null)
        	super.selectionChanged();
        else {
        	int index = getList().getSelectionIndex();
        	int size = getList().getItemCount();
        	
        	if (monitor.canRemove())
        		getRemoveButton().setEnabled(index >= 0);
        	
        	
        	if (monitor.canReorder()) {
        		getUpButton().setEnabled(size > 1 && index > 0);
        		getDownButton().setEnabled(size > 1 && index >= 0 && index < size - 1);
        	}        	
        }
        
        valueChanged();
	}
	
	
	@Override
	protected String[] parseString(String stringList) {
		return new String[] {"eventName", "sourceName", "eventSource", "eventKind", "seconds", "nanoseconds", "params"};
	}
	
	@Override
	protected String getNewInputObject() {		
		return (monitor == null) ? null : monitor.getNewInputObject();
	}
	
	@Override
	protected String createList(String[] items) {
		String separator = getSeparator();
		if (separator == null || separator.isEmpty())
			separator = ",";
		return String.join(separator, items);
	}
	
	/**
     * Hook for subclasses to do specific state checks.
     * <p>
     * The default implementation of this framework method does
     * nothing and returns <code>true</code>.  Subclasses should
     * override this method to specific state checks.
     * </p>
     *
     * @return <code>true</code> if the field value is valid,
     *   and <code>false</code> if invalid
     */
    protected boolean doCheckState() {
        return true;
    }
	
	/**
     * Checks whether the text input field contains a valid value or not.
     *
     * @return <code>true</code> if the field value is valid,
     *   and <code>false</code> if invalid
     */
    protected boolean checkState() {
        boolean result = false;
        if (emptyEntryAllowed) {
			result = true;
		}

        if (getList() == null) {
			result = false;
			errorMessage = "the list cannot be built";
		} else {
			String txt = this.createList(this.getList().getItems());
			result = (txt.trim().length() > 0) || emptyEntryAllowed;
			if (!result) {
				errorMessage = JFaceResources
		                .getString("StringFieldEditor.errorMessage");//$NON-NLS-1$
			}
		}
        
        String separator = getSeparator();
		if (separator == null || separator.isEmpty()) {
			errorMessage = "Please define a separator";
			result = false;
		}

        // call hook for subclasses
        result = result && doCheckState();

        if (result) {
			clearErrorMessage();
		} else {
			showErrorMessage(errorMessage);
		}

        return result;
    }
	
	@Override
	protected void refreshValidState() {
        isValid = checkState();
    }
	
	private void valueChanged() {
		boolean oldState = isValid;
        refreshValidState();

        if (isValid != oldState) {
			fireStateChanged(IS_VALID, oldState, isValid);
		}

        String newValue = this.createList(this.getList().getItems());
        if (!newValue.equals(oldValue)) {
            fireValueChanged(VALUE, oldValue, newValue);
            oldValue = newValue;
        }
	}
	
	public void setEmptyEntryAllowed(boolean emptyEntryAllowed) {
		this.emptyEntryAllowed = emptyEntryAllowed;
	}

}
