package ca.queensu.cs.observer.ui.preferences.fieldeditors;

import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;

public class ObserverListEditorBasicMonitor implements IObserverListEditorMonitor, IInputValidator {

		public boolean canReorder() {
			return true;
		}
		
		public boolean canAdd() {
			return true;
		}
		
		public boolean canRemove() {
			return true;
		}
		
		public String getNewInputObject() {
			
			InputDialog dialog = new InputDialog(Display.getDefault().getActiveShell(),
					"Add a new item to the list", 
					"Item ",
					"New item",
					this);
			
			if (dialog.open() == Window.OK)
				return dialog.getValue();
			
			return null;
		}

		@Override
		public String isValid(String newText) {
			if (newText == null || newText.isEmpty())
				return "Item cannot be empty";
			
			return null;
		}
}
